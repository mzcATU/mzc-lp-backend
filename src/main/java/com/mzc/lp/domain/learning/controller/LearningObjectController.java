package com.mzc.lp.domain.learning.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.learning.dto.request.CreateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.request.MoveFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.response.LearningObjectResponse;
import com.mzc.lp.domain.learning.service.LearningObjectService;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
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
@RequestMapping("/api/learning-objects")
@RequiredArgsConstructor
@Validated
public class LearningObjectController {

    private final LearningObjectService learningObjectService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<LearningObjectResponse>> create(
            @Valid @RequestBody CreateLearningObjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        LearningObjectResponse response = learningObjectService.create(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<Page<LearningObjectResponse>>> getLearningObjects(
            @RequestParam(required = false) Long folderId,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        Page<LearningObjectResponse> response = learningObjectService.getLearningObjects(
                tenantId, folderId, keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{learningObjectId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<LearningObjectResponse>> getLearningObject(
            @PathVariable Long learningObjectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        LearningObjectResponse response = learningObjectService.getLearningObject(
                learningObjectId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/content/{contentId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<LearningObjectResponse>> getLearningObjectByContentId(
            @PathVariable Long contentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        LearningObjectResponse response = learningObjectService.getLearningObjectByContentId(
                contentId, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{learningObjectId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<LearningObjectResponse>> update(
            @PathVariable Long learningObjectId,
            @Valid @RequestBody UpdateLearningObjectRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        LearningObjectResponse response = learningObjectService.update(
                learningObjectId, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{learningObjectId}/folder")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<ApiResponse<LearningObjectResponse>> moveToFolder(
            @PathVariable Long learningObjectId,
            @Valid @RequestBody MoveFolderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        LearningObjectResponse response = learningObjectService.moveToFolder(
                learningObjectId, request, tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{learningObjectId}")
    @PreAuthorize("hasAnyRole('DESIGNER', 'OPERATOR', 'TENANT_ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable Long learningObjectId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long tenantId = getTenantId(principal.id());
        learningObjectService.delete(learningObjectId, tenantId);
        return ResponseEntity.noContent().build();
    }

    private Long getTenantId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return user.getTenantId();
    }
}
