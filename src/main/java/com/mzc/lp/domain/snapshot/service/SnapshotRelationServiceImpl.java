package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.course.exception.CircularReferenceException;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRelationRequest;
import com.mzc.lp.domain.snapshot.dto.request.SetStartSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotRelationResponse;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.entity.SnapshotRelation;
import com.mzc.lp.domain.snapshot.exception.SnapshotItemNotFoundException;
import com.mzc.lp.domain.snapshot.exception.SnapshotNotFoundException;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotRelationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnapshotRelationServiceImpl implements SnapshotRelationService {

    private final CourseSnapshotRepository snapshotRepository;
    private final SnapshotItemRepository snapshotItemRepository;
    private final SnapshotRelationRepository snapshotRelationRepository;

    private static final Long DEFAULT_TENANT_ID = 1L;

    @Override
    public SnapshotRelationResponse.SnapshotRelationsResponse getRelations(Long snapshotId) {
        log.debug("Getting relations: snapshotId={}", snapshotId);

        validateSnapshotExists(snapshotId);

        List<SnapshotRelation> relations = snapshotRelationRepository.findBySnapshotIdWithItems(
                snapshotId, DEFAULT_TENANT_ID);

        if (relations.isEmpty()) {
            return SnapshotRelationResponse.SnapshotRelationsResponse.from(
                    snapshotId, Collections.emptyList(), Collections.emptyList());
        }

        List<SnapshotRelationResponse.OrderedItem> orderedItems = buildOrderedItems(relations);

        return SnapshotRelationResponse.SnapshotRelationsResponse.from(snapshotId, orderedItems, relations);
    }

    @Override
    public List<SnapshotRelationResponse.OrderedItem> getOrderedItems(Long snapshotId) {
        log.debug("Getting ordered items: snapshotId={}", snapshotId);

        validateSnapshotExists(snapshotId);

        List<SnapshotRelation> relations = snapshotRelationRepository.findBySnapshotIdWithItems(
                snapshotId, DEFAULT_TENANT_ID);

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        return buildOrderedItems(relations);
    }

    @Override
    @Transactional
    public SnapshotRelationResponse createRelation(Long snapshotId, CreateSnapshotRelationRequest request) {
        log.info("Creating relation: snapshotId={}, fromItemId={}, toItemId={}",
                snapshotId, request.fromItemId(), request.toItemId());

        CourseSnapshot snapshot = findSnapshotById(snapshotId);

        SnapshotItem fromItem = null;
        if (request.fromItemId() != null) {
            fromItem = findItemById(request.fromItemId());
            validateItemBelongsToSnapshot(fromItem, snapshotId);
        }

        SnapshotItem toItem = findItemById(request.toItemId());
        validateItemBelongsToSnapshot(toItem, snapshotId);

        // toItem이 이미 다른 관계에서 참조되고 있는지 확인
        if (snapshotRelationRepository.existsByToItemIdAndTenantId(request.toItemId(), DEFAULT_TENANT_ID)) {
            throw new IllegalArgumentException("이 항목은 이미 학습 순서에 포함되어 있습니다: " + request.toItemId());
        }

        // 순환 참조 검증
        if (request.fromItemId() != null) {
            validateNoCircularReference(snapshotId, request.fromItemId(), request.toItemId());
        }

        SnapshotRelation relation;
        if (request.fromItemId() == null) {
            // 기존 시작점이 있으면 삭제
            snapshotRelationRepository.findBySnapshotIdAndFromItemIsNullAndTenantId(snapshotId, DEFAULT_TENANT_ID)
                    .ifPresent(snapshotRelationRepository::delete);
            relation = SnapshotRelation.createStartPoint(snapshot, toItem);
        } else {
            relation = SnapshotRelation.create(snapshot, fromItem, toItem);
        }

        SnapshotRelation savedRelation = snapshotRelationRepository.save(relation);
        log.info("Relation created: relationId={}, snapshotId={}", savedRelation.getId(), snapshotId);

        return SnapshotRelationResponse.from(savedRelation);
    }

    @Override
    @Transactional
    public SnapshotRelationResponse setStartItem(Long snapshotId, SetStartSnapshotItemRequest request) {
        log.info("Setting start item: snapshotId={}, itemId={}", snapshotId, request.itemId());

        CourseSnapshot snapshot = findSnapshotById(snapshotId);

        SnapshotItem newStartItem = findItemById(request.itemId());
        validateItemBelongsToSnapshot(newStartItem, snapshotId);

        if (newStartItem.isFolder()) {
            throw new IllegalArgumentException("폴더는 시작점으로 설정할 수 없습니다");
        }

        // 기존 시작점 찾기
        Optional<SnapshotRelation> existingStart = snapshotRelationRepository
                .findBySnapshotIdAndFromItemIsNullAndTenantId(snapshotId, DEFAULT_TENANT_ID);

        // 새 시작점이 이미 다른 곳에서 참조되고 있다면 해당 관계 삭제
        Optional<SnapshotRelation> existingToNew = snapshotRelationRepository
                .findByToItemIdAndTenantId(request.itemId(), DEFAULT_TENANT_ID);
        existingToNew.ifPresent(snapshotRelationRepository::delete);

        SnapshotRelation startRelation;
        if (existingStart.isPresent()) {
            startRelation = existingStart.get();
            startRelation.updateToItem(newStartItem);
        } else {
            startRelation = SnapshotRelation.createStartPoint(snapshot, newStartItem);
            startRelation = snapshotRelationRepository.save(startRelation);
        }

        log.info("Start item set: snapshotId={}, itemId={}", snapshotId, request.itemId());

        return SnapshotRelationResponse.from(startRelation);
    }

    @Override
    @Transactional
    public void deleteRelation(Long snapshotId, Long relationId) {
        log.info("Deleting relation: snapshotId={}, relationId={}", snapshotId, relationId);

        validateSnapshotExists(snapshotId);

        SnapshotRelation relation = snapshotRelationRepository.findByIdAndTenantId(relationId, DEFAULT_TENANT_ID)
                .orElseThrow(() -> new IllegalArgumentException("순서 연결을 찾을 수 없습니다: " + relationId));

        if (!relation.getSnapshot().getId().equals(snapshotId)) {
            throw new IllegalArgumentException("해당 스냅샷의 순서 연결이 아닙니다");
        }

        snapshotRelationRepository.delete(relation);
        log.info("Relation deleted: relationId={}", relationId);
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

    private SnapshotItem findItemById(Long itemId) {
        return snapshotItemRepository.findByIdAndTenantId(itemId, DEFAULT_TENANT_ID)
                .orElseThrow(() -> new SnapshotItemNotFoundException(itemId));
    }

    private void validateItemBelongsToSnapshot(SnapshotItem item, Long snapshotId) {
        if (!item.getSnapshot().getId().equals(snapshotId)) {
            throw new SnapshotItemNotFoundException(item.getId());
        }
    }

    private void validateNoCircularReference(Long snapshotId, Long fromItemId, Long toItemId) {
        List<SnapshotRelation> relations = snapshotRelationRepository.findBySnapshotIdAndTenantId(
                snapshotId, DEFAULT_TENANT_ID);

        Map<Long, Long> fromToMap = new HashMap<>();
        for (SnapshotRelation relation : relations) {
            if (relation.getFromItem() != null) {
                fromToMap.put(relation.getFromItem().getId(), relation.getToItem().getId());
            }
        }

        // 새로운 관계 추가
        fromToMap.put(fromItemId, toItemId);

        // toItemId에서 시작해서 순환 참조 검사
        Set<Long> visited = new HashSet<>();
        Long current = toItemId;

        while (current != null) {
            if (visited.contains(current)) {
                throw new CircularReferenceException("순환 참조가 감지되었습니다");
            }
            visited.add(current);
            current = fromToMap.get(current);
        }
    }

    private List<SnapshotRelationResponse.OrderedItem> buildOrderedItems(List<SnapshotRelation> relations) {
        Map<Long, SnapshotRelation> fromMap = new HashMap<>();
        SnapshotRelation startRelation = null;

        for (SnapshotRelation relation : relations) {
            if (relation.isStartPoint()) {
                startRelation = relation;
            } else {
                fromMap.put(relation.getFromItem().getId(), relation);
            }
        }

        if (startRelation == null) {
            return Collections.emptyList();
        }

        List<SnapshotRelationResponse.OrderedItem> orderedItems = new ArrayList<>();
        int seq = 1;

        SnapshotItem current = startRelation.getToItem();
        Set<Long> visited = new HashSet<>();

        while (current != null && !visited.contains(current.getId())) {
            visited.add(current.getId());
            orderedItems.add(new SnapshotRelationResponse.OrderedItem(
                    current.getId(),
                    current.getItemName(),
                    seq++
            ));

            SnapshotRelation nextRelation = fromMap.get(current.getId());
            current = nextRelation != null ? nextRelation.getToItem() : null;
        }

        return orderedItems;
    }
}
