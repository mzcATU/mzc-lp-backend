package com.mzc.lp.domain.learning.service;

import com.mzc.lp.domain.learning.dto.request.CreateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.request.MoveFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.response.LearningObjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LearningObjectService {

    LearningObjectResponse create(CreateLearningObjectRequest request, Long tenantId);

    Page<LearningObjectResponse> getLearningObjects(Long tenantId, Long folderId, String keyword, Pageable pageable);

    LearningObjectResponse getLearningObject(Long id, Long tenantId);

    LearningObjectResponse getLearningObjectByContentId(Long contentId, Long tenantId);

    LearningObjectResponse update(Long id, UpdateLearningObjectRequest request, Long tenantId);

    LearningObjectResponse moveToFolder(Long id, MoveFolderRequest request, Long tenantId);

    void delete(Long id, Long tenantId);
}
