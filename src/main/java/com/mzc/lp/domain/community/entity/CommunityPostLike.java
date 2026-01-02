package com.mzc.lp.domain.community.entity;

import com.mzc.lp.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "community_post_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_post_like_post_user",
                columnNames = {"post_id", "user_id"}
        ),
        indexes = {
                @Index(name = "idx_post_like_post", columnList = "post_id"),
                @Index(name = "idx_post_like_user", columnList = "user_id")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityPostLike extends TenantEntity {

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public static CommunityPostLike create(Long postId, Long userId) {
        CommunityPostLike like = new CommunityPostLike();
        like.postId = postId;
        like.userId = userId;
        return like;
    }
}
