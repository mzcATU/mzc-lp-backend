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
                @Index(name = "idx_community_post_created", columnList = "created_at")
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

    @Column(length = 500)
    private String tags;

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
        return post;
    }

    public void update(String title, String content, String category, String tags) {
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
