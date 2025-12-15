package com.mzc.lp.domain.ts.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.response.CapacityResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeDetailResponse;
import com.mzc.lp.domain.ts.dto.response.CourseTimeResponse;
import com.mzc.lp.domain.ts.dto.response.PriceResponse;
import com.mzc.lp.domain.ts.service.CourseTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ts/course-times")
@RequiredArgsConstructor
@Validated
public class CourseTimeController {

    private final CourseTimeService courseTimeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> createCourseTime(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateCourseTimeRequest request
    ) {
        CourseTimeDetailResponse response = courseTimeService.createCourseTime(request, principal.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CourseTimeResponse>>> getCourseTimes(
            @RequestParam(required = false) CourseTimeStatus status,
            @RequestParam(required = false) Long cmCourseId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<CourseTimeResponse> response = courseTimeService.getCourseTimes(status, cmCourseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> getCourseTime(
            @PathVariable Long id
    ) {
        CourseTimeDetailResponse response = courseTimeService.getCourseTime(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> updateCourseTime(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCourseTimeRequest request
    ) {
        CourseTimeDetailResponse response = courseTimeService.updateCourseTime(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteCourseTime(
            @PathVariable Long id
    ) {
        courseTimeService.deleteCourseTime(id);
        return ResponseEntity.noContent().build();
    }

    // ========== 상태 전이 API ==========

    @PostMapping("/{id}/open")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> openCourseTime(
            @PathVariable Long id
    ) {
        CourseTimeDetailResponse response = courseTimeService.openCourseTime(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> startCourseTime(
            @PathVariable Long id
    ) {
        CourseTimeDetailResponse response = courseTimeService.startCourseTime(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> closeCourseTime(
            @PathVariable Long id
    ) {
        CourseTimeDetailResponse response = courseTimeService.closeCourseTime(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAnyRole('OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseTimeDetailResponse>> archiveCourseTime(
            @PathVariable Long id
    ) {
        CourseTimeDetailResponse response = courseTimeService.archiveCourseTime(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ========== Public API (permitAll) ==========

    @GetMapping("/{id}/capacity")
    public ResponseEntity<ApiResponse<CapacityResponse>> getCapacity(
            @PathVariable Long id
    ) {
        CapacityResponse response = courseTimeService.getCapacity(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<ApiResponse<PriceResponse>> getPrice(
            @PathVariable Long id
    ) {
        PriceResponse response = courseTimeService.getPrice(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
