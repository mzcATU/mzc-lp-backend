package com.mzc.lp.domain.community.dto.response;

import com.mzc.lp.domain.community.entity.CommunityPost;
import com.mzc.lp.domain.user.entity.User;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record PostDetailResponse(
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
        Instant updatedAt,
        RelatedCourseResponse relatedCourse
) {
    public static PostDetailResponse from(CommunityPost post, User author, long likeCount, long commentCount, boolean isLiked) {
        String excerpt = post.getContent();
        if (excerpt != null && excerpt.length() > 200) {
            excerpt = excerpt.substring(0, 200) + "...";
        }

        List<String> tagList = null;
        if (post.getTags() != null && !post.getTags().isBlank()) {
            tagList = Arrays.asList(post.getTags().split(","));
        }

        return new PostDetailResponse(
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
                post.getUpdatedAt(),
                null  // relatedCourse는 추후 구현
        );
    }

    public record RelatedCourseResponse(
            Long id,
            String title,
            String thumbnailUrl,
            InstructorInfo instructor,
            Double rating,
            Integer studentCount
    ) {
        public record InstructorInfo(
                Long id,
                String name,
                String profileImage
        ) {}
    }
}
