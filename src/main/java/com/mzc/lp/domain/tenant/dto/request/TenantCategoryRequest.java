package com.mzc.lp.domain.tenant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 테넌트 카테고리 생성/수정 요청 DTO
 */
public record TenantCategoryRequest(
        @NotBlank(message = "카테고리 이름은 필수입니다")
        @Size(max = 100, message = "카테고리 이름은 100자 이하여야 합니다")
        String name,

        @NotBlank(message = "카테고리 슬러그는 필수입니다")
        @Size(max = 100, message = "카테고리 슬러그는 100자 이하여야 합니다")
        String slug,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다")
        String description,

        @Size(max = 50, message = "아이콘은 50자 이하여야 합니다")
        String icon,

        Integer displayOrder,

        Boolean enabled
) {
    public TenantCategoryRequest {
        if (slug != null) {
            slug = slug.trim().toLowerCase().replaceAll("\\s+", "-");
        }
        if (name != null) {
            name = name.trim();
        }
    }
}
