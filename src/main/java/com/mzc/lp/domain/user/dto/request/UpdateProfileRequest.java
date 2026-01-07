package com.mzc.lp.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "이름은 50자 이하여야 합니다")
        String name,

        @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
        String phone,

        @Size(max = 500, message = "프로필 이미지 URL은 500자 이하여야 합니다")
        String profileImageUrl,

        @Size(max = 100, message = "부서는 100자 이하여야 합니다")
        String department,

        @Size(max = 50, message = "직급은 50자 이하여야 합니다")
        String position
) {}
