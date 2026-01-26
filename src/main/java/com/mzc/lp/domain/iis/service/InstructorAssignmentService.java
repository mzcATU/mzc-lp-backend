package com.mzc.lp.domain.iis.service;

import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.AssignmentHistoryResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentListResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAvailabilityResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorDetailStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatisticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface InstructorAssignmentService {

    // 배정
    InstructorAssignmentResponse assignInstructor(Long timeId, AssignInstructorRequest request, Long operatorId);

    // 조회
    List<InstructorAssignmentResponse> getInstructorsByTimeId(Long timeId, AssignmentStatus status);

    InstructorAssignmentResponse getAssignment(Long id);

    Page<InstructorAssignmentListResponse> getAssignments(
            Long instructorId,
            Long courseTimeId,
            InstructorRole role,
            AssignmentStatus status,
            String keyword,
            Pageable pageable
    );

    Page<InstructorAssignmentResponse> getAssignmentsByUserId(Long userId, AssignmentStatus status, Pageable pageable);

    List<InstructorAssignmentResponse> getMyAssignments(Long userId);

    // 수정
    InstructorAssignmentResponse updateRole(Long id, UpdateRoleRequest request, Long operatorId);

    InstructorAssignmentResponse replaceInstructor(Long id, ReplaceInstructorRequest request, Long operatorId);

    void cancelAssignment(Long id, CancelAssignmentRequest request, Long operatorId, boolean isTenantAdmin);

    // ========== TS 모듈 연동용 메서드 ==========

    /**
     * [검증용] 특정 차수에 ACTIVE 상태인 MAIN 강사가 존재하는지 확인
     * 용도: TS 모듈에서 CourseTime.open() 호출 시 검증 로직에 사용
     */
    boolean existsMainInstructor(Long timeId);

    /**
     * [성능용] 여러 차수의 강사 정보를 한 번에 조회 (Bulk)
     * 용도: TS 모듈의 '차수 목록(List)' 조회 시 N+1 문제 방지
     * 반환: Key=timeId, Value=강사목록
     */
    Map<Long, List<InstructorAssignmentResponse>> getInstructorsByTimeIds(List<Long> timeIds);

    // ========== 이력 조회 ==========

    /**
     * 특정 배정의 변경 이력 조회
     * @param assignmentId 배정 ID
     * @param action 액션 타입 필터 (null이면 전체 조회)
     * @return 이력 목록 (최신순 정렬)
     */
    List<AssignmentHistoryResponse> getAssignmentHistories(Long assignmentId, AssignmentAction action);

    // ========== 통계 API ==========

    /**
     * 전체 강사 배정 통계 조회
     * @return 전체 통계 (역할별, 상태별, 강사별)
     */
    InstructorStatisticsResponse getStatistics();

    /**
     * 전체 강사 배정 통계 조회 (기간 필터링)
     * @param startDate 시작일 (null이면 전체 기간)
     * @param endDate 종료일 (null이면 전체 기간)
     * @return 전체 통계 (역할별, 상태별, 강사별)
     */
    InstructorStatisticsResponse getStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 특정 강사의 배정 통계 조회
     * @param userId 강사 ID
     * @return 강사 개인 통계
     */
    InstructorStatResponse getInstructorStatistics(Long userId);

    /**
     * 특정 강사의 상세 배정 통계 조회 (차수별 통계 포함)
     * @param userId 강사 ID
     * @param startDate 시작일 (null이면 전체 기간)
     * @param endDate 종료일 (null이면 전체 기간)
     * @return 강사 개인 상세 통계 (차수별 통계 포함)
     */
    InstructorDetailStatResponse getInstructorDetailStatistics(Long userId, LocalDate startDate, LocalDate endDate);

    // ========== 가용성 확인 API ==========

    /**
     * 특정 강사의 가용성 확인
     * @param userId 강사 ID
     * @param startDate 확인할 기간 시작일
     * @param endDate 확인할 기간 종료일
     * @return 가용성 정보 (충돌 배정 목록 포함)
     */
    InstructorAvailabilityResponse checkAvailability(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 여러 강사의 가용성 일괄 확인
     * @param userIds 강사 ID 목록
     * @param startDate 확인할 기간 시작일
     * @param endDate 확인할 기간 종료일
     * @return 강사별 가용성 정보 목록
     */
    List<InstructorAvailabilityResponse> checkAvailabilityBulk(List<Long> userIds, LocalDate startDate, LocalDate endDate);
}
