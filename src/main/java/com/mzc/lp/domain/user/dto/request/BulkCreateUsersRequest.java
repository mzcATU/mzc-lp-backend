package com.mzc.lp.domain.user.dto.request;

import com.mzc.lp.domain.user.constant.TenantRole;
import jakarta.validation.constraints.*;

public record BulkCreateUsersRequest(
        @NotBlank(message = "이메일 접두사는 필수입니다")
        @Size(max = 30, message = "이메일 접두사는 30자 이하여야 합니다")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "이메일 접두사는 영문, 숫자, 밑줄, 하이픈만 사용 가능합니다")
        String emailPrefix,

        @NotBlank(message = "이메일 도메인은 필수입니다")
        @Pattern(regexp = "^@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "올바른 이메일 도메인 형식이 아닙니다 (예: @company.com)")
        String emailDomain,

        @NotNull(message = "생성할 계정 수는 필수입니다")
        @Min(value = 1, message = "최소 1개 이상의 계정을 생성해야 합니다")
        @Max(value = 100, message = "한 번에 최대 100개까지 생성 가능합니다")
        Integer count,

        @NotBlank(message = "초기 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        String password,

        @Min(value = 1, message = "시작 번호는 1 이상이어야 합니다")
        Integer startNumber,

        TenantRole role
) {
    public BulkCreateUsersRequest {
        if (emailPrefix != null) {
            emailPrefix = emailPrefix.toLowerCase().trim();
        }
        if (emailDomain != null) {
            emailDomain = emailDomain.toLowerCase().trim();
        }
        if (startNumber == null) {
            startNumber = 1;
        }
        if (role == null) {
            role = TenantRole.USER;
        }
    }
}
