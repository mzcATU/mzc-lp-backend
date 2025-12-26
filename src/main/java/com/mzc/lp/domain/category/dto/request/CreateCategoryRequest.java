package com.mzc.lp.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다")
        @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
        String name,

        @NotBlank(message = "카테고리 코드는 필수입니다")
        @Size(max = 50, message = "카테고리 코드는 50자 이하여야 합니다")
        String code,

        Integer sortOrder
) {
    public CreateCategoryRequest {
        if (name != null) {
            name = name.trim();
        }
        if (code != null) {
            code = code.trim();
        }
    }
}
