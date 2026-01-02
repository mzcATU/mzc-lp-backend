package com.mzc.lp.domain.community.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_comment_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_comment_like_comment_user",
                columnNames = {"comment_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_comment_like_comment", columnList = "comment_id"),
                @Index(name = "idx_comment_like_user", columnList = "user_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityCommentLike extends TenantEntity {

    @Column(name = "comment_id", nullable = false)
    private Long commentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public static CommunityCommentLike create(Long commentId, Long userId) {
        CommunityCommentLike like = new CommunityCommentLike();
        like.commentId = commentId;
        like.userId = userId;
        return like;
    }
}
