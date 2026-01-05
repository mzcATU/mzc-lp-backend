package com.mzc.lp.domain.memberpool.dto.response;

import com.mzc.lp.domain.memberpool.entity.MemberPool;

import java.time.Instant;

public record MemberPoolResponse(
        Long id,
        String name,
        String description,
        MemberPoolConditionResponse conditions,
        Boolean isActive,
        Integer sortOrder,
        Long memberCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static MemberPoolResponse from(MemberPool pool) {
        return new MemberPoolResponse(
                pool.getId(),
                pool.getName(),
                pool.getDescription(),
                MemberPoolConditionResponse.from(pool.getCondition()),
                pool.getIsActive(),
                pool.getSortOrder(),
                null,
                pool.getCreatedAt(),
                pool.getUpdatedAt()
        );
    }

    public static MemberPoolResponse from(MemberPool pool, Long memberCount) {
        return new MemberPoolResponse(
                pool.getId(),
                pool.getName(),
                pool.getDescription(),
                MemberPoolConditionResponse.from(pool.getCondition()),
                pool.getIsActive(),
                pool.getSortOrder(),
                memberCount,
                pool.getCreatedAt(),
                pool.getUpdatedAt()
        );
    }
}
