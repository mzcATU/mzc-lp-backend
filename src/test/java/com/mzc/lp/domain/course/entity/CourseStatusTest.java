package com.mzc.lp.domain.course.entity;

import com.mzc.lp.domain.course.constant.CourseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Course 상태 전환 테스트")
class CourseStatusTest {

    // ===== 테스트 헬퍼 메서드 =====
    private Course createDraftCourse() {
        return Course.create("테스트 강의", 1L);
    }

    private Course createReadyCourse() {
        Course course = createDraftCourse();
        course.markAsReady();
        return course;
    }

    private Course createRegisteredCourse() {
        Course course = createReadyCourse();
        course.register();
        return course;
    }

    // ===== markAsReady 테스트 =====
    @Nested
    @DisplayName("markAsReady() - 작성완료 전환")
    class MarkAsReady {

        @Test
        @DisplayName("성공 - DRAFT -> READY")
        void markAsReady_fromDraft_success() {
            // given
            Course course = createDraftCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);

            // when
            course.markAsReady();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);
            assertThat(course.isReady()).isTrue();
            assertThat(course.isModifiable()).isTrue();
        }

        @Test
        @DisplayName("성공 - READY -> READY (멱등성)")
        void markAsReady_fromReady_success() {
            // given
            Course course = createReadyCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);

            // when
            course.markAsReady();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);
        }

        @Test
        @DisplayName("실패 - REGISTERED 상태에서 호출")
        void markAsReady_fromRegistered_throwsException() {
            // given
            Course course = createRegisteredCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.REGISTERED);

            // when & then
            assertThatThrownBy(course::markAsReady)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("수정할 수 없습니다");
        }
    }

    // ===== markAsDraft 테스트 =====
    @Nested
    @DisplayName("markAsDraft() - 작성중 전환")
    class MarkAsDraft {

        @Test
        @DisplayName("성공 - READY -> DRAFT")
        void markAsDraft_fromReady_success() {
            // given
            Course course = createReadyCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);

            // when
            course.markAsDraft();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
            assertThat(course.isDraft()).isTrue();
            assertThat(course.isModifiable()).isTrue();
        }

        @Test
        @DisplayName("성공 - DRAFT -> DRAFT (멱등성)")
        void markAsDraft_fromDraft_success() {
            // given
            Course course = createDraftCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);

            // when
            course.markAsDraft();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        }

        @Test
        @DisplayName("실패 - REGISTERED 상태에서 호출")
        void markAsDraft_fromRegistered_throwsException() {
            // given
            Course course = createRegisteredCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.REGISTERED);

            // when & then
            assertThatThrownBy(course::markAsDraft)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("수정할 수 없습니다");
        }
    }

    // ===== register 테스트 =====
    @Nested
    @DisplayName("register() - 등록 전환")
    class Register {

        @Test
        @DisplayName("성공 - READY -> REGISTERED")
        void register_fromReady_success() {
            // given
            Course course = createReadyCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);

            // when
            course.register();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.REGISTERED);
            assertThat(course.isRegistered()).isTrue();
            assertThat(course.isModifiable()).isFalse();
            assertThat(course.canCreateCourseTime()).isTrue();
        }

        @Test
        @DisplayName("실패 - DRAFT 상태에서 호출")
        void register_fromDraft_throwsException() {
            // given
            Course course = createDraftCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);

            // when & then
            assertThatThrownBy(course::register)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("READY 상태의 강의만 등록");
        }

        @Test
        @DisplayName("실패 - REGISTERED 상태에서 재호출")
        void register_fromRegistered_throwsException() {
            // given
            Course course = createRegisteredCourse();
            assertThat(course.getStatus()).isEqualTo(CourseStatus.REGISTERED);

            // when & then
            assertThatThrownBy(course::register)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("READY 상태의 강의만 등록");
        }
    }

    // ===== 상태 헬퍼 메서드 테스트 =====
    @Nested
    @DisplayName("상태 확인 헬퍼 메서드")
    class StatusHelpers {

        @Test
        @DisplayName("isModifiable - DRAFT와 READY만 true")
        void isModifiable_correctness() {
            assertThat(createDraftCourse().isModifiable()).isTrue();
            assertThat(createReadyCourse().isModifiable()).isTrue();
            assertThat(createRegisteredCourse().isModifiable()).isFalse();
        }

        @Test
        @DisplayName("canCreateCourseTime - REGISTERED만 true")
        void canCreateCourseTime_correctness() {
            assertThat(createDraftCourse().canCreateCourseTime()).isFalse();
            assertThat(createReadyCourse().canCreateCourseTime()).isFalse();
            assertThat(createRegisteredCourse().canCreateCourseTime()).isTrue();
        }

        @Test
        @DisplayName("isDraft/isReady/isRegistered 정확성")
        void statusCheckers_correctness() {
            Course draft = createDraftCourse();
            assertThat(draft.isDraft()).isTrue();
            assertThat(draft.isReady()).isFalse();
            assertThat(draft.isRegistered()).isFalse();

            Course ready = createReadyCourse();
            assertThat(ready.isDraft()).isFalse();
            assertThat(ready.isReady()).isTrue();
            assertThat(ready.isRegistered()).isFalse();

            Course registered = createRegisteredCourse();
            assertThat(registered.isDraft()).isFalse();
            assertThat(registered.isReady()).isFalse();
            assertThat(registered.isRegistered()).isTrue();
        }
    }

    // ===== Deprecated 메서드 하위호환성 테스트 =====
    @Nested
    @DisplayName("Deprecated 메서드 하위호환성")
    class DeprecatedMethods {

        @Test
        @DisplayName("publish() -> markAsReady() 위임")
        @SuppressWarnings("deprecation")
        void publish_delegatesToMarkAsReady() {
            // given
            Course course = createDraftCourse();

            // when
            course.publish();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.READY);
        }

        @Test
        @DisplayName("unpublish() -> markAsDraft() 위임")
        @SuppressWarnings("deprecation")
        void unpublish_delegatesToMarkAsDraft() {
            // given
            Course course = createReadyCourse();

            // when
            course.unpublish();

            // then
            assertThat(course.getStatus()).isEqualTo(CourseStatus.DRAFT);
        }
    }
}
