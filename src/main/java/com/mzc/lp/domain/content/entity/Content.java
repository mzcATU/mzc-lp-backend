package com.mzc.lp.domain.content.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.content.constant.ContentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content", indexes = {
        @Index(name = "idx_content_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_content_type", columnList = "content_type"),
        @Index(name = "idx_content_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content extends TenantEntity {

    @Column(name = "original_file_name", length = 500)
    private String originalFileName;

    @Column(name = "stored_file_name", length = 255)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 50)
    private ContentType contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "resolution", length = 20)
    private String resolution;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "external_url", length = 2000)
    private String externalUrl;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    // 정적 팩토리 메서드 - 파일 업로드용
    public static Content createFile(String originalFileName, String storedFileName,
                                     ContentType contentType, Long fileSize, String filePath) {
        Content content = new Content();
        content.originalFileName = originalFileName;
        content.storedFileName = storedFileName;
        content.contentType = contentType;
        content.fileSize = fileSize;
        content.filePath = filePath;
        return content;
    }

    // 정적 팩토리 메서드 - 외부 링크용
    public static Content createExternalLink(String name, String externalUrl) {
        Content content = new Content();
        content.originalFileName = name;
        content.contentType = ContentType.EXTERNAL_LINK;
        content.externalUrl = externalUrl;
        return content;
    }

    // 비즈니스 메서드 - 비디오 메타데이터 설정
    public void setVideoMetadata(Integer duration, String resolution) {
        this.duration = duration;
        this.resolution = resolution;
    }

    // 비즈니스 메서드 - 오디오 메타데이터 설정
    public void setAudioMetadata(Integer duration) {
        this.duration = duration;
    }

    // 비즈니스 메서드 - 문서 메타데이터 설정
    public void setDocumentMetadata(Integer pageCount) {
        this.pageCount = pageCount;
    }

    // 비즈니스 메서드 - 이미지 메타데이터 설정
    public void setImageMetadata(String resolution) {
        this.resolution = resolution;
    }

    // 비즈니스 메서드 - 외부 링크 메타데이터 설정 (YouTube 등)
    public void setExternalLinkMetadata(Integer duration) {
        this.duration = duration;
    }

    // 비즈니스 메서드 - 메타데이터 수정
    public void updateMetadata(String originalFileName, Integer duration, String resolution) {
        if (originalFileName != null && !originalFileName.isBlank()) {
            this.originalFileName = originalFileName;
        }
        if (duration != null) {
            this.duration = duration;
        }
        if (resolution != null && !resolution.isBlank()) {
            this.resolution = resolution;
        }
    }

    // 비즈니스 메서드 - 파일 교체
    public void replaceFile(String originalFileName, String storedFileName,
                           Long fileSize, String filePath) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }
}
