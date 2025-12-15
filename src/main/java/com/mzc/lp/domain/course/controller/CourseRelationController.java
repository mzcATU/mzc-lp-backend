package com.mzc.lp.domain.course.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.course.dto.request.CreateRelationRequest;
import com.mzc.lp.domain.course.dto.request.SetStartItemRequest;
import com.mzc.lp.domain.course.dto.response.AutoRelationResponse;
import com.mzc.lp.domain.course.dto.response.CourseRelationResponse;
import com.mzc.lp.domain.course.dto.response.RelationCreateResponse;
import com.mzc.lp.domain.course.dto.response.SetStartItemResponse;
import com.mzc.lp.domain.course.service.CourseRelationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/courses/{courseId}/relations")
@RequiredArgsConstructor
@Validated
public class CourseRelationController {

    private final CourseRelationService courseRelationService;

    /**
     * 학습 순서 설정
     * POST /api/courses/{courseId}/relations
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<RelationCreateResponse>> createRelations(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CreateRelationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RelationCreateResponse response = courseRelationService.createRelations(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 학습 순서 조회
     * GET /api/courses/{courseId}/relations
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CourseRelationResponse>> getRelations(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseRelationResponse response = courseRelationService.getRelations(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 학습 순서 수정 (전체 교체)
     * PUT /api/courses/{courseId}/relations
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<RelationCreateResponse>> updateRelations(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CreateRelationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        RelationCreateResponse response = courseRelationService.updateRelations(courseId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 시작점 설정
     * PUT /api/courses/{courseId}/relations/start
     */
    @PutMapping("/start")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<SetStartItemResponse>> setStartItem(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody SetStartItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        SetStartItemResponse response = courseRelationService.setStartItem(courseId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 자동 순서 생성
     * POST /api/courses/{courseId}/relations/auto
     */
    @PostMapping("/auto")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<AutoRelationResponse>> createAutoRelations(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        AutoRelationResponse response = courseRelationService.createAutoRelations(courseId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 순서 연결 삭제
     * DELETE /api/courses/{courseId}/relations/{relationId}
     */
    @DeleteMapping("/{relationId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteRelation(
            @PathVariable @Positive Long courseId,
            @PathVariable @Positive Long relationId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseRelationService.deleteRelation(courseId, relationId);
        return ResponseEntity.noContent().build();
    }
}
