package com.mzc.lp.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNotificationTemplateRequest(
        @NotBlank(message = "템플릿 이름은 필수입니다")
        @Size(max = 100, message = "템플릿 이름은 100자 이내여야 합니다")
        String name,

        @NotBlank(message = "제목 템플릿은 필수입니다")
        @Size(max = 200, message = "제목 템플릿은 200자 이내여야 합니다")
        String titleTemplate,

        @NotBlank(message = "메시지 템플릿은 필수입니다")
        String messageTemplate,

        @Size(max = 500, message = "설명은 500자 이내여야 합니다")
        String description
) {}
