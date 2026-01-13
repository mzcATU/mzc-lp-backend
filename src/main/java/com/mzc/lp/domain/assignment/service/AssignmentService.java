package com.mzc.lp.domain.assignment.service;

import com.mzc.lp.domain.assignment.dto.request.CreateAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.request.GradeSubmissionRequest;
import com.mzc.lp.domain.assignment.dto.request.SubmitAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.request.UpdateAssignmentRequest;
import com.mzc.lp.domain.assignment.dto.response.AssignmentDetailResponse;
import com.mzc.lp.domain.assignment.dto.response.AssignmentResponse;
import com.mzc.lp.domain.assignment.dto.response.SubmissionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssignmentService {

    // ===== 관리자/강사용 API =====

    /**
     * 과제 생성
     */
    AssignmentResponse createAssignment(Long courseTimeId, CreateAssignmentRequest request, Long createdBy);

    /**
     * 과제 목록 조회 (관리자용 - 전체 상태)
     */
    Page<AssignmentResponse> getAssignments(Long courseTimeId, Pageable pageable);

    /**
     * 과제 상세 조회 (관리자용 - 통계 포함)
     */
    AssignmentDetailResponse getAssignmentDetail(Long assignmentId);

    /**
     * 과제 수정
     */
    AssignmentResponse updateAssignment(Long assignmentId, UpdateAssignmentRequest request);

    /**
     * 과제 삭제
     */
    void deleteAssignment(Long assignmentId);

    /**
     * 과제 발행
     */
    AssignmentResponse publishAssignment(Long assignmentId);

    /**
     * 과제 마감
     */
    AssignmentResponse closeAssignment(Long assignmentId);

    /**
     * 제출물 목록 조회
     */
    Page<SubmissionResponse> getSubmissions(Long assignmentId, Pageable pageable);

    /**
     * 채점
     */
    SubmissionResponse gradeSubmission(Long submissionId, GradeSubmissionRequest request, Long gradedBy);

    // ===== 학생용 API =====

    /**
     * 과제 목록 조회 (학생용 - 발행된 것만)
     */
    Page<AssignmentResponse> getPublishedAssignments(Long courseTimeId, Long studentId, Pageable pageable);

    /**
     * 과제 상세 조회 (학생용)
     */
    AssignmentDetailResponse getAssignmentForStudent(Long assignmentId, Long studentId);

    /**
     * 과제 제출
     */
    SubmissionResponse submitAssignment(Long assignmentId, SubmitAssignmentRequest request, Long studentId);

    /**
     * 내 제출물 조회
     */
    SubmissionResponse getMySubmission(Long assignmentId, Long studentId);
}
