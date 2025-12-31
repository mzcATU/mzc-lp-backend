package com.mzc.lp.domain.content.service;

import com.mzc.lp.domain.content.constant.ContentStatus;
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
     * @param originalFileName 사용자가 지정한 원본 파일명 (null인 경우 업로드 파일명 사용)
     * @param description 콘텐츠 설명
     * @param tags 태그 (쉼표로 구분)
     * @param thumbnail 커스텀 썸네일 이미지
     * @param downloadable 다운로드 허용 여부 (null인 경우 기본값 true)
     */
    ContentResponse uploadFile(MultipartFile file, Long folderId, String originalFileName,
                               String description, String tags, MultipartFile thumbnail,
                               Boolean downloadable, Long tenantId, Long userId);

    /**
     * 외부 링크 등록
     */
    ContentResponse createExternalLink(CreateExternalLinkRequest request, Long tenantId, Long userId);

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

    // ========== DESIGNER용 API (본인 콘텐츠 관리) ==========

    /**
     * 내 콘텐츠 목록 조회 (DESIGNER용)
     */
    Page<ContentListResponse> getMyContents(Long tenantId, Long userId, ContentType contentType, ContentStatus status, String keyword, Long folderId, Pageable pageable);

    /**
     * 콘텐츠 보관 (Archive)
     */
    ContentResponse archiveContent(Long contentId, Long tenantId, Long userId);

    /**
     * 콘텐츠 복원 (Restore from Archive)
     */
    ContentResponse restoreContent(Long contentId, Long tenantId, Long userId);
}
