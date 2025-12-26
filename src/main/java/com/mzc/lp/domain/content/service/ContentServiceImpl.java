package com.mzc.lp.domain.content.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.domain.content.constant.ContentStatus;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.constant.VersionChangeType;
import com.mzc.lp.domain.content.dto.request.CreateExternalLinkRequest;
import com.mzc.lp.domain.content.dto.request.UpdateContentRequest;
import com.mzc.lp.domain.content.dto.response.ContentListResponse;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.event.ContentCreatedEvent;
import com.mzc.lp.domain.content.exception.ContentInUseException;
import com.mzc.lp.domain.content.exception.ContentNotFoundException;
import com.mzc.lp.domain.content.exception.FileStorageException;
import com.mzc.lp.domain.content.exception.UnauthorizedContentAccessException;
import com.mzc.lp.domain.content.exception.UnsupportedContentTypeException;
import com.mzc.lp.domain.content.repository.ContentRepository;
import com.mzc.lp.domain.content.repository.ContentVersionRepository;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final FileStorageService fileStorageService;
    private final ThumbnailService thumbnailService;
    private final ContentVersionService contentVersionService;
    private final LearningObjectRepository learningObjectRepository;
    private final CourseItemRepository courseItemRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    private static final Set<String> SUPPORTED_YOUTUBE_PATTERNS = Set.of(
            "youtube.com/watch", "youtu.be/", "youtube.com/embed"
    );
    private static final Set<String> SUPPORTED_VIMEO_PATTERNS = Set.of(
            "vimeo.com/"
    );
    private static final Set<String> SUPPORTED_GOOGLE_FORM_PATTERNS = Set.of(
            "docs.google.com/forms"
    );

    @Override
    @Transactional
    public ContentResponse uploadFile(MultipartFile file, Long folderId, String displayName,
                                       String description, String tags, MultipartFile thumbnail,
                                       Long tenantId, Long userId) {
        String uploadedFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = fileStorageService.getFileExtension(uploadedFileName);

        ContentType contentType = ContentType.fromExtension(extension);
        if (contentType == null) {
            throw new UnsupportedContentTypeException(extension);
        }

        String storedFileName = fileStorageService.generateStoredFileName(uploadedFileName);
        String filePath = fileStorageService.storeFile(file);

        // uploadedFileName: 실제 업로드 파일명, displayName: 사용자 지정 콘텐츠 이름
        Content content = Content.createFile(
                uploadedFileName,
                displayName,
                storedFileName,
                contentType,
                file.getSize(),
                filePath,
                userId
        );

        // 설명, 태그 설정
        content.updateDescriptionAndTags(description, tags);

        // 커스텀 썸네일이 있으면 저장
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String customThumbnailPath = thumbnailService.storeCustomThumbnail(thumbnail);
            content.updateCustomThumbnailPath(customThumbnailPath);
        } else {
            // 썸네일 자동 생성
            generateAndSetThumbnail(content, filePath, contentType);
        }

        Content savedContent = contentRepository.save(content);
        log.info("Content created: id={}, type={}, file={}, displayName={}", savedContent.getId(), contentType, uploadedFileName, displayName);

        // 초기 버전 기록
        contentVersionService.createVersion(savedContent, VersionChangeType.FILE_UPLOAD, userId, "Initial upload");

        eventPublisher.publishEvent(new ContentCreatedEvent(this, savedContent, folderId));

        return ContentResponse.from(savedContent);
    }

    @Override
    @Transactional
    public ContentResponse createExternalLink(CreateExternalLinkRequest request, Long tenantId, Long userId) {
        String url = request.url();
        validateExternalUrl(url);

        Content content = Content.createExternalLink(request.name(), url, userId);
        Content savedContent = contentRepository.save(content);

        log.info("External link created: id={}, name={}, url={}",
                savedContent.getId(), request.name(), url);

        // 초기 버전 기록
        contentVersionService.createVersion(savedContent, VersionChangeType.FILE_UPLOAD, userId, "External link created");

        eventPublisher.publishEvent(new ContentCreatedEvent(this, savedContent, request.folderId()));

        return ContentResponse.from(savedContent);
    }

    @Override
    public Page<ContentListResponse> getContents(Long tenantId, ContentType contentType,
                                                  String keyword, Pageable pageable) {
        Page<Content> contents;

        // 기본적으로 ACTIVE 상태만 조회 (ARCHIVED 콘텐츠 제외)
        ContentStatus activeStatus = ContentStatus.ACTIVE;

        if (contentType != null && keyword != null && !keyword.isBlank()) {
            contents = contentRepository.findByTenantIdAndStatusAndContentTypeAndKeyword(
                    tenantId, activeStatus, contentType, keyword, pageable);
        } else if (contentType != null) {
            contents = contentRepository.findByTenantIdAndStatusAndContentType(
                    tenantId, activeStatus, contentType, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            contents = contentRepository.findByTenantIdAndStatusAndKeyword(
                    tenantId, activeStatus, keyword, pageable);
        } else {
            contents = contentRepository.findByTenantIdAndStatus(tenantId, activeStatus, pageable);
        }

        return contents.map(ContentListResponse::from);
    }

    @Override
    public ContentResponse getContent(Long contentId, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);
        boolean inCourse = isContentInCourse(contentId);
        return ContentResponse.from(content, inCourse);
    }

    @Override
    @Transactional
    public ContentResponse updateContent(Long contentId, UpdateContentRequest request, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        // 강의에서 사용 중인 콘텐츠는 수정 불가
        validateContentNotInUse(contentId);

        // 메타데이터 변경
        content.updateMetadata(request.originalFileName(), request.duration(), request.resolution());
        content.incrementVersion();

        // 버전 기록 (변경 후 상태 저장)
        contentVersionService.createVersion(content, VersionChangeType.METADATA_UPDATE,
                content.getCreatedBy(), "Metadata updated");

        log.info("Content updated: id={}", contentId);
        return ContentResponse.from(content);
    }

    @Override
    @Transactional
    public ContentResponse replaceFile(Long contentId, MultipartFile file, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        // 강의에서 사용 중인 콘텐츠는 파일 교체 불가
        validateContentNotInUse(contentId);

        if (content.getContentType() == ContentType.EXTERNAL_LINK) {
            throw new FileStorageException(ErrorCode.UNSUPPORTED_FILE_TYPE,
                    "Cannot replace file for external link content");
        }

        String oldFilePath = content.getFilePath();
        String oldThumbnailPath = content.getThumbnailPath();
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = fileStorageService.generateStoredFileName(originalFileName);
        String newFilePath = fileStorageService.storeFile(file);

        // 파일 교체
        content.replaceFile(originalFileName, storedFileName, file.getSize(), newFilePath);
        content.incrementVersion();

        // 썸네일 재생성
        generateAndSetThumbnail(content, newFilePath, content.getContentType());

        // 버전 기록 (변경 후 상태 저장)
        contentVersionService.createVersion(content, VersionChangeType.FILE_REPLACE,
                content.getCreatedBy(), "File replaced");

        // 이전 파일 삭제
        if (oldFilePath != null) {
            fileStorageService.deleteFile(oldFilePath);
        }
        if (oldThumbnailPath != null) {
            thumbnailService.deleteThumbnail(oldThumbnailPath);
        }

        log.info("Content file replaced: id={}, newFile={}", contentId, originalFileName);
        return ContentResponse.from(content);
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        // 강의에서 사용 중인 콘텐츠는 삭제 불가
        validateContentNotInUse(contentId);

        // LearningObject 먼저 삭제 (FK 제약조건)
        learningObjectRepository.deleteByContentId(contentId);

        // 버전 히스토리 삭제 (FK 제약조건)
        contentVersionRepository.deleteByContentId(contentId);

        if (content.getFilePath() != null) {
            fileStorageService.deleteFile(content.getFilePath());
        }
        if (content.getThumbnailPath() != null) {
            thumbnailService.deleteThumbnail(content.getThumbnailPath());
        }

        contentRepository.delete(content);
        log.info("Content deleted: id={}", contentId);
    }

    @Override
    public Resource getFileAsResource(Long contentId, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        if (content.getFilePath() == null) {
            throw new FileStorageException(ErrorCode.FILE_NOT_FOUND,
                    "No file associated with content: " + contentId);
        }

        return fileStorageService.loadFileAsResource(content.getFilePath());
    }

    @Override
    public ContentDownloadInfo getFileForDownload(Long contentId, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        if (content.getFilePath() == null) {
            throw new FileStorageException(ErrorCode.FILE_NOT_FOUND,
                    "No file associated with content: " + contentId);
        }

        Resource resource = fileStorageService.loadFileAsResource(content.getFilePath());

        // originalFileName에 확장자가 없으면 storedFileName에서 확장자 추출
        String originalFileName = content.getOriginalFileName();
        String extension = fileStorageService.getFileExtension(originalFileName);
        if (extension.isEmpty()) {
            extension = fileStorageService.getFileExtension(content.getStoredFileName());
        }

        String mimeType = determineMimeType(content.getContentType(), extension);

        // 다운로드 파일명에 확장자가 없으면 추가
        String downloadFileName = originalFileName;
        if (!originalFileName.contains(".") && !extension.isEmpty()) {
            downloadFileName = originalFileName + "." + extension;
        }

        return new ContentDownloadInfo(resource, downloadFileName, mimeType);
    }

    private Content findContentOrThrow(Long contentId, Long tenantId) {
        return contentRepository.findByIdAndTenantId(contentId, tenantId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
    }

    private void validateExternalUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new FileStorageException(ErrorCode.INVALID_EXTERNAL_URL, "URL is required");
        }

        boolean isSupported = SUPPORTED_YOUTUBE_PATTERNS.stream().anyMatch(url::contains)
                || SUPPORTED_VIMEO_PATTERNS.stream().anyMatch(url::contains)
                || SUPPORTED_GOOGLE_FORM_PATTERNS.stream().anyMatch(url::contains);

        if (!isSupported) {
            throw new FileStorageException(ErrorCode.INVALID_EXTERNAL_URL,
                    "Only YouTube, Vimeo, and Google Form URLs are supported");
        }
    }

    private String determineMimeType(ContentType contentType, String extension) {
        return switch (contentType) {
            case VIDEO -> switch (extension) {
                case "mp4" -> "video/mp4";
                case "webm" -> "video/webm";
                case "avi" -> "video/x-msvideo";
                case "mov" -> "video/quicktime";
                case "mkv" -> "video/x-matroska";
                default -> "video/mp4";
            };
            case AUDIO -> switch (extension) {
                case "mp3" -> "audio/mpeg";
                case "wav" -> "audio/wav";
                case "m4a" -> "audio/mp4";
                case "flac" -> "audio/flac";
                default -> "audio/mpeg";
            };
            case IMAGE -> switch (extension) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png" -> "image/png";
                case "gif" -> "image/gif";
                case "svg" -> "image/svg+xml";
                case "webp" -> "image/webp";
                default -> "image/jpeg";
            };
            case DOCUMENT -> switch (extension) {
                case "pdf" -> "application/pdf";
                case "doc" -> "application/msword";
                case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
                case "ppt" -> "application/vnd.ms-powerpoint";
                case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
                case "xls" -> "application/vnd.ms-excel";
                case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                case "txt" -> "text/plain";
                case "csv" -> "text/csv";
                case "html", "htm" -> "text/html";
                case "xml" -> "application/xml";
                case "json" -> "application/json";
                case "zip" -> "application/zip";
                case "rar" -> "application/vnd.rar";
                case "7z" -> "application/x-7z-compressed";
                default -> "application/octet-stream";
            };
            default -> "application/octet-stream";
        };
    }

    private void generateAndSetThumbnail(Content content, String filePath, ContentType contentType) {
        try {
            // /uploads/... 형태의 경로에서 실제 파일 경로로 변환
            String relativePath = filePath.startsWith("/uploads/")
                    ? filePath.substring("/uploads/".length())
                    : filePath;

            Path absolutePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relativePath);

            thumbnailService.generateThumbnail(absolutePath, contentType)
                    .ifPresent(content::updateThumbnailPath);
        } catch (Exception e) {
            log.warn("Failed to generate thumbnail for content: {}", filePath, e);
        }
    }

    // ========== DESIGNER용 API (본인 콘텐츠 관리) ==========

    @Override
    public Page<ContentListResponse> getMyContents(Long tenantId, Long userId,
                                                    ContentStatus status, String keyword,
                                                    Pageable pageable) {
        Page<Content> contents;

        // status가 null이면 전체 조회 (all 탭), 지정되면 해당 상태만 조회
        if (status != null && keyword != null && !keyword.isBlank()) {
            contents = contentRepository.findByTenantIdAndCreatedByAndStatusAndKeyword(
                    tenantId, userId, status, keyword, pageable);
        } else if (status != null) {
            contents = contentRepository.findByTenantIdAndCreatedByAndStatus(
                    tenantId, userId, status, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            contents = contentRepository.findByTenantIdAndCreatedByAndKeyword(
                    tenantId, userId, keyword, pageable);
        } else {
            contents = contentRepository.findByTenantIdAndCreatedBy(tenantId, userId, pageable);
        }

        return contents.map(ContentListResponse::from);
    }

    @Override
    @Transactional
    public ContentResponse archiveContent(Long contentId, Long tenantId, Long userId) {
        Content content = findContentOrThrow(contentId, tenantId);
        validateContentOwnership(content, userId);

        content.archive();
        log.info("Content archived: id={}, userId={}", contentId, userId);

        return ContentResponse.from(content);
    }

    @Override
    @Transactional
    public ContentResponse restoreContent(Long contentId, Long tenantId, Long userId) {
        Content content = findContentOrThrow(contentId, tenantId);
        validateContentOwnership(content, userId);

        content.restore();
        log.info("Content restored: id={}, userId={}", contentId, userId);

        return ContentResponse.from(content);
    }

    private void validateContentOwnership(Content content, Long userId) {
        if (content.getCreatedBy() == null || !content.getCreatedBy().equals(userId)) {
            throw new UnauthorizedContentAccessException(content.getId());
        }
    }

    private void validateContentNotInUse(Long contentId) {
        if (isContentInCourse(contentId)) {
            throw new ContentInUseException(contentId);
        }
    }

    /**
     * 콘텐츠가 강의(Course)에 포함되어 있는지 확인
     * Content → LearningObject → CourseItem 연결 확인
     */
    public boolean isContentInCourse(Long contentId) {
        // LearningObject가 없으면 Course에도 포함될 수 없음
        if (!learningObjectRepository.existsByContentId(contentId)) {
            return false;
        }
        // LearningObject가 CourseItem에 포함되어 있는지 확인
        return courseItemRepository.existsByContentIdThroughLearningObject(contentId);
    }
}
