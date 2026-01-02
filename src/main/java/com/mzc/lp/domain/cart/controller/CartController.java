package com.mzc.lp.domain.cart.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.cart.dto.request.CartAddRequest;
import com.mzc.lp.domain.cart.dto.request.CartRemoveRequest;
import com.mzc.lp.domain.cart.dto.response.CartCountResponse;
import com.mzc.lp.domain.cart.dto.response.CartItemResponse;
import com.mzc.lp.domain.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 목록 조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CartItemResponse>>> getCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<CartItemResponse> response = cartService.getCart(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 장바구니에 강의 추가
     */
    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody CartAddRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartItemResponse response = cartService.addToCart(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 장바구니에서 강의 삭제
     */
    @DeleteMapping("/items/{courseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        cartService.removeFromCart(principal.id(), courseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 장바구니에서 여러 강의 삭제 (선택 삭제)
     */
    @DeleteMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeFromCartBulk(
            @Valid @RequestBody CartRemoveRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        cartService.removeFromCartBulk(principal.id(), request);
        return ResponseEntity.noContent().build();
    }

    /**
     * 장바구니 개수 조회
     */
    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CartCountResponse>> getCartCount(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CartCountResponse response = cartService.getCartCount(principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 특정 강의 장바구니 여부 확인
     */
    @GetMapping("/items/{courseId}/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Boolean>> checkCartStatus(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isInCart = cartService.isInCart(principal.id(), courseId);
        return ResponseEntity.ok(ApiResponse.success(isInCart));
    }
}
