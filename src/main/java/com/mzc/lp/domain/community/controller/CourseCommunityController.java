package com.mzc.lp.domain.community.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.community.dto.request.CreateCoursePostRequest;
import com.mzc.lp.domain.community.dto.request.UpdatePostRequest;
import com.mzc.lp.domain.community.dto.response.CategoryResponse;
import com.mzc.lp.domain.community.dto.response.PostDetailResponse;
import com.mzc.lp.domain.community.dto.response.PostListResponse;
import com.mzc.lp.domain.community.dto.response.PostResponse;
import com.mzc.lp.domain.community.service.CourseCommunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 코스(차수) 단위 커뮤니티 API
 */
@RestController
@RequestMapping("/api/times/{timeId}/community")
@RequiredArgsConstructor
@Validated
public class CourseCommunityController {

    private final CourseCommunityService courseCommunityService;

    /**
     * 코스 커뮤니티 게시글 목록 조회
     * GET /api/times/{timeId}/community/posts
     */
    @GetMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostListResponse>> getPosts(
            @PathVariable Long timeId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PostListResponse response = courseCommunityService.getPosts(
                timeId, search, category, type, sortBy, page, pageSize, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 코스 커뮤니티 게시글 상세 조회
     * GET /api/times/{timeId}/community/posts/{postId}
     */
    @GetMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPost(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PostDetailResponse response = courseCommunityService.getPost(timeId, postId, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 코스 커뮤니티 게시글 작성
     * POST /api/times/{timeId}/community/posts
     */
    @PostMapping("/posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @PathVariable Long timeId,
            @Valid @RequestBody CreateCoursePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PostResponse response = courseCommunityService.createPost(timeId, principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 코스 커뮤니티 게시글 수정
     * PATCH /api/times/{timeId}/community/posts/{postId}
     */
    @PatchMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        PostResponse response = courseCommunityService.updatePost(timeId, principal.id(), postId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 코스 커뮤니티 게시글 삭제
     * DELETE /api/times/{timeId}/community/posts/{postId}
     */
    @DeleteMapping("/posts/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseCommunityService.deletePost(timeId, principal.id(), postId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 코스 커뮤니티 게시글 좋아요
     * POST /api/times/{timeId}/community/posts/{postId}/like
     */
    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likePost(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseCommunityService.likePost(timeId, principal.id(), postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 코스 커뮤니티 게시글 좋아요 취소
     * DELETE /api/times/{timeId}/community/posts/{postId}/like
     */
    @DeleteMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        courseCommunityService.unlikePost(timeId, principal.id(), postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 코스 커뮤니티 카테고리 목록 조회
     * GET /api/times/{timeId}/community/categories
     */
    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategories(
            @PathVariable Long timeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CategoryResponse response = courseCommunityService.getCategories(timeId, principal.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
