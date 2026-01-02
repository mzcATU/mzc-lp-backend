package com.mzc.lp.domain.community.dto.response;

import com.mzc.lp.domain.community.entity.CommunityComment;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.util.List;

public record CommentResponse(
        Long id,
        Long postId,
        String content,
        AuthorResponse author,
        Long likeCount,
        Boolean isLiked,
        Long parentId,
        List<CommentResponse> replies,
        Instant createdAt,
        Instant updatedAt
) {
    public static CommentResponse from(CommunityComment comment, User author, long likeCount, boolean isLiked, List<CommentResponse> replies) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getContent(),
                AuthorResponse.from(author),
                likeCount,
                isLiked,
                comment.getParentId(),
                replies,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    public static CommentResponse from(CommunityComment comment, User author, long likeCount, boolean isLiked) {
        return from(comment, author, likeCount, isLiked, null);
    }
}
