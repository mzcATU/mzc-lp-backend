package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.learning.entity.LearningObject;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotDetailResponse;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotItemResponse;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotResponse;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.entity.SnapshotLearningObject;
import com.mzc.lp.domain.snapshot.exception.SnapshotNotFoundException;
import com.mzc.lp.domain.snapshot.exception.SnapshotStateException;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotLearningObjectRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotRelationRepository;
import com.mzc.lp.domain.snapshot.entity.SnapshotRelation;
import com.mzc.lp.domain.course.repository.CourseRelationRepository;
import com.mzc.lp.domain.course.entity.CourseRelation;
import com.mzc.lp.common.context.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SnapshotServiceImpl implements SnapshotService {

    private final CourseSnapshotRepository snapshotRepository;
    private final SnapshotItemRepository snapshotItemRepository;
    private final SnapshotLearningObjectRepository snapshotLoRepository;
    private final SnapshotRelationRepository snapshotRelationRepository;
    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;
    private final CourseRelationRepository courseRelationRepository;
    private final LearningObjectRepository learningObjectRepository;

    @Override
    @Transactional
    public SnapshotDetailResponse createSnapshotFromCourse(Long courseId, Long createdBy) {
        log.info("Creating snapshot from course: courseId={}, createdBy={}", courseId, createdBy);

        Course course = courseRepository.findByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new CourseNotFoundException(courseId));

        CourseSnapshot snapshot = CourseSnapshot.createFromCourse(course, createdBy);
        CourseSnapshot savedSnapshot = snapshotRepository.save(snapshot);

        List<CourseItem> courseItems = courseItemRepository.findByCourseIdOrderByDepthAndSortOrder(
                courseId, TenantContext.getCurrentTenantId());

        Map<Long, SnapshotItem> itemMapping = copyItemsFromCourse(savedSnapshot, courseItems);

        // CourseRelation을 SnapshotRelation으로 복사
        copyRelationsFromCourse(savedSnapshot, courseId, itemMapping);

        Long itemCount = snapshotRepository.countItemsBySnapshotId(savedSnapshot.getId());
        Long totalDuration = snapshotRepository.sumDurationBySnapshotId(savedSnapshot.getId());

        List<SnapshotItem> rootItems = snapshotItemRepository.findRootItemsWithLo(
                savedSnapshot.getId(), TenantContext.getCurrentTenantId());
        List<SnapshotItemResponse> itemResponses = rootItems.stream()
                .map(SnapshotItemResponse::fromWithChildren)
                .toList();

        log.info("Snapshot created from course: snapshotId={}, courseId={}, itemCount={}",
                savedSnapshot.getId(), courseId, itemCount);

        return SnapshotDetailResponse.from(savedSnapshot, itemResponses, itemCount, totalDuration);
    }

    @Override
    @Transactional
    public SnapshotResponse createSnapshot(CreateSnapshotRequest request, Long createdBy) {
        log.info("Creating snapshot: snapshotName={}, createdBy={}", request.snapshotName(), createdBy);

        CourseSnapshot snapshot = CourseSnapshot.create(
                request.snapshotName(),
                request.description(),
                request.hashtags(),
                createdBy
        );

        CourseSnapshot savedSnapshot = snapshotRepository.save(snapshot);
        log.info("Snapshot created: snapshotId={}", savedSnapshot.getId());

        return SnapshotResponse.from(savedSnapshot);
    }

    @Override
    public Page<SnapshotResponse> getSnapshots(SnapshotStatus status, Long createdBy, Pageable pageable) {
        log.debug("Getting snapshots: status={}, createdBy={}", status, createdBy);

        Page<CourseSnapshot> snapshots;

        if (status != null && createdBy != null) {
            snapshots = snapshotRepository.findByTenantIdAndStatusAndCreatedBy(
                    TenantContext.getCurrentTenantId(), status, createdBy, pageable);
        } else if (status != null) {
            snapshots = snapshotRepository.findByTenantIdAndStatus(
                    TenantContext.getCurrentTenantId(), status, pageable);
        } else if (createdBy != null) {
            snapshots = snapshotRepository.findByTenantIdAndCreatedBy(
                    TenantContext.getCurrentTenantId(), createdBy, pageable);
        } else {
            snapshots = snapshotRepository.findByTenantId(TenantContext.getCurrentTenantId(), pageable);
        }

        return snapshots.map(SnapshotResponse::from);
    }

    @Override
    public SnapshotDetailResponse getSnapshotDetail(Long snapshotId) {
        log.debug("Getting snapshot detail: snapshotId={}", snapshotId);

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        List<SnapshotItem> rootItems = snapshotItemRepository.findRootItemsWithLo(
                snapshotId, TenantContext.getCurrentTenantId());
        List<SnapshotItemResponse> itemResponses = rootItems.stream()
                .map(SnapshotItemResponse::fromWithChildren)
                .toList();

        Long itemCount = snapshotRepository.countItemsBySnapshotId(snapshotId);
        Long totalDuration = snapshotRepository.sumDurationBySnapshotId(snapshotId);

        return SnapshotDetailResponse.from(snapshot, itemResponses, itemCount, totalDuration);
    }

    @Override
    public List<SnapshotResponse> getSnapshotsByCourse(Long courseId) {
        log.debug("Getting snapshots by course: courseId={}", courseId);

        if (!courseRepository.existsByIdAndTenantId(courseId, TenantContext.getCurrentTenantId())) {
            throw new CourseNotFoundException(courseId);
        }

        List<CourseSnapshot> snapshots = snapshotRepository.findBySourceCourseIdAndTenantId(
                courseId, TenantContext.getCurrentTenantId());

        return snapshots.stream()
                .map(SnapshotResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public SnapshotResponse updateSnapshot(Long snapshotId, UpdateSnapshotRequest request) {
        log.info("Updating snapshot: snapshotId={}", snapshotId);

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        if (!snapshot.isModifiable()) {
            throw new SnapshotStateException(snapshot.getStatus(), "수정");
        }

        snapshot.update(request.snapshotName(), request.description(), request.hashtags());

        log.info("Snapshot updated: snapshotId={}", snapshotId);
        return SnapshotResponse.from(snapshot);
    }

    @Override
    @Transactional
    public void deleteSnapshot(Long snapshotId) {
        log.info("Deleting snapshot: snapshotId={}", snapshotId);

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        snapshotRepository.delete(snapshot);
        log.info("Snapshot deleted: snapshotId={}", snapshotId);
    }

    @Override
    @Transactional
    public SnapshotResponse publishSnapshot(Long snapshotId) {
        log.info("Publishing snapshot: snapshotId={}", snapshotId);

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        if (!snapshot.isDraft()) {
            throw new SnapshotStateException(snapshot.getStatus(), "발행");
        }

        snapshot.publish();

        log.info("Snapshot published: snapshotId={}", snapshotId);
        return SnapshotResponse.from(snapshot);
    }

    @Override
    @Transactional
    public SnapshotResponse completeSnapshot(Long snapshotId) {
        log.info("Completing snapshot: snapshotId={}", snapshotId);

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        if (!snapshot.isActive()) {
            throw new SnapshotStateException(snapshot.getStatus(), "완료");
        }

        snapshot.complete();

        log.info("Snapshot completed: snapshotId={}", snapshotId);
        return SnapshotResponse.from(snapshot);
    }

    @Override
    @Transactional
    public SnapshotResponse archiveSnapshot(Long snapshotId) {
        log.info("Archiving snapshot: snapshotId={}", snapshotId);

        CourseSnapshot snapshot = snapshotRepository.findByIdAndTenantId(snapshotId, TenantContext.getCurrentTenantId())
                .orElseThrow(() -> new SnapshotNotFoundException(snapshotId));

        if (snapshot.getStatus() != SnapshotStatus.COMPLETED) {
            throw new SnapshotStateException(snapshot.getStatus(), "보관");
        }

        snapshot.archive();

        log.info("Snapshot archived: snapshotId={}", snapshotId);
        return SnapshotResponse.from(snapshot);
    }

    /**
     * Course의 Item들을 Snapshot으로 복사
     * @return CourseItem.id -> SnapshotItem 매핑 (Relation 복사에 사용)
     */
    private Map<Long, SnapshotItem> copyItemsFromCourse(CourseSnapshot snapshot, List<CourseItem> courseItems) {
        Map<Long, SnapshotItem> itemMapping = new HashMap<>();

        for (CourseItem courseItem : courseItems) {
            SnapshotLearningObject snapshotLo = null;
            String itemType = null;

            if (courseItem.getLearningObjectId() != null) {
                // LearningObject에서 Content 정보를 가져옴
                LearningObject lo = learningObjectRepository.findByIdAndTenantId(
                        courseItem.getLearningObjectId(), TenantContext.getCurrentTenantId())
                        .orElse(null);

                if (lo != null && lo.getContent() != null) {
                    var content = lo.getContent();

                    // displayName이 있으면 사용, 없으면 itemName(파일명) 사용
                    String displayName = courseItem.getDisplayName() != null && !courseItem.getDisplayName().isBlank()
                            ? courseItem.getDisplayName()
                            : courseItem.getItemName();

                    // createFromLo를 사용하여 올바른 contentId와 sourceLoId 설정
                    // description은 CourseItem에서 가져옴 (강의 디자인 시 설정한 값)
                    snapshotLo = SnapshotLearningObject.createFromLo(
                            courseItem.getLearningObjectId(),  // sourceLoId
                            content.getId(),                   // 실제 contentId
                            displayName,
                            content.getDuration(),
                            content.getThumbnailPath(),
                            content.getResolution(),
                            null,  // codec - Content에 없음
                            null,  // bitrate - Content에 없음
                            content.getPageCount(),
                            content.getExternalUrl(),          // 외부링크 URL
                            courseItem.getDescription(),       // 강의 디자인 시 설정한 설명
                            content.getDownloadable()          // 다운로드 허용 여부
                    );
                    snapshotLo = snapshotLoRepository.save(snapshotLo);

                    // Content의 contentType을 가져옴
                    itemType = content.getContentType() != null
                            ? content.getContentType().name()
                            : "VIDEO";
                }
            }

            SnapshotItem parentItem = null;
            if (courseItem.getParent() != null) {
                parentItem = itemMapping.get(courseItem.getParent().getId());
            }

            SnapshotItem snapshotItem = SnapshotItem.createFromCourseItem(
                    snapshot,
                    courseItem.getId(),
                    courseItem.getItemName(),
                    parentItem,
                    snapshotLo,
                    itemType
            );

            SnapshotItem savedItem = snapshotItemRepository.save(snapshotItem);
            itemMapping.put(courseItem.getId(), savedItem);
        }

        return itemMapping;
    }

    /**
     * Course의 학습 순서(Relation)를 Snapshot으로 복사
     * CourseRelation의 Linked List 구조를 SnapshotRelation으로 변환
     */
    private void copyRelationsFromCourse(CourseSnapshot snapshot, Long courseId, Map<Long, SnapshotItem> itemMapping) {
        List<CourseRelation> courseRelations = courseRelationRepository.findByCourseIdWithItems(
                courseId, TenantContext.getCurrentTenantId());

        if (courseRelations.isEmpty()) {
            log.debug("No course relations found for courseId={}", courseId);
            return;
        }

        for (CourseRelation courseRelation : courseRelations) {
            SnapshotItem fromItem = null;
            SnapshotItem toItem = null;

            // fromItem 매핑 (시작점의 경우 null일 수 있음)
            if (courseRelation.getFromItem() != null) {
                fromItem = itemMapping.get(courseRelation.getFromItem().getId());
                if (fromItem == null) {
                    log.warn("fromItem not found in mapping: courseItemId={}", courseRelation.getFromItem().getId());
                    continue;
                }
            }

            // toItem 매핑 (필수)
            if (courseRelation.getToItem() != null) {
                toItem = itemMapping.get(courseRelation.getToItem().getId());
                if (toItem == null) {
                    log.warn("toItem not found in mapping: courseItemId={}", courseRelation.getToItem().getId());
                    continue;
                }
            } else {
                log.warn("toItem is null in courseRelation: relationId={}", courseRelation.getId());
                continue;
            }

            // SnapshotRelation 생성
            SnapshotRelation snapshotRelation;
            if (fromItem == null) {
                // 시작점 (fromItem이 null인 경우)
                snapshotRelation = SnapshotRelation.createStartPoint(snapshot, toItem);
            } else {
                snapshotRelation = SnapshotRelation.create(snapshot, fromItem, toItem);
            }

            snapshotRelationRepository.save(snapshotRelation);
        }

        log.info("Copied {} relations from course {} to snapshot {}",
                courseRelations.size(), courseId, snapshot.getId());
    }
}
