package com.mzc.lp.domain.student.service;

/**
 * 학습자의 콘텐츠 접근 권한 검증 서비스
 * 수강 신청한 강의의 콘텐츠에 대한 접근 권한을 검증
 */
public interface LearnerContentAccessService {

    /**
     * 콘텐츠 접근 권한 검증
     * - 사용자가 해당 콘텐츠가 포함된 강의에 수강 신청했는지 확인
     *
     * @param contentId 콘텐츠 ID
     * @param userId 사용자 ID
     * @param tenantId 테넌트 ID
     * @throws com.mzc.lp.domain.student.exception.ContentAccessDeniedException 접근 권한이 없는 경우
     */
    void validateContentAccess(Long contentId, Long userId, Long tenantId);
}
