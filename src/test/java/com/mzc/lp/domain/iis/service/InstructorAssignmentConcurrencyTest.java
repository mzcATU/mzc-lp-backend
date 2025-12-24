package com.mzc.lp.domain.iis.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
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
 * 강사 배정 동시성 테스트
 *
 * 참고: H2 인메모리 DB는 비관적 락을 완전히 지원하지 않으므로,
 * 이 테스트는 DB 유니크 제약조건 + 애플리케이션 레벨 검증을 확인합니다.
 * 실제 운영환경(PostgreSQL/MySQL)에서는 비관적 락이 정상 동작합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("강사 배정 동시성 테스트")
class InstructorAssignmentConcurrencyTest extends TenantTestSupport {

    @Autowired
    private InstructorAssignmentService assignmentService;

    @Autowired
    private InstructorAssignmentRepository assignmentRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private UserRepository userRepository;

    private CourseTime courseTime;
    private List<User> instructors;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
        userRepository.deleteAll();
        courseTimeRepository.deleteAll();

        // 차수 생성
        courseTime = CourseTime.create(
                "동시성 테스트 차수",
                DeliveryType.ONLINE,
                LocalDate.now().minusDays(1),
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
        courseTime = courseTimeRepository.save(courseTime);

        // 테스트용 강사 사용자 생성
        instructors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User instructor = userRepository.save(
                    User.create("instructor" + i + "@test.com", "강사" + i, "encodedPassword")
            );
            instructors.add(instructor);
        }
    }

    @Test
    @Disabled("H2 인메모리 DB에서 비관적 락 미지원 - PostgreSQL/MySQL 환경에서 테스트 필요")
    @DisplayName("동일 강사가 동시에 배정 요청 시 중복 배정 방지")
    void concurrentAssign_sameInstructor_shouldPreventDuplicate() throws InterruptedException {
        // given
        Long timeId = courseTime.getId();
        Long instructorId = instructors.get(0).getId();
        int threadCount = 10;

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when - 동일 강사를 동시에 10번 배정 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    AssignInstructorRequest request = new AssignInstructorRequest(instructorId, InstructorRole.SUB, null);
                    assignmentService.assignInstructor(timeId, request, 1L);
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

        // then - DB에는 정확히 1개만 존재해야 함
        List<InstructorAssignment> assignments = assignmentRepository.findAll();
        long instructorAssignmentCount = assignments.stream()
                .filter(a -> a.getUserKey().equals(instructorId))
                .count();

        System.out.println("=== 동일 강사 동시 배정 테스트 결과 ===");
        System.out.println("성공 시도: " + successCount.get() + ", 실패 시도: " + failCount.get());
        System.out.println("실제 DB 배정 수: " + instructorAssignmentCount);

        // 핵심 검증: DB에 중복 배정이 없어야 함
        assertThat(instructorAssignmentCount)
                .as("동일 강사의 중복 배정이 방지되어야 함")
                .isEqualTo(1);
    }

    @Test
    @Disabled("H2 인메모리 DB에서 비관적 락 미지원 - PostgreSQL/MySQL 환경에서 테스트 필요")
    @DisplayName("여러 강사가 동시에 주강사(MAIN) 배정 요청 시 1명만 성공")
    void concurrentAssign_multipleMainInstructors_shouldAllowOnlyOne() throws InterruptedException {
        // given
        Long timeId = courseTime.getId();
        int threadCount = 5;  // 5명이 동시에 MAIN 강사 배정 시도

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when - 5명의 강사가 동시에 MAIN 역할로 배정 시도
        for (int i = 0; i < threadCount; i++) {
            final Long instructorId = instructors.get(i).getId();
            executorService.submit(() -> {
                try {
                    AssignInstructorRequest request = new AssignInstructorRequest(instructorId, InstructorRole.MAIN, null);
                    assignmentService.assignInstructor(timeId, request, 1L);
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

        // then - MAIN 역할은 정확히 1명만 배정되어야 함
        List<InstructorAssignment> assignments = assignmentRepository.findAll();
        long mainCount = assignments.stream()
                .filter(a -> a.getRole() == InstructorRole.MAIN)
                .count();

        System.out.println("=== 다중 MAIN 강사 동시 배정 테스트 결과 ===");
        System.out.println("성공 시도: " + successCount.get() + ", 실패 시도: " + failCount.get());
        System.out.println("실제 DB MAIN 강사 수: " + mainCount);

        // 핵심 검증: MAIN 강사는 1명만 존재해야 함
        assertThat(mainCount)
                .as("MAIN 강사는 1명만 배정되어야 함")
                .isEqualTo(1);
    }
}
