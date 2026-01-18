package com.mzc.lp.domain.ts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.repository.AssignmentHistoryRepository;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CourseTimeInstructorControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

    @Autowired
    private InstructorAssignmentRepository instructorAssignmentRepository;

    @Autowired
    private AssignmentHistoryRepository assignmentHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        assignmentHistoryRepository.deleteAll();
        instructorAssignmentRepository.deleteAll();
        courseTimeRepository.deleteAll();
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    private User createNormalUser() {
        User user = User.create("user@example.com", "일반사용자", passwordEncoder.encode("Password123!"));
        return userRepository.save(user);
    }

    private User createInstructorUser(String email, String name) {
        User user = User.create(email, name, passwordEncoder.encode("Password123!"));
        return userRepository.save(user);
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("accessToken").asText();
    }

    private CourseTime createTestCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                DurationType.FIXED,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                null,
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("100000"),
                false,
                null,
                true,
                null,
                1L
        );
        return courseTimeRepository.save(courseTime);
    }

    private CourseTime createOngoingCourseTime() {
        CourseTime courseTime = createTestCourseTime();
        courseTime.open();
        courseTime.startClass();
        return courseTimeRepository.save(courseTime);
    }

    private CourseTime createClosedCourseTime() {
        CourseTime courseTime = createOngoingCourseTime();
        courseTime.close();
        return courseTimeRepository.save(courseTime);
    }

    private InstructorAssignment createInstructorAssignment(Long userId, Long timeId, InstructorRole role, Long operatorId) {
        InstructorAssignment assignment = InstructorAssignment.create(userId, timeId, role, operatorId);
        return instructorAssignmentRepository.save(assignment);
    }

    // ==================== 강사 배정 테스트 ====================

    @Nested
    @DisplayName("POST /api/times/{timeId}/instructors - 강사 배정")
    class AssignInstructor {

        @Test
        @DisplayName("성공 - DRAFT 상태 차수에 강사 배정")
        void assignInstructor_success_draft() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(instructor.getId()))
                    .andExpect(jsonPath("$.data.timeId").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.role").value("MAIN"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));

            // DB 검증
            List<InstructorAssignment> assignments = instructorAssignmentRepository.findByTimeKeyAndTenantId(courseTime.getId(), 1L);
            assertThat(assignments).hasSize(1);
            assertThat(assignments.get(0).getUserKey()).isEqualTo(instructor.getId());
            assertThat(assignments.get(0).getRole()).isEqualTo(InstructorRole.MAIN);
        }

        @Test
        @DisplayName("성공 - RECRUITING 상태 차수에 강사 배정")
        void assignInstructor_success_recruiting() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.SUB, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("SUB"));
        }

        @Test
        @DisplayName("성공 - ONGOING 상태 차수에 강사 배정")
        void assignInstructor_success_ongoing() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createOngoingCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.ASSISTANT, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("ASSISTANT"));
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수에 강사 배정")
        void assignInstructor_fail_closed() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS006"));
        }

        @Test
        @DisplayName("실패 - ARCHIVED 상태 차수에 강사 배정")
        void assignInstructor_fail_archived() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createClosedCourseTime();
            courseTime.archive();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS006"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void assignInstructor_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 강사 배정 시도")
        void assignInstructor_fail_userRole() throws Exception {
            // given
            createNormalUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 이미 배정된 강사 중복 배정 시도")
        void assignInstructor_fail_duplicateAssignment() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 먼저 강사 배정
            createInstructorAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            AssignInstructorRequest request = new AssignInstructorRequest(instructor.getId(), InstructorRole.ASSISTANT, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("IIS002"));
        }

        @Test
        @DisplayName("실패 - MAIN 강사가 이미 존재하는 상태에서 MAIN 배정 시도")
        void assignInstructor_fail_mainAlreadyExists() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor1 = createInstructorUser("instructor1@example.com", "주강사");
            User instructor2 = createInstructorUser("instructor2@example.com", "신규강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 먼저 MAIN 강사 배정
            createInstructorAssignment(instructor1.getId(), courseTime.getId(), InstructorRole.MAIN, operator.getId());

            AssignInstructorRequest request = new AssignInstructorRequest(instructor2.getId(), InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("IIS003"));
        }
    }

    // ==================== 강사 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/times/{timeId}/instructors - 강사 목록 조회")
    class GetInstructors {

        @Test
        @DisplayName("성공 - 전체 강사 목록 조회")
        void getInstructors_success_all() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor1 = createInstructorUser("instructor1@example.com", "주강사");
            User instructor2 = createInstructorUser("instructor2@example.com", "보조강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            createInstructorAssignment(instructor1.getId(), courseTime.getId(), InstructorRole.MAIN, operator.getId());
            createInstructorAssignment(instructor2.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            // when & then
            mockMvc.perform(get("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("성공 - ACTIVE 상태 강사만 조회")
        void getInstructors_success_filterByStatus() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor1 = createInstructorUser("instructor1@example.com", "주강사");
            User instructor2 = createInstructorUser("instructor2@example.com", "취소된강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            createInstructorAssignment(instructor1.getId(), courseTime.getId(), InstructorRole.MAIN, operator.getId());
            InstructorAssignment cancelledAssignment = createInstructorAssignment(instructor2.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());
            cancelledAssignment.cancel();
            instructorAssignmentRepository.save(cancelledAssignment);

            // when & then
            mockMvc.perform(get("/api/times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].role").value("MAIN"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void getInstructors_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{timeId}/instructors", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }
    }

    // ==================== 강사 역할 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/times/{timeId}/instructors/{assignmentId} - 역할 수정")
    class UpdateRole {

        @Test
        @DisplayName("성공 - 역할 수정")
        void updateRole_success() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createInstructorAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.ASSISTANT, "역할 변경 사유");

            // when & then
            mockMvc.perform(put("/api/times/{timeId}/instructors/{assignmentId}", courseTime.getId(), assignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("ASSISTANT"));

            // DB 검증
            InstructorAssignment updated = instructorAssignmentRepository.findById(assignment.getId()).orElseThrow();
            assertThat(updated.getRole()).isEqualTo(InstructorRole.ASSISTANT);
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수")
        void updateRole_fail_closed() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createInstructorAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.ASSISTANT, null);

            // when & then
            mockMvc.perform(put("/api/times/{timeId}/instructors/{assignmentId}", courseTime.getId(), assignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("TS006"));
        }
    }

    // ==================== 강사 교체 테스트 ====================

    @Nested
    @DisplayName("POST /api/times/{timeId}/instructors/{assignmentId}/replace - 강사 교체")
    class ReplaceInstructor {

        @Test
        @DisplayName("성공 - 강사 교체")
        void replaceInstructor_success() throws Exception {
            // given
            User operator = createOperatorUser();
            User oldInstructor = createInstructorUser("old@example.com", "기존강사");
            User newInstructor = createInstructorUser("new@example.com", "신규강사");
            CourseTime courseTime = createOngoingCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment oldAssignment = createInstructorAssignment(oldInstructor.getId(), courseTime.getId(), InstructorRole.MAIN, operator.getId());

            ReplaceInstructorRequest request = new ReplaceInstructorRequest(newInstructor.getId(), InstructorRole.MAIN, "강사 교체 사유");

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors/{assignmentId}/replace", courseTime.getId(), oldAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(newInstructor.getId()))
                    .andExpect(jsonPath("$.data.role").value("MAIN"));

            // DB 검증
            InstructorAssignment replaced = instructorAssignmentRepository.findById(oldAssignment.getId()).orElseThrow();
            assertThat(replaced.getStatus()).isEqualTo(AssignmentStatus.REPLACED);

            List<InstructorAssignment> activeAssignments = instructorAssignmentRepository.findByTimeKeyAndTenantIdAndStatus(
                    courseTime.getId(), 1L, AssignmentStatus.ACTIVE);
            assertThat(activeAssignments).hasSize(1);
            assertThat(activeAssignments.get(0).getUserKey()).isEqualTo(newInstructor.getId());
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수")
        void replaceInstructor_fail_closed() throws Exception {
            // given
            User operator = createOperatorUser();
            User oldInstructor = createInstructorUser("old@example.com", "기존강사");
            User newInstructor = createInstructorUser("new@example.com", "신규강사");
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment oldAssignment = createInstructorAssignment(oldInstructor.getId(), courseTime.getId(), InstructorRole.MAIN, operator.getId());

            ReplaceInstructorRequest request = new ReplaceInstructorRequest(newInstructor.getId(), InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/times/{timeId}/instructors/{assignmentId}/replace", courseTime.getId(), oldAssignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("TS006"));
        }
    }

    // ==================== 배정 취소 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/times/{timeId}/instructors/{assignmentId} - 배정 취소")
    class CancelAssignment {

        @Test
        @DisplayName("성공 - 배정 취소")
        void cancelAssignment_success() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createInstructorAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            CancelAssignmentRequest request = new CancelAssignmentRequest("배정 취소 사유");

            // when & then
            mockMvc.perform(delete("/api/times/{timeId}/instructors/{assignmentId}", courseTime.getId(), assignment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // DB 검증
            InstructorAssignment cancelled = instructorAssignmentRepository.findById(assignment.getId()).orElseThrow();
            assertThat(cancelled.getStatus()).isEqualTo(AssignmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("성공 - Request Body 없이 배정 취소")
        void cancelAssignment_success_noBody() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createInstructorAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            // when & then
            mockMvc.perform(delete("/api/times/{timeId}/instructors/{assignmentId}", courseTime.getId(), assignment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // DB 검증
            InstructorAssignment cancelled = instructorAssignmentRepository.findById(assignment.getId()).orElseThrow();
            assertThat(cancelled.getStatus()).isEqualTo(AssignmentStatus.CANCELLED);
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수")
        void cancelAssignment_fail_closed() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser("instructor@example.com", "강사");
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createInstructorAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB, operator.getId());

            // when & then
            mockMvc.perform(delete("/api/times/{timeId}/instructors/{assignmentId}", courseTime.getId(), assignment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("TS006"));
        }
    }
}
