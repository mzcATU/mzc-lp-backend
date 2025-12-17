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
import com.mzc.lp.domain.iis.constant.AssignmentAction;
import com.mzc.lp.domain.iis.entity.AssignmentHistory;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.exception.CannotModifyInactiveAssignmentException;
import com.mzc.lp.domain.iis.exception.InstructorAlreadyAssignedException;
import com.mzc.lp.domain.iis.exception.InstructorAssignmentNotFoundException;
import com.mzc.lp.domain.iis.exception.MainInstructorAlreadyExistsException;
import com.mzc.lp.domain.iis.repository.AssignmentHistoryRepository;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
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

import java.util.List;
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

    // ==================== 배정 테스트 ====================

    @Nested
    @DisplayName("assignInstructor - 강사 배정")
    class AssignInstructor {

        @Test
        @DisplayName("성공 - MAIN 강사 배정")
        void assignInstructor_success_main() {
            // given
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.MAIN);
            InstructorAssignment saved = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.MAIN);
            User user = createTestUser(USER_ID, "강사", "instructor@example.com");

            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            given(assignmentRepository.findActiveByTimeKeyAndRole(TIME_ID, TENANT_ID, InstructorRole.MAIN))
                    .willReturn(Optional.empty());
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
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.SUB);
            InstructorAssignment saved = createTestAssignment(1L, USER_ID, TIME_ID, InstructorRole.SUB);
            User user = createTestUser(USER_ID, "부강사", "sub@example.com");

            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
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
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.MAIN);

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
            AssignInstructorRequest request = new AssignInstructorRequest(USER_ID, InstructorRole.MAIN);
            InstructorAssignment existingMain = createTestAssignment(99L, 999L, TIME_ID, InstructorRole.MAIN);

            given(assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_ID, USER_ID, TENANT_ID, AssignmentStatus.ACTIVE)).willReturn(false);
            given(assignmentRepository.findActiveByTimeKeyAndRole(TIME_ID, TENANT_ID, InstructorRole.MAIN))
                    .willReturn(Optional.of(existingMain));

            // when & then
            assertThatThrownBy(() -> assignmentService.assignInstructor(TIME_ID, request, OPERATOR_ID))
                    .isInstanceOf(MainInstructorAlreadyExistsException.class);
        }
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

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
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

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(assignment));
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

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(oldAssignment));
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

            given(assignmentRepository.findByIdAndTenantId(1L, TENANT_ID)).willReturn(Optional.of(oldAssignment));
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
            assignmentService.cancelAssignment(1L, request, OPERATOR_ID);

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
            assertThatThrownBy(() -> assignmentService.cancelAssignment(1L, request, OPERATOR_ID))
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
}
