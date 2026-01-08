package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.ContentType;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 미디어 파일의 메타데이터(duration, pageCount 등)를 추출하는 서비스
 */
public interface MediaMetadataService {

    /**
     * 비디오/오디오 파일의 재생 시간(초)을 추출
     */
    Optional<Integer> extractDuration(Path filePath);

    /**
     * PDF 문서의 페이지 수를 추출
     */
    Optional<Integer> extractPageCount(Path filePath);

    /**
     * 콘텐츠 타입에 따라 적절한 메타데이터 추출
     */
    MediaMetadata extractMetadata(Path filePath, ContentType contentType);

    /**
     * 메타데이터 결과 DTO
     */
    record MediaMetadata(
            Integer duration,
            Integer pageCount,
            String resolution
    ) {
        public static MediaMetadata empty() {
            return new MediaMetadata(null, null, null);
        }

        public static MediaMetadata ofDuration(Integer duration, String resolution) {
            return new MediaMetadata(duration, null, resolution);
        }

        public static MediaMetadata ofPageCount(Integer pageCount) {
            return new MediaMetadata(null, pageCount, null);
        }
    }
}
