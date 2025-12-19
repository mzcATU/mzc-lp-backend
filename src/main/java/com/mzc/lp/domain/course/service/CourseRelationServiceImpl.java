package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateRelationRequest;
import com.mzc.lp.domain.course.dto.request.SetStartItemRequest;
import com.mzc.lp.domain.course.dto.response.*;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.entity.CourseRelation;
import com.mzc.lp.domain.course.exception.CircularReferenceException;
import com.mzc.lp.domain.course.exception.CourseItemNotFoundException;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRelationRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.common.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseRelationServiceImpl implements CourseRelationService {

    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;
    private final CourseRelationRepository courseRelationRepository;

    @Override
    @Transactional
    public RelationCreateResponse createRelations(Long courseId, CreateRelationRequest request) {
        log.info("Creating relations for course: courseId={}, relationCount={}", courseId, request.relations().size());

        validateCourseExists(courseId);
        validateRelationRequest(courseId, request);

        List<CourseRelation> relations = new ArrayList<>();
        Long startItemId = null;

        for (CreateRelationRequest.RelationItem item : request.relations()) {
            CourseItem fromItem = null;
            if (item.fromItemId() != null) {
                fromItem = findCourseItem(item.fromItemId());
            } else {
                startItemId = item.toItemId();
            }
            CourseItem toItem = findCourseItem(item.toItemId());

            CourseRelation relation = item.fromItemId() == null
                    ? CourseRelation.createStartPoint(toItem)
                    : CourseRelation.create(fromItem, toItem);
            relations.add(relation);
        }

        courseRelationRepository.saveAll(relations);
        log.info("Relations created: courseId={}, count={}", courseId, relations.size());

        return RelationCreateResponse.of(courseId, relations.size(), startItemId);
    }

    @Override
    public CourseRelationResponse getRelations(Long courseId) {
        log.debug("Getting relations for course: courseId={}", courseId);

        validateCourseExists(courseId);

        List<CourseRelation> relations = courseRelationRepository.findByCourseIdWithItems(courseId, TenantContext.getCurrentTenantId());

        if (relations.isEmpty()) {
            return CourseRelationResponse.from(courseId, Collections.emptyList(), Collections.emptyList());
        }

        List<CourseRelationResponse.OrderedItem> orderedItems = buildOrderedItems(relations);

        return CourseRelationResponse.from(courseId, orderedItems, relations);
    }

    @Override
    @Transactional
    public RelationCreateResponse updateRelations(Long courseId, CreateRelationRequest request) {
        log.info("Updating relations for course: courseId={}", courseId);

        validateCourseExists(courseId);
        validateRelationRequest(courseId, request);

        // 기존 관계 삭제
        int deleted = courseRelationRepository.deleteByCourseId(courseId, TenantContext.getCurrentTenantId());
        log.debug("Deleted existing relations: courseId={}, count={}", courseId, deleted);

        // 새 관계 생성
        List<CourseRelation> relations = new ArrayList<>();
        Long startItemId = null;

        for (CreateRelationRequest.RelationItem item : request.relations()) {
            CourseItem fromItem = null;
            if (item.fromItemId() != null) {
                fromItem = findCourseItem(item.fromItemId());
            } else {
                startItemId = item.toItemId();
            }
            CourseItem toItem = findCourseItem(item.toItemId());

            CourseRelation relation = item.fromItemId() == null
                    ? CourseRelation.createStartPoint(toItem)
                    : CourseRelation.create(fromItem, toItem);
            relations.add(relation);
        }

        courseRelationRepository.saveAll(relations);
        log.info("Relations updated: courseId={}, count={}", courseId, relations.size());

        return RelationCreateResponse.of(courseId, relations.size(), startItemId);
    }

    @Override
    @Transactional
    public SetStartItemResponse setStartItem(Long courseId, SetStartItemRequest request) {
        log.info("Setting start item: courseId={}, startItemId={}", courseId, request.startItemId());

        validateCourseExists(courseId);

        CourseItem newStartItem = findCourseItem(request.startItemId());
        validateItemBelongsToCourse(newStartItem, courseId);

        if (newStartItem.isFolder()) {
            throw new IllegalArgumentException("폴더는 시작점으로 설정할 수 없습니다");
        }

        // 기존 시작점 찾기
        Optional<CourseRelation> existingStart = courseRelationRepository
                .findStartPointByCourseId(courseId, TenantContext.getCurrentTenantId());

        // 새 시작점이 이미 다른 곳에서 참조되고 있다면 해당 관계 삭제
        Optional<CourseRelation> existingToNew = courseRelationRepository
                .findByToItemId(request.startItemId(), TenantContext.getCurrentTenantId());
        existingToNew.ifPresent(courseRelationRepository::delete);

        if (existingStart.isPresent()) {
            CourseRelation startRelation = existingStart.get();
            // 시작점의 toItem을 새 시작점으로 변경
            startRelation.updateToItem(newStartItem);
        } else {
            // 시작점이 없으면 새로 생성
            CourseRelation newStart = CourseRelation.createStartPoint(newStartItem);
            courseRelationRepository.save(newStart);
        }

        log.info("Start item set: courseId={}, startItemId={}", courseId, request.startItemId());

        return SetStartItemResponse.of(courseId, request.startItemId());
    }

    @Override
    @Transactional
    public AutoRelationResponse createAutoRelations(Long courseId) {
        log.info("Creating auto relations for course: courseId={}", courseId);

        validateCourseExists(courseId);

        // 기존 관계 삭제
        courseRelationRepository.deleteByCourseId(courseId, TenantContext.getCurrentTenantId());

        // 차시만 조회 (폴더 제외), depth와 id 순으로 정렬
        List<CourseItem> items = courseItemRepository.findItemsOnlyByCourseId(courseId, TenantContext.getCurrentTenantId());

        if (items.isEmpty()) {
            log.info("No items found for auto relation: courseId={}", courseId);
            return AutoRelationResponse.of(courseId, 0);
        }

        // depth, id 기준 정렬
        items.sort(Comparator.comparing(CourseItem::getDepth)
                .thenComparing(CourseItem::getId));

        List<CourseRelation> relations = new ArrayList<>();

        // 첫 번째 항목은 시작점
        CourseRelation startRelation = CourseRelation.createStartPoint(items.get(0));
        relations.add(startRelation);

        // 나머지 항목들 연결
        for (int i = 0; i < items.size() - 1; i++) {
            CourseRelation relation = CourseRelation.create(items.get(i), items.get(i + 1));
            relations.add(relation);
        }

        courseRelationRepository.saveAll(relations);
        log.info("Auto relations created: courseId={}, count={}", courseId, relations.size());

        return AutoRelationResponse.of(courseId, relations.size());
    }

    @Override
    @Transactional
    public void deleteRelation(Long courseId, Long relationId) {
        log.info("Deleting relation: courseId={}, relationId={}", courseId, relationId);

        validateCourseExists(courseId);

        CourseRelation relation = courseRelationRepository.findByIdAndTenantId(relationId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new IllegalArgumentException("순서 연결을 찾을 수 없습니다: " + relationId));

        // 해당 relation이 이 course에 속하는지 확인
        if (!relation.getToItem().getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 순서 연결이 아닙니다");
        }

        courseRelationRepository.delete(relation);
        log.info("Relation deleted: relationId={}", relationId);
    }

    // ===== Private Helper Methods =====

    private void validateCourseExists(Long courseId) {
        if (!courseRepository.existsByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())) {
            throw new CourseNotFoundException(courseId);
        }
    }

    private CourseItem findCourseItem(Long itemId) {
        return courseItemRepository.findByIdAndTenantId(itemId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseItemNotFoundException(itemId));
    }

    private void validateItemBelongsToCourse(CourseItem item, Long courseId) {
        if (!item.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("해당 강의의 항목이 아닙니다: " + item.getId());
        }
    }

    private void validateRelationRequest(Long courseId, CreateRelationRequest request) {
        Set<Long> toItemIds = new HashSet<>();
        boolean hasStartPoint = false;

        for (CreateRelationRequest.RelationItem item : request.relations()) {
            // 시작점 중복 검사
            if (item.fromItemId() == null) {
                if (hasStartPoint) {
                    throw new IllegalArgumentException("시작점은 하나만 존재해야 합니다");
                }
                hasStartPoint = true;
            }

            // toItemId 중복 검사 (한 항목이 여러 곳에서 참조될 수 없음)
            if (toItemIds.contains(item.toItemId())) {
                throw new IllegalArgumentException("하나의 항목은 한 번만 참조될 수 있습니다: " + item.toItemId());
            }
            toItemIds.add(item.toItemId());

            // 항목 존재 및 소속 검사
            CourseItem toItem = findCourseItem(item.toItemId());
            validateItemBelongsToCourse(toItem, courseId);

            if (item.fromItemId() != null) {
                CourseItem fromItem = findCourseItem(item.fromItemId());
                validateItemBelongsToCourse(fromItem, courseId);
            }
        }

        // 순환 참조 검사
        validateNoCircularReference(request);
    }

    private void validateNoCircularReference(CreateRelationRequest request) {
        Map<Long, Long> fromToMap = new HashMap<>();

        for (CreateRelationRequest.RelationItem item : request.relations()) {
            if (item.fromItemId() != null) {
                fromToMap.put(item.fromItemId(), item.toItemId());
            }
        }

        // 각 시작점에서 순환 참조 검사
        for (CreateRelationRequest.RelationItem item : request.relations()) {
            if (item.fromItemId() == null) {
                Set<Long> visited = new HashSet<>();
                Long current = item.toItemId();

                while (current != null) {
                    if (visited.contains(current)) {
                        throw new CircularReferenceException("순환 참조가 감지되었습니다");
                    }
                    visited.add(current);
                    current = fromToMap.get(current);
                }
            }
        }
    }

    private List<CourseRelationResponse.OrderedItem> buildOrderedItems(List<CourseRelation> relations) {
        // fromItemId -> relation 매핑
        Map<Long, CourseRelation> fromMap = new HashMap<>();
        CourseRelation startRelation = null;

        for (CourseRelation relation : relations) {
            if (relation.isStartPoint()) {
                startRelation = relation;
            } else {
                fromMap.put(relation.getFromItem().getId(), relation);
            }
        }

        if (startRelation == null) {
            return Collections.emptyList();
        }

        List<CourseRelationResponse.OrderedItem> orderedItems = new ArrayList<>();
        int order = 1;

        // 시작점부터 순회
        CourseItem current = startRelation.getToItem();
        Set<Long> visited = new HashSet<>();

        while (current != null && !visited.contains(current.getId())) {
            visited.add(current.getId());
            orderedItems.add(new CourseRelationResponse.OrderedItem(
                    current.getId(),
                    current.getItemName(),
                    order++
            ));

            CourseRelation nextRelation = fromMap.get(current.getId());
            current = nextRelation != null ? nextRelation.getToItem() : null;
        }

        return orderedItems;
    }
}
