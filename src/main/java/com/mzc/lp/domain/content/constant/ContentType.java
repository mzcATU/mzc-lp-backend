package com.mzc.lp.domain.content.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum ContentType {

    VIDEO("동영상", Set.of("mp4", "avi", "mov", "mkv", "webm")),
    DOCUMENT("문서", Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx")),
    IMAGE("이미지", Set.of("jpg", "jpeg", "png", "gif", "svg", "webp")),
    AUDIO("오디오", Set.of("mp3", "wav", "m4a", "flac")),
    EXTERNAL_LINK("외부 링크", Set.of());

    private final String description;
    private final Set<String> extensions;

    public static ContentType fromExtension(String extension) {
        if (extension == null || extension.isBlank()) {
            return null;
        }
        String ext = extension.toLowerCase();
        return Arrays.stream(values())
                .filter(type -> type.getExtensions().contains(ext))
                .findFirst()
                .orElse(null);
    }

    public boolean hasMetadata() {
        return this == VIDEO || this == AUDIO || this == DOCUMENT || this == IMAGE;
    }
}
