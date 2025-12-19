package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
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
    private final CourseRepository courseRepository;
    private final CourseItemRepository courseItemRepository;

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

        copyItemsFromCourse(savedSnapshot, courseItems);

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

    private void copyItemsFromCourse(CourseSnapshot snapshot, List<CourseItem> courseItems) {
        Map<Long, SnapshotItem> itemMapping = new HashMap<>();

        for (CourseItem courseItem : courseItems) {
            SnapshotLearningObject snapshotLo = null;

            if (courseItem.getLearningObjectId() != null) {
                snapshotLo = SnapshotLearningObject.create(
                        courseItem.getLearningObjectId(),
                        courseItem.getItemName()
                );
                snapshotLo = snapshotLoRepository.save(snapshotLo);
            }

            SnapshotItem parentItem = null;
            if (courseItem.getParent() != null) {
                parentItem = itemMapping.get(courseItem.getParent().getId());
            }

            String itemType = courseItem.isFolder() ? null : "CONTENT";

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
    }
}
