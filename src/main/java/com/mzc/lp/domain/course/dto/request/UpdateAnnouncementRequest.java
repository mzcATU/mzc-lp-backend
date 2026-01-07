package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.Size;

/**
 * 공지사항 수정 요청 DTO
 */
public record UpdateAnnouncementRequest(
        @Size(max = 255, message = "제목은 255자 이내여야 합니다")
        String title,

        String content,

        Boolean isImportant
) {
}
