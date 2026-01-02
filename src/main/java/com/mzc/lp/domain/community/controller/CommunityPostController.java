package com.mzc.lp.domain.community.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.community.dto.request.CreatePostRequest;
import com.mzc.lp.domain.community.dto.request.UpdatePostRequest;
import com.mzc.lp.domain.community.dto.response.CategoryResponse;
import com.mzc.lp.domain.community.dto.response.PostDetailResponse;
import com.mzc.lp.domain.community.dto.response.PostListResponse;
import com.mzc.lp.domain.community.dto.response.PostResponse;
import com.mzc.lp.domain.community.service.CommunityPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
@Validated
public class CommunityPostController {

    private final CommunityPostService postService;

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal != null ? principal.id() : null;
        PostListResponse response = postService.getPosts(search, category, type, sortBy, page, pageSize, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/posts/popular")
    public ResponseEntity<ApiResponse<PostListResponse>> getPopularPosts(
            @RequestParam(required = false, defaultValue = "10") int limit,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal != null ? principal.id() : null;
        PostListResponse response = postService.getPopularPosts(limit, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal != null ? principal.id() : null;
        PostDetailResponse response = postService.getPost(postId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PostResponse response = postService.createPost(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PatchMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PostResponse response = postService.updatePost(principal.id(), postId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        postService.deletePost(principal.id(), postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        postService.likePost(principal.id(), postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        postService.unlikePost(principal.id(), postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategories() {
        CategoryResponse response = postService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
