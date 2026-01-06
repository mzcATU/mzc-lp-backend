package com.mzc.lp.domain.certificate.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevokeCertificateRequest(
        @NotBlank(message = "폐기 사유는 필수입니다")
        @Size(max = 500, message = "폐기 사유는 500자를 초과할 수 없습니다")
        String reason
) {
}
