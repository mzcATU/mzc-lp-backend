package com.mzc.lp.domain.ts.repository;

import com.mzc.lp.common.config.JpaConfig;
import com.mzc.lp.common.dto.stats.BooleanCountProjection;
import com.mzc.lp.common.dto.stats.StatusCountProjection;
import com.mzc.lp.common.dto.stats.TypeCountProjection;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class CourseTimeRepositoryTest extends TenantTestSupport {

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    private CourseTime testCourseTime;

    @BeforeEach
    void setUp() {
        courseTimeRepository.deleteAll();
        testCourseTime = createTestCourseTime();
    }

    private CourseTime createTestCourseTime() {
        return CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                1L
        );
    }

    private CourseTime createCourseTimeWithStatus(CourseTimeStatus status) {
        CourseTime courseTime = createTestCourseTime();
        courseTime = courseTimeRepository.save(courseTime);

        switch (status) {
            case DRAFT -> { /* 기본 상태, 변경 없음 */ }
            case RECRUITING -> courseTime.open();
            case ONGOING -> {
                courseTime.open();
                courseTime.startClass();
            }
            case CLOSED -> {
                courseTime.open();
                courseTime.startClass();
                courseTime.close();
            }
            case ARCHIVED -> {
                courseTime.open();
                courseTime.startClass();
                courseTime.close();
                courseTime.archive();
            }
        }
        return courseTimeRepository.save(courseTime);
    }

    @Nested
    @DisplayName("CourseTime 저장 테스트")
    class SaveTest {

        @Test
        @DisplayName("성공 - CourseTime 저장")
        void save_success() {
            // when
            CourseTime saved = courseTimeRepository.save(testCourseTime);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitle()).isEqualTo("테스트 차수");
            assertThat(saved.getDeliveryType()).isEqualTo(DeliveryType.ONLINE);
            assertThat(saved.getStatus()).isEqualTo(CourseTimeStatus.DRAFT);
            assertThat(saved.getCapacity()).isEqualTo(30);
            assertThat(saved.getCurrentEnrollment()).isEqualTo(0);
            assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(saved.isFree()).isFalse();
            assertThat(saved.isAllowLateEnrollment()).isTrue();
            assertThat(saved.getTenantId()).isEqualTo(1L);
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 무료 차수 저장 시 가격 0 자동 설정")
        void save_success_freeCoursePriceZero() {
            // given
            CourseTime freeCourseTime = CourseTime.create(
                    "무료 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    null,
                    null,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("50000"),
                    true,
                    null,
                    true,
                    1L
            );

            // when
            CourseTime saved = courseTimeRepository.save(freeCourseTime);

            // then
            assertThat(saved.isFree()).isTrue();
            assertThat(saved.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("성공 - 무제한 정원 차수 저장")
        void save_success_unlimitedCapacity() {
            // given
            CourseTime unlimitedCourseTime = CourseTime.create(
                    "무제한 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    null,
                    null,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    null,
                    true,
                    1L
            );

            // when
            CourseTime saved = courseTimeRepository.save(unlimitedCourseTime);

            // then
            assertThat(saved.getCapacity()).isNull();
            assertThat(saved.hasUnlimitedCapacity()).isTrue();
            assertThat(saved.hasAvailableSeats()).isTrue();
        }
    }

    @Nested
    @DisplayName("CourseTime 조회 테스트")
    class FindTest {

        @Test
        @DisplayName("성공 - ID로 조회")
        void findById_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);

            // when
            Optional<CourseTime> found = courseTimeRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("테스트 차수");
        }

        @Test
        @DisplayName("성공 - ID와 TenantId로 조회")
        void findByIdAndTenantId_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);

            // when
            Optional<CourseTime> found = courseTimeRepository.findByIdAndTenantId(saved.getId(), 1L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTenantId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("성공 - ID로 비관적 락과 함께 조회")
        void findByIdWithLock_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);

            // when - 비관적 락을 사용한 조회
            Optional<CourseTime> found = courseTimeRepository.findByIdWithLock(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(saved.getId());
            assertThat(found.get().getTitle()).isEqualTo("테스트 차수");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 ID로 비관적 락 조회")
        void findByIdWithLock_fail_notFound() {
            // when
            Optional<CourseTime> found = courseTimeRepository.findByIdWithLock(99999L);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("실패 - 다른 TenantId로 조회")
        void findByIdAndTenantId_fail_wrongTenant() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);

            // when
            Optional<CourseTime> found = courseTimeRepository.findByIdAndTenantId(saved.getId(), 999L);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("성공 - 상태별 조회")
        void findByTenantIdAndStatus_success() {
            // given
            createCourseTimeWithStatus(CourseTimeStatus.DRAFT);
            createCourseTimeWithStatus(CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus(CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus(CourseTimeStatus.ONGOING);

            // when
            Page<CourseTime> recruitingList = courseTimeRepository.findByTenantIdAndStatus(
                    1L, CourseTimeStatus.RECRUITING, PageRequest.of(0, 10));

            // then
            assertThat(recruitingList.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 여러 상태로 조회")
        void findByTenantIdAndStatusIn_success() {
            // given
            createCourseTimeWithStatus(CourseTimeStatus.DRAFT);
            createCourseTimeWithStatus(CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus(CourseTimeStatus.ONGOING);
            createCourseTimeWithStatus(CourseTimeStatus.CLOSED);

            // when
            Page<CourseTime> activeList = courseTimeRepository.findByTenantIdAndStatusIn(
                    1L,
                    List.of(CourseTimeStatus.RECRUITING, CourseTimeStatus.ONGOING),
                    PageRequest.of(0, 10)
            );

            // then
            assertThat(activeList.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 강의별 차수 조회")
        void findByCmCourseIdAndTenantId_success() {
            // given
            CourseTime courseTime1 = createTestCourseTime();
            courseTime1.linkCourse(100L, 1L);
            courseTimeRepository.save(courseTime1);

            CourseTime courseTime2 = createTestCourseTime();
            courseTime2.linkCourse(100L, 2L);
            courseTimeRepository.save(courseTime2);

            CourseTime courseTime3 = createTestCourseTime();
            courseTime3.linkCourse(200L, 1L);
            courseTimeRepository.save(courseTime3);

            // when
            List<CourseTime> course100List = courseTimeRepository.findByCmCourseIdAndTenantId(100L, 1L);

            // then
            assertThat(course100List).hasSize(2);
        }
    }

    @Nested
    @DisplayName("CourseTime 상태 전이 테스트")
    class StatusTransitionTest {

        @Test
        @DisplayName("성공 - DRAFT → RECRUITING")
        void open_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            assertThat(saved.getStatus()).isEqualTo(CourseTimeStatus.DRAFT);

            // when
            saved.open();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getStatus()).isEqualTo(CourseTimeStatus.RECRUITING);
            assertThat(updated.isRecruiting()).isTrue();
        }

        @Test
        @DisplayName("성공 - RECRUITING → ONGOING")
        void startClass_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.open();
            saved = courseTimeRepository.save(saved);

            // when
            saved.startClass();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getStatus()).isEqualTo(CourseTimeStatus.ONGOING);
            assertThat(updated.isOngoing()).isTrue();
        }

        @Test
        @DisplayName("성공 - ONGOING → CLOSED")
        void close_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.open();
            saved.startClass();
            saved = courseTimeRepository.save(saved);

            // when
            saved.close();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getStatus()).isEqualTo(CourseTimeStatus.CLOSED);
            assertThat(updated.isClosed()).isTrue();
        }

        @Test
        @DisplayName("성공 - CLOSED → ARCHIVED")
        void archive_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.open();
            saved.startClass();
            saved.close();
            saved = courseTimeRepository.save(saved);

            // when
            saved.archive();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getStatus()).isEqualTo(CourseTimeStatus.ARCHIVED);
            assertThat(updated.isArchived()).isTrue();
        }
    }

    @Nested
    @DisplayName("CourseTime 정원 관리 테스트")
    class CapacityTest {

        @Test
        @DisplayName("성공 - 등록 인원 증가")
        void incrementEnrollment_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            assertThat(saved.getCurrentEnrollment()).isEqualTo(0);

            // when
            saved.incrementEnrollment();
            saved.incrementEnrollment();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getCurrentEnrollment()).isEqualTo(2);
            assertThat(updated.getAvailableSeats()).isEqualTo(28);
        }

        @Test
        @DisplayName("성공 - 등록 인원 감소")
        void decrementEnrollment_success() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.incrementEnrollment();
            saved.incrementEnrollment();
            saved = courseTimeRepository.save(saved);

            // when
            saved.decrementEnrollment();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getCurrentEnrollment()).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 0 이하로 감소 시 0 유지")
        void decrementEnrollment_notBelowZero() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            assertThat(saved.getCurrentEnrollment()).isEqualTo(0);

            // when
            saved.decrementEnrollment();
            CourseTime updated = courseTimeRepository.save(saved);

            // then
            assertThat(updated.getCurrentEnrollment()).isEqualTo(0);
        }

        @Test
        @DisplayName("성공 - 정원 여유 확인")
        void hasAvailableSeats_success() {
            // given
            CourseTime courseTime = CourseTime.create(
                    "소규모 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    2,
                    0,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    null,
                    true,
                    1L
            );
            CourseTime saved = courseTimeRepository.save(courseTime);

            // when & then
            assertThat(saved.hasAvailableSeats()).isTrue();

            saved.incrementEnrollment();
            assertThat(saved.hasAvailableSeats()).isTrue();

            saved.incrementEnrollment();
            assertThat(saved.hasAvailableSeats()).isFalse();
        }
    }

    @Nested
    @DisplayName("CourseTime 수강 신청 가능 여부 테스트")
    class CanEnrollTest {

        @Test
        @DisplayName("DRAFT 상태 - 수강 신청 불가")
        void canEnroll_draft_false() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);

            // then
            assertThat(saved.canEnroll()).isFalse();
        }

        @Test
        @DisplayName("RECRUITING 상태 - 수강 신청 가능")
        void canEnroll_recruiting_true() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.open();

            // then
            assertThat(saved.canEnroll()).isTrue();
        }

        @Test
        @DisplayName("ONGOING 상태 + allowLateEnrollment=true - 수강 신청 가능")
        void canEnroll_ongoing_allowLate_true() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.open();
            saved.startClass();

            // then
            assertThat(saved.isAllowLateEnrollment()).isTrue();
            assertThat(saved.canEnroll()).isTrue();
        }

        @Test
        @DisplayName("ONGOING 상태 + allowLateEnrollment=false - 수강 신청 불가")
        void canEnroll_ongoing_notAllowLate_false() {
            // given
            CourseTime courseTime = CourseTime.create(
                    "중간 합류 불가 차수",
                    DeliveryType.OFFLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    30,
                    0,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    "{\"location\": \"서울\"}",
                    false,
                    1L
            );
            CourseTime saved = courseTimeRepository.save(courseTime);
            saved.open();
            saved.startClass();

            // then
            assertThat(saved.isAllowLateEnrollment()).isFalse();
            assertThat(saved.canEnroll()).isFalse();
        }

        @Test
        @DisplayName("CLOSED 상태 - 수강 신청 불가")
        void canEnroll_closed_false() {
            // given
            CourseTime saved = courseTimeRepository.save(testCourseTime);
            saved.open();
            saved.startClass();
            saved.close();

            // then
            assertThat(saved.canEnroll()).isFalse();
        }
    }

    @Nested
    @DisplayName("CourseTime 장소 정보 필수 여부 테스트")
    class LocationRequiredTest {

        @Test
        @DisplayName("ONLINE - 장소 정보 불필요")
        void requiresLocationInfo_online_false() {
            // given
            CourseTime courseTime = createTestCourseTime();
            courseTime = courseTimeRepository.save(courseTime);

            // then
            assertThat(courseTime.getDeliveryType()).isEqualTo(DeliveryType.ONLINE);
            assertThat(courseTime.requiresLocationInfo()).isFalse();
        }

        @Test
        @DisplayName("OFFLINE - 장소 정보 필수")
        void requiresLocationInfo_offline_true() {
            // given
            CourseTime courseTime = CourseTime.create(
                    "오프라인 차수",
                    DeliveryType.OFFLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    30,
                    0,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    "{\"location\": \"서울\"}",
                    false,
                    1L
            );
            courseTime = courseTimeRepository.save(courseTime);

            // then
            assertThat(courseTime.getDeliveryType()).isEqualTo(DeliveryType.OFFLINE);
            assertThat(courseTime.requiresLocationInfo()).isTrue();
        }

        @Test
        @DisplayName("BLENDED - 장소 정보 필수")
        void requiresLocationInfo_blended_true() {
            // given
            CourseTime courseTime = CourseTime.create(
                    "블렌디드 차수",
                    DeliveryType.BLENDED,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    30,
                    0,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    "{\"location\": \"부산\"}",
                    false,
                    1L
            );
            courseTime = courseTimeRepository.save(courseTime);

            // then
            assertThat(courseTime.getDeliveryType()).isEqualTo(DeliveryType.BLENDED);
            assertThat(courseTime.requiresLocationInfo()).isTrue();
        }
    }

    @Nested
    @DisplayName("CourseTime 통계 집계 쿼리 테스트")
    class StatsQueryTest {

        @Test
        @DisplayName("성공 - 상태별 차수 카운트")
        void countByTenantIdGroupByStatus_success() {
            // given
            createCourseTimeWithStatus(CourseTimeStatus.DRAFT);
            createCourseTimeWithStatus(CourseTimeStatus.DRAFT);
            createCourseTimeWithStatus(CourseTimeStatus.RECRUITING);
            createCourseTimeWithStatus(CourseTimeStatus.ONGOING);

            // when
            List<StatusCountProjection> result = courseTimeRepository.countByTenantIdGroupByStatus(1L);

            // then
            assertThat(result).hasSize(3);

            long draftCount = result.stream()
                    .filter(p -> "DRAFT".equals(p.getStatus()))
                    .mapToLong(StatusCountProjection::getCount)
                    .sum();
            long recruitingCount = result.stream()
                    .filter(p -> "RECRUITING".equals(p.getStatus()))
                    .mapToLong(StatusCountProjection::getCount)
                    .sum();
            long ongoingCount = result.stream()
                    .filter(p -> "ONGOING".equals(p.getStatus()))
                    .mapToLong(StatusCountProjection::getCount)
                    .sum();

            assertThat(draftCount).isEqualTo(2);
            assertThat(recruitingCount).isEqualTo(1);
            assertThat(ongoingCount).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 운영 방식별 차수 카운트")
        void countByTenantIdGroupByDeliveryType_success() {
            // given
            courseTimeRepository.save(createTestCourseTime()); // ONLINE
            courseTimeRepository.save(createTestCourseTime()); // ONLINE

            CourseTime offlineCourseTime = CourseTime.create(
                    "오프라인 차수",
                    DeliveryType.OFFLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    30,
                    0,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    "{\"location\": \"서울\"}",
                    true,
                    1L
            );
            courseTimeRepository.save(offlineCourseTime);

            // when
            List<TypeCountProjection> result = courseTimeRepository.countByTenantIdGroupByDeliveryType(1L);

            // then
            assertThat(result).hasSize(2);

            long onlineCount = result.stream()
                    .filter(p -> "ONLINE".equals(p.getType()))
                    .mapToLong(TypeCountProjection::getCount)
                    .sum();
            long offlineCount = result.stream()
                    .filter(p -> "OFFLINE".equals(p.getType()))
                    .mapToLong(TypeCountProjection::getCount)
                    .sum();

            assertThat(onlineCount).isEqualTo(2);
            assertThat(offlineCount).isEqualTo(1);
        }

        @Test
        @DisplayName("성공 - 무료/유료 차수 카운트")
        void countByTenantIdGroupByFree_success() {
            // given
            courseTimeRepository.save(createTestCourseTime()); // 유료

            courseTimeRepository.save(createFreeCourseTime());
            courseTimeRepository.save(createFreeCourseTime());

            // when
            List<BooleanCountProjection> result = courseTimeRepository.countByTenantIdGroupByFree(1L);

            // then
            assertThat(result).hasSize(2);

            long freeCount = result.stream()
                    .filter(p -> Boolean.TRUE.equals(p.getValue()))
                    .mapToLong(BooleanCountProjection::getCount)
                    .sum();
            long paidCount = result.stream()
                    .filter(p -> Boolean.FALSE.equals(p.getValue()))
                    .mapToLong(BooleanCountProjection::getCount)
                    .sum();

            assertThat(freeCount).isEqualTo(2);
            assertThat(paidCount).isEqualTo(1);
        }

        private CourseTime createFreeCourseTime() {
            return CourseTime.create(
                    "무료 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    null,
                    null,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    BigDecimal.ZERO,
                    true,
                    null,
                    true,
                    1L
            );
        }

        @Test
        @DisplayName("성공 - 전체 차수 카운트")
        void countByTenantId_success() {
            // given
            courseTimeRepository.save(createTestCourseTime());
            courseTimeRepository.save(createTestCourseTime());
            courseTimeRepository.save(createTestCourseTime());

            // when
            long count = courseTimeRepository.countByTenantId(1L);

            // then
            assertThat(count).isEqualTo(3);
        }

        @Test
        @DisplayName("성공 - 평균 정원 활용률")
        void getAverageCapacityUtilization_success() {
            // given
            CourseTime courseTime1 = createTestCourseTime(); // capacity=30
            courseTime1.incrementEnrollment(); // 10명 등록
            for (int i = 0; i < 9; i++) {
                courseTime1.incrementEnrollment();
            }
            courseTimeRepository.save(courseTime1); // 10/30 = 33.33%

            CourseTime courseTime2 = createTestCourseTime(); // capacity=30
            for (int i = 0; i < 20; i++) {
                courseTime2.incrementEnrollment();
            }
            courseTimeRepository.save(courseTime2); // 20/30 = 66.67%

            // when
            Double avgUtilization = courseTimeRepository.getAverageCapacityUtilization(1L);

            // then - (33.33 + 66.67) / 2 = 50%
            assertThat(avgUtilization).isNotNull();
            assertThat(avgUtilization).isBetween(49.0, 51.0);
        }

        @Test
        @DisplayName("성공 - 과정별 상태별 차수 카운트")
        void countByCmCourseIdGroupByStatus_success() {
            // given
            CourseTime ct1 = createTestCourseTime();
            ct1.linkCourse(100L, 1L);
            courseTimeRepository.save(ct1);

            CourseTime ct2 = createTestCourseTime();
            ct2.linkCourse(100L, 2L);
            ct2 = courseTimeRepository.save(ct2);
            ct2.open();
            courseTimeRepository.save(ct2);

            CourseTime ct3 = createTestCourseTime();
            ct3.linkCourse(200L, 1L); // 다른 과정
            courseTimeRepository.save(ct3);

            // when
            List<StatusCountProjection> result = courseTimeRepository.countByCmCourseIdGroupByStatus(100L, 1L);

            // then
            assertThat(result).hasSize(2);

            long totalCount = result.stream()
                    .mapToLong(StatusCountProjection::getCount)
                    .sum();
            assertThat(totalCount).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 과정별 전체 차수 카운트")
        void countByCmCourseIdAndTenantId_success() {
            // given
            CourseTime ct1 = createTestCourseTime();
            ct1.linkCourse(100L, 1L);
            courseTimeRepository.save(ct1);

            CourseTime ct2 = createTestCourseTime();
            ct2.linkCourse(100L, 2L);
            courseTimeRepository.save(ct2);

            CourseTime ct3 = createTestCourseTime();
            ct3.linkCourse(200L, 1L); // 다른 과정
            courseTimeRepository.save(ct3);

            // when
            long count = courseTimeRepository.countByCmCourseIdAndTenantId(100L, 1L);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 데이터 없을 때 빈 리스트 반환")
        void countByTenantIdGroupByStatus_emptyList() {
            // when
            List<StatusCountProjection> result = courseTimeRepository.countByTenantIdGroupByStatus(1L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("성공 - 데이터 없을 때 null 반환 (평균 활용률)")
        void getAverageCapacityUtilization_null() {
            // when
            Double avgUtilization = courseTimeRepository.getAverageCapacityUtilization(1L);

            // then
            assertThat(avgUtilization).isNull();
        }
    }
}
