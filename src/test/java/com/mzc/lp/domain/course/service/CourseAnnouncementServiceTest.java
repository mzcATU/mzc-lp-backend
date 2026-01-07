package com.mzc.lp.domain.course.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.course.dto.request.CreateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.request.UpdateAnnouncementRequest;
import com.mzc.lp.domain.course.dto.response.AnnouncementListResponse;
import com.mzc.lp.domain.course.dto.response.AnnouncementResponse;
import com.mzc.lp.domain.course.entity.CourseAnnouncement;
import com.mzc.lp.domain.course.exception.AnnouncementNotFoundException;
import com.mzc.lp.domain.course.exception.NotAnnouncementAuthorException;
import com.mzc.lp.domain.course.repository.CourseAnnouncementRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseAnnouncementService 테스트")
class CourseAnnouncementServiceTest extends TenantTestSupport {

    @InjectMocks
    private CourseAnnouncementServiceImpl announcementService;

    @Mock
    private CourseAnnouncementRepository announcementRepository;

    @Mock
    private UserRepository userRepository;

    private CourseAnnouncement createTestAnnouncement(Long id, Long courseId, Long authorId, boolean isImportant) {
        CourseAnnouncement announcement = CourseAnnouncement.createForCourse(
                courseId, authorId, "테스트 공지", "테스트 내용", isImportant);
        setId(announcement, id);
        return announcement;
    }

    private User createTestUser(Long id, String name) {
        User user = User.create("test" + id + "@test.com", name, "encodedPassword");
        setId(user, id);
        return user;
    }

    private void setId(Object entity, Long id) {
        try {
            var idField = entity.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("createAnnouncement - 코스 공지 작성")
    class CreateAnnouncement {

        @Test
        @DisplayName("성공 - 코스 공지 작성")
        void createAnnouncement_success() {
            // given
            Long courseId = 1L;
            Long authorId = 100L;
            CreateAnnouncementRequest request = new CreateAnnouncementRequest("테스트 공지", "테스트 내용", true);

            CourseAnnouncement saved = createTestAnnouncement(1L, courseId, authorId, true);
            given(announcementRepository.save(any(CourseAnnouncement.class))).willReturn(saved);
            given(userRepository.findById(authorId)).willReturn(Optional.of(createTestUser(authorId, "작성자")));

            // when
            AnnouncementResponse response = announcementService.createAnnouncement(courseId, authorId, request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.courseId()).isEqualTo(courseId);
            assertThat(response.isImportant()).isTrue();
            verify(announcementRepository).save(any(CourseAnnouncement.class));
        }
    }

    @Nested
    @DisplayName("getAnnouncementsByCourse - 코스 공지 목록 조회")
    class GetAnnouncementsByCourse {

        @Test
        @DisplayName("성공 - 코스 공지 목록 조회")
        void getAnnouncementsByCourse_success() {
            // given
            Long courseId = 1L;
            List<CourseAnnouncement> announcements = List.of(
                    createTestAnnouncement(1L, courseId, 100L, true),
                    createTestAnnouncement(2L, courseId, 100L, false)
            );
            Page<CourseAnnouncement> page = new PageImpl<>(announcements);

            given(announcementRepository.findByCourseId(eq(courseId), eq(DEFAULT_TENANT_ID), any(Pageable.class)))
                    .willReturn(page);
            given(userRepository.findAllById(anySet()))
                    .willReturn(List.of(createTestUser(100L, "작성자")));

            // when
            AnnouncementListResponse response = announcementService.getAnnouncementsByCourse(courseId, 0, 20);

            // then
            assertThat(response).isNotNull();
            assertThat(response.announcements()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateAnnouncement - 공지 수정")
    class UpdateAnnouncement {

        @Test
        @DisplayName("성공 - 작성자가 공지 수정")
        void updateAnnouncement_success() {
            // given
            Long announcementId = 1L;
            Long authorId = 100L;
            CourseAnnouncement announcement = createTestAnnouncement(announcementId, 1L, authorId, false);

            given(announcementRepository.findByIdAndTenantId(announcementId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(announcement));
            given(userRepository.findById(authorId)).willReturn(Optional.of(createTestUser(authorId, "작성자")));

            UpdateAnnouncementRequest request = new UpdateAnnouncementRequest("수정된 제목", "수정된 내용", true);

            // when
            AnnouncementResponse response = announcementService.updateAnnouncement(announcementId, authorId, request, false);

            // then
            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.isImportant()).isTrue();
        }

        @Test
        @DisplayName("실패 - 작성자가 아닌 경우")
        void updateAnnouncement_fail_notAuthor() {
            // given
            Long announcementId = 1L;
            Long authorId = 100L;
            Long anotherUserId = 200L;
            CourseAnnouncement announcement = createTestAnnouncement(announcementId, 1L, authorId, false);

            given(announcementRepository.findByIdAndTenantId(announcementId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(announcement));

            UpdateAnnouncementRequest request = new UpdateAnnouncementRequest("수정된 제목", null, null);

            // when & then
            assertThatThrownBy(() -> announcementService.updateAnnouncement(announcementId, anotherUserId, request, false))
                    .isInstanceOf(NotAnnouncementAuthorException.class);
        }

        @Test
        @DisplayName("성공 - 관리자가 다른 사람 공지 수정")
        void updateAnnouncement_success_admin() {
            // given
            Long announcementId = 1L;
            Long authorId = 100L;
            Long adminId = 1L;
            CourseAnnouncement announcement = createTestAnnouncement(announcementId, 1L, authorId, false);

            given(announcementRepository.findByIdAndTenantId(announcementId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(announcement));
            given(userRepository.findById(authorId)).willReturn(Optional.of(createTestUser(authorId, "작성자")));

            UpdateAnnouncementRequest request = new UpdateAnnouncementRequest("관리자 수정", null, null);

            // when
            AnnouncementResponse response = announcementService.updateAnnouncement(announcementId, adminId, request, true);

            // then
            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("관리자 수정");
        }
    }

    @Nested
    @DisplayName("deleteAnnouncement - 공지 삭제")
    class DeleteAnnouncement {

        @Test
        @DisplayName("성공 - 작성자가 공지 삭제")
        void deleteAnnouncement_success() {
            // given
            Long announcementId = 1L;
            Long authorId = 100L;
            CourseAnnouncement announcement = createTestAnnouncement(announcementId, 1L, authorId, false);

            given(announcementRepository.findByIdAndTenantId(announcementId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.of(announcement));

            // when
            announcementService.deleteAnnouncement(announcementId, authorId, false);

            // then
            verify(announcementRepository).delete(announcement);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 공지")
        void deleteAnnouncement_fail_notFound() {
            // given
            Long announcementId = 999L;
            Long userId = 100L;

            given(announcementRepository.findByIdAndTenantId(announcementId, DEFAULT_TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> announcementService.deleteAnnouncement(announcementId, userId, false))
                    .isInstanceOf(AnnouncementNotFoundException.class);
        }
    }
}
