package com.mzc.lp.domain.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.student.dto.request.ForceEnrollRequest;
import com.mzc.lp.domain.student.dto.request.UpdateProgressRequest;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.DurationType;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EnrollmentControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseTimeRepository.deleteAll();
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

    private User createNormalUser(String email, String name) {
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

    private CourseTime createRecruitingCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                DurationType.FIXED,
                LocalDate.now().minusDays(1),  // 과거 날짜이므로 RECRUITING 상태로 자동 생성
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
        // enrollStartDate가 과거이므로 이미 RECRUITING 상태
        return courseTimeRepository.save(courseTime);
    }

    private CourseTime createDraftCourseTime() {
        CourseTime courseTime = CourseTime.create(
                "Draft 차수",
                DeliveryType.ONLINE,
                DurationType.FIXED,
                LocalDate.now().plusDays(1),  // 미래 날짜로 변경하여 DRAFT 상태로 생성
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

    private Enrollment createEnrollment(Long userId, Long courseTimeId) {
        Enrollment enrollment = Enrollment.createVoluntary(userId, courseTimeId);
        return enrollmentRepository.save(enrollment);
    }

    // ==================== 수강 신청 테스트 ====================

    @Nested
    @DisplayName("POST /api/times/{courseTimeId}/enrollments - 수강 신청")
    class Enroll {

        @Test
        @DisplayName("성공 - 인증된 사용자가 수강 신청")
        void enroll_success() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime = createRecruitingCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{courseTimeId}/enrollments", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(user.getId()))
                    .andExpect(jsonPath("$.data.courseTimeId").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.status").value("ENROLLED"));
        }

        @Test
        @DisplayName("실패 - 중복 수강 신청")
        void enroll_fail_alreadyEnrolled() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime = createRecruitingCourseTime();
            createEnrollment(user.getId(), courseTime.getId());
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{courseTimeId}/enrollments", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("SIS002"));
        }

        @Test
        @DisplayName("실패 - 모집 기간이 아닌 차수")
        void enroll_fail_notRecruiting() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = createDraftCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{courseTimeId}/enrollments", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("SIS004"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void enroll_fail_unauthorized() throws Exception {
            // given
            CourseTime courseTime = createRecruitingCourseTime();

            // when & then
            mockMvc.perform(post("/api/times/{courseTimeId}/enrollments", courseTime.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 내 수강 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/users/me/enrollments - 내 수강 목록 조회")
    class GetMyEnrollments {

        @Test
        @DisplayName("성공 - 내 수강 목록 조회")
        void getMyEnrollments_success() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime1 = createRecruitingCourseTime();
            CourseTime courseTime2 = createRecruitingCourseTime();
            createEnrollment(user.getId(), courseTime1.getId());
            createEnrollment(user.getId(), courseTime2.getId());
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/enrollments")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 상태 필터링 (ENROLLED)")
        void getMyEnrollments_success_filterByStatus() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime1 = createRecruitingCourseTime();
            CourseTime courseTime2 = createRecruitingCourseTime();
            Enrollment enrollment1 = createEnrollment(user.getId(), courseTime1.getId());
            Enrollment enrollment2 = createEnrollment(user.getId(), courseTime2.getId());
            enrollment2.complete(90);
            enrollmentRepository.save(enrollment2);
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/enrollments")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("status", "ENROLLED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 수강 내역 없음")
        void getMyEnrollments_success_empty() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/enrollments")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("성공 - 페이징")
        void getMyEnrollments_success_paging() throws Exception {
            // given
            User user = createNormalUser();
            for (int i = 0; i < 5; i++) {
                CourseTime courseTime = createRecruitingCourseTime();
                createEnrollment(user.getId(), courseTime.getId());
            }
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/enrollments")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("page", "0")
                            .param("size", "2"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(5))
                    .andExpect(jsonPath("$.data.totalPages").value(3));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getMyEnrollments_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/me/enrollments"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 사용자별 수강 이력 조회 테스트 (관리자) ====================

    @Nested
    @DisplayName("GET /api/users/{userId}/enrollments - 사용자별 수강 이력 조회")
    class GetEnrollmentsByUser {

        @Test
        @DisplayName("성공 - OPERATOR가 사용자 수강 이력 조회")
        void getEnrollmentsByUser_success() throws Exception {
            // given
            createOperatorUser();
            User targetUser = createNormalUser("target@example.com", "대상사용자");
            CourseTime courseTime = createRecruitingCourseTime();
            createEnrollment(targetUser.getId(), courseTime.getId());
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/enrollments", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 타인 수강 이력 조회")
        void getEnrollmentsByUser_fail_userRole() throws Exception {
            // given
            createNormalUser();
            User targetUser = createNormalUser("target@example.com", "대상사용자");
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}/enrollments", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 수강 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/enrollments/{enrollmentId} - 수강 상세 조회")
    class GetEnrollment {

        @Test
        @DisplayName("성공 - 본인 수강 상세 조회")
        void getEnrollment_success() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime = createRecruitingCourseTime();
            Enrollment enrollment = createEnrollment(user.getId(), courseTime.getId());
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/enrollments/{enrollmentId}", enrollment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(enrollment.getId()))
                    .andExpect(jsonPath("$.data.userId").value(user.getId()))
                    .andExpect(jsonPath("$.data.status").value("ENROLLED"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수강")
        void getEnrollment_fail_notFound() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/enrollments/{enrollmentId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("SIS001"));
        }
    }

    // ==================== 진도율 업데이트 테스트 ====================

    @Nested
    @DisplayName("PATCH /api/enrollments/{enrollmentId}/progress - 진도율 업데이트")
    class UpdateProgress {

        @Test
        @DisplayName("성공 - 진도율 업데이트")
        void updateProgress_success() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime = createRecruitingCourseTime();
            Enrollment enrollment = createEnrollment(user.getId(), courseTime.getId());
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            UpdateProgressRequest request = new UpdateProgressRequest(1L, 50, 0);

            // when & then
            mockMvc.perform(patch("/api/enrollments/{enrollmentId}/progress", enrollment.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.progressPercent").value(50));
        }
    }

    // ==================== 강제 배정 테스트 ====================

    @Nested
    @DisplayName("POST /api/times/{courseTimeId}/enrollments/force - 강제 배정")
    class ForceEnroll {

        @Test
        @DisplayName("성공 - OPERATOR가 강제 배정")
        void forceEnroll_success() throws Exception {
            // given
            createOperatorUser();
            User user1 = createNormalUser("user1@example.com", "사용자1");
            User user2 = createNormalUser("user2@example.com", "사용자2");
            CourseTime courseTime = createRecruitingCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            ForceEnrollRequest request = new ForceEnrollRequest(
                    List.of(user1.getId(), user2.getId()),
                    "필수 교육"
            );

            // when & then
            mockMvc.perform(post("/api/times/{courseTimeId}/enrollments/force", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.successCount").value(2))
                    .andExpect(jsonPath("$.data.failCount").value(0));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 강제 배정")
        void forceEnroll_fail_userRole() throws Exception {
            // given
            createNormalUser();
            User targetUser = createNormalUser("target@example.com", "대상사용자");
            CourseTime courseTime = createRecruitingCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            ForceEnrollRequest request = new ForceEnrollRequest(
                    List.of(targetUser.getId()),
                    "강제 배정 시도"
            );

            // when & then
            mockMvc.perform(post("/api/times/{courseTimeId}/enrollments/force", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 내 학습 통계 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/users/me/learning-stats - 내 학습 통계 조회")
    class GetMyLearningStats {

        @Test
        @DisplayName("성공 - 내 학습 통계 조회")
        void getMyLearningStats_success() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime1 = createRecruitingCourseTime();
            CourseTime courseTime2 = createRecruitingCourseTime();
            CourseTime courseTime3 = createRecruitingCourseTime();

            createEnrollment(user.getId(), courseTime1.getId());
            Enrollment completedEnrollment = createEnrollment(user.getId(), courseTime2.getId());
            completedEnrollment.complete(90);
            enrollmentRepository.save(completedEnrollment);
            createEnrollment(user.getId(), courseTime3.getId());

            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/learning-stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.overview.totalCourses").value(3))
                    .andExpect(jsonPath("$.data.overview.completed").value(1))
                    .andExpect(jsonPath("$.data.overview.inProgress").value(2))
                    .andExpect(jsonPath("$.data.overview.byType.voluntary").value(3))
                    .andExpect(jsonPath("$.data.overview.byType.mandatory").value(0))
                    .andExpect(jsonPath("$.data.progress").exists());
        }

        @Test
        @DisplayName("성공 - 수강 이력 없음")
        void getMyLearningStats_success_empty() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/me/learning-stats")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.overview.totalCourses").value(0))
                    .andExpect(jsonPath("$.data.overview.completed").value(0))
                    .andExpect(jsonPath("$.data.overview.inProgress").value(0))
                    .andExpect(jsonPath("$.data.overview.completionRate").value(0))
                    .andExpect(jsonPath("$.data.overview.byType.voluntary").value(0))
                    .andExpect(jsonPath("$.data.overview.byType.mandatory").value(0));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getMyLearningStats_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/me/learning-stats"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 수강 취소 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/enrollments/{enrollmentId} - 수강 취소")
    class CancelEnrollment {

        @Test
        @DisplayName("성공 - 수강 취소")
        void cancelEnrollment_success() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime = createRecruitingCourseTime();
            Enrollment enrollment = createEnrollment(user.getId(), courseTime.getId());
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/enrollments/{enrollmentId}", enrollment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 수료 상태에서 취소")
        void cancelEnrollment_fail_completed() throws Exception {
            // given
            User user = createNormalUser();
            CourseTime courseTime = createRecruitingCourseTime();
            Enrollment enrollment = createEnrollment(user.getId(), courseTime.getId());
            enrollment.complete(90);
            enrollmentRepository.save(enrollment);
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/enrollments/{enrollmentId}", enrollment.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("SIS003"));
        }
    }
}
