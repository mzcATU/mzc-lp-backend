package com.mzc.lp.domain.sa.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.domain.sa.dto.request.CreateSystemAdminRequest;
import com.mzc.lp.domain.sa.dto.response.SystemAdminUserResponse;
import com.mzc.lp.domain.sa.service.SaUserService;
import com.mzc.lp.domain.user.constant.UserStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/sa/users")
@RequiredArgsConstructor
public class SaUserController {

    private final SaUserService saUserService;

    /**
     * SYSTEM_ADMIN 역할을 가진 사용자 목록 조회
     * GET /api/sa/users
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SystemAdminUserResponse>>> getSystemAdmins(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        log.debug("Getting system admin users: keyword={}, status={}", keyword, status);
        Page<SystemAdminUserResponse> response = saUserService.getSystemAdmins(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * SYSTEM_ADMIN 사용자 생성
     * POST /api/sa/users
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<SystemAdminUserResponse>> createSystemAdmin(
            @Valid @RequestBody CreateSystemAdminRequest request
    ) {
        log.info("Creating system admin: email={}", request.email());
        SystemAdminUserResponse response = saUserService.createSystemAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }
}
