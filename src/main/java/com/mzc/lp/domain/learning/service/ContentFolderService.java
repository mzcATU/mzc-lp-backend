package com.mzc.lp.domain.learning.service;

import com.mzc.lp.domain.learning.dto.request.CreateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.MoveContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.response.ContentFolderResponse;

import java.util.List;

public interface ContentFolderService {

    ContentFolderResponse create(CreateContentFolderRequest request, Long tenantId);

    List<ContentFolderResponse> getFolderTree(Long tenantId);

    ContentFolderResponse getFolder(Long id, Long tenantId);

    List<ContentFolderResponse> getChildFolders(Long parentId, Long tenantId);

    ContentFolderResponse update(Long id, UpdateContentFolderRequest request, Long tenantId);

    ContentFolderResponse move(Long id, MoveContentFolderRequest request, Long tenantId);

    void delete(Long id, Long tenantId);
}
