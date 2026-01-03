package com.mzc.lp.domain.tenant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 네비게이션 아이템 생성/수정 요청 DTO
 */
public record NavigationItemRequest(
        @NotBlank(message = "메뉴 이름은 필수입니다")
        @Size(max = 100)
        String label,

        @NotBlank(message = "아이콘은 필수입니다")
        @Size(max = 50)
        String icon,

        @NotBlank(message = "경로는 필수입니다")
        @Size(max = 500)
        String path,

        Boolean enabled,

        Integer displayOrder,

        @Size(max = 50)
        String target
) {
}
