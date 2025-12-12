package com.mzc.lp.domain.content.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 파일 저장
     * @param file 업로드할 파일
     * @return 저장된 파일 경로
     */
    String storeFile(MultipartFile file);

    /**
     * 파일 로드 (Resource 형태)
     * @param filePath 파일 경로
     * @return 파일 리소스
     */
    Resource loadFileAsResource(String filePath);

    /**
     * 파일 삭제
     * @param filePath 파일 경로
     */
    void deleteFile(String filePath);

    /**
     * UUID 기반 저장 파일명 생성
     * @param originalFileName 원본 파일명
     * @return UUID 기반 저장 파일명
     */
    String generateStoredFileName(String originalFileName);

    /**
     * 파일 확장자 추출
     * @param fileName 파일명
     * @return 확장자 (소문자)
     */
    String getFileExtension(String fileName);
}
