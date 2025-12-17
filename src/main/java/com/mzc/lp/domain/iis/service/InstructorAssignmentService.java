package com.mzc.lp.domain.iis.service;

import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.AssignmentHistoryResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface InstructorAssignmentService {

    // 배정
    InstructorAssignmentResponse assignInstructor(Long timeId, AssignInstructorRequest request, Long operatorId);

    // 조회
    List<InstructorAssignmentResponse> getInstructorsByTimeId(Long timeId, AssignmentStatus status);

    InstructorAssignmentResponse getAssignment(Long id);

    Page<InstructorAssignmentResponse> getAssignmentsByUserId(Long userId, AssignmentStatus status, Pageable pageable);

    List<InstructorAssignmentResponse> getMyAssignments(Long userId);

    // 수정
    InstructorAssignmentResponse updateRole(Long id, UpdateRoleRequest request, Long operatorId);

    InstructorAssignmentResponse replaceInstructor(Long id, ReplaceInstructorRequest request, Long operatorId);

    void cancelAssignment(Long id, CancelAssignmentRequest request, Long operatorId);

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
}
