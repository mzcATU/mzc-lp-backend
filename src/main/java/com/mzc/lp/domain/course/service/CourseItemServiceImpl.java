package com.mzc.lp.domain.course.service;

import com.mzc.lp.domain.course.dto.request.CreateFolderRequest;
import com.mzc.lp.domain.course.dto.request.CreateItemRequest;
import com.mzc.lp.domain.course.dto.request.MoveItemRequest;
import com.mzc.lp.domain.course.dto.request.UpdateDisplayInfoRequest;
import com.mzc.lp.domain.course.dto.request.UpdateItemNameRequest;
import com.mzc.lp.domain.course.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.course.dto.response.CourseItemHierarchyResponse;
import com.mzc.lp.domain.course.dto.response.CourseItemResponse;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.exception.CourseItemNotFoundException;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.exception.InvalidParentException;
import com.mzc.lp.domain.course.exception.MaxDepthExceededException;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.repository.ContentRepository;
import com.mzc.lp.domain.content.exception.ContentNotFoundException;
import com.mzc.lp.domain.learning.entity.LearningObject;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import com.mzc.lp.common.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseItemServiceImpl implements CourseItemService {

    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;
    private final ContentRepository contentRepository;
    private final LearningObjectRepository learningObjectRepository;

    @Override
    @Transactional
    public CourseItemResponse createItem(Long courseId, CreateItemRequest request) {
        log.info("Creating item: courseId={}, itemName={}, contentId={}", courseId, request.itemName(), request.contentId());

        Course course = findCourseById(courseId);
        CourseItem parent = findParentIfExists(request.parentId());

        validateParentBelongsToCourse(parent, courseId);

        // contentId로 LearningObject 조회 또는 생성
        Long learningObjectId = getOrCreateLearningObject(request.contentId(), request.itemName());

        try {
            CourseItem item = CourseItem.createItem(
                    course,
                    request.itemName(),
                    parent,
                    learningObjectId,
                    request.displayName(),
                    request.description()
            );

            CourseItem savedItem = courseItemRepository.save(item);
            log.info("Item created: id={}, name={}, loId={}", savedItem.getId(), savedItem.getItemName(), learningObjectId);

            return CourseItemResponse.from(savedItem);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("최대 깊이")) {
                throw new MaxDepthExceededException();
            }
            throw e;
        }
    }

    /**
     * contentId로 기존 LearningObject 조회, 없으면 새로 생성
     */
    private Long getOrCreateLearningObject(Long contentId, String name) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // 해당 Content에 대한 LearningObject가 이미 있는지 확인
        return learningObjectRepository.findByContentIdAndTenantId(contentId, tenantId)
                .map(LearningObject::getId)
                .orElseGet(() -> {
                    // Content 조회
                    Content content = contentRepository.findByIdAndTenantId(contentId, tenantId)
                            .orElseThrow(() -> new ContentNotFoundException(contentId));

                    // LearningObject 생성
                    LearningObject lo = LearningObject.create(name, content);
                    LearningObject savedLo = learningObjectRepository.save(lo);
                    log.info("LearningObject auto-created: id={}, contentId={}", savedLo.getId(), contentId);
                    return savedLo.getId();
                });
    }

    @Override
    @Transactional
    public CourseItemResponse createFolder(Long courseId, CreateFolderRequest request) {
        log.info("Creating folder: courseId={}, folderName={}", courseId, request.folderName());

        Course course = findCourseById(courseId);
        CourseItem parent = findParentIfExists(request.parentId());

        validateParentBelongsToCourse(parent, courseId);

        try {
            CourseItem folder = CourseItem.createFolder(
                    course,
                    request.folderName(),
                    parent
            );

            CourseItem savedFolder = courseItemRepository.save(folder);
            log.info("Folder created: id={}, name={}", savedFolder.getId(), savedFolder.getItemName());

            return CourseItemResponse.from(savedFolder);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("최대 깊이")) {
                throw new MaxDepthExceededException();
            }
            throw e;
        }
    }

    @Override
    public List<CourseItemHierarchyResponse> getHierarchy(Long courseId) {
        log.debug("Getting hierarchy: courseId={}", courseId);

        validateCourseExists(courseId);

        List<CourseItem> rootItems = courseItemRepository.findRootItemsWithChildren(courseId, TenantContext.getCurrentTenantId());

        return CourseItemHierarchyResponse.fromList(rootItems);
    }

    @Override
    public List<CourseItemResponse> getOrderedItems(Long courseId) {
        log.debug("Getting ordered items: courseId={}", courseId);

        validateCourseExists(courseId);

        List<CourseItem> items = courseItemRepository.findItemsOnlyByCourseId(courseId, TenantContext.getCurrentTenantId());

        return items.stream()
                .map(CourseItemResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public CourseItemResponse moveItem(Long courseId, MoveItemRequest request) {
        log.info("Moving item: courseId={}, itemId={}, targetParentId={}",
                courseId, request.itemId(), request.targetParentId());

        validateCourseExists(courseId);

        CourseItem item = findItemById(request.itemId());
        validateItemBelongsToCourse(item, courseId);

        CourseItem newParent = findParentIfExists(request.targetParentId());
        validateParentBelongsToCourse(newParent, courseId);

        try {
            item.moveTo(newParent);
            log.info("Item moved: id={}, newParentId={}, newDepth={}",
                    item.getId(), request.targetParentId(), item.getDepth());

            return CourseItemResponse.from(item);
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
    public CourseItemResponse updateItemName(Long courseId, Long itemId, UpdateItemNameRequest request) {
        log.info("Updating item name: courseId={}, itemId={}, newName={}",
                courseId, itemId, request.itemName());

        validateCourseExists(courseId);

        CourseItem item = findItemById(itemId);
        validateItemBelongsToCourse(item, courseId);

        item.updateItemName(request.itemName());
        log.info("Item name updated: id={}", itemId);

        return CourseItemResponse.from(item);
    }

    @Override
    @Transactional
    public CourseItemResponse updateLearningObject(Long courseId, Long itemId, UpdateLearningObjectRequest request) {
        log.info("Updating learning object: courseId={}, itemId={}, loId={}",
                courseId, itemId, request.learningObjectId());

        validateCourseExists(courseId);

        CourseItem item = findItemById(itemId);
        validateItemBelongsToCourse(item, courseId);

        if (item.isFolder()) {
            throw new InvalidParentException("폴더에는 학습 객체를 연결할 수 없습니다");
        }

        item.updateLearningObjectId(request.learningObjectId());
        log.info("Learning object updated: itemId={}, loId={}", itemId, request.learningObjectId());

        return CourseItemResponse.from(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long courseId, Long itemId) {
        log.info("Deleting item: courseId={}, itemId={}", courseId, itemId);

        validateCourseExists(courseId);

        CourseItem item = findItemById(itemId);
        validateItemBelongsToCourse(item, courseId);

        courseItemRepository.delete(item);
        log.info("Item deleted: id={}", itemId);
    }

    @Override
    @Transactional
    public CourseItemResponse updateDisplayInfo(Long courseId, Long itemId, UpdateDisplayInfoRequest request) {
        log.info("Updating display info: courseId={}, itemId={}", courseId, itemId);

        validateCourseExists(courseId);

        CourseItem item = findItemById(itemId);
        validateItemBelongsToCourse(item, courseId);

        if (item.isFolder()) {
            throw new InvalidParentException("폴더에는 표시 정보를 설정할 수 없습니다");
        }

        item.updateDisplayInfo(request.displayName(), request.description());
        log.info("Display info updated: itemId={}", itemId);

        return CourseItemResponse.from(item);
    }

    // ===== Private Helper Methods =====

    private Course findCourseById(Long courseId) {
        return courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));
    }

    private void validateCourseExists(Long courseId) {
        if (!courseRepository.existsByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())) {
            throw new CourseNotFoundException(courseId);
        }
    }

    private CourseItem findItemById(Long itemId) {
        return courseItemRepository.findByIdAndTenantId(itemId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseItemNotFoundException(itemId));
    }

    private CourseItem findParentIfExists(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return courseItemRepository.findByIdAndTenantId(parentId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseItemNotFoundException(parentId));
    }

    private void validateParentBelongsToCourse(CourseItem parent, Long courseId) {
        if (parent != null && !parent.getCourse().getId().equals(courseId)) {
            throw new InvalidParentException("부모 항목이 해당 강의에 속하지 않습니다");
        }
    }

    private void validateItemBelongsToCourse(CourseItem item, Long courseId) {
        if (!item.getCourse().getId().equals(courseId)) {
            throw new CourseItemNotFoundException(item.getId());
        }
    }
}
