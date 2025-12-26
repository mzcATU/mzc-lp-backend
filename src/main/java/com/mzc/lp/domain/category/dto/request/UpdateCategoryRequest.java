package com.mzc.lp.domain.category.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
        String name,

        @Size(max = 50, message = "카테고리 코드는 50자 이하여야 합니다")
        String code,

        Integer sortOrder,

        Boolean active
) {
    public UpdateCategoryRequest {
        if (name != null) {
            name = name.trim();
        }
        if (code != null) {
            code = code.trim();
        }
    }
}
