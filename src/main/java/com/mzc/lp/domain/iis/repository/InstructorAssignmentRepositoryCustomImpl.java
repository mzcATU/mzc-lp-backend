package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class InstructorAssignmentRepositoryCustomImpl implements InstructorAssignmentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<InstructorAssignment> searchAssignments(
            Long tenantId,
            Long instructorId,
            Long courseTimeId,
            InstructorRole role,
            AssignmentStatus status,
            String keyword,
            Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query
        CriteriaQuery<InstructorAssignment> query = cb.createQuery(InstructorAssignment.class);
        Root<InstructorAssignment> assignment = query.from(InstructorAssignment.class);

        List<Predicate> predicates = buildPredicates(cb, query, assignment, tenantId, instructorId, courseTimeId, role, status, keyword);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(assignment.get("createdAt")));

        TypedQuery<InstructorAssignment> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<InstructorAssignment> assignments = typedQuery.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InstructorAssignment> countRoot = countQuery.from(InstructorAssignment.class);

        List<Predicate> countPredicates = buildPredicates(cb, countQuery, countRoot, tenantId, instructorId, courseTimeId, role, status, keyword);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(assignments, pageable, total);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            CriteriaQuery<?> query,
            Root<InstructorAssignment> assignment,
            Long tenantId,
            Long instructorId,
            Long courseTimeId,
            InstructorRole role,
            AssignmentStatus status,
            String keyword
    ) {
        List<Predicate> predicates = new ArrayList<>();

        // tenantId 필수
        predicates.add(cb.equal(assignment.get("tenantId"), tenantId));

        if (instructorId != null) {
            predicates.add(cb.equal(assignment.get("userKey"), instructorId));
        }

        if (courseTimeId != null) {
            predicates.add(cb.equal(assignment.get("timeKey"), courseTimeId));
        }

        if (role != null) {
            predicates.add(cb.equal(assignment.get("role"), role));
        }

        if (status != null) {
            predicates.add(cb.equal(assignment.get("status"), status));
        }

        // keyword 검색: 강사명(User.name) 또는 차수명(CourseTime.title)
        if (keyword != null && !keyword.isBlank()) {
            // User 서브쿼리 - 강사명 검색
            Subquery<Long> userSubquery = query.subquery(Long.class);
            Root<User> userRoot = userSubquery.from(User.class);
            userSubquery.select(userRoot.get("id"))
                    .where(cb.like(userRoot.get("name"), "%" + keyword + "%"));

            // CourseTime 서브쿼리 - 차수명 검색
            Subquery<Long> courseTimeSubquery = query.subquery(Long.class);
            Root<CourseTime> courseTimeRoot = courseTimeSubquery.from(CourseTime.class);
            courseTimeSubquery.select(courseTimeRoot.get("id"))
                    .where(cb.like(courseTimeRoot.get("title"), "%" + keyword + "%"));

            // OR 조건: userKey IN (userSubquery) OR timeKey IN (courseTimeSubquery)
            predicates.add(cb.or(
                    assignment.get("userKey").in(userSubquery),
                    assignment.get("timeKey").in(courseTimeSubquery)
            ));
        }

        return predicates;
    }
}
