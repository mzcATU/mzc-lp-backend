package com.mzc.lp.domain.iis.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.AssignmentHistoryResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorAvailabilityResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorDetailStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatResponse;
import com.mzc.lp.domain.iis.dto.response.InstructorStatisticsResponse;
import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.entity.AssignmentHistory;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.exception.CannotModifyInactiveAssignmentException;
import com.mzc.lp.domain.iis.exception.InstructorAlreadyAssignedException;
import com.mzc.lp.domain.iis.exception.InstructorAssignmentNotFoundException;
import com.mzc.lp.domain.iis.exception.InstructorScheduleConflictException;
import com.mzc.lp.domain.iis.exception.MainInstructorAlreadyExistsException;
import com.mzc.lp.domain.iis.repository.AssignmentHistoryRepository;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.student.service.EnrollmentStatsService;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstructorAssignmentServiceTest extends TenantTestSupport {

    @InjectMocks
    private InstructorAssignmentServiceImpl assignmentService;

    @Mock
    private InstructorAssignmentRepository assignmentRepository;

    @Mock
    private AssignmentHistoryRepository historyRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private EnrollmentStatsService enrollmentStatsService;

    private static final Long TENANT_ID = 1L;
    private static final Long TIME_ID = 100L;
    private static final Long USER_ID = 10L;
    private static final Long OPERATOR_ID = 1L;

    private InstructorAssignment createTestAssignment(Long id, Long userId, Long timeId, InstructorRole role) {
        InstructorAssignment assignment = InstructorAssignment.create(userId, timeId, role, OPERATOR_ID);
        // Reflection으로 ID 설정 (BaseEntity까지 올라가야 함)
        try {
            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(assignment, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return assignment;
    }

    private User createTestUser(Long id, String name, String email) {
        User user = User.create(email, name, "encodedPassword");
        try {
            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return user;
    }

    private CourseTime createTestCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "테스트 차수",
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
        // 테스트용 tenantId 설정 (리플렉션 사용)
        try {
            var tenantIdField = com.mzc.lp.common.entity.TenantEntity.class.getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(courseTime, TENANT_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return courseTime;
    }

    // ==================== 배정 테스트 ====================

    @Nested
    @DisplayName("assignInstructor - 강사 배정")
    class AssignInstructor {

        @Test
        @DisplayName("성공 - MAIN 강사 배정")
        void assignInstructor_success_main() {
            // given
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.MAIN, false);
            InstructorAssignment saved = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            CourseTime courseTime = createTestCourseTime();

            // Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            given(assignmentRepository.findActiveByTimeKeyAndRole(TIME_ID, TENANT_ID, InstructorRole.MAIN))
                    .willReturn(Optional.empty());
            // 일정 충돌 없음
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of());
            given(assignmentRepository.save(any())).willReturn(saved);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            // when
            InstructorAssignmentResponse response = assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID);

            // then
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.userName()).isEqualTo("강사");
            assertThat(response.userEmail()).isEqualTo("instructor@example.com");
            assertThat(response.role()).isEqualTo(InstructorRole.MAIN);
            verify(historyRepository).save(any());
        }

        @Test
        @DisplayName("성공 - SUB 강사 배정")
        void assignInstructor_success_sub() {
            // given
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.SUB, false);
            InstructorAssignment saved = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.SUB);
            User user = createTestUser(USER_ID, "부강사", "sub@example.com");
            CourseTime courseTime = createTestCourseTime();

            // Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            // 일정 충돌 없음
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of());
            given(assignmentRepository.save(any())).willReturn(saved);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            // when
            InstructorAssignmentResponse response = assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID);

            // then
            assertThat(response.role()).isEqualTo(InstructorRole.SUB);
        }

        @Test
        @DisplayName("실패 - 중복 배정")
        void assignInstructor_fail_alreadyAssigned() {
            // given
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.MAIN, false);
            CourseTime courseTime = createTestCourseTime();

            // Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID))
                    .isInstanceOf(InstructorAlreadyAssignedException.class);
        }

        @Test
        @DisplayName("실패 - 주강사 중복")
        void assignInstructor_fail_mainAlreadyExists() {
            // given
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.MAIN, false);
            InstructorAssignment existingMain = createTestAssignment(99L, 999L, TIME_ID, InstructorRole.MAIN);
            CourseTime courseTime = createTestCourseTime();

            // Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            given(assignmentRepository.findActiveByTimeKeyAndRole(TIME_ID, TENANT_ID, InstructorRole.MAIN))
                    .willReturn(Optional.of(existingMain));

            // when & then
            assertThatThrownBy(() -> assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID))
                    .isInstanceOf(MainInstructorAlreadyExistsException.class);
        }

        @Test
        @DisplayName("실패 - 일정 충돌 (forceAssign=false)")
        void assignInstructor_fail_scheduleConflict() {
            // given
            Long conflictingTimeId = 200L;
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.SUB, false);
            CourseTime targetCourseTime = createTestCourseTime();
            CourseTime conflictingCourseTime = createTestCourseTimeWithId(conflictingTimeId,
                    LocalDate.now().plusDays(10), LocalDate.now().plusDays(20)); // 겹치는 기간

            // 기존 배정 (다른 차수에 이미 배정되어 있음)
            InstructorAssignment existingAssignment = createTestAssignment(50L, USER_ID, conflictingTimeId, InstructorRole.MAIN);

            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(targetCourseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            // 기존 ACTIVE 배정 존재
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of(existingAssignment));
            // 기간 겹치는 CourseTime 조회
            given(courseTimeRepository.findByIdInAndDateRangeOverlap(
                    eq(List.of(conflictingTimeId)),
                    any(LocalDate.class),
                    any(LocalDate.class)
            )).willReturn(List.of(conflictingCourseTime));

            // when & then
            assertThatThrownBy(() -> assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID))
                    .isInstanceOf(InstructorScheduleConflictException.class)
                    .satisfies(ex -> {
                        InstructorScheduleConflictException conflictException = (InstructorScheduleConflictException) ex;
                        assertThat(conflictException.getConflicts()).hasSize(1);
                        assertThat(conflictException.getConflicts().get(0).conflictingTimeId()).isEqualTo(conflictingTimeId);
                    });
        }

        @Test
        @DisplayName("성공 - 일정 충돌 무시 (forceAssign=true)")
        void assignInstructor_success_forceAssignWithConflict() {
            // given
            Long conflictingTimeId = 200L;
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.SUB, true); // forceAssign=true
            InstructorAssignment saved = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.SUB);
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            CourseTime targetCourseTime = createTestCourseTime();

            // 기존 배정 (다른 차수에 이미 배정되어 있음)
            InstructorAssignment existingAssignment = createTestAssignment(50L, USER_ID, conflictingTimeId, InstructorRole.MAIN);

            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(targetCourseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            // forceAssign=true이므로 충돌 검사 스킵 -> findActiveByUserKey 호출 안됨
            given(assignmentRepository.save(any())).willReturn(saved);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            // when
            InstructorAssignmentResponse response = assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID);

            // then
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(USER_ID);
        }
    }

    private CourseTime createTestCourseTimeWithId(Long id, LocalDate classStartDate, LocalDate classEndDate) {
        CourseTime courseTime = CourseTime.create(
                "충돌 테스트 차수",
                DeliveryType.ONLINE,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(7),
                classStartDate,
                classEndDate,
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
        try {
            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(courseTime, id);
            var tenantIdField = com.mzc.lp.common.entity.TenantEntity.class.getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(courseTime, TENANT_ID);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return courseTime;
    }

    // ==================== 조회 테스트 ====================

    @Nested
    @DisplayName("getAssignment - 배정 조회")
    class GetAssignment {

        @Test
        @DisplayName("성공 - 배정 단건 조회")
        void getAssignment_success() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            // when
            InstructorAssignmentResponse response = assignmentService.getAssignment(1L);

            // then
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.userId()).isEqualTo(USER_ID);
            assertThat(response.userName()).isEqualTo("강사");
            assertThat(response.userEmail()).isEqualTo("instructor@example.com");
        }

        @Test
        @DisplayName("실패 - 배정 없음")
        void getAssignment_fail_notFound() {
            // given
            given(assignmentRepository.findByIdAndTenantId(999L, TENANT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> assignmentService.getAssignment(999L))
                    .isInstanceOf(InstructorAssignmentNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getInstructorsByTimeId - 차수별 강사 목록 조회")
    class GetInstructorsByTimeId {

        @Test
        @DisplayName("성공 - 전체 조회")
        void getInstructorsByTimeId_success_all() {
            // given
            List<InstructorAssignment> assignments = List.of(
                    createTestAssignment(1L, 10L, TIME_ID, InstructorRole.MAIN),
                    createTestAssignment(2L, 20L, TIME_ID, InstructorRole.SUB)
            );
            List<User> users = List.of(
                    createTestUser(10L, "주강사", "main@example.com"),
                    createTestUser(20L, "부강사", "sub@example.com")
            );

            given(assignmentRepository.findByTimeKeyAndTenantId(TIME_ID, TENANT_ID)).willReturn(assignments);
            given(userRepository.findAllById(List.of(10L, 20L))).willReturn(users);

            // when
            List<InstructorAssignmentResponse> response = assignmentService.getInstructorsByTimeId(TIME_ID, null);

            // then
            assertThat(response).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 상태별 조회")
        void getInstructorsByTimeId_success_byStatus() {
            // given
            List<InstructorAssignment> assignments = List.of(
                    createTestAssignment(1L, 10L, TIME_ID, InstructorRole.MAIN)
            );
            List<User> users = List.of(createTestUser(10L, "강사", "instructor@example.com"));

            given(assignmentRepository.findByTimeKeyAndTenantIdAndStatus(TIME_ID, TENANT_ID, AssignmentStatus.ACTIVE))
                    .willReturn(assignments);
            given(userRepository.findAllById(List.of(10L))).willReturn(users);

            // when
            List<InstructorAssignmentResponse> response = assignmentService.getInstructorsByTimeId(
                    TIME_ID, AssignmentStatus.ACTIVE);

            // then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAssignmentsByUserId - 강사별 배정 목록 조회")
    class GetAssignmentsByUserId {

        @Test
        @DisplayName("성공 - 페이징 조회")
        void getAssignmentsByUserId_success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<InstructorAssignment> assignments = List.of(
                    createTestAssignment(1L, USER_ID, 100L, InstructorRole.MAIN),
                    createTestAssignment(2L, USER_ID, 200L, InstructorRole.SUB)
            );
            Page<InstructorAssignment> page = new PageImpl<>(assignments, pageable, 2);
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");

            given(assignmentRepository.findByUserKeyAndTenantId(USER_ID, TENANT_ID, pageable)).willReturn(page);
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            // when
            Page<InstructorAssignmentResponse> response = assignmentService.getAssignmentsByUserId(
                    USER_ID, null, pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
            assertThat(response.getTotalElements()).isEqualTo(2);
        }
    }

    // ==================== 역할 변경 테스트 ====================

    @Nested
    @DisplayName("updateRole - 역할 변경")
    class UpdateRole {

        @Test
        @DisplayName("성공 - SUB에서 MAIN으로 변경")
        void updateRole_success_subToMain() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.SUB);
            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.MAIN, "주강사 승격");
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            CourseTime courseTime = createTestCourseTime();

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
            // MAIN으로 변경 시 Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.findActiveByTimeKeyAndRole(TIME_ID, TENANT_ID, InstructorRole.MAIN))
                    .willReturn(Optional.empty());
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));

            // when
            InstructorAssignmentResponse response = assignmentService.updateRole(1L, request, OPERATOR_ID);

            // then
            assertThat(response.role()).isEqualTo(InstructorRole.MAIN);
            verify(historyRepository).save(any());
        }

        @Test
        @DisplayName("실패 - 비활성 배정 수정")
        void updateRole_fail_inactive() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.SUB);
            assignment.cancel();
            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.MAIN, "주강사 승격");

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));

            // when & then
            assertThatThrownBy(() -> assignmentService.updateRole(1L, request, OPERATOR_ID))
                    .isInstanceOf(CannotModifyInactiveAssignmentException.class);
        }

        @Test
        @DisplayName("실패 - 주강사 중복")
        void updateRole_fail_mainAlreadyExists() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.SUB);
            InstructorAssignment existingMain = createTestAssignment(99L, 999L, TIME_ID, InstructorRole.MAIN);
            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.MAIN, "주강사 승격");
            CourseTime courseTime = createTestCourseTime();

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
            // MAIN으로 변경 시 Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.findActiveByTimeKeyAndRole(TIME_ID, TENANT_ID, InstructorRole.MAIN))
                    .willReturn(Optional.of(existingMain));

            // when & then
            assertThatThrownBy(() -> assignmentService.updateRole(1L, request, OPERATOR_ID))
                    .isInstanceOf(MainInstructorAlreadyExistsException.class);
        }
    }

    // ==================== 교체 테스트 ====================

    @Nested
    @DisplayName("replaceInstructor - 강사 교체")
    class ReplaceInstructor {

        @Test
        @DisplayName("성공 - 강사 교체")
        void replaceInstructor_success() {
            // given
            Long newUserId = 20L;
            InstructorAssignment oldAssignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            InstructorAssignment newAssignment = createTestAssignment(2L, newUserId, TIME_ID, InstructorRole.MAIN);
            ReplaceInstructorRequest request = new ReplaceInstructorRequest(newUserId, InstructorRole.MAIN, "교체 사유");
            User newUser = createTestUser(newUserId, "새강사", "new@example.com");
            CourseTime courseTime = createTestCourseTime();

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(oldAssignment));
            // Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, newUserId, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            given(assignmentRepository.save(any())).willReturn(newAssignment);
            given(userRepository.findById(newUserId)).willReturn(Optional.of(newUser));

            // when
            InstructorAssignmentResponse response = assignmentService.replaceInstructor(1L, request, OPERATOR_ID);

            // then
            assertThat(response.userId()).isEqualTo(newUserId);
            assertThat(response.userName()).isEqualTo("새강사");
            assertThat(oldAssignment.getStatus()).isEqualTo(AssignmentStatus.REPLACED);
        }

        @Test
        @DisplayName("실패 - 새 강사 중복 배정")
        void replaceInstructor_fail_newUserAlreadyAssigned() {
            // given
            Long newUserId = 20L;
            InstructorAssignment oldAssignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            ReplaceInstructorRequest request = new ReplaceInstructorRequest(newUserId, InstructorRole.MAIN, "교체 사유");
            CourseTime courseTime = createTestCourseTime();

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(oldAssignment));
            // Race Condition 방지를 위한 비관적 락
            given(courseTimeRepository.findByIdWithLock(TIME_ID)).willReturn(Optional.of(courseTime));
            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, newUserId, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> assignmentService.replaceInstructor(1L, request, OPERATOR_ID))
                    .isInstanceOf(InstructorAlreadyAssignedException.class);
        }
    }

    // ==================== 취소 테스트 ====================

    @Nested
    @DisplayName("cancelAssignment - 배정 취소")
    class CancelAssignment {

        @Test
        @DisplayName("성공 - 배정 취소")
        void cancelAssignment_success() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            CancelAssignmentRequest request = new CancelAssignmentRequest("취소 사유");

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));

            // when
            assignmentService.cancelAssignment(1L, request, OPERATOR_ID, false);

            // then
            assertThat(assignment.getStatus()).isEqualTo(AssignmentStatus.CANCELLED);
            verify(historyRepository).save(any());
        }

        @Test
        @DisplayName("실패 - 비활성 배정 취소")
        void cancelAssignment_fail_inactive() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            assignment.cancel();
            CancelAssignmentRequest request = new CancelAssignmentRequest("취소 사유");

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));

            // when & then
            assertThatThrownBy(() -> assignmentService.cancelAssignment(1L, request, OPERATOR_ID, false))
                    .isInstanceOf(CannotModifyInactiveAssignmentException.class);
        }
    }

    // ==================== 이력 조회 테스트 ====================

    @Nested
    @DisplayName("getAssignmentHistories - 이력 조회")
    class GetAssignmentHistories {

        @Test
        @DisplayName("성공 - 전체 이력 조회")
        void getAssignmentHistories_success_all() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            List<AssignmentHistory> histories = List.of(
                    AssignmentHistory.ofAssign(1L, InstructorRole.MAIN, OPERATOR_ID),
                    AssignmentHistory.ofRoleChange(1L, InstructorRole.MAIN, InstructorRole.SUB, "역할 변경", OPERATOR_ID)
            );

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
            given(historyRepository.findByAssignmentIdOrderByChangedAtDesc(1L)).willReturn(histories);

            // when
            List<AssignmentHistoryResponse> response = assignmentService.getAssignmentHistories(1L, null);

            // then
            assertThat(response).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 액션 타입 필터링 조회")
        void getAssignmentHistories_success_byAction() {
            // given
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            List<AssignmentHistory> histories = List.of(
                    AssignmentHistory.ofRoleChange(1L, InstructorRole.MAIN, InstructorRole.SUB, "역할 변경", OPERATOR_ID)
            );

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
            given(historyRepository.findByAssignmentIdAndActionOrderByChangedAtDesc(1L, AssignmentAction.ROLE_CHANGE))
                    .willReturn(histories);

            // when
            List<AssignmentHistoryResponse> response = assignmentService.getAssignmentHistories(1L, AssignmentAction.ROLE_CHANGE);

            // then
            assertThat(response).hasSize(1);
            assertThat(response.get(0).action()).isEqualTo(AssignmentAction.ROLE_CHANGE);
        }

        @Test
        @DisplayName("실패 - 배정 없음")
        void getAssignmentHistories_fail_notFound() {
            // given
            given(assignmentRepository.findByIdAndTenantId(999L, TENANT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> assignmentService.getAssignmentHistories(999L, null))
                    .isInstanceOf(InstructorAssignmentNotFoundException.class);
        }
    }

    // ==================== TS 모듈 연동 테스트 ====================

    @Nested
    @DisplayName("existsMainInstructor - MAIN 강사 존재 확인")
    class ExistsMainInstructor {

        @Test
        @DisplayName("성공 - MAIN 강사 존재")
        void existsMainInstructor_success_exists() {
            // given
            given(assignmentRepository.existsActiveMainInstructor(TIME_ID, TENANT_ID)).willReturn(true);

            // when
            boolean result = assignmentService.existsMainInstructor(TIME_ID);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("성공 - MAIN 강사 없음")
        void existsMainInstructor_success_notExists() {
            // given
            given(assignmentRepository.existsActiveMainInstructor(TIME_ID, TENANT_ID)).willReturn(false);

            // when
            boolean result = assignmentService.existsMainInstructor(TIME_ID);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getInstructorsByTimeIds - 여러 차수의 강사 조회")
    class GetInstructorsByTimeIds {

        @Test
        @DisplayName("성공 - 여러 차수 강사 조회")
        void getInstructorsByTimeIds_success() {
            // given
            List<Long> timeIds = List.of(100L, 200L);
            List<InstructorAssignment> assignments = List.of(
                    createTestAssignment(1L, 10L, 100L, InstructorRole.MAIN),
                    createTestAssignment(2L, 20L, 100L, InstructorRole.SUB),
                    createTestAssignment(3L, 30L, 200L, InstructorRole.MAIN)
            );
            List<User> users = List.of(
                    createTestUser(10L, "강사1", "instructor1@example.com"),
                    createTestUser(20L, "강사2", "instructor2@example.com"),
                    createTestUser(30L, "강사3", "instructor3@example.com")
            );

            given(assignmentRepository.findActiveByTimeKeyIn(timeIds, TENANT_ID)).willReturn(assignments);
            given(userRepository.findAllById(List.of(10L, 20L, 30L))).willReturn(users);

            // when
            Map<Long, List<InstructorAssignmentResponse>> result = assignmentService.getInstructorsByTimeIds(timeIds);

            // then
            assertThat(result).hasSize(2);
            assertThat(result.get(100L)).hasSize(2);
            assertThat(result.get(200L)).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 빈 목록")
        void getInstructorsByTimeIds_success_empty() {
            // given
            List<Long> timeIds = List.of();

            // when
            Map<Long, List<InstructorAssignmentResponse>> result = assignmentService.getInstructorsByTimeIds(timeIds);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ==================== 통계 API 테스트 ====================

    @Nested
    @DisplayName("getStatistics - 전체 통계 조회")
    class GetStatistics {

        @Test
        @DisplayName("성공 - 전체 통계 조회")
        void getStatistics_success() {
            // given
            given(assignmentRepository.countByTenantId(TENANT_ID)).willReturn(50L);
            given(assignmentRepository.countByTenantIdAndStatus(TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(35L);

            List<Object[]> roleStats = List.of(
                    new Object[]{InstructorRole.MAIN, 20L},
                    new Object[]{InstructorRole.SUB, 15L}
            );
            given(assignmentRepository.countGroupByRole(TENANT_ID)).willReturn(roleStats);

            List<Object[]> statusStats = List.of(
                    new Object[]{AssignmentStatus.ACTIVE, 35L},
                    new Object[]{AssignmentStatus.CANCELLED, 10L},
                    new Object[]{AssignmentStatus.REPLACED, 5L}
            );
            given(assignmentRepository.countGroupByStatus(TENANT_ID)).willReturn(statusStats);

            List<Object[]> instructorStats = List.of(
                    new Object[]{10L, 5L, 3L, 2L},
                    new Object[]{20L, 3L, 1L, 2L}
            );
            given(assignmentRepository.getInstructorStatistics(TENANT_ID)).willReturn(instructorStats);

            List<User> users = List.of(
                    createTestUser(10L, "강사1", "instructor1@example.com"),
                    createTestUser(20L, "강사2", "instructor2@example.com")
            );
            given(userRepository.findAllById(List.of(10L, 20L))).willReturn(users);

            // when
            InstructorStatisticsResponse result = assignmentService.getStatistics();

            // then
            assertThat(result.totalAssignments()).isEqualTo(50L);
            assertThat(result.activeAssignments()).isEqualTo(35L);
            assertThat(result.byRole().get(InstructorRole.MAIN)).isEqualTo(20L);
            assertThat(result.byRole().get(InstructorRole.SUB)).isEqualTo(15L);
            assertThat(result.byStatus().get(AssignmentStatus.ACTIVE)).isEqualTo(35L);
            assertThat(result.instructorStats()).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 데이터 없음")
        void getStatistics_success_noData() {
            // given
            given(assignmentRepository.countByTenantId(TENANT_ID)).willReturn(0L);
            given(assignmentRepository.countByTenantIdAndStatus(TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(0L);
            given(assignmentRepository.countGroupByRole(TENANT_ID)).willReturn(List.of());
            given(assignmentRepository.countGroupByStatus(TENANT_ID)).willReturn(List.of());
            given(assignmentRepository.getInstructorStatistics(TENANT_ID)).willReturn(List.of());
            given(userRepository.findAllById(List.of())).willReturn(List.of());

            // when
            InstructorStatisticsResponse result = assignmentService.getStatistics();

            // then
            assertThat(result.totalAssignments()).isEqualTo(0L);
            assertThat(result.activeAssignments()).isEqualTo(0L);
            assertThat(result.byRole().get(InstructorRole.MAIN)).isEqualTo(0L);
            assertThat(result.instructorStats()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getInstructorStatistics - 강사 개인 통계 조회")
    class GetInstructorStatistics {

        @Test
        @DisplayName("성공 - 강사 개인 통계 조회")
        void getInstructorStatistics_success() {
            // given
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            Object[] stats = new Object[]{5L, 3L, 2L};

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(assignmentRepository.getInstructorStatisticsByUserId(TENANT_ID, USER_ID)).willReturn(List.<Object[]>of(stats));

            // when
            InstructorStatResponse result = assignmentService.getInstructorStatistics(USER_ID);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.userName()).isEqualTo("강사");
            assertThat(result.totalCount()).isEqualTo(5L);
            assertThat(result.mainCount()).isEqualTo(3L);
            assertThat(result.subCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("성공 - 배정 없는 강사")
        void getInstructorStatistics_success_noAssignment() {
            // given
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            Object[] stats = new Object[]{null, null, null};

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(assignmentRepository.getInstructorStatisticsByUserId(TENANT_ID, USER_ID)).willReturn(List.<Object[]>of(stats));

            // when
            InstructorStatResponse result = assignmentService.getInstructorStatistics(USER_ID);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.userName()).isEqualTo("강사");
            assertThat(result.totalCount()).isEqualTo(0L);
            assertThat(result.mainCount()).isEqualTo(0L);
            assertThat(result.subCount()).isEqualTo(0L);
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 사용자")
        void getInstructorStatistics_success_userNotFound() {
            // given
            Object[] stats = new Object[]{null, null, null};

            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());
            given(assignmentRepository.getInstructorStatisticsByUserId(TENANT_ID, USER_ID)).willReturn(List.<Object[]>of(stats));

            // when
            InstructorStatResponse result = assignmentService.getInstructorStatistics(USER_ID);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.userName()).isNull();
            assertThat(result.totalCount()).isEqualTo(0L);
        }
    }

    // ==================== 기간 필터링 통계 테스트 ====================

    @Nested
    @DisplayName("getStatistics (기간 필터링) - 전체 통계 조회")
    class GetStatisticsWithDateRange {

        @Test
        @DisplayName("성공 - 기간 필터링 통계 조회")
        void getStatistics_withDateRange_success() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            given(assignmentRepository.countByTenantIdAndDateRange(TENANT_ID, startDate, endDate)).willReturn(30L);
            given(assignmentRepository.countByTenantIdAndStatusAndDateRange(TENANT_ID, AssignmentStatus.ACTIVE, startDate, endDate)).willReturn(20L);

            List<Object[]> roleStats = List.of(
                    new Object[]{InstructorRole.MAIN, 15L},
                    new Object[]{InstructorRole.SUB, 15L}
            );
            given(assignmentRepository.countGroupByRoleAndDateRange(TENANT_ID, startDate, endDate)).willReturn(roleStats);

            List<Object[]> statusStats = List.of(
                    new Object[]{AssignmentStatus.ACTIVE, 20L},
                    new Object[]{AssignmentStatus.CANCELLED, 5L},
                    new Object[]{AssignmentStatus.REPLACED, 5L}
            );
            given(assignmentRepository.countGroupByStatusAndDateRange(TENANT_ID, startDate, endDate)).willReturn(statusStats);

            Object[] instructorStat = new Object[]{10L, 3L, 2L, 1L};
            List<Object[]> instructorStats = List.<Object[]>of(instructorStat);
            given(assignmentRepository.getInstructorStatisticsWithDateRange(TENANT_ID, startDate, endDate)).willReturn(instructorStats);

            List<User> users = List.of(createTestUser(10L, "강사1", "instructor1@example.com"));
            given(userRepository.findAllById(List.of(10L))).willReturn(users);

            // when
            InstructorStatisticsResponse result = assignmentService.getStatistics(startDate, endDate);

            // then
            assertThat(result.totalAssignments()).isEqualTo(30L);
            assertThat(result.activeAssignments()).isEqualTo(20L);
            assertThat(result.byRole().get(InstructorRole.MAIN)).isEqualTo(15L);
            assertThat(result.instructorStats()).hasSize(1);
        }

        @Test
        @DisplayName("성공 - 기간 미지정시 전체 조회")
        void getStatistics_withoutDateRange_success() {
            // given
            given(assignmentRepository.countByTenantId(TENANT_ID)).willReturn(50L);
            given(assignmentRepository.countByTenantIdAndStatus(TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(35L);
            given(assignmentRepository.countGroupByRole(TENANT_ID)).willReturn(List.of());
            given(assignmentRepository.countGroupByStatus(TENANT_ID)).willReturn(List.of());
            given(assignmentRepository.getInstructorStatistics(TENANT_ID)).willReturn(List.of());
            given(userRepository.findAllById(List.of())).willReturn(List.of());

            // when
            InstructorStatisticsResponse result = assignmentService.getStatistics(null, null);

            // then
            assertThat(result.totalAssignments()).isEqualTo(50L);
            assertThat(result.activeAssignments()).isEqualTo(35L);
        }
    }

    @Nested
    @DisplayName("getInstructorDetailStatistics - 강사 상세 통계 조회")
    class GetInstructorDetailStatistics {

        @Test
        @DisplayName("성공 - 강사 상세 통계 조회 (차수별 통계 포함)")
        void getInstructorDetailStatistics_success() {
            // given
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            Object[] stats = new Object[]{5L, 3L, 2L};

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(assignmentRepository.getInstructorStatisticsByUserId(TENANT_ID, USER_ID)).willReturn(List.<Object[]>of(stats));

            // 배정 목록
            InstructorAssignment assignment = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of(assignment));

            // CourseTime 정보
            // courseTimeRepository.findAllById 모킹 필요

            // when
            InstructorDetailStatResponse result = assignmentService.getInstructorDetailStatistics(USER_ID, null, null);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.userName()).isEqualTo("강사");
            assertThat(result.totalCount()).isEqualTo(5L);
            assertThat(result.mainCount()).isEqualTo(3L);
            assertThat(result.subCount()).isEqualTo(2L);
        }

        @Test
        @DisplayName("성공 - 기간 필터링 적용")
        void getInstructorDetailStatistics_withDateRange_success() {
            // given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            Object[] stats = new Object[]{3L, 2L, 1L};

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(assignmentRepository.getInstructorStatisticsByUserIdAndDateRange(TENANT_ID, USER_ID, startDate, endDate))
                    .willReturn(List.<Object[]>of(stats));
            given(assignmentRepository.findActiveByUserKeyAndDateRange(TENANT_ID, USER_ID, startDate, endDate))
                    .willReturn(List.of());

            // when
            InstructorDetailStatResponse result = assignmentService.getInstructorDetailStatistics(USER_ID, startDate, endDate);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.totalCount()).isEqualTo(3L);
            assertThat(result.courseTimeStats()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 배정 없는 강사")
        void getInstructorDetailStatistics_noAssignment_success() {
            // given
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");
            Object[] stats = new Object[]{null, null, null};

            given(userRepository.findById(USER_ID)).willReturn(Optional.of(user));
            given(assignmentRepository.getInstructorStatisticsByUserId(TENANT_ID, USER_ID)).willReturn(List.<Object[]>of(stats));
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of());

            // when
            InstructorDetailStatResponse result = assignmentService.getInstructorDetailStatistics(USER_ID, null, null);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.totalCount()).isEqualTo(0L);
            assertThat(result.mainCount()).isEqualTo(0L);
            assertThat(result.subCount()).isEqualTo(0L);
            assertThat(result.courseTimeStats()).isEmpty();
        }
    }

    // ==================== 가용성 확인 API 테스트 ====================

    @Nested
    @DisplayName("checkAvailability - 강사 가용성 확인")
    class CheckAvailability {

        @Test
        @DisplayName("성공 - 배정 없는 강사 (가용)")
        void checkAvailability_success_available_noAssignment() {
            // given
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);

            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of());

            // when
            InstructorAvailabilityResponse result = assignmentService.checkAvailability(USER_ID, startDate, endDate);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.available()).isTrue();
            assertThat(result.conflictingAssignments()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 겹치지 않는 기간 (가용)")
        void checkAvailability_success_available_noOverlap() {
            // given
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);

            // 기존 배정 (다른 기간)
            InstructorAssignment existingAssignment = createTestAssignment(1L, USER_ID, 200L, InstructorRole.MAIN);
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of(existingAssignment));

            // 기간이 겹치지 않음
            given(courseTimeRepository.findByIdInAndDateRangeOverlap(
                    List.of(200L), startDate, endDate
            )).willReturn(List.of());

            // when
            InstructorAvailabilityResponse result = assignmentService.checkAvailability(USER_ID, startDate, endDate);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.available()).isTrue();
            assertThat(result.conflictingAssignments()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 기간 충돌 (불가용)")
        void checkAvailability_success_unavailable_conflict() {
            // given
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);
            Long conflictingTimeId = 200L;

            // 기존 배정
            InstructorAssignment existingAssignment = createTestAssignment(1L, USER_ID, conflictingTimeId, InstructorRole.MAIN);
            given(assignmentRepository.findActiveByUserKey(TENANT_ID, USER_ID)).willReturn(List.of(existingAssignment));

            // 기간 충돌하는 CourseTime
            CourseTime conflictingCourseTime = createTestCourseTimeWithId(conflictingTimeId,
                    LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 20));
            given(courseTimeRepository.findByIdInAndDateRangeOverlap(
                    List.of(conflictingTimeId), startDate, endDate
            )).willReturn(List.of(conflictingCourseTime));

            // when
            InstructorAvailabilityResponse result = assignmentService.checkAvailability(USER_ID, startDate, endDate);

            // then
            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.available()).isFalse();
            assertThat(result.conflictingAssignments()).hasSize(1);
            assertThat(result.conflictingAssignments().get(0).timeId()).isEqualTo(conflictingTimeId);
            assertThat(result.conflictingAssignments().get(0).role()).isEqualTo(InstructorRole.MAIN);
        }
    }

    @Nested
    @DisplayName("checkAvailabilityBulk - 여러 강사 가용성 일괄 확인")
    class CheckAvailabilityBulk {

        @Test
        @DisplayName("성공 - 모든 강사 가용")
        void checkAvailabilityBulk_success_allAvailable() {
            // given
            List<Long> userIds = List.of(10L, 20L);
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);

            // 배정 없음
            given(assignmentRepository.findActiveByUserKeyIn(TENANT_ID, userIds)).willReturn(List.of());

            // when
            List<InstructorAvailabilityResponse> result = assignmentService.checkAvailabilityBulk(userIds, startDate, endDate);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(InstructorAvailabilityResponse::available);
        }

        @Test
        @DisplayName("성공 - 일부 강사만 가용")
        void checkAvailabilityBulk_success_partiallyAvailable() {
            // given
            List<Long> userIds = List.of(10L, 20L);
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);
            Long conflictingTimeId = 200L;

            // userId=10L은 배정 있음, userId=20L은 배정 없음
            InstructorAssignment existingAssignment = createTestAssignment(1L, 10L, conflictingTimeId, InstructorRole.MAIN);
            given(assignmentRepository.findActiveByUserKeyIn(TENANT_ID, userIds)).willReturn(List.of(existingAssignment));

            // 기간 충돌하는 CourseTime
            CourseTime conflictingCourseTime = createTestCourseTimeWithId(conflictingTimeId,
                    LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 20));
            given(courseTimeRepository.findByIdInAndDateRangeOverlap(
                    List.of(conflictingTimeId), startDate, endDate
            )).willReturn(List.of(conflictingCourseTime));

            // when
            List<InstructorAvailabilityResponse> result = assignmentService.checkAvailabilityBulk(userIds, startDate, endDate);

            // then
            assertThat(result).hasSize(2);

            // userId=10L은 불가용
            InstructorAvailabilityResponse user10Result = result.stream()
                    .filter(r -> r.userId().equals(10L))
                    .findFirst().orElseThrow();
            assertThat(user10Result.available()).isFalse();
            assertThat(user10Result.conflictingAssignments()).hasSize(1);

            // userId=20L은 가용
            InstructorAvailabilityResponse user20Result = result.stream()
                    .filter(r -> r.userId().equals(20L))
                    .findFirst().orElseThrow();
            assertThat(user20Result.available()).isTrue();
            assertThat(user20Result.conflictingAssignments()).isEmpty();
        }

        @Test
        @DisplayName("성공 - 모든 강사 불가용")
        void checkAvailabilityBulk_success_allUnavailable() {
            // given
            List<Long> userIds = List.of(10L, 20L);
            LocalDate startDate = LocalDate.of(2024, 3, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 15);

            // 각 강사별 배정
            InstructorAssignment assignment1 = createTestAssignment(1L, 10L, 200L, InstructorRole.MAIN);
            InstructorAssignment assignment2 = createTestAssignment(2L, 20L, 300L, InstructorRole.SUB);
            given(assignmentRepository.findActiveByUserKeyIn(TENANT_ID, userIds))
                    .willReturn(List.of(assignment1, assignment2));

            // 기간 충돌하는 CourseTime
            CourseTime conflictingCourseTime1 = createTestCourseTimeWithId(200L,
                    LocalDate.of(2024, 3, 5), LocalDate.of(2024, 3, 20));
            CourseTime conflictingCourseTime2 = createTestCourseTimeWithId(300L,
                    LocalDate.of(2024, 3, 10), LocalDate.of(2024, 3, 25));
            given(courseTimeRepository.findByIdInAndDateRangeOverlap(
                    any(), eq(startDate), eq(endDate)
            )).willReturn(List.of(conflictingCourseTime1, conflictingCourseTime2));

            // when
            List<InstructorAvailabilityResponse> result = assignmentService.checkAvailabilityBulk(userIds, startDate, endDate);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).noneMatch(InstructorAvailabilityResponse::available);
        }
    }
}
