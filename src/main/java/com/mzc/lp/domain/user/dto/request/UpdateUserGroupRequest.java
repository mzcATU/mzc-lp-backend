package com.mzc.lp.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateUserGroupRequest(
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        Boolean isActive
) {
}
