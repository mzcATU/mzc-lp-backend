package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.ContentType;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Optional;

public interface ThumbnailService {

    /**
     * 콘텐츠 타입에 따라 썸네일 생성
     * @param sourcePath 원본 파일 경로
     * @param contentType 콘텐츠 타입
     * @return 생성된 썸네일 경로 (생성 실패 시 empty)
     */
    Optional<String> generateThumbnail(Path sourcePath, ContentType contentType);

    /**
     * 동영상 썸네일 생성 (ffmpeg 사용)
     * @param videoPath 동영상 파일 경로
     * @return 썸네일 경로
     */
    Optional<String> generateVideoThumbnail(Path videoPath);

    /**
     * 이미지 썸네일 생성 (리사이즈)
     * @param imagePath 이미지 파일 경로
     * @return 썸네일 경로
     */
    Optional<String> generateImageThumbnail(Path imagePath);

    /**
     * PDF 문서 첫 페이지 썸네일 생성
     * @param pdfPath PDF 파일 경로
     * @return 썸네일 경로
     */
    Optional<String> generatePdfThumbnail(Path pdfPath);

    /**
     * 썸네일 삭제
     * @param thumbnailPath 썸네일 경로
     */
    void deleteThumbnail(String thumbnailPath);

    /**
     * 커스텀 썸네일 저장
     * @param thumbnail 썸네일 이미지 파일
     * @return 저장된 썸네일 경로
     */
    String storeCustomThumbnail(MultipartFile thumbnail);
}
