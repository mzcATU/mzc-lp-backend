package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.VersionChangeType;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import com.mzc.lp.domain.content.dto.response.ContentVersionResponse;
import com.mzc.lp.domain.content.entity.Content;

import java.util.List;

public interface ContentVersionService {

    /**
     * 버전 생성 (수정 전 호출)
     */
    void createVersion(Content content, VersionChangeType changeType, Long userId, String changeSummary);

    /**
     * 버전 이력 조회
     */
    List<ContentVersionResponse> getVersionHistory(Long contentId, Long tenantId, Long userId);

    /**
     * 특정 버전 조회
     */
    ContentVersionResponse getVersion(Long contentId, Integer versionNumber, Long tenantId, Long userId);

    /**
     * 버전 복원
     */
    ContentResponse restoreVersion(Long contentId, Integer versionNumber, Long tenantId, Long userId, String changeSummary);
}
