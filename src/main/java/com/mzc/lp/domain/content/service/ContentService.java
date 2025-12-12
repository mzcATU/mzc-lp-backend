package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.dto.request.CreateExternalLinkRequest;
import com.mzc.lp.domain.content.dto.request.UpdateContentRequest;
import com.mzc.lp.domain.content.dto.response.ContentListResponse;
import com.mzc.lp.domain.content.dto.response.ContentResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ContentService {

    /**
     * 파일 업로드 및 Content 생성
     */
    ContentResponse uploadFile(MultipartFile file, Long folderId, Long tenantId);

    /**
     * 외부 링크 등록
     */
    ContentResponse createExternalLink(CreateExternalLinkRequest request, Long tenantId);

    /**
     * 콘텐츠 목록 조회
     */
    Page<ContentListResponse> getContents(Long tenantId, ContentType contentType, String keyword, Pageable pageable);

    /**
     * 콘텐츠 상세 조회
     */
    ContentResponse getContent(Long contentId, Long tenantId);

    /**
     * 콘텐츠 메타데이터 수정
     */
    ContentResponse updateContent(Long contentId, UpdateContentRequest request, Long tenantId);

    /**
     * 파일 교체
     */
    ContentResponse replaceFile(Long contentId, MultipartFile file, Long tenantId);

    /**
     * 콘텐츠 삭제
     */
    void deleteContent(Long contentId, Long tenantId);

    /**
     * 파일 스트리밍용 리소스 조회
     */
    Resource getFileAsResource(Long contentId, Long tenantId);

    /**
     * 파일 다운로드용 리소스 및 원본 파일명 조회
     */
    ContentDownloadInfo getFileForDownload(Long contentId, Long tenantId);

    record ContentDownloadInfo(Resource resource, String originalFileName, String contentType) {}
}
