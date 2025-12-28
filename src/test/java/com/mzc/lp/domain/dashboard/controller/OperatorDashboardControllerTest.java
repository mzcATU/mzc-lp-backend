package com.mzc.lp.domain.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.iis.constant.InstructorRole;
import com.mzc.lp.domain.iis.entity.InstructorAssignment;
import com.mzc.lp.domain.iis.repository.InstructorAssignmentRepository;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OperatorDashboardControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private InstructorAssignmentRepository instructorAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        instructorAssignmentRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseTimeRepository.deleteAll();
        programRepository.deleteAll();
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

    private Program createPendingProgram() {
        Program program = Program.create("테스트 프로그램", 1L);
        program.submit();
        return programRepository.save(program);
    }

    private CourseTime createRecruitingCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "모집중 차수",
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
        courseTime.open();
        return courseTimeRepository.save(courseTime);
    }

    private CourseTime createOngoingCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "진행중 차수",
                DeliveryType.OFFLINE,
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(30),
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                BigDecimal.ZERO,
                true,
                "{\"address\": \"서울시\"}",
                true,
                1L
        );
        courseTime.open();
        courseTime.startClass();
        return courseTimeRepository.save(courseTime);
    }

    private CourseTime createDraftCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "Draft 차수",
                DeliveryType.BLENDED,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                30,
                5,
                EnrollmentMethod.FIRST_COME,
                80,
                new BigDecimal("50000"),
                false,
                null,
                true,
                1L
        );
        return courseTimeRepository.save(courseTime);
    }

    private InstructorAssignment createInstructorAssignment(Long courseTimeId, Long userId) {
        InstructorAssignment assignment = InstructorAssignment.create(
                userId,
                courseTimeId,
                InstructorRole.MAIN,
                userId
        );
        return instructorAssignmentRepository.save(assignment);
    }

    private Enrollment createEnrollment(Long userId, Long courseTimeId) {
        Enrollment enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        return enrollmentRepository.save(enrollment);
    }

    // ==================== 운영 대시보드 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/operator/dashboard/tasks - 운영 대시보드 조회")
    class GetOperatorTasks {

        @Test
        @DisplayName("성공 - 운영 대시보드 전체 통계 조회")
        void getOperatorTasks_success() throws Exception {
            // given
            User operator = createOperatorUser();
            User user = createNormalUser();

            // 승인 대기 프로그램 생성
            createPendingProgram();
            createPendingProgram();

            // 차수 생성 (강사 미배정 포함)
            CourseTime recruiting1 = createRecruitingCourseTime();
            CourseTime recruiting2 = createRecruitingCourseTime();
            createOngoingCourseTime();
            createDraftCourseTime();

            // recruiting1에만 강사 배정 (recruiting2, ongoing은 강사 미배정)
            createInstructorAssignment(recruiting1.getId(), operator.getId());

            // 수강 생성
            createEnrollment(user.getId(), recruiting1.getId());
            Enrollment completedEnrollment = createEnrollment(user.getId(), recruiting2.getId());
            completedEnrollment.complete(90);
            enrollmentRepository.save(completedEnrollment);

            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/operator/dashboard/tasks")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    // PendingTasks
                    .andExpect(jsonPath("$.data.pendingTasks.programsPendingApproval").value(2))
                    .andExpect(jsonPath("$.data.pendingTasks.courseTimesNeedingInstructor").value(2))
                    // CourseTimeStats
                    .andExpect(jsonPath("$.data.courseTimeStats.total").value(4))
                    .andExpect(jsonPath("$.data.courseTimeStats.byStatus.draft").value(1))
                    .andExpect(jsonPath("$.data.courseTimeStats.byStatus.recruiting").value(2))
                    .andExpect(jsonPath("$.data.courseTimeStats.byStatus.ongoing").value(1))
                    .andExpect(jsonPath("$.data.courseTimeStats.byDeliveryType.online").value(2))
                    .andExpect(jsonPath("$.data.courseTimeStats.byDeliveryType.offline").value(1))
                    .andExpect(jsonPath("$.data.courseTimeStats.byDeliveryType.blended").value(1))
                    .andExpect(jsonPath("$.data.courseTimeStats.freeVsPaid.free").value(1))
                    .andExpect(jsonPath("$.data.courseTimeStats.freeVsPaid.paid").value(3))
                    // EnrollmentStats
                    .andExpect(jsonPath("$.data.enrollmentStats.totalEnrollments").value(2))
                    .andExpect(jsonPath("$.data.enrollmentStats.byStatus.enrolled").value(1))
                    .andExpect(jsonPath("$.data.enrollmentStats.byStatus.completed").value(1))
                    .andExpect(jsonPath("$.data.enrollmentStats.byType.voluntary").value(2))
                    .andExpect(jsonPath("$.data.enrollmentStats.byType.mandatory").value(0))
                    // DailyTrend
                    .andExpect(jsonPath("$.data.dailyTrend").isArray());
        }

        @Test
        @DisplayName("성공 - 데이터 없는 경우")
        void getOperatorTasks_success_noData() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            MvcResult result = mockMvc.perform(get("/api/operator/dashboard/tasks")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andReturn();

            System.out.println("Response Body: " + result.getResponse().getContentAsString());
            System.out.println("Status: " + result.getResponse().getStatus());

            assertThat(result.getResponse().getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("실패 - 일반 사용자 접근 불가")
        void getOperatorTasks_fail_unauthorized() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/operator/dashboard/tasks")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getOperatorTasks_fail_noAuth() throws Exception {
            // when & then
            mockMvc.perform(get("/api/operator/dashboard/tasks"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
