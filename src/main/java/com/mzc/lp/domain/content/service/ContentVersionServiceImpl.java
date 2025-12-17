package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.VersionChangeType;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import com.mzc.lp.domain.content.dto.response.ContentVersionResponse;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.entity.ContentVersion;
import com.mzc.lp.domain.content.exception.ContentInUseException;
import com.mzc.lp.domain.content.exception.ContentNotFoundException;
import com.mzc.lp.domain.content.exception.ContentVersionNotFoundException;
import com.mzc.lp.domain.content.exception.UnauthorizedContentAccessException;
import com.mzc.lp.domain.content.repository.ContentRepository;
import com.mzc.lp.domain.content.repository.ContentVersionRepository;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentVersionServiceImpl implements ContentVersionService {

    private final ContentRepository contentRepository;
    private final ContentVersionRepository contentVersionRepository;
    private final LearningObjectRepository learningObjectRepository;

    @Override
    @Transactional
    public void createVersion(Content content, VersionChangeType changeType, Long userId, String changeSummary) {
        int nextVersion = contentVersionRepository.findMaxVersionNumber(content.getId())
                .map(v -> v + 1)
                .orElse(1);

        ContentVersion version = ContentVersion.createFrom(content, nextVersion, changeType, userId, changeSummary);
        contentVersionRepository.save(version);

        log.info("Content version created: contentId={}, version={}, changeType={}",
                content.getId(), nextVersion, changeType);
    }

    @Override
    public List<ContentVersionResponse> getVersionHistory(Long contentId, Long tenantId, Long userId) {
        Content content = findContentOrThrow(contentId, tenantId);
        validateOwnership(content, userId);

        return contentVersionRepository.findByContentIdOrderByVersionNumberDesc(contentId)
                .stream()
                .map(ContentVersionResponse::from)
                .toList();
    }

    @Override
    public ContentVersionResponse getVersion(Long contentId, Integer versionNumber, Long tenantId, Long userId) {
        Content content = findContentOrThrow(contentId, tenantId);
        validateOwnership(content, userId);

        ContentVersion version = contentVersionRepository.findByContentIdAndVersionNumber(contentId, versionNumber)
                .orElseThrow(() -> new ContentVersionNotFoundException(contentId, versionNumber));

        return ContentVersionResponse.from(version);
    }

    @Override
    @Transactional
    public ContentResponse restoreVersion(Long contentId, Integer versionNumber, Long tenantId, Long userId, String changeSummary) {
        Content content = findContentOrThrow(contentId, tenantId);
        validateOwnership(content, userId);
        validateContentNotInUse(contentId);

        ContentVersion version = contentVersionRepository.findByContentIdAndVersionNumber(contentId, versionNumber)
                .orElseThrow(() -> new ContentVersionNotFoundException(contentId, versionNumber));

        // 현재 상태 백업
        String summary = "Before restore to v" + versionNumber;
        if (changeSummary != null && !changeSummary.isBlank()) {
            summary += ": " + changeSummary;
        }
        createVersion(content, VersionChangeType.FILE_REPLACE, userId, summary);

        // 버전 복원
        content.replaceFile(
                version.getOriginalFileName(),
                version.getStoredFileName(),
                version.getFileSize(),
                version.getFilePath()
        );
        content.setThumbnailPath(version.getThumbnailPath());
        content.incrementVersion();

        log.info("Content restored to version {}: contentId={}", versionNumber, contentId);

        return ContentResponse.from(content);
    }

    private Content findContentOrThrow(Long contentId, Long tenantId) {
        return contentRepository.findByIdAndTenantId(contentId, tenantId)
                .orElseThrow(() -> new ContentNotFoundException(contentId));
    }

    private void validateOwnership(Content content, Long userId) {
        if (content.getCreatedBy() == null || !content.getCreatedBy().equals(userId)) {
            throw new UnauthorizedContentAccessException(content.getId());
        }
    }

    private void validateContentNotInUse(Long contentId) {
        if (learningObjectRepository.existsByContentId(contentId)) {
            throw new ContentInUseException(contentId);
        }
    }
}
