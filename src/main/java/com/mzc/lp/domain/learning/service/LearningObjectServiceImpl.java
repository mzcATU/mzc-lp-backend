package com.mzc.lp.domain.learning.service;

import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.exception.ContentNotFoundException;
import com.mzc.lp.domain.content.repository.ContentRepository;
import com.mzc.lp.domain.learning.dto.request.CreateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.request.MoveFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.response.LearningObjectResponse;
import com.mzc.lp.domain.learning.entity.ContentFolder;
import com.mzc.lp.domain.learning.entity.LearningObject;
import com.mzc.lp.domain.learning.exception.ContentFolderNotFoundException;
import com.mzc.lp.domain.learning.exception.LearningObjectNotFoundException;
import com.mzc.lp.domain.learning.repository.ContentFolderRepository;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LearningObjectServiceImpl implements LearningObjectService {

    private final LearningObjectRepository learningObjectRepository;
    private final ContentFolderRepository contentFolderRepository;
    private final ContentRepository contentRepository;

    @Override
    @Transactional
    public LearningObjectResponse create(CreateLearningObjectRequest request, Long tenantId) {
        Content content = contentRepository.findByIdAndTenantId(request.contentId(), tenantId)
                .orElseThrow(() -> new ContentNotFoundException(request.contentId()));

        ContentFolder folder = null;
        if (request.folderId() != null) {
            folder = contentFolderRepository.findByIdAndTenantId(request.folderId(), tenantId)
                    .orElseThrow(() -> new ContentFolderNotFoundException(request.folderId()));
        }

        LearningObject lo = LearningObject.create(request.name(), content, folder);
        LearningObject saved = learningObjectRepository.save(lo);

        log.info("LearningObject created: id={}, name={}", saved.getId(), saved.getName());
        return LearningObjectResponse.from(saved);
    }

    @Override
    public Page<LearningObjectResponse> getLearningObjects(Long tenantId, Long folderId,
                                                            String keyword, Pageable pageable) {
        Page<LearningObject> learningObjects;

        if (folderId != null && keyword != null && !keyword.isBlank()) {
            learningObjects = learningObjectRepository.findByTenantIdAndFolderIdAndKeyword(
                    tenantId, folderId, keyword, pageable);
        } else if (folderId != null) {
            learningObjects = learningObjectRepository.findByTenantIdAndFolderId(tenantId, folderId, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            learningObjects = learningObjectRepository.findByTenantIdAndKeyword(tenantId, keyword, pageable);
        } else {
            learningObjects = learningObjectRepository.findByTenantId(tenantId, pageable);
        }

        return learningObjects.map(LearningObjectResponse::from);
    }

    @Override
    public LearningObjectResponse getLearningObject(Long id, Long tenantId) {
        LearningObject lo = findLearningObjectOrThrow(id, tenantId);
        return LearningObjectResponse.from(lo);
    }

    @Override
    public LearningObjectResponse getLearningObjectByContentId(Long contentId, Long tenantId) {
        LearningObject lo = learningObjectRepository.findByContentIdAndTenantId(contentId, tenantId)
                .orElseThrow(() -> new LearningObjectNotFoundException());
        return LearningObjectResponse.from(lo);
    }

    @Override
    @Transactional
    public LearningObjectResponse update(Long id, UpdateLearningObjectRequest request, Long tenantId) {
        LearningObject lo = findLearningObjectOrThrow(id, tenantId);
        lo.updateName(request.name());

        log.info("LearningObject updated: id={}", id);
        return LearningObjectResponse.from(lo);
    }

    @Override
    @Transactional
    public LearningObjectResponse moveToFolder(Long id, MoveFolderRequest request, Long tenantId) {
        LearningObject lo = findLearningObjectOrThrow(id, tenantId);

        if (request.folderId() != null) {
            ContentFolder folder = contentFolderRepository.findByIdAndTenantId(request.folderId(), tenantId)
                    .orElseThrow(() -> new ContentFolderNotFoundException(request.folderId()));
            lo.moveToFolder(folder);
        } else {
            lo.moveToRoot();
        }

        log.info("LearningObject moved: id={}, folderId={}", id, request.folderId());
        return LearningObjectResponse.from(lo);
    }

    @Override
    @Transactional
    public void delete(Long id, Long tenantId) {
        LearningObject lo = findLearningObjectOrThrow(id, tenantId);
        learningObjectRepository.delete(lo);
        log.info("LearningObject deleted: id={}", id);
    }

    private LearningObject findLearningObjectOrThrow(Long id, Long tenantId) {
        return learningObjectRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new LearningObjectNotFoundException(id));
    }
}
