package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.course.exception.InvalidParentException;
import com.mzc.lp.domain.course.exception.MaxDepthExceededException;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.MoveSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotItemResponse;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.entity.SnapshotLearningObject;
import com.mzc.lp.domain.snapshot.exception.SnapshotItemNotFoundException;
import com.mzc.lp.domain.snapshot.exception.SnapshotNotFoundException;
import com.mzc.lp.domain.snapshot.exception.SnapshotStateException;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotLearningObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnapshotItemServiceImpl implements SnapshotItemService {

    private final CourseSnapshotRepository snapshotRepository;
    private final SnapshotItemRepository snapshotItemRepository;
    private final SnapshotLearningObjectRepository snapshotLoRepository;

    private static final Long DEFAULT_TENANT_ID = 1L;

    @Override
    public List<SnapshotItemResponse> getHierarchy(Long snapshotId) {
        log.debug("Getting item hierarchy: snapshotId={}", snapshotId);

        validateSnapshotExists(snapshotId);

        List<SnapshotItem> rootItems = snapshotItemRepository.findRootItemsWithLo(snapshotId, DEFAULT_TENANT_ID);

        return rootItems.stream()
                .map(SnapshotItemResponse::fromWithChildren)
                .toList();
    }

    @Override
    public List<SnapshotItemResponse> getFlatItems(Long snapshotId) {
        log.debug("Getting flat items: snapshotId={}", snapshotId);

        validateSnapshotExists(snapshotId);

        List<SnapshotItem> items = snapshotItemRepository.findBySnapshotIdWithLo(snapshotId, DEFAULT_TENANT_ID);

        return items.stream()
                .map(SnapshotItemResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public SnapshotItemResponse createItem(Long snapshotId, CreateSnapshotItemRequest request) {
        log.info("Creating snapshot item: snapshotId={}, itemName={}", snapshotId, request.itemName());

        CourseSnapshot snapshot = findSnapshotById(snapshotId);
        validateItemModifiable(snapshot);

        SnapshotItem parent = findParentIfExists(request.parentId(), snapshotId);

        SnapshotLearningObject snapshotLo = null;
        if (request.learningObjectId() != null) {
            snapshotLo = SnapshotLearningObject.create(
                    request.learningObjectId(),
                    request.itemName()
            );
            snapshotLo = snapshotLoRepository.save(snapshotLo);
        }

        try {
            SnapshotItem item;
            if (request.learningObjectId() == null) {
                item = SnapshotItem.createFolder(snapshot, request.itemName(), parent);
            } else {
                item = SnapshotItem.createItem(
                        snapshot,
                        request.itemName(),
                        parent,
                        snapshotLo,
                        request.itemType()
                );
            }

            SnapshotItem savedItem = snapshotItemRepository.save(item);
            log.info("Snapshot item created: itemId={}, snapshotId={}", savedItem.getId(), snapshotId);

            return SnapshotItemResponse.from(savedItem);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("최대 깊이")) {
                throw new MaxDepthExceededException();
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public SnapshotItemResponse updateItem(Long snapshotId, Long itemId, UpdateSnapshotItemRequest request) {
        log.info("Updating snapshot item: snapshotId={}, itemId={}, newName={}",
                snapshotId, itemId, request.itemName());

        CourseSnapshot snapshot = findSnapshotById(snapshotId);
        validateMetadataModifiable(snapshot);

        SnapshotItem item = findItemById(itemId);
        validateItemBelongsToSnapshot(item, snapshotId);

        item.updateItemName(request.itemName());
        log.info("Snapshot item updated: itemId={}", itemId);

        return SnapshotItemResponse.from(item);
    }

    @Override
    @Transactional
    public SnapshotItemResponse moveItem(Long snapshotId, Long itemId, MoveSnapshotItemRequest request) {
        log.info("Moving snapshot item: snapshotId={}, itemId={}, newParentId={}",
                snapshotId, itemId, request.newParentId());

        CourseSnapshot snapshot = findSnapshotById(snapshotId);
        validateItemModifiable(snapshot);

        SnapshotItem item = findItemById(itemId);
        validateItemBelongsToSnapshot(item, snapshotId);

        SnapshotItem newParent = findParentIfExists(request.newParentId(), snapshotId);

        try {
            item.moveTo(newParent);
            log.info("Snapshot item moved: itemId={}, newParentId={}, newDepth={}",
                    itemId, request.newParentId(), item.getDepth());

            return SnapshotItemResponse.from(item);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("최대 깊이")) {
                throw new MaxDepthExceededException();
            }
            if (e.getMessage().contains("하위 항목으로 이동")) {
                throw new InvalidParentException("자기 자신 또는 하위 항목으로 이동할 수 없습니다");
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteItem(Long snapshotId, Long itemId) {
        log.info("Deleting snapshot item: snapshotId={}, itemId={}", snapshotId, itemId);

        CourseSnapshot snapshot = findSnapshotById(snapshotId);
        validateItemModifiable(snapshot);

        SnapshotItem item = findItemById(itemId);
        validateItemBelongsToSnapshot(item, snapshotId);

        snapshotItemRepository.delete(item);
        log.info("Snapshot item deleted: itemId={}", itemId);
    }

    // ===== Private Helper Methods =====

    private CourseSnapshot findSnapshotById(Long snapshotId) {
        return snapshotRepository.findByIdAndTenantId(snapshotId, DEFAULT_TENANT_ID)
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));
    }

    private void validateSnapshotExists(Long snapshotId) {
        if (!snapshotRepository.existsByIdAndTenantId(snapshotId, DEFAULT_TENANT_ID)) {
            throw new SnapshotNotFoundException(snapshotId);
        }
    }

    private void validateItemModifiable(CourseSnapshot snapshot) {
        if (!snapshot.isItemModifiable()) {
            throw new SnapshotStateException(snapshot.getStatus(), "아이템 수정");
        }
    }

    private void validateMetadataModifiable(CourseSnapshot snapshot) {
        if (!snapshot.isModifiable()) {
            throw new SnapshotStateException(snapshot.getStatus(), "수정");
        }
    }

    private SnapshotItem findItemById(Long itemId) {
        return snapshotItemRepository.findByIdAndTenantId(itemId, DEFAULT_TENANT_ID)
                .orElseThrow(() -> new SnapshotItemNotFoundException(itemId));
    }

    private SnapshotItem findParentIfExists(Long parentId, Long snapshotId) {
        if (parentId == null) {
            return null;
        }
        SnapshotItem parent = snapshotItemRepository.findByIdAndTenantId(parentId, DEFAULT_TENANT_ID)
                .orElseThrow(() -> new SnapshotItemNotFoundException(parentId));
        validateItemBelongsToSnapshot(parent, snapshotId);
        return parent;
    }

    private void validateItemBelongsToSnapshot(SnapshotItem item, Long snapshotId) {
        if (!item.getSnapshot().getId().equals(snapshotId)) {
            throw new SnapshotItemNotFoundException(item.getId());
        }
    }
}
