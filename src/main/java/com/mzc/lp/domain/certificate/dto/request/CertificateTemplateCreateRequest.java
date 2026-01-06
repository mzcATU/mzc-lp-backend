package com.mzc.lp.domain.certificate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CertificateTemplateCreateRequest(
        @NotBlank(message = "템플릿 코드는 필수입니다")
        @Size(max = 50, message = "템플릿 코드는 50자를 초과할 수 없습니다")
        String templateCode,

        @NotBlank(message = "템플릿 제목은 필수입니다")
        @Size(max = 200, message = "템플릿 제목은 200자를 초과할 수 없습니다")
        String title,

        String description,

        @Size(max = 500, message = "배경 이미지 URL은 500자를 초과할 수 없습니다")
        String backgroundImageUrl,

        @Size(max = 500, message = "서명 이미지 URL은 500자를 초과할 수 없습니다")
        String signatureImageUrl,

        String certificateBodyHtml,

        Integer validityMonths,

        Boolean isDefault
) {
}
