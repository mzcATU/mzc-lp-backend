package com.mzc.lp.domain.course.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseStatus;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.dto.response.CourseResponse;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.exception.CourseIncompleteException;
import com.mzc.lp.domain.course.exception.CourseNotFoundException;
import com.mzc.lp.domain.course.exception.CourseNotModifiableException;
import com.mzc.lp.domain.course.exception.InvalidCourseStatusTransitionException;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Course 상태 전환 서비스 테스트")
class CourseStatusServiceTest extends TenantTestSupport {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @BeforeEach
    void setUp() {
        // FK 순서: CourseTime → Course
        courseTimeRepository.deleteAll();
        courseRepository.deleteAll();
    }

    // ===== 테스트 헬퍼 메서드 =====
    private Course createCompleteCourse(String title) {
        Course course = Course.create(
                title,
                "상세한 설명입니다",
                CourseLevel.BEGINNER,
                CourseType.ONLINE,
                10,
                1L, // categoryId
                null,
                null,
                1L // createdBy
        );
        CourseItem item = CourseItem.createFolder(course, "폴더1", null);
        course.addItem(item);
        return courseRepository.save(course);
    }

    private Course createIncompleteCourse(String title) {
        Course course = Course.create(
                title,
                null, // description 없음 -> 미완성
                null,
                null,
                null,
                null,
                null,
                null,
                1L
        );
        return courseRepository.save(course);
    }

    // ===== readyCourse 테스트 =====
    @Nested
    @DisplayName("readyCourse() - 작성완료 전환")
    class ReadyCourse {

        @Test
        @DisplayName("성공 - DRAFT -> READY (완성된 강의)")
        void readyCourse_fromDraft_success() {
            // given
            Course course = createCompleteCourse("완성 강의");
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);

            // when
            CourseResponse response = courseService.readyCourse(course.getId());

            // then
            assertThat(response.status()).isEqualTo(CourseStatus.READY);

            Course updated = courseRepository.findById(course.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourseStatus.READY);
        }

        @Test
        @DisplayName("실패 - 미완성 강의")
        void readyCourse_incompleteCourse_throwsException() {
            // given
            Course course = createIncompleteCourse("미완성 강의");

            // when & then
            assertThatThrownBy(() -> courseService.readyCourse(course.getId()))
                    .isInstanceOf(CourseIncompleteException.class);
        }

        @Test
        @DisplayName("실패 - REGISTERED 상태")
        void readyCourse_fromRegistered_throwsException() {
            // given
            Course course = createCompleteCourse("등록된 강의");
            course.markAsReady();
            course.register();
            courseRepository.save(course);
            assertThat(course.getStatus()).isEqualTo(CourseStatus.REGISTERED);

            // when & then
            assertThatThrownBy(() -> courseService.readyCourse(course.getId()))
                    .isInstanceOf(CourseNotModifiableException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void readyCourse_notFound_throwsException() {
            // when & then
            assertThatThrownBy(() -> courseService.readyCourse(99999L))
                    .isInstanceOf(CourseNotFoundException.class);
        }
    }

    // ===== unreadyCourse 테스트 =====
    @Nested
    @DisplayName("unreadyCourse() - 작성중 전환")
    class UnreadyCourse {

        @Test
        @DisplayName("성공 - READY -> DRAFT")
        void unreadyCourse_fromReady_success() {
            // given
            Course course = createCompleteCourse("완성 강의");
            course.markAsReady();
            courseRepository.save(course);
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);

            // when
            CourseResponse response = courseService.unreadyCourse(course.getId());

            // then
            assertThat(response.status()).isEqualTo(CourseStatus.DRAFT);

            Course updated = courseRepository.findById(course.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourseStatus.DRAFT);
        }

        @Test
        @DisplayName("실패 - REGISTERED 상태")
        void unreadyCourse_fromRegistered_throwsException() {
            // given
            Course course = createCompleteCourse("등록된 강의");
            course.markAsReady();
            course.register();
            courseRepository.save(course);

            // when & then
            assertThatThrownBy(() -> courseService.unreadyCourse(course.getId()))
                    .isInstanceOf(CourseNotModifiableException.class);
        }
    }

    // ===== registerCourse 테스트 =====
    @Nested
    @DisplayName("registerCourse() - 등록 전환")
    class RegisterCourse {

        @Test
        @DisplayName("성공 - READY -> REGISTERED")
        void registerCourse_fromReady_success() {
            // given
            Course course = createCompleteCourse("READY 강의");
            course.markAsReady();
            courseRepository.save(course);
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);

            // when
            CourseResponse response = courseService.registerCourse(course.getId());

            // then
            assertThat(response.status()).isEqualTo(CourseStatus.REGISTERED);

            Course updated = courseRepository.findById(course.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(CourseStatus.REGISTERED);
            assertThat(updated.isModifiable()).isFalse();
            assertThat(updated.canCreateCourseTime()).isTrue();
        }

        @Test
        @DisplayName("실패 - DRAFT 상태에서 호출")
        void registerCourse_fromDraft_throwsException() {
            // given
            Course course = createCompleteCourse("DRAFT 강의");
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);

            // when & then
            assertThatThrownBy(() -> courseService.registerCourse(course.getId()))
                    .isInstanceOf(InvalidCourseStatusTransitionException.class);
        }

        @Test
        @DisplayName("실패 - REGISTERED 상태에서 재호출")
        void registerCourse_fromRegistered_throwsException() {
            // given
            Course course = createCompleteCourse("등록된 강의");
            course.markAsReady();
            course.register();
            courseRepository.save(course);

            // when & then
            assertThatThrownBy(() -> courseService.registerCourse(course.getId()))
                    .isInstanceOf(InvalidCourseStatusTransitionException.class);
        }
    }

    // ===== updateCourse 상태 검증 테스트 =====
    @Nested
    @DisplayName("updateCourse() - 수정 가능 상태 검증")
    class UpdateCourseModifiableCheck {

        @Test
        @DisplayName("실패 - REGISTERED 상태에서 수정 시도")
        void updateCourse_registeredCourse_throwsException() {
            // given
            Course course = createCompleteCourse("등록된 강의");
            course.markAsReady();
            course.register();
            courseRepository.save(course);

            // when & then
            assertThatThrownBy(() -> courseService.updateCourse(
                    course.getId(),
                    new com.mzc.lp.domain.course.dto.request.UpdateCourseRequest(
                            "수정 시도",
                            null, null, null, null, null, null, null, null
                    )
            )).isInstanceOf(CourseNotModifiableException.class);
        }
    }
}
