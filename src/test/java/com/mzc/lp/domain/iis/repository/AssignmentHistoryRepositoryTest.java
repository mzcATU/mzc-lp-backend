package com.mzc.lp.domain.iis.repository;
import com.mzc.lp.common.support.TenantTestSupport;

import com.mzc.lp.common.config.JpaConfig;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.AssignmentHistory;
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

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@ActiveProfiles("test")
class AssignmentHistoryRepositoryTest extends TenantTestSupport {

    @Autowired
    private AssignmentHistoryRepository historyRepository;

    private static final Long ASSIGNMENT_ID = 1L;
    private static final Long OPERATOR_ID = 100L;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
    }

    @Nested
    @DisplayName("AssignmentHistory 저장 테스트")
    class SaveTest {

        @Test
        @DisplayName("성공 - 배정 이력 저장")
        void save_assign_success() {
            // given
            AssignmentHistory history = AssignmentHistory.ofAssign(
                    ASSIGNMENT_ID, InstructorRole.MAIN, OPERATOR_ID);

            // when
            AssignmentHistory saved = historyRepository.save(history);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getAssignmentId()).isEqualTo(ASSIGNMENT_ID);
            assertThat(saved.getNewStatus()).isEqualTo(AssignmentStatus.ACTIVE);
            assertThat(saved.getNewRole()).isEqualTo(InstructorRole.MAIN);
            assertThat(saved.getChangedBy()).isEqualTo(OPERATOR_ID);
            assertThat(saved.getChangedAt()).isNotNull();
        }

        @Test
        @DisplayName("성공 - 교체 이력 저장")
        void save_replace_success() {
            // given
            AssignmentHistory history = AssignmentHistory.ofReplace(
                    ASSIGNMENT_ID, "일정 충돌로 인한 교체", OPERATOR_ID);

            // when
            AssignmentHistory saved = historyRepository.save(history);

            // then
            assertThat(saved.getOldStatus()).isEqualTo(AssignmentStatus.ACTIVE);
            assertThat(saved.getNewStatus()).isEqualTo(AssignmentStatus.REPLACED);
            assertThat(saved.getReason()).isEqualTo("일정 충돌로 인한 교체");
        }

        @Test
        @DisplayName("성공 - 취소 이력 저장")
        void save_cancel_success() {
            // given
            AssignmentHistory history = AssignmentHistory.ofCancel(
                    ASSIGNMENT_ID, "강사 요청으로 취소", OPERATOR_ID);

            // when
            AssignmentHistory saved = historyRepository.save(history);

            // then
            assertThat(saved.getOldStatus()).isEqualTo(AssignmentStatus.ACTIVE);
            assertThat(saved.getNewStatus()).isEqualTo(AssignmentStatus.CANCELLED);
            assertThat(saved.getReason()).isEqualTo("강사 요청으로 취소");
        }

        @Test
        @DisplayName("성공 - 역할 변경 이력 저장")
        void save_roleChange_success() {
            // given
            AssignmentHistory history = AssignmentHistory.ofRoleChange(
                    ASSIGNMENT_ID,
                    InstructorRole.SUB,
                    InstructorRole.MAIN,
                    "주강사 승격",
                    OPERATOR_ID
            );

            // when
            AssignmentHistory saved = historyRepository.save(history);

            // then
            assertThat(saved.getOldRole()).isEqualTo(InstructorRole.SUB);
            assertThat(saved.getNewRole()).isEqualTo(InstructorRole.MAIN);
            assertThat(saved.getReason()).isEqualTo("주강사 승격");
        }
    }

    @Nested
    @DisplayName("AssignmentHistory 조회 테스트")
    class FindTest {

        @Test
        @DisplayName("성공 - 배정 ID로 이력 조회 (최신순)")
        void findByAssignmentIdOrderByChangedAtDesc_success() {
            // given
            historyRepository.save(AssignmentHistory.ofAssign(ASSIGNMENT_ID, InstructorRole.SUB, OPERATOR_ID));
            historyRepository.save(AssignmentHistory.ofRoleChange(
                    ASSIGNMENT_ID, InstructorRole.SUB, InstructorRole.MAIN, "승격", OPERATOR_ID));
            historyRepository.save(AssignmentHistory.ofCancel(ASSIGNMENT_ID, "취소", OPERATOR_ID));

            // 다른 배정 ID의 이력
            historyRepository.save(AssignmentHistory.ofAssign(999L, InstructorRole.MAIN, OPERATOR_ID));

            // when
            List<AssignmentHistory> histories = historyRepository.findByAssignmentIdOrderByChangedAtDesc(ASSIGNMENT_ID);

            // then
            assertThat(histories).hasSize(3);
        }

        @Test
        @DisplayName("성공 - 배정 ID로 이력 조회 (페이징)")
        void findByAssignmentId_paging_success() {
            // given
            for (int i = 0; i < 15; i++) {
                historyRepository.save(AssignmentHistory.ofAssign(ASSIGNMENT_ID, InstructorRole.MAIN, OPERATOR_ID));
            }

            // when
            Page<AssignmentHistory> page1 = historyRepository.findByAssignmentId(
                    ASSIGNMENT_ID, PageRequest.of(0, 10));
            Page<AssignmentHistory> page2 = historyRepository.findByAssignmentId(
                    ASSIGNMENT_ID, PageRequest.of(1, 10));

            // then
            assertThat(page1.getTotalElements()).isEqualTo(15);
            assertThat(page1.getContent()).hasSize(10);
            assertThat(page2.getContent()).hasSize(5);
        }

        @Test
        @DisplayName("성공 - 이력 없는 배정 ID 조회")
        void findByAssignmentIdOrderByChangedAtDesc_empty() {
            // when
            List<AssignmentHistory> histories = historyRepository.findByAssignmentIdOrderByChangedAtDesc(999L);

            // then
            assertThat(histories).isEmpty();
        }
    }
}
