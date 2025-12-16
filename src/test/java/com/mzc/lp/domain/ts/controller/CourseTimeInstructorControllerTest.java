package com.mzc.lp.domain.ts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.constant.AssignmentStatus;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.dto.request.AssignInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.CancelAssignmentRequest;
import com.mzc.lp.domain.iis.dto.request.ReplaceInstructorRequest;
import com.mzc.lp.domain.iis.dto.request.UpdateRoleRequest;
import com.mzc.lp.domain.iis.dto.response.InstructorAssignmentResponse;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private InstructorAssignmentService instructorAssignmentService;

    @BeforeEach
    void setUp() {
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

    private InstructorAssignmentResponse createMockResponse(Long id, Long userId, Long timeId, InstructorRole role) {
        return new InstructorAssignmentResponse(
                id,
                userId,
                timeId,
                role,
                AssignmentStatus.ACTIVE,
                Instant.now(),
                null,
                1L,
                Instant.now()
        );
    }

    // ==================== 강사 배정 테스트 ====================

    @Nested
    @DisplayName("POST /api/ts/course-times/{timeId}/instructors - 강사 배정")
    class AssignInstructor {

        @Test
        @DisplayName("성공 - DRAFT 상태 차수에 강사 배정")
        void assignInstructor_success_draft() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.MAIN);
            InstructorAssignmentResponse mockResponse = createMockResponse(1L, 100L, courseTime.getId(), InstructorRole.MAIN);

            given(instructorAssignmentService.assignInstructor(eq(courseTime.getId()), any(), any()))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.userId").value(100))
                    .andExpect(jsonPath("$.data.role").value("MAIN"));
        }

        @Test
        @DisplayName("성공 - RECRUITING 상태 차수에 강사 배정")
        void assignInstructor_success_recruiting() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.SUB);
            InstructorAssignmentResponse mockResponse = createMockResponse(1L, 100L, courseTime.getId(), InstructorRole.SUB);

            given(instructorAssignmentService.assignInstructor(eq(courseTime.getId()), any(), any()))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
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
            CourseTime courseTime = createOngoingCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.ASSISTANT);
            InstructorAssignmentResponse mockResponse = createMockResponse(1L, 100L, courseTime.getId(), InstructorRole.ASSISTANT);

            given(instructorAssignmentService.assignInstructor(eq(courseTime.getId()), any(), any()))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수에 강사 배정")
        void assignInstructor_fail_closed() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.MAIN);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
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
            CourseTime courseTime = createClosedCourseTime();
            courseTime.archive();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.MAIN);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
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

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.MAIN);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", 99999L)
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
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            AssignInstructorRequest request = new AssignInstructorRequest(100L, InstructorRole.MAIN);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 강사 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/ts/course-times/{timeId}/instructors - 강사 목록 조회")
    class GetInstructors {

        @Test
        @DisplayName("성공 - 전체 강사 목록 조회")
        void getInstructors_success_all() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            List<InstructorAssignmentResponse> mockResponses = List.of(
                    createMockResponse(1L, 100L, courseTime.getId(), InstructorRole.MAIN),
                    createMockResponse(2L, 101L, courseTime.getId(), InstructorRole.SUB)
            );

            given(instructorAssignmentService.getInstructorsByTimeId(eq(courseTime.getId()), eq(null)))
                    .willReturn(mockResponses);

            // when & then
            mockMvc.perform(get("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
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
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            List<InstructorAssignmentResponse> mockResponses = List.of(
                    createMockResponse(1L, 100L, courseTime.getId(), InstructorRole.MAIN)
            );

            given(instructorAssignmentService.getInstructorsByTimeId(eq(courseTime.getId()), eq(AssignmentStatus.ACTIVE)))
                    .willReturn(mockResponses);

            // when & then
            mockMvc.perform(get("/api/ts/course-times/{timeId}/instructors", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void getInstructors_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/ts/course-times/{timeId}/instructors", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }
    }

    // ==================== 강사 역할 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/ts/course-times/{timeId}/instructors/{assignmentId} - 역할 수정")
    class UpdateRole {

        @Test
        @DisplayName("성공 - 역할 수정")
        void updateRole_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.SUB, "역할 변경 사유");
            InstructorAssignmentResponse mockResponse = createMockResponse(1L, 100L, courseTime.getId(), InstructorRole.SUB);

            given(instructorAssignmentService.updateRole(eq(1L), any(), any()))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(put("/api/ts/course-times/{timeId}/instructors/{assignmentId}", courseTime.getId(), 1L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("SUB"));
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수")
        void updateRole_fail_closed() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            UpdateRoleRequest request = new UpdateRoleRequest(InstructorRole.SUB, null);

            // when & then
            mockMvc.perform(put("/api/ts/course-times/{timeId}/instructors/{assignmentId}", courseTime.getId(), 1L)
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
    @DisplayName("POST /api/ts/course-times/{timeId}/instructors/{assignmentId}/replace - 강사 교체")
    class ReplaceInstructor {

        @Test
        @DisplayName("성공 - 강사 교체")
        void replaceInstructor_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createOngoingCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            ReplaceInstructorRequest request = new ReplaceInstructorRequest(200L, InstructorRole.MAIN, "강사 교체 사유");
            InstructorAssignmentResponse mockResponse = createMockResponse(2L, 200L, courseTime.getId(), InstructorRole.MAIN);

            given(instructorAssignmentService.replaceInstructor(eq(1L), any(), any()))
                    .willReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors/{assignmentId}/replace", courseTime.getId(), 1L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(2))
                    .andExpect(jsonPath("$.data.userId").value(200));
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수")
        void replaceInstructor_fail_closed() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            ReplaceInstructorRequest request = new ReplaceInstructorRequest(200L, InstructorRole.MAIN, null);

            // when & then
            mockMvc.perform(post("/api/ts/course-times/{timeId}/instructors/{assignmentId}/replace", courseTime.getId(), 1L)
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
    @DisplayName("DELETE /api/ts/course-times/{timeId}/instructors/{assignmentId} - 배정 취소")
    class CancelAssignment {

        @Test
        @DisplayName("성공 - 배정 취소")
        void cancelAssignment_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CancelAssignmentRequest request = new CancelAssignmentRequest("배정 취소 사유");

            doNothing().when(instructorAssignmentService).cancelAssignment(eq(1L), any(), any());

            // when & then
            mockMvc.perform(delete("/api/ts/course-times/{timeId}/instructors/{assignmentId}", courseTime.getId(), 1L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("성공 - Request Body 없이 배정 취소")
        void cancelAssignment_success_noBody() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            doNothing().when(instructorAssignmentService).cancelAssignment(eq(1L), any(), any());

            // when & then
            mockMvc.perform(delete("/api/ts/course-times/{timeId}/instructors/{assignmentId}", courseTime.getId(), 1L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - CLOSED 상태 차수")
        void cancelAssignment_fail_closed() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createClosedCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/ts/course-times/{timeId}/instructors/{assignmentId}", courseTime.getId(), 1L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("TS006"));
        }
    }
}
