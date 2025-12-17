package com.mzc.lp.domain.content.entity;

import com.mzc.lp.common.entity.TenantEntity;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.constant.VersionChangeType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "content_version", indexes = {
        @Index(name = "idx_cv_content", columnList = "tenant_id, content_id"),
        @Index(name = "idx_cv_version", columnList = "content_id, version_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentVersion extends TenantEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    private Content content;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 20)
    private VersionChangeType changeType;

    // 스냅샷 데이터 (변경 전 상태 저장)
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

    @Column(name = "thumbnail_path", length = 1000)
    private String thumbnailPath;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "change_summary", length = 500)
    private String changeSummary;

    // 정적 팩토리 메서드
    public static ContentVersion createFrom(Content content, int versionNumber,
                                            VersionChangeType changeType,
                                            Long createdBy, String changeSummary) {
        ContentVersion version = new ContentVersion();
        version.content = content;
        version.versionNumber = versionNumber;
        version.changeType = changeType;
        // Content 현재 상태 스냅샷
        version.originalFileName = content.getOriginalFileName();
        version.storedFileName = content.getStoredFileName();
        version.contentType = content.getContentType();
        version.fileSize = content.getFileSize();
        version.duration = content.getDuration();
        version.resolution = content.getResolution();
        version.pageCount = content.getPageCount();
        version.externalUrl = content.getExternalUrl();
        version.filePath = content.getFilePath();
        version.thumbnailPath = content.getThumbnailPath();
        version.createdBy = createdBy;
        version.changeSummary = changeSummary;
        return version;
    }
}
