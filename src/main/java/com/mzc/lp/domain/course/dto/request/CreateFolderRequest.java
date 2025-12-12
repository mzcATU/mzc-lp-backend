package com.mzc.lp.domain.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFolderRequest(
        @NotBlank(message = "폴더 이름은 필수입니다")
        @Size(max = 255, message = "폴더 이름은 255자 이하여야 합니다")
        String folderName,

        Long parentId
) {
    public CreateFolderRequest {
        if (folderName != null) {
            folderName = folderName.trim();
        }
    }
}
