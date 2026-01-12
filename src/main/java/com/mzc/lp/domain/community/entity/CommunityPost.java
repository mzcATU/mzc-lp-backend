package com.mzc.lp.domain.community.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.community.constant.PostType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_posts",
        indexes = {
                @Index(name = "idx_community_post_category", columnList = "category"),
                @Index(name = "idx_community_post_type", columnList = "type"),
                @Index(name = "idx_community_post_author", columnList = "author_id"),
                @Index(name = "idx_community_post_created", columnList = "created_at"),
                @Index(name = "idx_community_post_course_time", columnList = "course_time_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPost extends TenantEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostType type;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    @Column(name = "is_solved", nullable = false)
    private Boolean isSolved = false;

    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;

    @Column(length = 500)
    private String tags;

    /**
     * 코스 커뮤니티용 차수 ID (null이면 전체 커뮤니티)
     */
    @Column(name = "course_time_id")
    private Long courseTimeId;

    public static CommunityPost create(PostType type, String category, String title, String content, Long authorId, String tags) {
        CommunityPost post = new CommunityPost();
        post.type = type;
        post.category = category;
        post.title = title;
        post.content = content;
        post.authorId = authorId;
        post.tags = tags;
        post.viewCount = 0;
        post.isPinned = false;
        post.isSolved = false;
        post.isPrivate = false;
        return post;
    }

    /**
     * 코스 커뮤니티 게시글 생성
     */
    public static CommunityPost createForCourse(PostType type, String category, String title, String content,
                                                 Long authorId, String tags, Long courseTimeId, Boolean isPrivate) {
        CommunityPost post = create(type, category, title, content, authorId, tags);
        post.courseTimeId = courseTimeId;
        post.isPrivate = isPrivate != null ? isPrivate : false;
        return post;
    }

    public void update(String title, String content, String category, String tags, Boolean isPrivate) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (category != null && !category.isBlank()) {
            this.category = category;
        }
        if (tags != null) {
            this.tags = tags;
        }
        if (isPrivate != null) {
            this.isPrivate = isPrivate;
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void markAsSolved() {
        this.isSolved = true;
    }

    public void pin() {
        this.isPinned = true;
    }

    public void unpin() {
        this.isPinned = false;
    }
}
