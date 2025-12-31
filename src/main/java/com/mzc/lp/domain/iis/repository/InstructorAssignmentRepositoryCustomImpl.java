package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
            Pageable pageable
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // Main query
        CriteriaQuery<InstructorAssignment> query = cb.createQuery(InstructorAssignment.class);
        Root<InstructorAssignment> assignment = query.from(InstructorAssignment.class);

        List<Predicate> predicates = buildPredicates(cb, assignment, tenantId, instructorId, courseTimeId, role, status);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(assignment.get("createdAt")));

        TypedQuery<InstructorAssignment> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<InstructorAssignment> assignments = typedQuery.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InstructorAssignment> countRoot = countQuery.from(InstructorAssignment.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, tenantId, instructorId, courseTimeId, role, status);
        countQuery.select(cb.count(countRoot));
        countQuery.where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(assignments, pageable, total);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<InstructorAssignment> assignment,
            Long tenantId,
            Long instructorId,
            Long courseTimeId,
            InstructorRole role,
            AssignmentStatus status
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

        return predicates;
    }
}
