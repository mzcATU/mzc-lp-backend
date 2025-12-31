package com.mzc.lp.domain.content.dto.request;

import org.springframework.web.multipart.MultipartFile;

public record UploadFileRequest(
        MultipartFile file,
        Long folderId,
        String originalFileName,
        String description,
        String tags,
        MultipartFile thumbnail
) {
    public static UploadFileRequest of(MultipartFile file, Long folderId, String originalFileName,
                                       String description, String tags, MultipartFile thumbnail) {
        return new UploadFileRequest(file, folderId, originalFileName, description, tags, thumbnail);
    }
}
