package com.mzc.lp.domain.iis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.repository.AssignmentHistoryRepository;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * InstructorAssignmentController 테스트
 *
 * 참고: /api/times/{timeId}/instructors 엔드포인트는 CourseTimeInstructorController에서 처리하며,
 * 해당 테스트는 CourseTimeInstructorControllerTest에서 수행합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class InstructorAssignmentControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InstructorAssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentHistoryRepository historyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    private static final Long TIME_ID = 100L;

    @BeforeEach
    void setUp() {
        historyRepository.deleteAll();
        assignmentRepository.deleteAll();
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

    private User createInstructorUser() {
        User user = User.create("instructor@example.com", "강사", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.USER);
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

    private InstructorAssignment createAssignment(Long userId, Long timeId, InstructorRole role) {
        return createAssignment(userId, timeId, role, 1L);
    }

    private InstructorAssignment createAssignment(Long userId, Long timeId, InstructorRole role, Long assignedBy) {
        return assignmentRepository.save(InstructorAssignment.create(userId, timeId, role, assignedBy));
    }

    private CourseTime createCourseTime() {
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
        return courseTimeRepository.save(courseTime);
    }

    // ==================== 배정 단건 조회 API 테스트 ====================

    @Nested
    @DisplayName("GET /api/instructor-assignments/{id} - 배정 단건 조회")
    class GetAssignment {

        @Test
        @DisplayName("성공 - 배정 조회")
        void getAssignment_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);

            // when & then
            mockMvc.perform(get("/api/instructor-assignments/{id}", assignment.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(assignment.getId()))
                    .andExpect(jsonPath("$.data.role").value("MAIN"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 배정")
        void getAssignment_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/instructor-assignments/{id}", 999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("IIS001"));
        }
    }

    // ==================== 역할 변경 API 테스트 ====================

    @Nested
    @DisplayName("PUT /api/instructor-assignments/{id} - 역할 변경")
    class UpdateRole {

        @Test
        @DisplayName("성공 - 역할 변경")
        void updateRole_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // Race Condition 방지를 위해 CourseTime이 필요함
            CourseTime courseTime = createCourseTime();
            InstructorAssignment assignment = createAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB);

            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.MAIN, "주강사 승격");

            // when & then
            mockMvc.perform(put("/api/instructor-assignments/{id}", assignment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.role").value("MAIN"));
        }
    }

    // ==================== 교체 API 테스트 ====================

    @Nested
    @DisplayName("POST /api/instructor-assignments/{id}/replace - 강사 교체")
    class ReplaceInstructor {

        @Test
        @DisplayName("성공 - 강사 교체")
        void replaceInstructor_success() throws Exception {
            // given
            createOperatorUser();
            User instructor1 = createInstructorUser();
            User instructor2 = User.create("instructor2@example.com", "강사2", passwordEncoder.encode("Password123!"));
            instructor2.updateRole(TenantRole.USER);
            instructor2 = userRepository.save(instructor2);

            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // Race Condition 방지를 위해 CourseTime이 필요함
            CourseTime courseTime = createCourseTime();
            InstructorAssignment assignment = createAssignment(instructor1.getId(), courseTime.getId(), InstructorRole.MAIN);

            ReplaceInstructorRequest request = new ReplaceInstructorRequest(
                    instructor2.getId(), InstructorRole.MAIN, "강사 교체");

            // when & then
            mockMvc.perform(post("/api/instructor-assignments/{id}/replace", assignment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.userId").value(instructor2.getId()));
        }
    }

    // ==================== 취소 API 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/instructor-assignments/{id} - 배정 취소")
    class CancelAssignment {

        @Test
        @DisplayName("성공 - 배정 취소")
        void cancelAssignment_success() throws Exception {
            // given
            User operator = createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN, operator.getId());

            CancelAssignmentRequest request = new CancelAssignmentRequest("취소 사유");

            // when & then
            mockMvc.perform(delete("/api/instructor-assignments/{id}", assignment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }

    // ==================== 이력 조회 API 테스트 ====================

    @Nested
    @DisplayName("GET /api/instructor-assignments/{id}/histories - 이력 조회")
    class GetAssignmentHistories {

        @Test
        @DisplayName("성공 - 전체 이력 조회")
        void getAssignmentHistories_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment assignment = createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);

            // when & then - 배정 생성 시 자동으로 ASSIGN 이력이 생성됨
            mockMvc.perform(get("/api/instructor-assignments/{id}/histories", assignment.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("성공 - 액션 타입 필터링 조회")
        void getAssignmentHistories_withActionFilter_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // Race Condition 방지를 위해 CourseTime이 필요함
            CourseTime courseTime = createCourseTime();
            InstructorAssignment assignment = createAssignment(instructor.getId(), courseTime.getId(), InstructorRole.SUB);

            // 역할 변경하여 ROLE_CHANGE 이력 생성
            UpdateRoleRequest updateRequest = new UpdateRoleRequest(InstructorRole.MAIN, "주강사 승격");
            mockMvc.perform(put("/api/instructor-assignments/{id}", assignment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk());

            // when & then - ROLE_CHANGE 액션만 필터링
            mockMvc.perform(get("/api/instructor-assignments/{id}/histories", assignment.getId())
                            .param("action", "ROLE_CHANGE")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].action").value("ROLE_CHANGE"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 배정")
        void getAssignmentHistories_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/instructor-assignments/{id}/histories", 999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("IIS001"));
        }
    }

    // ==================== 사용자 기준 API 테스트 ====================

    @Nested
    @DisplayName("GET /api/users/me/instructor-assignments - 내 배정 목록")
    class GetMyAssignments {

        @Test
        @DisplayName("성공 - 내 배정 목록 조회")
        void getMyAssignments_success() throws Exception {
            // given
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);
            createAssignment(instructor.getId(), 200L, InstructorRole.SUB);

            // when & then
            mockMvc.perform(get("/api/users/me/instructor-assignments")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }
    }

    // ==================== 강사별 배정 조회 API 테스트 ====================

    @Nested
    @DisplayName("GET /api/users/{userId}/instructor-assignments - 강사별 배정 목록")
    class GetAssignmentsByUserId {

        @Test
        @DisplayName("성공 - 강사별 배정 목록 조회")
        void getAssignmentsByUserId_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);
            createAssignment(instructor.getId(), 200L, InstructorRole.SUB);

            // when & then
            mockMvc.perform(get("/api/users/{userId}/instructor-assignments", instructor.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(2));
        }

        @Test
        @DisplayName("성공 - 상태 필터링")
        void getAssignmentsByUserId_withStatusFilter_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            InstructorAssignment activeAssignment = createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);
            InstructorAssignment cancelledAssignment = createAssignment(instructor.getId(), 200L, InstructorRole.SUB);
            cancelledAssignment.cancel();
            assignmentRepository.save(cancelledAssignment);

            // when & then
            mockMvc.perform(get("/api/users/{userId}/instructor-assignments", instructor.getId())
                            .param("status", "ACTIVE")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void getAssignmentsByUserId_fail_forbidden() throws Exception {
            // given
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/instructor-assignments", instructor.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 통계 API 테스트 ====================

    @Nested
    @DisplayName("GET /api/instructor-assignments/statistics - 전체 통계 조회")
    class GetStatistics {

        @Test
        @DisplayName("성공 - 전체 통계 조회")
        void getStatistics_success() throws Exception {
            // given
            createOperatorUser();
            User instructor1 = createInstructorUser();
            User instructor2 = User.create("instructor2@example.com", "강사2", passwordEncoder.encode("Password123!"));
            instructor2.updateRole(TenantRole.USER);
            instructor2 = userRepository.save(instructor2);

            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 배정 데이터 생성
            createAssignment(instructor1.getId(), TIME_ID, InstructorRole.MAIN);
            createAssignment(instructor1.getId(), 200L, InstructorRole.SUB);
            createAssignment(instructor2.getId(), 300L, InstructorRole.MAIN);

            // when & then
            mockMvc.perform(get("/api/instructor-assignments/statistics")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalAssignments").value(3))
                    .andExpect(jsonPath("$.data.activeAssignments").value(3))
                    .andExpect(jsonPath("$.data.byRole.MAIN").value(2))
                    .andExpect(jsonPath("$.data.byRole.SUB").value(1))
                    .andExpect(jsonPath("$.data.byStatus.ACTIVE").value(3))
                    .andExpect(jsonPath("$.data.instructorStats.length()").value(2));
        }

        @Test
        @DisplayName("성공 - 데이터 없음")
        void getStatistics_success_noData() throws Exception {
            // given
            createOperatorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/instructor-assignments/statistics")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalAssignments").value(0))
                    .andExpect(jsonPath("$.data.activeAssignments").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void getStatistics_fail_forbidden() throws Exception {
            // given
            createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/instructor-assignments/statistics")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/users/me/instructor-statistics - 내 통계 조회")
    class GetMyInstructorStatistics {

        @Test
        @DisplayName("성공 - 내 통계 조회")
        void getMyInstructorStatistics_success() throws Exception {
            // given
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            // 배정 데이터 생성
            createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);
            createAssignment(instructor.getId(), 200L, InstructorRole.SUB);
            createAssignment(instructor.getId(), 300L, InstructorRole.SUB);

            // when & then
            mockMvc.perform(get("/api/users/me/instructor-statistics")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(instructor.getId()))
                    .andExpect(jsonPath("$.data.userName").value("강사"))
                    .andExpect(jsonPath("$.data.totalCount").value(3))
                    .andExpect(jsonPath("$.data.mainCount").value(1))
                    .andExpect(jsonPath("$.data.subCount").value(2));
        }

        @Test
        @DisplayName("성공 - 기간 필터링으로 내 통계 조회")
        void getMyInstructorStatistics_withDateRange_success() throws Exception {
            // given
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            // 배정 데이터 생성
            createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);
            createAssignment(instructor.getId(), 200L, InstructorRole.SUB);

            LocalDate today = LocalDate.now();

            // when & then
            mockMvc.perform(get("/api/users/me/instructor-statistics")
                            .param("startDate", today.minusDays(1).toString())
                            .param("endDate", today.plusDays(1).toString())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(instructor.getId()));
        }

        @Test
        @DisplayName("성공 - 배정 없는 경우")
        void getMyInstructorStatistics_success_noAssignment() throws Exception {
            // given
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/instructor-statistics")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(instructor.getId()))
                    .andExpect(jsonPath("$.data.totalCount").value(0))
                    .andExpect(jsonPath("$.data.mainCount").value(0))
                    .andExpect(jsonPath("$.data.subCount").value(0));
        }

    }

    @Nested
    @DisplayName("GET /api/users/{userId}/instructor-statistics - 강사 개인 통계 조회")
    class GetInstructorStatistics {

        @Test
        @DisplayName("성공 - 강사 개인 통계 조회")
        void getInstructorStatistics_success() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 배정 데이터 생성
            createAssignment(instructor.getId(), TIME_ID, InstructorRole.MAIN);
            createAssignment(instructor.getId(), 200L, InstructorRole.SUB);
            createAssignment(instructor.getId(), 300L, InstructorRole.SUB);

            // when & then
            mockMvc.perform(get("/api/users/{userId}/instructor-statistics", instructor.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(instructor.getId()))
                    .andExpect(jsonPath("$.data.userName").value("강사"))
                    .andExpect(jsonPath("$.data.totalCount").value(3))
                    .andExpect(jsonPath("$.data.mainCount").value(1))
                    .andExpect(jsonPath("$.data.subCount").value(2));
        }

        @Test
        @DisplayName("성공 - 배정 없는 강사")
        void getInstructorStatistics_success_noAssignment() throws Exception {
            // given
            createOperatorUser();
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/instructor-statistics", instructor.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userId").value(instructor.getId()))
                    .andExpect(jsonPath("$.data.totalCount").value(0))
                    .andExpect(jsonPath("$.data.mainCount").value(0))
                    .andExpect(jsonPath("$.data.subCount").value(0));
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void getInstructorStatistics_fail_forbidden() throws Exception {
            // given
            User instructor = createInstructorUser();
            String token = loginAndGetAccessToken("instructor@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/instructor-statistics", instructor.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
