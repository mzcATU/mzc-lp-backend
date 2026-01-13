package com.mzc.lp.domain.assignment.dto.request;

import jakarta.validation.constraints.Size;

public record SubmitAssignmentRequest(
        String textContent,

        @Size(max = 500, message = "파일 URL은 500자 이내여야 합니다")
        String fileUrl,

        @Size(max = 255, message = "파일명은 255자 이내여야 합니다")
        String fileName
) {
    public boolean hasContent() {
        return (textContent != null && !textContent.isBlank())
                || (fileUrl != null && !fileUrl.isBlank());
    }
}
