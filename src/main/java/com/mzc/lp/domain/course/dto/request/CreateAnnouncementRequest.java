package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 공지사항 생성 요청 DTO
 */
public record CreateAnnouncementRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자 이내여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        Boolean isImportant
) {
}
