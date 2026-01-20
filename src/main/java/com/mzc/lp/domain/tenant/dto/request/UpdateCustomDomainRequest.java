package com.mzc.lp.domain.tenant.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateCustomDomainRequest(
        @NotBlank(message = "커스텀 도메인은 필수입니다")
        @Pattern(
                regexp = "^(?!-)[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$",
                message = "올바른 도메인 형식이 아닙니다 (예: learn.example.com)"
        )
        String customDomain
) {
    public UpdateCustomDomainRequest {
        if (customDomain != null) {
            customDomain = customDomain.trim().toLowerCase();
        }
    }
}
