package com.mzc.lp.domain.tenant.dto.request;

import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateTenantRequest(
        @NotBlank(message = "테넌트 코드는 필수입니다")
        @Size(max = 50, message = "테넌트 코드는 50자 이하여야 합니다")
        @Pattern(regexp = "^[A-Z0-9_]+$", message = "테넌트 코드는 대문자, 숫자, 언더스코어만 허용됩니다")
        String code,

        @NotBlank(message = "테넌트 이름은 필수입니다")
        @Size(max = 100, message = "테넌트 이름은 100자 이하여야 합니다")
        String name,

        @NotNull(message = "테넌트 타입은 필수입니다")
        TenantType type,

        @NotBlank(message = "서브도메인은 필수입니다")
        @Size(max = 50, message = "서브도메인은 50자 이하여야 합니다")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "서브도메인은 소문자, 숫자, 하이픈만 허용됩니다")
        String subdomain,

        PlanType plan,

        @Size(max = 255, message = "커스텀 도메인은 255자 이하여야 합니다")
        String customDomain
) {
    public CreateTenantRequest {
        if (code != null) {
            code = code.trim().toUpperCase();
        }
        if (name != null) {
            name = name.trim();
        }
        if (subdomain != null) {
            subdomain = subdomain.trim().toLowerCase();
        }
        if (customDomain != null) {
            customDomain = customDomain.trim().toLowerCase();
        }
    }
}
