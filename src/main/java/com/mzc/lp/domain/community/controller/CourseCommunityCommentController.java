package com.mzc.lp.domain.community.controller;

import com.mzc.lp.common.dto.ApiResponse;
import com.mzc.lp.common.security.UserPrincipal;
import com.mzc.lp.domain.community.dto.request.CreateCommentRequest;
import com.mzc.lp.domain.community.dto.request.UpdateCommentRequest;
import com.mzc.lp.domain.community.dto.response.CommentListResponse;
import com.mzc.lp.domain.community.dto.response.CommentResponse;
import com.mzc.lp.domain.community.service.CommunityCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 코스 커뮤니티 댓글 API
 * /api/times/{timeId}/community/posts/{postId}/comments
 */
@RestController
@RequestMapping("/api/times/{timeId}/community/posts/{postId}/comments")
@RequiredArgsConstructor
@Validated
public class CourseCommunityCommentController {

    private final CommunityCommentService commentService;

    /**
     * 댓글 목록 조회
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentListResponse>> getComments(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int pageSize,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long userId = principal != null ? principal.id() : null;
        CommentListResponse response = commentService.getComments(postId, page, pageSize, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 댓글 작성
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CommentResponse response = commentService.createComment(principal.id(), postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    /**
     * 댓글 수정
     */
    @PatchMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CommentResponse response = commentService.updateComment(principal.id(), postId, commentId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        commentService.deleteComment(principal.id(), postId, commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 댓글 좋아요
     */
    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likeComment(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        commentService.likeComment(principal.id(), postId, commentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 댓글 좋아요 취소
     */
    @DeleteMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long timeId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        commentService.unlikeComment(principal.id(), postId, commentId);
        return ResponseEntity.ok().build();
    }
}
