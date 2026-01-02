package com.mzc.lp.domain.community.dto.request;

import com.mzc.lp.domain.community.constant.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePostRequest(
        @NotNull(message = "게시글 타입은 필수입니다")
        PostType type,

        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 255, message = "제목은 255자 이내여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        @NotBlank(message = "카테고리는 필수입니다")
        @Size(max = 50, message = "카테고리는 50자 이내여야 합니다")
        String category,

        List<String> tags
) {
    public String tagsAsString() {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        return String.join(",", tags);
    }
}
