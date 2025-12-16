package com.mzc.lp.domain.iis.service;

import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;

import java.util.List;
import java.util.Map;

public interface InstructorAssignmentService {

    // ===== Command =====

    InstructorAssignmentResponse assignInstructor(Long timeId, AssignInstructorRequest request, Long operatorId);

    InstructorAssignmentResponse updateRole(Long assignmentId, UpdateRoleRequest request, Long operatorId);

    InstructorAssignmentResponse replaceInstructor(Long assignmentId, ReplaceInstructorRequest request, Long operatorId);

    void cancelAssignment(Long assignmentId, CancelAssignmentRequest request, Long operatorId);

    // ===== Query =====

    List<InstructorAssignmentResponse> getInstructorsByTimeId(Long timeId, AssignmentStatus status);

    // ===== TS 연동용 =====

    boolean existsMainInstructor(Long timeId);

    Map<Long, List<InstructorAssignmentResponse>> getInstructorsByTimeIds(List<Long> timeIds);
}
