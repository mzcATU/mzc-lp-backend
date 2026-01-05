package com.mzc.lp.domain.memberpool.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateMemberPoolRequest(
        @Size(max = 200, message = "멤버 풀 이름은 200자 이하여야 합니다")
        String name,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다")
        String description,

        MemberPoolConditionDto conditions,

        Integer sortOrder
) {}
