package com.mzc.lp.domain.snapshot.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.entity.CourseRelation;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRelationRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.snapshot.dto.response.SnapshotDetailResponse;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.entity.SnapshotRelation;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotLearningObjectRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotRelationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnapshotServiceImplTest extends TenantTestSupport {

    @InjectMocks
    private SnapshotServiceImpl snapshotService;

    @Mock
    private CourseSnapshotRepository snapshotRepository;

    @Mock
    private SnapshotItemRepository snapshotItemRepository;

    @Mock
    private SnapshotLearningObjectRepository snapshotLoRepository;

    @Mock
    private SnapshotRelationRepository snapshotRelationRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseItemRepository courseItemRepository;

    @Mock
    private CourseRelationRepository courseRelationRepository;

    @Captor
    private ArgumentCaptor<SnapshotRelation> relationCaptor;

    @Nested
    @DisplayName("createSnapshotFromCourse - Course에서 Snapshot 생성")
    class CreateSnapshotFromCourse {

        @Test
        @DisplayName("성공 - Item과 Relation이 함께 복사됨")
        void createSnapshotFromCourse_success_withRelations() {
            // given
            Long courseId = 1L;
            Long createdBy = 100L;

            // Course mock
            Course course = mock(Course.class);
            given(course.getId()).willReturn(courseId);
            given(course.getTitle()).willReturn("테스트 강의");
            given(courseRepository.findByIdAndTenantId(courseId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(course));

            // CourseSnapshot mock
            CourseSnapshot snapshot = mock(CourseSnapshot.class);
            given(snapshot.getId()).willReturn(10L);
            given(snapshotRepository.save(any(CourseSnapshot.class))).willReturn(snapshot);
            given(snapshotRepository.countItemsBySnapshotId(10L)).willReturn(2L);
            given(snapshotRepository.sumDurationBySnapshotId(10L)).willReturn(3600L);

            // CourseItem mocks (item1 -> item2 순서)
            CourseItem item1 = mock(CourseItem.class);
            given(item1.getId()).willReturn(1L);
            given(item1.getItemName()).willReturn("차시 1");
            given(item1.getLearningObjectId()).willReturn(null);
            given(item1.getParent()).willReturn(null);
            given(item1.isFolder()).willReturn(false);

            CourseItem item2 = mock(CourseItem.class);
            given(item2.getId()).willReturn(2L);
            given(item2.getItemName()).willReturn("차시 2");
            given(item2.getLearningObjectId()).willReturn(null);
            given(item2.getParent()).willReturn(null);
            given(item2.isFolder()).willReturn(false);

            given(courseItemRepository.findByCourseIdOrderByDepthAndSortOrder(courseId, DEFAULT_TENANT_ID))
                    .willReturn(List.of(item1, item2));

            // SnapshotItem mocks
            SnapshotItem snapshotItem1 = mock(SnapshotItem.class);
            given(snapshotItem1.getId()).willReturn(101L);
            SnapshotItem snapshotItem2 = mock(SnapshotItem.class);
            given(snapshotItem2.getId()).willReturn(102L);

            when(snapshotItemRepository.save(any(SnapshotItem.class)))
                    .thenReturn(snapshotItem1)
                    .thenReturn(snapshotItem2);

            // CourseRelation mock (item1 -> item2 연결)
            CourseRelation relation = mock(CourseRelation.class);
            given(relation.getId()).willReturn(1L);
            given(relation.getFromItem()).willReturn(item1);
            given(relation.getToItem()).willReturn(item2);

            given(courseRelationRepository.findByCourseIdWithItems(courseId, DEFAULT_TENANT_ID))
                    .willReturn(List.of(relation));

            // SnapshotItem 조회 (응답용)
            given(snapshotItemRepository.findRootItemsWithLo(10L, DEFAULT_TENANT_ID))
                    .willReturn(Collections.emptyList());

            // when
            SnapshotDetailResponse response = snapshotService.createSnapshotFromCourse(courseId, createdBy);

            // then
            assertThat(response).isNotNull();

            // SnapshotRelation이 저장되었는지 확인
            verify(snapshotRelationRepository).save(relationCaptor.capture());
            SnapshotRelation savedRelation = relationCaptor.getValue();
            assertThat(savedRelation).isNotNull();
        }

        @Test
        @DisplayName("성공 - Relation이 없는 Course에서 Snapshot 생성")
        void createSnapshotFromCourse_success_noRelations() {
            // given
            Long courseId = 1L;
            Long createdBy = 100L;

            // Course mock
            Course course = mock(Course.class);
            given(course.getId()).willReturn(courseId);
            given(course.getTitle()).willReturn("테스트 강의");
            given(courseRepository.findByIdAndTenantId(courseId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(course));

            // CourseSnapshot mock
            CourseSnapshot snapshot = mock(CourseSnapshot.class);
            given(snapshot.getId()).willReturn(10L);
            given(snapshotRepository.save(any(CourseSnapshot.class))).willReturn(snapshot);
            given(snapshotRepository.countItemsBySnapshotId(10L)).willReturn(1L);
            given(snapshotRepository.sumDurationBySnapshotId(10L)).willReturn(1800L);

            // CourseItem mock
            CourseItem item1 = mock(CourseItem.class);
            given(item1.getId()).willReturn(1L);
            given(item1.getItemName()).willReturn("차시 1");
            given(item1.getLearningObjectId()).willReturn(null);
            given(item1.getParent()).willReturn(null);
            given(item1.isFolder()).willReturn(false);

            given(courseItemRepository.findByCourseIdOrderByDepthAndSortOrder(courseId, DEFAULT_TENANT_ID))
                    .willReturn(List.of(item1));

            // SnapshotItem mock
            SnapshotItem snapshotItem1 = mock(SnapshotItem.class);
            given(snapshotItem1.getId()).willReturn(101L);
            when(snapshotItemRepository.save(any(SnapshotItem.class))).thenReturn(snapshotItem1);

            // CourseRelation - 빈 목록
            given(courseRelationRepository.findByCourseIdWithItems(courseId, DEFAULT_TENANT_ID))
                    .willReturn(Collections.emptyList());

            // SnapshotItem 조회 (응답용)
            given(snapshotItemRepository.findRootItemsWithLo(10L, DEFAULT_TENANT_ID))
                    .willReturn(Collections.emptyList());

            // when
            SnapshotDetailResponse response = snapshotService.createSnapshotFromCourse(courseId, createdBy);

            // then
            assertThat(response).isNotNull();

            // SnapshotRelation은 저장되지 않아야 함
            verify(snapshotRelationRepository, never()).save(any(SnapshotRelation.class));
        }

        @Test
        @DisplayName("성공 - 시작점 Relation이 있는 경우 (fromItem이 null)")
        void createSnapshotFromCourse_success_withStartPointRelation() {
            // given
            Long courseId = 1L;
            Long createdBy = 100L;

            // Course mock
            Course course = mock(Course.class);
            given(course.getId()).willReturn(courseId);
            given(course.getTitle()).willReturn("테스트 강의");
            given(courseRepository.findByIdAndTenantId(courseId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(course));

            // CourseSnapshot mock
            CourseSnapshot snapshot = mock(CourseSnapshot.class);
            given(snapshot.getId()).willReturn(10L);
            given(snapshotRepository.save(any(CourseSnapshot.class))).willReturn(snapshot);
            given(snapshotRepository.countItemsBySnapshotId(10L)).willReturn(1L);
            given(snapshotRepository.sumDurationBySnapshotId(10L)).willReturn(1800L);

            // CourseItem mock
            CourseItem item1 = mock(CourseItem.class);
            given(item1.getId()).willReturn(1L);
            given(item1.getItemName()).willReturn("첫 번째 차시");
            given(item1.getLearningObjectId()).willReturn(null);
            given(item1.getParent()).willReturn(null);
            given(item1.isFolder()).willReturn(false);

            given(courseItemRepository.findByCourseIdOrderByDepthAndSortOrder(courseId, DEFAULT_TENANT_ID))
                    .willReturn(List.of(item1));

            // SnapshotItem mock
            SnapshotItem snapshotItem1 = mock(SnapshotItem.class);
            given(snapshotItem1.getId()).willReturn(101L);
            when(snapshotItemRepository.save(any(SnapshotItem.class))).thenReturn(snapshotItem1);

            // CourseRelation mock (시작점 - fromItem이 null)
            CourseRelation startRelation = mock(CourseRelation.class);
            given(startRelation.getId()).willReturn(1L);
            given(startRelation.getFromItem()).willReturn(null);  // 시작점
            given(startRelation.getToItem()).willReturn(item1);

            given(courseRelationRepository.findByCourseIdWithItems(courseId, DEFAULT_TENANT_ID))
                    .willReturn(List.of(startRelation));

            // SnapshotItem 조회 (응답용)
            given(snapshotItemRepository.findRootItemsWithLo(10L, DEFAULT_TENANT_ID))
                    .willReturn(Collections.emptyList());

            // when
            SnapshotDetailResponse response = snapshotService.createSnapshotFromCourse(courseId, createdBy);

            // then
            assertThat(response).isNotNull();

            // 시작점 SnapshotRelation이 저장되었는지 확인
            verify(snapshotRelationRepository).save(relationCaptor.capture());
            SnapshotRelation savedRelation = relationCaptor.getValue();
            assertThat(savedRelation).isNotNull();
        }
    }
}
