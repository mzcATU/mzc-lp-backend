package com.mzc.lp.domain.iis.repository;

import com.mzc.lp.common.config.JpaConfig;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class InstructorAssignmentRepositoryTest {

    @Autowired
    private InstructorAssignmentRepository assignmentRepository;

    private static final Long TENANT_ID = 1L;
    private static final Long TIME_KEY = 100L;
    private static final Long USER_KEY = 10L;
    private static final Long OPERATOR_ID = 1L;

    @BeforeEach
    void setUp() {
        assignmentRepository.deleteAll();
    }

    private InstructorAssignment createAssignment(Long userKey, Long timeKey, InstructorRole role) {
        return InstructorAssignment.create(userKey, timeKey, role, OPERATOR_ID);
    }

    @Nested
    @DisplayName("InstructorAssignment 저장 테스트")
    class SaveTest {

        @Test
        @DisplayName("성공 - 강사 배정 저장")
        void save_success() {
            // given
            InstructorAssignment assignment = createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN);

            // when
            InstructorAssignment saved = assignmentRepository.save(assignment);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUserKey()).isEqualTo(USER_KEY);
            assertThat(saved.getTimeKey()).isEqualTo(TIME_KEY);
            assertThat(saved.getRole()).isEqualTo(InstructorRole.MAIN);
            assertThat(saved.getStatus()).isEqualTo(AssignmentStatus.ACTIVE);
            assertThat(saved.getAssignedAt()).isNotNull();
            assertThat(saved.getAssignedBy()).isEqualTo(OPERATOR_ID);
            assertThat(saved.getTenantId()).isEqualTo(TENANT_ID);
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - SUB 역할 저장")
        void save_success_subRole() {
            // given
            InstructorAssignment assignment = createAssignment(USER_KEY, TIME_KEY, InstructorRole.SUB);

            // when
            InstructorAssignment saved = assignmentRepository.save(assignment);

            // then
            assertThat(saved.getRole()).isEqualTo(InstructorRole.SUB);
            assertThat(saved.isSub()).isTrue();
            assertThat(saved.isMain()).isFalse();
        }

        @Test
        @DisplayName("성공 - ASSISTANT 역할 저장")
        void save_success_assistantRole() {
            // given
            InstructorAssignment assignment = createAssignment(USER_KEY, TIME_KEY, InstructorRole.ASSISTANT);

            // when
            InstructorAssignment saved = assignmentRepository.save(assignment);

            // then
            assertThat(saved.getRole()).isEqualTo(InstructorRole.ASSISTANT);
            assertThat(saved.isAssistant()).isTrue();
        }
    }

    @Nested
    @DisplayName("InstructorAssignment 조회 테스트")
    class FindTest {

        @Test
        @DisplayName("성공 - ID로 조회")
        void findById_success() {
            // given
            InstructorAssignment saved = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));

            // when
            Optional<InstructorAssignment> found = assignmentRepository.findById(saved.getId());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getUserKey()).isEqualTo(USER_KEY);
        }

        @Test
        @DisplayName("성공 - ID와 TenantId로 조회")
        void findByIdAndTenantId_success() {
            // given
            InstructorAssignment saved = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));

            // when
            Optional<InstructorAssignment> found = assignmentRepository.findByIdAndTenantId(
                    saved.getId(), TENANT_ID);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTenantId()).isEqualTo(TENANT_ID);
        }

        @Test
        @DisplayName("실패 - 다른 TenantId로 조회")
        void findByIdAndTenantId_fail_wrongTenant() {
            // given
            InstructorAssignment saved = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));

            // when
            Optional<InstructorAssignment> found = assignmentRepository.findByIdAndTenantId(
                    saved.getId(), 999L);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("성공 - 차수별 강사 목록 조회")
        void findByTimeKeyAndTenantId_success() {
            // given
            assignmentRepository.save(createAssignment(10L, TIME_KEY, InstructorRole.MAIN));
            assignmentRepository.save(createAssignment(20L, TIME_KEY, InstructorRole.SUB));
            assignmentRepository.save(createAssignment(30L, TIME_KEY, InstructorRole.ASSISTANT));
            assignmentRepository.save(createAssignment(40L, 200L, InstructorRole.MAIN)); // 다른 차수

            // when
            List<InstructorAssignment> found = assignmentRepository.findByTimeKeyAndTenantId(
                    TIME_KEY, TENANT_ID);

            // then
            assertThat(found).hasSize(3);
        }

        @Test
        @DisplayName("성공 - 차수별 ACTIVE 강사 목록 조회")
        void findByTimeKeyAndTenantIdAndStatus_success() {
            // given
            assignmentRepository.save(createAssignment(10L, TIME_KEY, InstructorRole.MAIN));
            assignmentRepository.save(createAssignment(20L, TIME_KEY, InstructorRole.SUB));
            InstructorAssignment cancelled = assignmentRepository.save(
                    createAssignment(30L, TIME_KEY, InstructorRole.SUB));
            cancelled.cancel();
            assignmentRepository.save(cancelled);

            // when
            List<InstructorAssignment> found = assignmentRepository.findByTimeKeyAndTenantIdAndStatus(
                    TIME_KEY, TENANT_ID, AssignmentStatus.ACTIVE);

            // then
            assertThat(found).hasSize(2);
        }

        @Test
        @DisplayName("성공 - 강사별 배정 목록 조회 (페이징)")
        void findByUserKeyAndTenantId_paging_success() {
            // given
            for (long i = 1; i <= 15; i++) {
                assignmentRepository.save(createAssignment(USER_KEY, i, InstructorRole.MAIN));
            }

            // when
            Page<InstructorAssignment> page1 = assignmentRepository.findByUserKeyAndTenantId(
                    USER_KEY, TENANT_ID, PageRequest.of(0, 10));
            Page<InstructorAssignment> page2 = assignmentRepository.findByUserKeyAndTenantId(
                    USER_KEY, TENANT_ID, PageRequest.of(1, 10));

            // then
            assertThat(page1.getTotalElements()).isEqualTo(15);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(5);
        }
    }

    @Nested
    @DisplayName("중복 체크 테스트")
    class ExistsTest {

        @Test
        @DisplayName("성공 - ACTIVE 배정 존재 확인")
        void existsByTimeKeyAndUserKeyAndTenantIdAndStatus_exists() {
            // given
            assignmentRepository.save(createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));

            // when
            boolean exists = assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_KEY, USER_KEY, TENANT_ID, AssignmentStatus.ACTIVE);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("성공 - ACTIVE 배정 없음 확인")
        void existsByTimeKeyAndUserKeyAndTenantIdAndStatus_notExists() {
            // given
            InstructorAssignment cancelled = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));
            cancelled.cancel();
            assignmentRepository.save(cancelled);

            // when
            boolean exists = assignmentRepository.existsByTimeKeyAndUserKeyAndTenantIdAndStatus(
                    TIME_KEY, USER_KEY, TENANT_ID, AssignmentStatus.ACTIVE);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("주강사 조회 테스트")
    class MainInstructorTest {

        @Test
        @DisplayName("성공 - ACTIVE 주강사 조회")
        void findActiveByTimeKeyAndRole_success() {
            // given
            assignmentRepository.save(createAssignment(10L, TIME_KEY, InstructorRole.MAIN));
            assignmentRepository.save(createAssignment(20L, TIME_KEY, InstructorRole.SUB));

            // when
            Optional<InstructorAssignment> found = assignmentRepository.findActiveByTimeKeyAndRole(
                    TIME_KEY, TENANT_ID, InstructorRole.MAIN);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getUserKey()).isEqualTo(10L);
            assertThat(found.get().isMain()).isTrue();
        }

        @Test
        @DisplayName("실패 - 주강사 없음 (CANCELLED 상태)")
        void findActiveByTimeKeyAndRole_notFound_cancelled() {
            // given
            InstructorAssignment cancelled = assignmentRepository.save(
                    createAssignment(10L, TIME_KEY, InstructorRole.MAIN));
            cancelled.cancel();
            assignmentRepository.save(cancelled);

            // when
            Optional<InstructorAssignment> found = assignmentRepository.findActiveByTimeKeyAndRole(
                    TIME_KEY, TENANT_ID, InstructorRole.MAIN);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("배정 수 조회 테스트")
    class CountTest {

        @Test
        @DisplayName("성공 - 차수별 ACTIVE 강사 수 조회")
        void countActiveByTimeKey_success() {
            // given
            assignmentRepository.save(createAssignment(10L, TIME_KEY, InstructorRole.MAIN));
            assignmentRepository.save(createAssignment(20L, TIME_KEY, InstructorRole.SUB));
            InstructorAssignment cancelled = assignmentRepository.save(
                    createAssignment(30L, TIME_KEY, InstructorRole.SUB));
            cancelled.cancel();
            assignmentRepository.save(cancelled);

            // when
            long count = assignmentRepository.countActiveByTimeKey(TIME_KEY, TENANT_ID);

            // then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("성공 - 강사별 ACTIVE 배정 수 조회")
        void countActiveByUserKey_success() {
            // given
            assignmentRepository.save(createAssignment(USER_KEY, 100L, InstructorRole.MAIN));
            assignmentRepository.save(createAssignment(USER_KEY, 200L, InstructorRole.SUB));
            assignmentRepository.save(createAssignment(USER_KEY, 300L, InstructorRole.MAIN));

            // when
            long count = assignmentRepository.countActiveByUserKey(USER_KEY, TENANT_ID);

            // then
            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("상태 변경 테스트")
    class StatusChangeTest {

        @Test
        @DisplayName("성공 - 배정 취소")
        void cancel_success() {
            // given
            InstructorAssignment saved = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));
            assertThat(saved.isActive()).isTrue();

            // when
            saved.cancel();
            InstructorAssignment updated = assignmentRepository.save(saved);

            // then
            assertThat(updated.getStatus()).isEqualTo(AssignmentStatus.CANCELLED);
            assertThat(updated.isActive()).isFalse();
            assertThat(updated.getReplacedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 배정 교체")
        void replace_success() {
            // given
            InstructorAssignment saved = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.MAIN));

            // when
            saved.replace();
            InstructorAssignment updated = assignmentRepository.save(saved);

            // then
            assertThat(updated.getStatus()).isEqualTo(AssignmentStatus.REPLACED);
            assertThat(updated.getReplacedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 역할 변경")
        void updateRole_success() {
            // given
            InstructorAssignment saved = assignmentRepository.save(
                    createAssignment(USER_KEY, TIME_KEY, InstructorRole.SUB));
            assertThat(saved.isSub()).isTrue();

            // when
            saved.updateRole(InstructorRole.MAIN);
            InstructorAssignment updated = assignmentRepository.save(saved);

            // then
            assertThat(updated.getRole()).isEqualTo(InstructorRole.MAIN);
            assertThat(updated.isMain()).isTrue();
        }
    }
}
