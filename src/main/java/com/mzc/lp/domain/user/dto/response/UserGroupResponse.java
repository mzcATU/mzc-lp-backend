package com.mzc.lp.domain.user.dto.response;

import com.mzc.lp.domain.user.entity.UserGroup;

import java.time.Instant;

public record UserGroupResponse(
        Long id,
        String name,
        String description,
        Boolean isActive,
        Integer memberCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static UserGroupResponse from(UserGroup group) {
        return new UserGroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getIsActive(),
                group.getMemberCount(),
                group.getCreatedAt(),
                group.getUpdatedAt()
        );
    }
}
