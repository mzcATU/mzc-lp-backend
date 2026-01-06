package com.mzc.lp.domain.community.dto.response;

import com.mzc.lp.domain.community.entity.CommunityPost;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record PostResponse(
        Long id,
        String type,
        String title,
        String content,
        String excerpt,
        AuthorResponse author,
        String category,
        List<String> tags,
        Integer viewCount,
        Long likeCount,
        Long commentCount,
        Boolean isLiked,
        Boolean isPinned,
        Boolean isSolved,
        Long courseTimeId,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(CommunityPost post, User author, long likeCount, long commentCount, boolean isLiked) {
        String excerpt = post.getContent();
        if (excerpt != null && excerpt.length() > 200) {
            excerpt = excerpt.substring(0, 200) + "...";
        }

        List<String> tagList = null;
        if (post.getTags() != null && !post.getTags().isBlank()) {
            tagList = Arrays.asList(post.getTags().split(","));
        }

        return new PostResponse(
                post.getId(),
                post.getType().name().toLowerCase(),
                post.getTitle(),
                post.getContent(),
                excerpt,
                AuthorResponse.from(author),
                post.getCategory(),
                tagList,
                post.getViewCount(),
                likeCount,
                commentCount,
                isLiked,
                post.getIsPinned(),
                post.getIsSolved(),
                post.getCourseTimeId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
