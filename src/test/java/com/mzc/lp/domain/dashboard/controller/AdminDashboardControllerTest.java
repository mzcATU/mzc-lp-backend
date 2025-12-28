package com.mzc.lp.domain.dashboard.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
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
class AdminDashboardControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseTimeRepository.deleteAll();
        programRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createTenantAdminUser() {
        User user = User.create("admin@example.com", "테넌트관리자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.TENANT_ADMIN);
        return userRepository.save(user);
    }

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

    private Program createApprovedProgram() {
        Program program = Program.create("승인된 프로그램", 1L);
        program.submit();
        program.approve(1L, "승인");
        return programRepository.save(program);
    }

    private Program createDraftProgram() {
        Program program = Program.create("임시저장 프로그램", 1L);
        return programRepository.save(program);
    }

    private Program createPendingProgram() {
        Program program = Program.create("승인대기 프로그램", 1L);
        program.submit();
        return programRepository.save(program);
    }

    private CourseTime createCourseTime(Long programId) {
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
                programId
        );
        courseTime.open();
        return courseTimeRepository.save(courseTime);
    }

    private Enrollment createEnrollment(Long userId, Long courseTimeId) {
        Enrollment enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        return enrollmentRepository.save(enrollment);
    }

    // ==================== KPI 대시보드 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/admin/dashboard/kpi - 관리자 KPI 대시보드 조회")
    class GetKpiStats {

        @Test
        @DisplayName("성공 - KPI 대시보드 전체 통계 조회")
        void getKpiStats_success() throws Exception {
            // given
            User admin = createTenantAdminUser();
            User user = createNormalUser();

            // 프로그램 생성
            Program approvedProgram = createApprovedProgram();
            createDraftProgram();
            createPendingProgram();

            // 차수 생성
            CourseTime courseTime = createCourseTime(approvedProgram.getId());

            // 수강 생성
            createEnrollment(user.getId(), courseTime.getId());
            Enrollment completedEnrollment = createEnrollment(admin.getId(), courseTime.getId());
            completedEnrollment.complete(90);
            enrollmentRepository.save(completedEnrollment);

            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/admin/dashboard/kpi")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    // UserStats
                    .andExpect(jsonPath("$.data.userStats.total").value(2))
                    .andExpect(jsonPath("$.data.userStats.active").value(2))
                    // ProgramStats
                    .andExpect(jsonPath("$.data.programStats.total").value(3))
                    .andExpect(jsonPath("$.data.programStats.draft").value(1))
                    .andExpect(jsonPath("$.data.programStats.pending").value(1))
                    .andExpect(jsonPath("$.data.programStats.approved").value(1))
                    // EnrollmentStats
                    .andExpect(jsonPath("$.data.enrollmentStats.totalEnrollments").value(2))
                    .andExpect(jsonPath("$.data.enrollmentStats.byStatus.enrolled").value(1))
                    .andExpect(jsonPath("$.data.enrollmentStats.byStatus.completed").value(1))
                    // MonthlyTrend
                    .andExpect(jsonPath("$.data.monthlyTrend").isArray());
        }

        @Test
        @DisplayName("성공 - 데이터 없는 경우")
        void getKpiStats_success_noData() throws Exception {
            // given
            createTenantAdminUser();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");

            // when & then
            MvcResult result = mockMvc.perform(get("/api/admin/dashboard/kpi")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andReturn();

            assertThat(result.getResponse().getStatus()).isEqualTo(200);
        }

        @Test
        @DisplayName("실패 - OPERATOR는 접근 불가")
        void getKpiStats_fail_operatorUnauthorized() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/admin/dashboard/kpi")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 일반 사용자 접근 불가")
        void getKpiStats_fail_unauthorized() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/admin/dashboard/kpi")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getKpiStats_fail_noAuth() throws Exception {
            // when & then
            mockMvc.perform(get("/api/admin/dashboard/kpi"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
