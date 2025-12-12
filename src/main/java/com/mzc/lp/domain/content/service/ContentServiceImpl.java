package com.mzc.lp.domain.content.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.dto.request.CreateExternalLinkRequest;
import com.mzc.lp.domain.content.dto.request.UpdateContentRequest;
import com.mzc.lp.domain.content.dto.response.ContentListResponse;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.event.ContentCreatedEvent;
import com.mzc.lp.domain.content.exception.ContentNotFoundException;
import com.mzc.lp.domain.content.exception.FileStorageException;
import com.mzc.lp.domain.content.exception.UnsupportedContentTypeException;
import com.mzc.lp.domain.content.repository.ContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentServiceImpl implements ContentService {

    private final ContentRepository contentRepository;
    private final FileStorageService fileStorageService;
    private final ApplicationEventPublisher eventPublisher;

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
    public ContentResponse uploadFile(MultipartFile file, Long folderId, Long tenantId) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = fileStorageService.getFileExtension(originalFileName);

        ContentType contentType = ContentType.fromExtension(extension);
        if (contentType == null) {
            throw new UnsupportedContentTypeException(extension);
        }

        String storedFileName = fileStorageService.generateStoredFileName(originalFileName);
        String filePath = fileStorageService.storeFile(file);

        Content content = Content.createFile(
                originalFileName,
                storedFileName,
                contentType,
                file.getSize(),
                filePath
        );

        Content savedContent = contentRepository.save(content);
        log.info("Content created: id={}, type={}, file={}", savedContent.getId(), contentType, originalFileName);

        eventPublisher.publishEvent(new ContentCreatedEvent(this, savedContent, folderId));

        return ContentResponse.from(savedContent);
    }

    @Override
    @Transactional
    public ContentResponse createExternalLink(CreateExternalLinkRequest request, Long tenantId) {
        String url = request.url();
        validateExternalUrl(url);

        Content content = Content.createExternalLink(request.name(), url);
        Content savedContent = contentRepository.save(content);

        log.info("External link created: id={}, name={}, url={}",
                savedContent.getId(), request.name(), url);

        eventPublisher.publishEvent(new ContentCreatedEvent(this, savedContent, request.folderId()));

        return ContentResponse.from(savedContent);
    }

    @Override
    public Page<ContentListResponse> getContents(Long tenantId, ContentType contentType,
                                                  String keyword, Pageable pageable) {
        Page<Content> contents;

        if (contentType != null && keyword != null && !keyword.isBlank()) {
            contents = contentRepository.findByTenantIdAndContentTypeAndKeyword(
                    tenantId, contentType, keyword, pageable);
        } else if (contentType != null) {
            contents = contentRepository.findByTenantIdAndContentType(tenantId, contentType, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            contents = contentRepository.findByTenantIdAndKeyword(tenantId, keyword, pageable);
        } else {
            contents = contentRepository.findByTenantId(tenantId, pageable);
        }

        return contents.map(ContentListResponse::from);
    }

    @Override
    public ContentResponse getContent(Long contentId, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);
        return ContentResponse.from(content);
    }

    @Override
    @Transactional
    public ContentResponse updateContent(Long contentId, UpdateContentRequest request, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);
        content.updateMetadata(request.originalFileName(), request.duration(), request.resolution());

        log.info("Content updated: id={}", contentId);
        return ContentResponse.from(content);
    }

    @Override
    @Transactional
    public ContentResponse replaceFile(Long contentId, MultipartFile file, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        if (content.getContentType() == ContentType.EXTERNAL_LINK) {
            throw new FileStorageException(ErrorCode.UNSUPPORTED_FILE_TYPE,
                    "Cannot replace file for external link content");
        }

        String oldFilePath = content.getFilePath();
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String storedFileName = fileStorageService.generateStoredFileName(originalFileName);
        String newFilePath = fileStorageService.storeFile(file);

        content.replaceFile(originalFileName, storedFileName, file.getSize(), newFilePath);

        if (oldFilePath != null) {
            fileStorageService.deleteFile(oldFilePath);
        }

        log.info("Content file replaced: id={}, newFile={}", contentId, originalFileName);
        return ContentResponse.from(content);
    }

    @Override
    @Transactional
    public void deleteContent(Long contentId, Long tenantId) {
        Content content = findContentOrThrow(contentId, tenantId);

        if (content.getFilePath() != null) {
            fileStorageService.deleteFile(content.getFilePath());
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
        String mimeType = determineMimeType(content.getContentType(),
                fileStorageService.getFileExtension(content.getOriginalFileName()));

        return new ContentDownloadInfo(resource, content.getOriginalFileName(), mimeType);
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
                default -> "application/octet-stream";
            };
            default -> "application/octet-stream";
        };
    }
}
