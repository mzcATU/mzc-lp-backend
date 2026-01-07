package com.mzc.lp.domain.course.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.course.dto.request.CreateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.request.UpdateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.response.AnnouncementListResponse;
import com.mzc.lp.domain.course.dto.response.AnnouncementResponse;
import com.mzc.lp.domain.course.service.CourseAnnouncementService;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 차수별 공지사항 API
 */
@RestController
@RequestMapping("/api/times/{timeId}/announcements")
@RequiredArgsConstructor
@Validated
public class CourseTimeAnnouncementController {

    private final CourseAnnouncementService announcementService;
    private final CourseTimeRepository courseTimeRepository;

    /**
     * 차수별 공지 작성 (강사/운영자)
     * POST /api/times/{timeId}/announcements
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @PathVariable Long timeId,
            @Valid @RequestBody CreateAnnouncementRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        // 차수에서 코스 ID 조회
        Long courseId = courseTimeRepository.findById(timeId)
                .map(ct -> ct.getCmCourseId())
                .orElse(null);

        AnnouncementResponse response = announcementService.createAnnouncementForCourseTime(
                courseId, timeId, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 차수별 공지 목록 조회
     * GET /api/times/{timeId}/announcements
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AnnouncementListResponse>> getAnnouncements(
            @PathVariable Long timeId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize
    ) {
        AnnouncementListResponse response = announcementService.getAnnouncementsByCourseTime(timeId, page, pageSize);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공지 상세 조회
     * GET /api/times/{timeId}/announcements/{announcementId}
     */
    @GetMapping("/{announcementId}")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> getAnnouncement(
            @PathVariable Long timeId,
            @PathVariable Long announcementId
    ) {
        AnnouncementResponse response = announcementService.getAnnouncement(announcementId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공지 수정 (작성자 또는 관리자)
     * PUT /api/times/{timeId}/announcements/{announcementId}
     */
    @PutMapping("/{announcementId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @PathVariable Long timeId,
            @PathVariable Long announcementId,
            @Valid @RequestBody UpdateAnnouncementRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = isAdmin(principal);
        AnnouncementResponse response = announcementService.updateAnnouncement(announcementId, principal.id(), request, isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 공지 삭제 (작성자 또는 관리자)
     * DELETE /api/times/{timeId}/announcements/{announcementId}
     */
    @DeleteMapping("/{announcementId}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long timeId,
            @PathVariable Long announcementId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isAdmin = isAdmin(principal);
        announcementService.deleteAnnouncement(announcementId, principal.id(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(UserPrincipal principal) {
        return "OPERATOR".equals(principal.role())
                || "TENANT_ADMIN".equals(principal.role())
                || "SYSTEM_ADMIN".equals(principal.role());
    }
}
