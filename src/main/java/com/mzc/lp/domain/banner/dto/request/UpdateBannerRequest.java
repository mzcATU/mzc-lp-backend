package com.mzc.lp.domain.banner.dto.request;

import com.mzc.lp.domain.banner.constant.BannerPosition;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateBannerRequest(
        @Size(max = 200, message = "배너 제목은 200자 이하여야 합니다")
        String title,

        @Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다")
        String imageUrl,

        @Size(max = 500, message = "링크 URL은 500자 이하여야 합니다")
        String linkUrl,

        @Size(max = 20, message = "링크 타겟은 20자 이하여야 합니다")
        String linkTarget,

        BannerPosition position,

        Integer sortOrder,

        LocalDate startDate,

        LocalDate endDate,

        @Size(max = 500, message = "설명은 500자 이하여야 합니다")
        String description
) {}
