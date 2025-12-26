package com.mzc.lp.domain.content.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content", indexes = {
        @Index(name = "idx_content_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_content_type", columnList = "content_type"),
        @Index(name = "idx_content_created_at", columnList = "created_at"),
        @Index(name = "idx_content_status", columnList = "tenant_id, status"),
        @Index(name = "idx_content_created_by", columnList = "tenant_id, created_by")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content extends TenantEntity {

    // 낙관적 락 (동시 수정 감지)
    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContentStatus status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "current_version")
    private Integer currentVersion;

    @Column(name = "original_file_name", length = 500)
    private String originalFileName;

    @Column(name = "uploaded_file_name", length = 500)
    private String uploadedFileName;

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

    @Column(name = "thumbnail_path", length = 1000)
    private String thumbnailPath;

    @Column(name = "custom_thumbnail_path", length = 1000)
    private String customThumbnailPath;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "tags", length = 500)
    private String tags;

    // 정적 팩토리 메서드 - 파일 업로드용 (하위 호환성 유지)
    public static Content createFile(String originalFileName, String storedFileName,
                                     ContentType contentType, Long fileSize, String filePath) {
        return createFile(originalFileName, null, storedFileName, contentType, fileSize, filePath, null);
    }

    // 정적 팩토리 메서드 - 파일 업로드용 (createdBy 포함, 하위 호환성)
    public static Content createFile(String originalFileName, String storedFileName,
                                     ContentType contentType, Long fileSize, String filePath,
                                     Long createdBy) {
        return createFile(originalFileName, null, storedFileName, contentType, fileSize, filePath, createdBy);
    }

    // 정적 팩토리 메서드 - 파일 업로드용 (displayName 포함)
    public static Content createFile(String uploadedFileName, String displayName, String storedFileName,
                                     ContentType contentType, Long fileSize, String filePath,
                                     Long createdBy) {
        Content content = new Content();
        content.status = ContentStatus.ACTIVE;
        content.createdBy = createdBy;
        content.currentVersion = 1;
        // displayName이 있으면 콘텐츠 이름으로 사용, 없으면 업로드 파일명 사용
        content.originalFileName = (displayName != null && !displayName.isBlank()) ? displayName : uploadedFileName;
        content.uploadedFileName = uploadedFileName;  // 실제 업로드한 파일명
        content.storedFileName = storedFileName;
        content.contentType = contentType;
        content.fileSize = fileSize;
        content.filePath = filePath;
        return content;
    }

    // 정적 팩토리 메서드 - 외부 링크용 (하위 호환성 유지)
    public static Content createExternalLink(String name, String externalUrl) {
        return createExternalLink(name, externalUrl, null);
    }

    // 정적 팩토리 메서드 - 외부 링크용 (createdBy 포함)
    public static Content createExternalLink(String name, String externalUrl, Long createdBy) {
        Content content = new Content();
        content.status = ContentStatus.ACTIVE;
        content.createdBy = createdBy;
        content.currentVersion = 1;
        content.originalFileName = name;
        content.contentType = ContentType.EXTERNAL_LINK;
        content.externalUrl = externalUrl;
        return content;
    }

    // 비즈니스 메서드 - 비디오 메타데이터 설정
    public void updateVideoMetadata(Integer duration, String resolution) {
        this.duration = duration;
        this.resolution = resolution;
    }

    // 비즈니스 메서드 - 오디오 메타데이터 설정
    public void updateAudioMetadata(Integer duration) {
        this.duration = duration;
    }

    // 비즈니스 메서드 - 문서 메타데이터 설정
    public void updateDocumentMetadata(Integer pageCount) {
        this.pageCount = pageCount;
    }

    // 비즈니스 메서드 - 이미지 메타데이터 설정
    public void updateImageMetadata(String resolution) {
        this.resolution = resolution;
    }

    // 비즈니스 메서드 - 외부 링크 메타데이터 설정 (YouTube 등)
    public void updateExternalLinkMetadata(Integer duration) {
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

    // 비즈니스 메서드 - 설명, 태그 설정
    public void updateDescriptionAndTags(String description, String tags) {
        this.description = description;
        this.tags = tags;
    }

    // 비즈니스 메서드 - 커스텀 썸네일 설정
    public void updateCustomThumbnailPath(String customThumbnailPath) {
        this.customThumbnailPath = customThumbnailPath;
    }

    // 비즈니스 메서드 - 파일 교체 (콘텐츠 이름은 유지, 파일 정보만 교체)
    public void replaceFile(String uploadedFileName, String storedFileName,
                           Long fileSize, String filePath) {
        this.uploadedFileName = uploadedFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }

    // 비즈니스 메서드 - 버전 복원 (모든 정보 복원)
    public void restoreFromVersion(String originalFileName, String uploadedFileName,
                                   String storedFileName, Long fileSize, String filePath,
                                   Integer duration, String resolution) {
        this.originalFileName = originalFileName;
        this.uploadedFileName = uploadedFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.filePath = filePath;
        this.duration = duration;
        this.resolution = resolution;
    }

    // 비즈니스 메서드 - 썸네일 설정
    public void updateThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    // 상태 전이 메서드
    public void archive() {
        this.status = ContentStatus.ARCHIVED;
    }

    public void restore() {
        this.status = ContentStatus.ACTIVE;
    }

    // 상태 확인 메서드
    public boolean isActive() {
        return this.status == ContentStatus.ACTIVE;
    }

    public boolean isArchived() {
        return this.status == ContentStatus.ARCHIVED;
    }

    // 버전 증가 메서드
    public void incrementVersion() {
        if (this.currentVersion == null) {
            this.currentVersion = 1;
        }
        this.currentVersion++;
    }
}
