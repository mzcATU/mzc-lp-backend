package com.mzc.lp.domain.learning.service;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.domain.learning.dto.request.CreateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.MoveContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.response.ContentFolderResponse;
import com.mzc.lp.domain.learning.entity.ContentFolder;
import com.mzc.lp.domain.learning.exception.ContentFolderNotFoundException;
import com.mzc.lp.domain.learning.exception.MaxDepthExceededException;
import com.mzc.lp.domain.learning.repository.ContentFolderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentFolderServiceImpl implements ContentFolderService {

    private final ContentFolderRepository contentFolderRepository;

    @Override
    @Transactional
    public ContentFolderResponse create(CreateContentFolderRequest request, Long tenantId) {
        validateDuplicateName(tenantId, request.parentId(), request.folderName());

        ContentFolder folder;
        if (request.parentId() != null) {
            ContentFolder parent = contentFolderRepository.findByIdAndTenantId(request.parentId(), tenantId)
                    .orElseThrow(() -> new ContentFolderNotFoundException(request.parentId()));

            if (parent.getDepth() >= ContentFolder.MAX_DEPTH) {
                throw new MaxDepthExceededException(ContentFolder.MAX_DEPTH);
            }

            folder = ContentFolder.createChild(request.folderName(), parent);
        } else {
            folder = ContentFolder.createRoot(request.folderName());
        }

        ContentFolder saved = contentFolderRepository.save(folder);
        log.info("ContentFolder created: id={}, name={}, depth={}",
                saved.getId(), saved.getFolderName(), saved.getDepth());

        return ContentFolderResponse.from(saved);
    }

    @Override
    public List<ContentFolderResponse> getFolderTree(Long tenantId) {
        List<ContentFolder> rootFolders = contentFolderRepository
                .findByTenantIdAndParentIsNullOrderByFolderNameAsc(tenantId);

        return rootFolders.stream()
                .map(ContentFolderResponse::fromWithChildren)
                .toList();
    }

    @Override
    public ContentFolderResponse getFolder(Long id, Long tenantId) {
        ContentFolder folder = findFolderOrThrow(id, tenantId);
        return ContentFolderResponse.from(folder);
    }

    @Override
    public List<ContentFolderResponse> getChildFolders(Long parentId, Long tenantId) {
        List<ContentFolder> children = contentFolderRepository
                .findByTenantIdAndParentIdOrderByFolderNameAsc(tenantId, parentId);

        return children.stream()
                .map(ContentFolderResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public ContentFolderResponse update(Long id, UpdateContentFolderRequest request, Long tenantId) {
        ContentFolder folder = findFolderOrThrow(id, tenantId);

        Long parentId = folder.getParent() != null ? folder.getParent().getId() : null;
        if (!folder.getFolderName().equals(request.folderName())) {
            validateDuplicateName(tenantId, parentId, request.folderName());
        }

        folder.updateName(request.folderName());
        log.info("ContentFolder updated: id={}, name={}", id, request.folderName());

        return ContentFolderResponse.from(folder);
    }

    @Override
    @Transactional
    public ContentFolderResponse move(Long id, MoveContentFolderRequest request, Long tenantId) {
        ContentFolder folder = findFolderOrThrow(id, tenantId);

        ContentFolder newParent = null;
        if (request.targetParentId() != null) {
            newParent = contentFolderRepository.findByIdAndTenantId(request.targetParentId(), tenantId)
                    .orElseThrow(() -> new ContentFolderNotFoundException(request.targetParentId()));

            if (newParent.getDepth() >= ContentFolder.MAX_DEPTH) {
                throw new MaxDepthExceededException(ContentFolder.MAX_DEPTH);
            }

            if (isDescendant(folder, newParent)) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                        "Cannot move folder to its descendant");
            }
        }

        validateDuplicateName(tenantId, request.targetParentId(), folder.getFolderName());
        folder.moveTo(newParent);

        log.info("ContentFolder moved: id={}, newParentId={}", id, request.targetParentId());
        return ContentFolderResponse.from(folder);
    }

    @Override
    @Transactional
    public void delete(Long id, Long tenantId) {
        ContentFolder folder = findFolderOrThrow(id, tenantId);

        if (!folder.isEmpty()) {
            throw new BusinessException(ErrorCode.FOLDER_NOT_EMPTY,
                    "Folder contains items. Please delete or move items first.");
        }

        contentFolderRepository.delete(folder);
        log.info("ContentFolder deleted: id={}", id);
    }

    private ContentFolder findFolderOrThrow(Long id, Long tenantId) {
        return contentFolderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new ContentFolderNotFoundException(id));
    }

    private void validateDuplicateName(Long tenantId, Long parentId, String folderName) {
        boolean exists;
        if (parentId == null) {
            exists = contentFolderRepository.existsByTenantIdAndParentIsNullAndFolderName(tenantId, folderName);
        } else {
            exists = contentFolderRepository.existsByTenantIdAndParentIdAndFolderName(tenantId, parentId, folderName);
        }

        if (exists) {
            throw new BusinessException(ErrorCode.DUPLICATE_FOLDER_NAME,
                    "Folder name '" + folderName + "' already exists in this location");
        }
    }

    private boolean isDescendant(ContentFolder folder, ContentFolder potentialDescendant) {
        ContentFolder current = potentialDescendant;
        while (current != null) {
            if (current.getId().equals(folder.getId())) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}
