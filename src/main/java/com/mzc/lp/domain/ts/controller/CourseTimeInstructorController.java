package com.mzc.lp.domain.ts.controller;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.exception.CourseTimeNotModifiableException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/ts/course-times/{timeId}/instructors")
@RequiredArgsConstructor
@Validated
public class CourseTimeInstructorController {

    private final CourseTimeRepository courseTimeRepository;
    private final InstructorAssignmentService instructorAssignmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorAssignmentResponse>> assignInstructor(
            @PathVariable Long timeId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AssignInstructorRequest request
    ) {
        log.info("Assigning instructor to course time: timeId={}, userId={}", timeId, request.userId());

        CourseTime courseTime = getCourseTimeOrThrow(timeId);
        validateModifiable(courseTime);

        InstructorAssignmentResponse response = instructorAssignmentService.assignInstructor(
                timeId, request, principal.id()
        );

        log.info("Instructor assigned: timeId={}, assignmentId={}", timeId, response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<List<InstructorAssignmentResponse>>> getInstructors(
            @PathVariable Long timeId,
            @RequestParam(required = false) AssignmentStatus status
    ) {
        log.debug("Getting instructors for course time: timeId={}, status={}", timeId, status);

        getCourseTimeOrThrow(timeId);

        List<InstructorAssignmentResponse> response = instructorAssignmentService.getInstructorsByTimeId(timeId, status);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorAssignmentResponse>> updateRole(
            @PathVariable Long timeId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        log.info("Updating instructor role: timeId={}, assignmentId={}, newRole={}",
                timeId, assignmentId, request.role());

        CourseTime courseTime = getCourseTimeOrThrow(timeId);
        validateModifiable(courseTime);

        InstructorAssignmentResponse response = instructorAssignmentService.updateRole(
                assignmentId, request, principal.id()
        );

        log.info("Instructor role updated: assignmentId={}", assignmentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{assignmentId}/replace")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<InstructorAssignmentResponse>> replaceInstructor(
            @PathVariable Long timeId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReplaceInstructorRequest request
    ) {
        log.info("Replacing instructor: timeId={}, assignmentId={}, newUserId={}",
                timeId, assignmentId, request.newUserId());

        CourseTime courseTime = getCourseTimeOrThrow(timeId);
        validateModifiable(courseTime);

        InstructorAssignmentResponse response = instructorAssignmentService.replaceInstructor(
                assignmentId, request, principal.id()
        );

        log.info("Instructor replaced: oldAssignmentId={}, newAssignmentId={}", assignmentId, response.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> cancelAssignment(
            @PathVariable Long timeId,
            @PathVariable Long assignmentId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody(required = false) CancelAssignmentRequest request
    ) {
        log.info("Cancelling instructor assignment: timeId={}, assignmentId={}", timeId, assignmentId);

        CourseTime courseTime = getCourseTimeOrThrow(timeId);
        validateModifiable(courseTime);

        CancelAssignmentRequest cancelRequest = request != null ? request : new CancelAssignmentRequest(null);
        instructorAssignmentService.cancelAssignment(assignmentId, cancelRequest, principal.id());

        log.info("Instructor assignment cancelled: assignmentId={}", assignmentId);
        return ResponseEntity.noContent().build();
    }

    // ========== Private Methods ==========

    private CourseTime getCourseTimeOrThrow(Long timeId) {
        return courseTimeRepository.findByIdAndTenantId(timeId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseTimeNotFoundException(timeId));
    }

    private void validateModifiable(CourseTime courseTime) {
        if (courseTime.isClosed() || courseTime.isArchived()) {
            throw new CourseTimeNotModifiableException(courseTime.getId(), courseTime.getStatus());
        }
    }
}
