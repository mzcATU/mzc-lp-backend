package com.mzc.lp.domain.wishlist.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.wishlist.dto.request.WishlistAddRequest;
import com.mzc.lp.domain.wishlist.dto.request.WishlistCheckRequest;
import com.mzc.lp.domain.wishlist.dto.response.WishlistCheckResponse;
import com.mzc.lp.domain.wishlist.dto.response.WishlistCountResponse;
import com.mzc.lp.domain.wishlist.dto.response.WishlistItemResponse;
import com.mzc.lp.domain.wishlist.service.WishlistService;
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
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Validated
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * 찜 추가
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistItemResponse>> addToWishlist(
            @Valid @RequestBody WishlistAddRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WishlistItemResponse response = wishlistService.addToWishlist(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 찜 삭제
     */
    @DeleteMapping("/courses/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        wishlistService.removeFromWishlist(principal.id(), courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 내 찜 목록 조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<WishlistItemResponse>>> getMyWishlist(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Page<WishlistItemResponse> response = wishlistService.getWishlist(principal.id(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 강의 찜 여부 확인
     */
    @GetMapping("/courses/{courseId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> checkWishlistStatus(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isInWishlist = wishlistService.isInWishlist(principal.id(), courseId);
        return ResponseEntity.ok(ApiResponse.success(isInWishlist));
    }

    /**
     * 여러 강의 찜 여부 일괄 확인
     */
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistCheckResponse>> checkWishlistStatusBulk(
            @Valid @RequestBody WishlistCheckRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WishlistCheckResponse response = wishlistService.checkWishlistStatus(principal.id(), request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 내 찜 개수 조회
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<WishlistCountResponse>> getMyWishlistCount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        WishlistCountResponse response = wishlistService.getWishlistCount(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 강의의 찜 개수 조회 (공개)
     */
    @GetMapping("/courses/{courseId}/count")
    public ResponseEntity<ApiResponse<WishlistCountResponse>> getCourseWishlistCount(
            @PathVariable Long courseId
    ) {
        WishlistCountResponse response = wishlistService.getCourseWishlistCount(courseId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
