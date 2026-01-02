package com.mzc.lp.domain.community.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_comments",
        indexes = {
                @Index(name = "idx_community_comment_post", columnList = "post_id"),
                @Index(name = "idx_community_comment_parent", columnList = "parent_id"),
                @Index(name = "idx_community_comment_author", columnList = "author_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityComment extends TenantEntity {

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "parent_id")
    private Long parentId;

    public static CommunityComment create(Long postId, Long authorId, String content, Long parentId) {
        CommunityComment comment = new CommunityComment();
        comment.postId = postId;
        comment.authorId = authorId;
        comment.content = content;
        comment.parentId = parentId;
        return comment;
    }

    public void update(String content) {
        if (content != null && !content.isBlank()) {
            this.content = content;
        }
    }

    public boolean isReply() {
        return this.parentId != null;
    }
}
