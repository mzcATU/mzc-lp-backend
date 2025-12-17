package com.mzc.lp.domain.ts.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.iis.service.InstructorAssignmentService;
import com.mzc.lp.domain.ts.constant.CourseTimeStatus;
import com.mzc.lp.domain.ts.constant.DeliveryType;
import com.mzc.lp.domain.ts.constant.EnrollmentMethod;
import com.mzc.lp.domain.ts.dto.request.CloneCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.CreateCourseTimeRequest;
import com.mzc.lp.domain.ts.dto.request.UpdateCourseTimeRequest;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CourseTimeControllerTest extends TenantTestSupport {

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

    private CreateCourseTimeRequest createValidRequest() {
        return new CreateCourseTimeRequest(
                null,
                null,
                "스프링 부트 마스터 1차",
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
                true
        );
    }

    private CreateCourseTimeRequest createOfflineRequest() {
        return new CreateCourseTimeRequest(
                null,
                null,
                "오프라인 교육 1차",
                DeliveryType.OFFLINE,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(30),
                20,
                null,
                EnrollmentMethod.APPROVAL,
                70,
                new BigDecimal("200000"),
                false,
                "{\"address\": \"서울시 강남구\", \"room\": \"A동 101호\"}",
                false
        );
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

    // ==================== 차수 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/times - 차수 생성")
    class CreateCourseTime {

        @Test
        @DisplayName("성공 - OPERATOR가 온라인 차수 생성")
        void createCourseTime_success_online() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseTimeRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/times")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("스프링 부트 마스터 1차"))
                    .andExpect(jsonPath("$.data.deliveryType").value("ONLINE"))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andExpect(jsonPath("$.data.capacity").value(30))
                    .andExpect(jsonPath("$.data.currentEnrollment").value(0));
        }

        @Test
        @DisplayName("성공 - OPERATOR가 오프라인 차수 생성")
        void createCourseTime_success_offline() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseTimeRequest request = createOfflineRequest();

            // when & then
            mockMvc.perform(post("/api/times")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.deliveryType").value("OFFLINE"))
                    .andExpect(jsonPath("$.data.locationInfo").isNotEmpty());
        }

        @Test
        @DisplayName("실패 - 오프라인인데 장소 정보 없음")
        void createCourseTime_fail_offlineWithoutLocation() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    null, null, "오프라인 교육",
                    DeliveryType.OFFLINE,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(30),
                    20, null, EnrollmentMethod.FIRST_COME, 70,
                    new BigDecimal("100000"), false,
                    null,  // 장소 정보 없음
                    false
            );

            // when & then
            mockMvc.perform(post("/api/times")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS005"));
        }

        @Test
        @DisplayName("실패 - 모집 종료일이 학습 종료일 이후")
        void createCourseTime_fail_invalidDateRange() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseTimeRequest request = new CreateCourseTimeRequest(
                    null, null, "날짜 오류 테스트",
                    DeliveryType.ONLINE,
                    LocalDate.now(), LocalDate.now().plusDays(60),  // 모집 종료일이 학습 종료일 이후
                    LocalDate.now().plusDays(7), LocalDate.now().plusDays(30),
                    20, null, EnrollmentMethod.FIRST_COME, 70,
                    new BigDecimal("100000"), false, null, false
            );

            // when & then
            mockMvc.perform(post("/api/times")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS004"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createCourseTime_fail_userRole() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            CreateCourseTimeRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/times")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void createCourseTime_fail_unauthorized() throws Exception {
            // given
            CreateCourseTimeRequest request = createValidRequest();

            // when & then
            mockMvc.perform(post("/api/times")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 차수 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/times - 차수 목록 조회")
    class GetCourseTimes {

        @Test
        @DisplayName("성공 - 인증된 사용자가 목록 조회")
        void getCourseTimes_success() throws Exception {
            // given
            createNormalUser();
            createTestCourseTime();
            createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 상태별 필터링")
        void getCourseTimes_success_filterByStatus() throws Exception {
            // given
            createNormalUser();
            CourseTime draft = createTestCourseTime();
            CourseTime recruiting = createTestCourseTime();

            // MAIN 강사 Mock (상태 전이를 위해)
            when(instructorAssignmentService.existsMainInstructor(anyLong())).thenReturn(true);

            recruiting.open();
            courseTimeRepository.save(recruiting);
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("status", "RECRUITING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    // ==================== 차수 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/times/{id} - 차수 상세 조회")
    class GetCourseTime {

        @Test
        @DisplayName("성공 - 인증된 사용자가 상세 조회")
        void getCourseTime_success() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.title").value("테스트 차수"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void getCourseTime_fail_notFound() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }
    }

    // ==================== 차수 수정 테스트 ====================

    @Nested
    @DisplayName("PATCH /api/times/{id} - 차수 수정")
    class UpdateCourseTime {

        @Test
        @DisplayName("성공 - DRAFT 상태 차수 수정")
        void updateCourseTime_success_draft() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            UpdateCourseTimeRequest request = new UpdateCourseTimeRequest(
                    "수정된 제목",
                    null,  // deliveryType
                    null, null, null, null,  // dates
                    50, null,  // capacity, maxWaitingCount
                    null,  // enrollmentMethod
                    null,  // minProgressForCompletion
                    new BigDecimal("150000"), null,  // price, isFree
                    null, null  // locationInfo, allowLateEnrollment
            );

            // when & then
            mockMvc.perform(patch("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.capacity").value(50))
                    .andExpect(jsonPath("$.data.price").value(150000));
        }

        @Test
        @DisplayName("실패 - ONGOING 상태 차수 수정")
        void updateCourseTime_fail_ongoing() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTime.startClass();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            UpdateCourseTimeRequest request = new UpdateCourseTimeRequest(
                    "수정 시도", null, null, null, null, null,
                    null, null, null, null, null, null, null, null
            );

            // when & then
            mockMvc.perform(patch("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS002"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 수정 시도")
        void updateCourseTime_fail_userRole() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            UpdateCourseTimeRequest request = new UpdateCourseTimeRequest(
                    "수정 시도", null, null, null, null, null,
                    null, null, null, null, null, null, null, null
            );

            // when & then
            mockMvc.perform(patch("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 차수 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/times/{id} - 차수 삭제")
    class DeleteCourseTime {

        @Test
        @DisplayName("성공 - DRAFT 상태 차수 삭제")
        void deleteCourseTime_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - RECRUITING 상태 차수 삭제")
        void deleteCourseTime_fail_recruiting() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS002"));
        }
    }

    // ==================== 상태 전이 테스트 ====================

    @Nested
    @DisplayName("POST /api/times/{id}/open - 모집 시작")
    class OpenCourseTime {

        @Test
        @DisplayName("성공 - DRAFT → RECRUITING (MAIN 강사 배정됨)")
        void openCourseTime_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // MAIN 강사가 배정되어 있음을 Mock
            when(instructorAssignmentService.existsMainInstructor(courseTime.getId())).thenReturn(true);

            // when & then
            mockMvc.perform(post("/api/times/{id}/open", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("RECRUITING"));
        }

        @Test
        @DisplayName("실패 - MAIN 강사 미배정")
        void openCourseTime_fail_noMainInstructor() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // MAIN 강사가 배정되어 있지 않음을 Mock
            when(instructorAssignmentService.existsMainInstructor(courseTime.getId())).thenReturn(false);

            // when & then
            mockMvc.perform(post("/api/times/{id}/open", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS008"));
        }

        @Test
        @DisplayName("실패 - RECRUITING 상태에서 open 시도")
        void openCourseTime_fail_alreadyRecruiting() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();

            // MAIN 강사 Mock (상태 전이를 위해)
            when(instructorAssignmentService.existsMainInstructor(courseTime.getId())).thenReturn(true);

            courseTime.open();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{id}/open", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("TS002"));
        }
    }

    @Nested
    @DisplayName("POST /api/times/{id}/start - 학습 시작")
    class StartCourseTime {

        @Test
        @DisplayName("성공 - RECRUITING → ONGOING")
        void startCourseTime_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{id}/start", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ONGOING"));
        }
    }

    @Nested
    @DisplayName("POST /api/times/{id}/close - 학습 종료")
    class CloseCourseTime {

        @Test
        @DisplayName("성공 - ONGOING → CLOSED")
        void closeCourseTime_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTime.startClass();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{id}/close", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));
        }
    }

    @Nested
    @DisplayName("POST /api/times/{id}/archive - 아카이브")
    class ArchiveCourseTime {

        @Test
        @DisplayName("성공 - CLOSED → ARCHIVED")
        void archiveCourseTime_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime courseTime = createTestCourseTime();
            courseTime.open();
            courseTime.startClass();
            courseTime.close();
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/times/{id}/archive", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
        }
    }

    // ==================== Public API 테스트 ====================

    @Nested
    @DisplayName("GET /api/times/{id}/capacity - 정원 조회")
    class GetCapacity {

        @Test
        @DisplayName("성공 - 정원 조회 (정원 제한 있음)")
        void getCapacity_success_limited() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}/capacity", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseTimeId").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.capacity").value(30))
                    .andExpect(jsonPath("$.data.currentEnrollment").value(0))
                    .andExpect(jsonPath("$.data.availableSeats").value(30))
                    .andExpect(jsonPath("$.data.unlimited").value(false));
        }

        @Test
        @DisplayName("성공 - 정원 조회 (무제한)")
        void getCapacity_success_unlimited() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = CourseTime.create(
                    "무제한 정원 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    null,  // capacity = null (무제한)
                    null,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("100000"),
                    false,
                    null,
                    true,
                    1L
            );
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}/capacity", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.capacity").isEmpty())
                    .andExpect(jsonPath("$.data.unlimited").value(true))
                    .andExpect(jsonPath("$.data.availableSeats").isEmpty());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수")
        void getCapacity_fail_notFound() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}/capacity", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }
    }

    @Nested
    @DisplayName("GET /api/times/{id}/price - 가격 조회")
    class GetPrice {

        @Test
        @DisplayName("성공 - 유료 차수 가격 조회")
        void getPrice_success_paid() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}/price", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseTimeId").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.price").value(100000))
                    .andExpect(jsonPath("$.data.free").value(false));
        }

        @Test
        @DisplayName("성공 - 무료 차수 가격 조회")
        void getPrice_success_free() throws Exception {
            // given
            createNormalUser();
            CourseTime courseTime = CourseTime.create(
                    "무료 차수",
                    DeliveryType.ONLINE,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    30,
                    null,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    BigDecimal.ZERO,
                    true,  // isFree = true
                    null,
                    true,
                    1L
            );
            courseTimeRepository.save(courseTime);
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}/price", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.price").value(0))
                    .andExpect(jsonPath("$.data.free").value(true));
        }
    }

    // ==================== 차수 복제 테스트 ====================

    @Nested
    @DisplayName("POST /api/times/{id}/clone - 차수 복제")
    class CloneCourseTime {

        @Test
        @DisplayName("성공 - 차수 복제")
        void cloneCourseTime_success() throws Exception {
            // given
            createOperatorUser();
            CourseTime source = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CloneCourseTimeRequest request = new CloneCourseTimeRequest(
                    "복제된 차수",
                    LocalDate.now().plusMonths(1),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(2)
            );

            // when & then
            mockMvc.perform(post("/api/times/{id}/clone", source.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("복제된 차수"))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andExpect(jsonPath("$.data.deliveryType").value(source.getDeliveryType().name()))
                    .andExpect(jsonPath("$.data.capacity").value(source.getCapacity()))
                    .andExpect(jsonPath("$.data.price").value(source.getPrice().intValue()))
                    .andExpect(jsonPath("$.data.currentEnrollment").value(0));
        }

        @Test
        @DisplayName("성공 - ONGOING 상태의 차수도 복제 가능")
        void cloneCourseTime_success_fromOngoing() throws Exception {
            // given
            createOperatorUser();
            CourseTime source = createTestCourseTime();

            // MAIN 강사 Mock 설정 후 상태 전이
            when(instructorAssignmentService.existsMainInstructor(source.getId())).thenReturn(true);
            source.open();
            source.startClass();
            courseTimeRepository.save(source);

            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CloneCourseTimeRequest request = new CloneCourseTimeRequest(
                    "ONGOING에서 복제",
                    LocalDate.now().plusMonths(1),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(2)
            );

            // when & then
            mockMvc.perform(post("/api/times/{id}/clone", source.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"));
        }

        @Test
        @DisplayName("실패 - 원본 차수 미존재")
        void cloneCourseTime_fail_sourceNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CloneCourseTimeRequest request = new CloneCourseTimeRequest(
                    "복제된 차수",
                    LocalDate.now().plusMonths(1),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(2)
            );

            // when & then
            mockMvc.perform(post("/api/times/{id}/clone", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }

        @Test
        @DisplayName("실패 - 잘못된 날짜 범위 (모집 종료일 > 학습 종료일)")
        void cloneCourseTime_fail_invalidDateRange() throws Exception {
            // given
            createOperatorUser();
            CourseTime source = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CloneCourseTimeRequest request = new CloneCourseTimeRequest(
                    "복제된 차수",
                    LocalDate.now().plusMonths(1),
                    LocalDate.now().plusMonths(3),  // 모집 종료일
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(2)   // 학습 종료일 (모집 종료일보다 이전)
            );

            // when & then
            mockMvc.perform(post("/api/times/{id}/clone", source.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS004"));
        }

        @Test
        @DisplayName("실패 - 제목 누락")
        void cloneCourseTime_fail_titleRequired() throws Exception {
            // given
            createOperatorUser();
            CourseTime source = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CloneCourseTimeRequest request = new CloneCourseTimeRequest(
                    "",  // 빈 제목
                    LocalDate.now().plusMonths(1),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(2)
            );

            // when & then
            mockMvc.perform(post("/api/times/{id}/clone", source.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 권한 없음 (일반 사용자)")
        void cloneCourseTime_fail_forbidden() throws Exception {
            // given
            createNormalUser();
            CourseTime source = createTestCourseTime();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            CloneCourseTimeRequest request = new CloneCourseTimeRequest(
                    "복제된 차수",
                    LocalDate.now().plusMonths(1),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(1).plusDays(7),
                    LocalDate.now().plusMonths(2)
            );

            // when & then
            mockMvc.perform(post("/api/times/{id}/clone", source.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
