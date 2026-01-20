package com.mzc.lp.domain.user.repository;

import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<User> searchUsers(Long tenantId, String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<Predicate> predicates = buildPredicates(cb, query, user, tenantId, keyword, role, status, hasCourseRole);
        query.where(predicates.toArray(new Predicate[0]));

        // Pageable에서 정렬 정보 적용
        List<Order> orders = buildOrders(cb, user, pageable.getSort());
        if (orders.isEmpty()) {
            // 기본 정렬: createdAt DESC
            orders.add(cb.desc(user.get("createdAt")));
        }
        query.orderBy(orders);

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> users = typedQuery.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);

        List<Predicate> countPredicates = buildPredicates(cb, countQuery, countRoot, tenantId, keyword, role, status, hasCourseRole);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(users, pageable, total);
    }

    /**
     * Pageable의 Sort 정보를 JPA Criteria Order 리스트로 변환
     */
    private List<Order> buildOrders(CriteriaBuilder cb, Root<User> user, Sort sort) {
        List<Order> orders = new ArrayList<>();

        if (sort == null || sort.isUnsorted()) {
            return orders;
        }

        for (Sort.Order sortOrder : sort) {
            String property = sortOrder.getProperty();

            // 허용된 정렬 필드만 처리 (보안)
            if (!isAllowedSortProperty(property)) {
                continue;
            }

            Path<?> path = user.get(property);

            if (sortOrder.isAscending()) {
                orders.add(cb.asc(path));
            } else {
                orders.add(cb.desc(path));
            }
        }

        return orders;
    }

    /**
     * 정렬 가능한 필드인지 확인 (SQL Injection 방지)
     */
    private boolean isAllowedSortProperty(String property) {
        return switch (property) {
            case "id", "name", "email", "status", "role", "createdAt", "updatedAt", "lastLoginAt" -> true;
            default -> false;
        };
    }

    @Override
    public Page<User> searchSystemAdmins(String keyword, UserStatus status, Pageable pageable) {
        // Native Query를 사용하여 Hibernate 테넌트 필터 우회
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM users WHERE role = 'SYSTEM_ADMIN'");

        List<String> conditions = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            conditions.add("(LOWER(email) LIKE :keyword OR LOWER(name) LIKE :keyword)");
        }

        if (status != null) {
            conditions.add("status = :status");
        }

        if (!conditions.isEmpty()) {
            sqlBuilder.append(" AND ").append(String.join(" AND ", conditions));
        }

        sqlBuilder.append(" ORDER BY created_at DESC");

        // Count query
        String countSql = "SELECT COUNT(*) FROM users WHERE role = 'SYSTEM_ADMIN'"
                + (!conditions.isEmpty() ? " AND " + String.join(" AND ", conditions) : "");

        jakarta.persistence.Query countQuery = entityManager.createNativeQuery(countSql);

        if (keyword != null && !keyword.isBlank()) {
            countQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        if (status != null) {
            countQuery.setParameter("status", status.name());
        }

        Long total = ((Number) countQuery.getSingleResult()).longValue();

        // Main query with pagination
        jakarta.persistence.Query mainQuery = entityManager.createNativeQuery(sqlBuilder.toString(), User.class);

        if (keyword != null && !keyword.isBlank()) {
            mainQuery.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        if (status != null) {
            mainQuery.setParameter("status", status.name());
        }

        mainQuery.setFirstResult((int) pageable.getOffset());
        mainQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<User> users = mainQuery.getResultList();

        return new PageImpl<>(users, pageable, total);
    }

    private <T> List<Predicate> buildPredicates(CriteriaBuilder cb, CriteriaQuery<T> query, Root<User> user,
                                            Long tenantId, String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole) {
        List<Predicate> predicates = new ArrayList<>();

        // 테넌트 필터링 (필수)
        if (tenantId != null) {
            predicates.add(cb.equal(user.get("tenantId"), tenantId));
        }

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            Predicate emailLike = cb.like(cb.lower(user.get("email")), pattern);
            Predicate nameLike = cb.like(cb.lower(user.get("name")), pattern);
            predicates.add(cb.or(emailLike, nameLike));
        }

        if (role != null) {
            predicates.add(cb.equal(user.get("role"), role));
        }

        if (status != null) {
            predicates.add(cb.equal(user.get("status"), status));
        }

        // CourseRole 보유자 필터링 (서브쿼리)
        if (Boolean.TRUE.equals(hasCourseRole)) {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<UserCourseRole> ucr = subquery.from(UserCourseRole.class);
            subquery.select(ucr.get("user").get("id"));
            predicates.add(user.get("id").in(subquery));
        }

        return predicates;
    }
}
