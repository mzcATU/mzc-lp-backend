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
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<User> searchUsers(String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> user = query.from(User.class);

        List<Predicate> predicates = buildPredicates(cb, query, user, keyword, role, status, hasCourseRole);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(user.get("createdAt")));

        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<User> users = typedQuery.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> countRoot = countQuery.from(User.class);

        List<Predicate> countPredicates = buildPredicates(cb, countQuery, countRoot, keyword, role, status, hasCourseRole);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(users, pageable, total);
    }

    private <T> List<Predicate> buildPredicates(CriteriaBuilder cb, CriteriaQuery<T> query, Root<User> user,
                                            String keyword, TenantRole role, UserStatus status, Boolean hasCourseRole) {
        List<Predicate> predicates = new ArrayList<>();

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
