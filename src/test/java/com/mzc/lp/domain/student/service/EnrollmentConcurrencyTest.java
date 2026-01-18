package com.mzc.lp.domain.student.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 수강신청 동시성 테스트
 *
 * 참고: H2 인메모리 DB는 비관적 락을 완전히 지원하지 않으므로,
 * 이 테스트는 DB 유니크 제약조건 + 애플리케이션 레벨 검증을 확인합니다.
 * 실제 운영환경(PostgreSQL/MySQL)에서는 비관적 락이 정상 동작합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("수강신청 동시성 테스트")
class EnrollmentConcurrencyTest extends TenantTestSupport {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    private CourseTime courseTime;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseTimeRepository.deleteAll();

        // 정원 5명인 차수 생성
        courseTime = CourseTime.create(
                "동시성 테스트 차수",
                DeliveryType.ONLINE,
                DurationType.FIXED,
                LocalDate.now().minusDays(1),  // 과거 날짜이므로 RECRUITING 상태로 자동 생성
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                null,
                5,  // 정원 5명
                0,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                null,
                1L
        );
        // enrollStartDate가 과거이므로 이미 RECRUITING 상태
        courseTime = courseTimeRepository.save(courseTime);
    }

    @Test
    @Disabled("H2 인메모리 DB에서 비관적 락 미지원 - PostgreSQL/MySQL 환경에서 테스트 필요")
    @DisplayName("동일 사용자가 동시에 수강신청 시 중복 등록 방지")
    void concurrentEnroll_sameUser_shouldPreventDuplicate() throws InterruptedException {
        // given
        Long userId = 100L;
        Long courseTimeId = courseTime.getId();
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when - 동일 사용자가 동시에 10번 수강신청 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    enrollmentService.enroll(courseTimeId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then - DB에는 정확히 1개만 존재해야 함 (비관적 락 또는 DB 제약조건)
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        long userEnrollmentCount = enrollments.stream()
                .filter(e -> e.getUserId().equals(userId))
                .count();

        System.out.println("=== 동일 사용자 동시 수강신청 테스트 결과 ===");
        System.out.println("성공 시도: " + successCount.get() + ", 실패 시도: " + failCount.get());
        System.out.println("실제 DB 등록 수: " + userEnrollmentCount);

        // 핵심 검증: DB에 중복 등록이 없어야 함
        assertThat(userEnrollmentCount)
                .as("동일 사용자의 중복 등록이 방지되어야 함")
                .isEqualTo(1);
    }

    @Test
    @DisplayName("여러 사용자가 동시에 수강신청 시 정원 초과 방지")
    void concurrentEnroll_multipleUsers_shouldRespectCapacity() throws InterruptedException {
        // given
        Long courseTimeId = courseTime.getId();
        int threadCount = 20;  // 20명이 동시에 신청 (정원 5명)

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 20명의 사용자가 동시에 수강신청 시도
        for (int i = 0; i < threadCount; i++) {
            final Long userId = (long) (1000 + i);
            executorService.submit(() -> {
                try {
                    enrollmentService.enroll(courseTimeId, userId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then - 정원(5명)을 초과하지 않아야 함
        List<Enrollment> enrollments = enrollmentRepository.findAll();
        CourseTime updatedCourseTime = courseTimeRepository.findById(courseTimeId).orElseThrow();

        assertThat(enrollments.size()).isLessThanOrEqualTo(5);
        assertThat(updatedCourseTime.getCurrentEnrollment()).isLessThanOrEqualTo(5);

        System.out.println("=== 다중 사용자 동시 수강신청 테스트 결과 ===");
        System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
        System.out.println("실제 등록 수: " + enrollments.size());
        System.out.println("현재 수강인원: " + updatedCourseTime.getCurrentEnrollment());
    }
}
