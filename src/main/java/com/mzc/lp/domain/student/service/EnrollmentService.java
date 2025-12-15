package com.mzc.lp.domain.student.service;

import com.mzc.lp.domain.student.constant.EnrollmentStatus;
import com.mzc.lp.domain.student.dto.request.CompleteEnrollmentRequest;
import com.mzc.lp.domain.student.dto.request.ForceEnrollRequest;
import com.mzc.lp.domain.student.dto.request.UpdateEnrollmentStatusRequest;
import com.mzc.lp.domain.student.dto.request.UpdateProgressRequest;
import com.mzc.lp.domain.student.dto.response.EnrollmentDetailResponse;
import com.mzc.lp.domain.student.dto.response.EnrollmentResponse;
import com.mzc.lp.domain.student.dto.response.ForceEnrollResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EnrollmentService {

    // 수강 신청
    EnrollmentDetailResponse enroll(Long courseTimeId, Long userId);

    // 강제 배정 (필수 교육)
    ForceEnrollResultResponse forceEnroll(Long courseTimeId, ForceEnrollRequest request, Long operatorId);

    // 수강 상세 조회
    EnrollmentDetailResponse getEnrollment(Long enrollmentId);

    // 차수별 수강생 목록 조회
    Page<EnrollmentResponse> getEnrollmentsByCourseTime(Long courseTimeId, EnrollmentStatus status, Pageable pageable);

    // 내 수강 목록 조회
    Page<EnrollmentResponse> getMyEnrollments(Long userId, EnrollmentStatus status, Pageable pageable);

    // 사용자별 수강 이력 조회 (관리자)
    Page<EnrollmentResponse> getEnrollmentsByUser(Long userId, EnrollmentStatus status, Pageable pageable);

    // 진도율 업데이트
    EnrollmentResponse updateProgress(Long enrollmentId, UpdateProgressRequest request, Long userId);

    // 수료 처리
    EnrollmentDetailResponse completeEnrollment(Long enrollmentId, CompleteEnrollmentRequest request);

    // 상태 변경 (관리자)
    EnrollmentDetailResponse updateStatus(Long enrollmentId, UpdateEnrollmentStatusRequest request);

    // 수강 취소
    void cancelEnrollment(Long enrollmentId, Long userId);
}
