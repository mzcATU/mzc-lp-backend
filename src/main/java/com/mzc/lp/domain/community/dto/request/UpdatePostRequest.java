package com.mzc.lp.domain.community.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePostRequest(
        @Size(max = 255, message = "제목은 255자 이내여야 합니다")
        String title,

        String content,

        @Size(max = 50, message = "카테고리는 50자 이내여야 합니다")
        String category,

        List<String> tags,

        Boolean isPrivate
) {
    public String tagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }
}
