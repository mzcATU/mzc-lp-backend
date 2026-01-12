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
import com.mzc.lp.domain.user.entity.UserCourseRole;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OwnerStatsControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

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
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createOwnerUser() {
        User user = User.create("owner@example.com", "강의소유자", passwordEncoder.encode("Password123!"));
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

    private void grantOwnerRole(User user, Long courseId) {
        UserCourseRole ownerRole = UserCourseRole.createCourseDesigner(user, courseId);
        userCourseRoleRepository.save(ownerRole);
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

    private Program createApprovedProgram(Long createdBy) {
        Program program = Program.create("테스트 프로그램", createdBy);
        program.submit();
        program.approve(1L, "승인");
        return programRepository.save(program);
    }

    private CourseTime createCourseTime(Program program) {
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
                program.getCreatedBy()
        );
        courseTime.linkProgram(program);
        courseTime.open();
        return courseTimeRepository.save(courseTime);
    }

    private Enrollment createEnrollment(Long userId, Long courseTimeId) {
        Enrollment enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        return enrollmentRepository.save(enrollment);
    }

    // ==================== OWNER 통계 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/owners/me/stats - OWNER 내 강의 통계 조회")
    class GetOwnerStats {

        @Test
        @DisplayName("성공 - OWNER 권한으로 전체 통계 조회")
        void getOwnerStats_success() throws Exception {
            // given
            User owner = createOwnerUser();
            User student = createNormalUser();

            // 프로그램 생성 (owner가 생성자)
            Program program1 = createApprovedProgram(owner.getId());
            Program program2 = createApprovedProgram(owner.getId());

            // OWNER 역할 부여
            grantOwnerRole(owner, program1.getId());
            grantOwnerRole(owner, program2.getId());

            // 차수 생성
            CourseTime courseTime1 = createCourseTime(program1);
            CourseTime courseTime2 = createCourseTime(program1);
            CourseTime courseTime3 = createCourseTime(program2);

            // 수강 생성
            createEnrollment(student.getId(), courseTime1.getId());
            Enrollment completedEnrollment = createEnrollment(student.getId(), courseTime2.getId());
            completedEnrollment.complete(90);
            enrollmentRepository.save(completedEnrollment);
            createEnrollment(student.getId(), courseTime3.getId());

            String accessToken = loginAndGetAccessToken("owner@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/owners/me/stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    // Overview
                    .andExpect(jsonPath("$.data.overview.totalPrograms").value(2))
                    .andExpect(jsonPath("$.data.overview.totalCourseTimes").value(3))
                    .andExpect(jsonPath("$.data.overview.totalStudents").value(3))
                    // EnrollmentStats
                    .andExpect(jsonPath("$.data.enrollmentStats.totalEnrollments").value(3))
                    .andExpect(jsonPath("$.data.enrollmentStats.byStatus.enrolled").value(2))
                    .andExpect(jsonPath("$.data.enrollmentStats.byStatus.completed").value(1))
                    // ProgramStats
                    .andExpect(jsonPath("$.data.programStats").isArray())
                    .andExpect(jsonPath("$.data.programStats.length()").value(2));
        }

        @Test
        @DisplayName("성공 - OPERATOR 권한으로 통계 조회 (본인 소유 프로그램)")
        void getOwnerStats_success_operatorAccess() throws Exception {
            // given
            User operator = createOperatorUser();

            // OPERATOR가 프로그램 생성
            Program program = createApprovedProgram(operator.getId());
            createCourseTime(program);

            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/owners/me/stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.overview.totalPrograms").value(1))
                    .andExpect(jsonPath("$.data.overview.totalCourseTimes").value(1));
        }

        @Test
        @DisplayName("성공 - 소유 프로그램 없는 경우")
        void getOwnerStats_success_noPrograms() throws Exception {
            // given
            User owner = createOwnerUser();
            grantOwnerRole(owner, 999L);  // 존재하지 않는 프로그램 ID

            String accessToken = loginAndGetAccessToken("owner@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/owners/me/stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.overview.totalPrograms").value(0))
                    .andExpect(jsonPath("$.data.overview.totalCourseTimes").value(0))
                    .andExpect(jsonPath("$.data.overview.totalStudents").value(0))
                    .andExpect(jsonPath("$.data.programStats").isEmpty());
        }

        @Test
        @DisplayName("성공 - 다른 사람이 생성한 프로그램은 조회 안됨")
        void getOwnerStats_success_onlyOwnPrograms() throws Exception {
            // given
            User owner = createOwnerUser();
            User otherUser = createNormalUser();

            // owner가 생성한 프로그램
            Program myProgram = createApprovedProgram(owner.getId());
            grantOwnerRole(owner, myProgram.getId());
            createCourseTime(myProgram);

            // 다른 사용자가 생성한 프로그램
            Program otherProgram = createApprovedProgram(otherUser.getId());
            createCourseTime(otherProgram);
            createCourseTime(otherProgram);

            String accessToken = loginAndGetAccessToken("owner@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/owners/me/stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    // 내 프로그램만 조회됨
                    .andExpect(jsonPath("$.data.overview.totalPrograms").value(1))
                    .andExpect(jsonPath("$.data.overview.totalCourseTimes").value(1))
                    .andExpect(jsonPath("$.data.programStats.length()").value(1));
        }

        @Test
        @DisplayName("실패 - OWNER 역할 없는 일반 사용자 접근 불가")
        void getOwnerStats_fail_unauthorized() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/owners/me/stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getOwnerStats_fail_noAuth() throws Exception {
            // when & then
            mockMvc.perform(get("/api/owners/me/stats"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
