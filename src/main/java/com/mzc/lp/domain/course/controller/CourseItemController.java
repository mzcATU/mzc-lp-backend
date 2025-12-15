package com.mzc.lp.domain.course.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.course.dto.request.CreateFolderRequest;
import com.mzc.lp.domain.course.dto.request.CreateItemRequest;
import com.mzc.lp.domain.course.dto.request.MoveItemRequest;
import com.mzc.lp.domain.course.dto.request.UpdateItemNameRequest;
import com.mzc.lp.domain.course.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.course.dto.response.CourseItemHierarchyResponse;
import com.mzc.lp.domain.course.dto.response.CourseItemResponse;
import com.mzc.lp.domain.course.service.CourseItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses/{courseId}")
@RequiredArgsConstructor
@Validated
public class CourseItemController {

    private final CourseItemService courseItemService;

    /**
     * 차시 추가
     * POST /api/courses/{courseId}/items
     */
    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseItemResponse>> createItem(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CreateItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseItemResponse response = courseItemService.createItem(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 폴더 생성
     * POST /api/courses/{courseId}/folders
     */
    @PostMapping("/folders")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseItemResponse>> createFolder(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CreateFolderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseItemResponse response = courseItemService.createFolder(courseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 계층 구조 조회
     * GET /api/courses/{courseId}/items/hierarchy
     */
    @GetMapping("/items/hierarchy")
    public ResponseEntity<ApiResponse<List<CourseItemHierarchyResponse>>> getHierarchy(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CourseItemHierarchyResponse> response = courseItemService.getHierarchy(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 순서대로 차시 조회
     * GET /api/courses/{courseId}/items/ordered
     */
    @GetMapping("/items/ordered")
    public ResponseEntity<ApiResponse<List<CourseItemResponse>>> getOrderedItems(
            @PathVariable @Positive Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CourseItemResponse> response = courseItemService.getOrderedItems(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 항목 이동
     * PUT /api/courses/{courseId}/items/move
     */
    @PutMapping("/items/move")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseItemResponse>> moveItem(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody MoveItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseItemResponse response = courseItemService.moveItem(courseId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 항목 이름 변경
     * PATCH /api/courses/{courseId}/items/{itemId}/name
     */
    @PatchMapping("/items/{itemId}/name")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseItemResponse>> updateItemName(
            @PathVariable @Positive Long courseId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody UpdateItemNameRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseItemResponse response = courseItemService.updateItemName(courseId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 학습 객체 변경
     * PATCH /api/courses/{courseId}/items/{itemId}/learning-object
     */
    @PatchMapping("/items/{itemId}/learning-object")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<CourseItemResponse>> updateLearningObject(
            @PathVariable @Positive Long courseId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody UpdateLearningObjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CourseItemResponse response = courseItemService.updateLearningObject(courseId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 항목 삭제
     * DELETE /api/courses/{courseId}/items/{itemId}
     */
    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable @Positive Long courseId,
            @PathVariable @Positive Long itemId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseItemService.deleteItem(courseId, itemId);
        return ResponseEntity.noContent().build();
    }
}
