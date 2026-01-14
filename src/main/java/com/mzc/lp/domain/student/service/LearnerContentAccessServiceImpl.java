package com.mzc.lp.domain.student.service;

import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.exception.ContentAccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 * 학습자의 콘텐츠 접근 권한 검증 서비스 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LearnerContentAccessServiceImpl implements LearnerContentAccessService {

    private final EntityManager entityManager;

    @Override
    public void validateContentAccess(Long contentId, Long userId, Long tenantId) {
        boolean hasAccess = checkUserAccessToContent(userId, contentId, tenantId);

        if (!hasAccess) {
            log.warn("User {} denied access to content {} for tenant {}", userId, contentId, tenantId);
            throw new ContentAccessDeniedException(contentId, userId);
        }

        log.debug("User {} granted access to content {} for tenant {}", userId, contentId, tenantId);
    }

    /**
     * 사용자가 해당 콘텐츠에 접근 권한이 있는지 확인
     * 접근 조건: 사용자가 수강 중인 강의(CourseTime)의 스냅샷에 해당 콘텐츠가 포함되어 있어야 함
     *
     * 조회 경로: Enrollment -> CourseTime -> Snapshot -> SnapshotItem -> SnapshotLearningObject(contentId)
     */
    private boolean checkUserAccessToContent(Long userId, Long contentId, Long tenantId) {
        String jpql = """
            SELECT COUNT(e) > 0
            FROM Enrollment e
            JOIN CourseTime ct ON e.courseTimeId = ct.id AND ct.tenantId = :tenantId
            JOIN CourseSnapshot cs ON ct.snapshot.id = cs.id AND cs.tenantId = :tenantId
            JOIN SnapshotItem si ON si.snapshot.id = cs.id AND si.tenantId = :tenantId
            JOIN SnapshotLearningObject slo ON si.snapshotLearningObject.id = slo.id AND slo.tenantId = :tenantId
            WHERE e.userId = :userId
            AND e.tenantId = :tenantId
            AND e.status IN :activeStatuses
            AND slo.contentId = :contentId
            """;

        TypedQuery<Boolean> query = entityManager.createQuery(jpql, Boolean.class);
        query.setParameter("userId", userId);
        query.setParameter("contentId", contentId);
        query.setParameter("tenantId", tenantId);
        query.setParameter("activeStatuses", List.of(EnrollmentStatus.ENROLLED, EnrollmentStatus.COMPLETED));

        return query.getSingleResult();
    }
}