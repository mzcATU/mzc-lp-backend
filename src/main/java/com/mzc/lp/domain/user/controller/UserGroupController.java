package com.mzc.lp.domain.user.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.user.dto.request.CreateUserGroupRequest;
import com.mzc.lp.domain.user.dto.request.UpdateUserGroupRequest;
import com.mzc.lp.domain.user.dto.response.UserGroupResponse;
import com.mzc.lp.domain.user.service.UserGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 그룹 컨트롤러
 * TA/Operator가 테넌트 내 사용자 그룹을 관리
 */
@Tag(name = "User Groups", description = "사용자 그룹 관리 API")
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class UserGroupController {

    private final UserGroupService userGroupService;

    @Operation(summary = "그룹 생성", description = "새로운 사용자 그룹을 생성합니다")
    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<UserGroupResponse>> createGroup(
            @Valid @RequestBody CreateUserGroupRequest request
    ) {
        UserGroupResponse response = userGroupService.createGroup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "그룹 목록 조회", description = "사용자 그룹 목록을 조회합니다")
    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<Page<UserGroupResponse>>> getGroups(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Page<UserGroupResponse> response = userGroupService.getGroups(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "활성 그룹 목록 조회", description = "활성화된 모든 그룹을 조회합니다")
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<List<UserGroupResponse>>> getActiveGroups() {
        List<UserGroupResponse> response = userGroupService.getAllActiveGroups();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "그룹 상세 조회", description = "특정 그룹의 상세 정보를 조회합니다")
    @GetMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<UserGroupResponse>> getGroup(
            @PathVariable Long groupId
    ) {
        UserGroupResponse response = userGroupService.getGroup(groupId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "그룹 수정", description = "그룹 정보를 수정합니다")
    @PutMapping("/{groupId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<ApiResponse<UserGroupResponse>> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateUserGroupRequest request
    ) {
        UserGroupResponse response = userGroupService.updateGroup(groupId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "그룹 삭제", description = "그룹을 삭제합니다")
    @DeleteMapping("/{groupId}")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId
    ) {
        userGroupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "그룹에 멤버 추가", description = "그룹에 사용자를 추가합니다")
    @PostMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> addMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        userGroupService.addMember(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "그룹에서 멤버 제거", description = "그룹에서 사용자를 제거합니다")
    @DeleteMapping("/{groupId}/members/{userId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'OPERATOR')")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long userId
    ) {
        userGroupService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
