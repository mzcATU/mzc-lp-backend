package com.mzc.lp.domain.ts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.category.repository.CategoryRepository;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.repository.CourseRepository;
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
    private CourseRepository courseRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        courseTimeRepository.deleteAll();
        courseRepository.deleteAll();
        categoryRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== Helper Methods ==========

    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
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

    private Category createCategory(String name, String code) {
        Category category = Category.create(name, code);
        return categoryRepository.save(category);
    }

    private Course createCourse(String title, String description, CourseLevel level, Long categoryId, String thumbnailUrl) {
        Course course = Course.create(
                title,
                description,
                level,
                null,
                null,
                categoryId,
                thumbnailUrl,
                null,
                1L
        );
        course.markAsReady();
        course.register();
        return courseRepository.save(course);
    }

    private CourseTime createTestCourseTime(Course course, String title, String description) {
        CourseTime courseTime = CourseTime.create(
                title,
                description,
                DeliveryType.ONLINE,
                DurationType.FIXED,
                LocalDate.now().plusDays(1),
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
        courseTime.linkCourseAndSnapshot(course, null);
        return courseTimeRepository.save(courseTime);
    }

    // ==================== CourseTime 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/times/{id} - 차수 상세 조회")
    class GetCourseTime {

        @Test
        @DisplayName("성공 - 차수 상세 조회 시 과정 메타데이터 포함 (카테고리, 난이도, 썸네일)")
        void getCourseTime_success_withCourseMetadata() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            Category category = createCategory("직무 역량", "JOB_SKILL");
            Course course = createCourse(
                    "영업 역량 강화 과정",
                    "영업 담당자를 위한 핵심 역량 교육",
                    CourseLevel.BEGINNER,
                    category.getId(),
                    "https://cdn.example.com/thumbnail.jpg"
            );
            CourseTime courseTime = createTestCourseTime(
                    course,
                    "2025년 1차",
                    "2025년 상반기 신입사원 대상 차수입니다."
            );

            // when & then
            mockMvc.perform(get("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(courseTime.getId()))
                    .andExpect(jsonPath("$.data.title").value("2025년 1차"))
                    .andExpect(jsonPath("$.data.description").value("2025년 상반기 신입사원 대상 차수입니다."))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.courseTitle").value("영업 역량 강화 과정"))
                    .andExpect(jsonPath("$.data.courseDescription").value("영업 담당자를 위한 핵심 역량 교육"))
                    .andExpect(jsonPath("$.data.courseCategory").value("직무 역량"))
                    .andExpect(jsonPath("$.data.courseDifficulty").value("BEGINNER"))
                    .andExpect(jsonPath("$.data.courseThumbnailUrl").value("https://cdn.example.com/thumbnail.jpg"));
        }

        @Test
        @DisplayName("성공 - 카테고리 없는 과정의 차수 조회")
        void getCourseTime_success_withoutCategory() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            Course course = createCourse(
                    "테스트 과정",
                    "테스트 설명",
                    CourseLevel.INTERMEDIATE,
                    null,  // 카테고리 없음
                    null   // 썸네일 없음
            );
            CourseTime courseTime = createTestCourseTime(course, "테스트 차수", null);

            // when & then
            mockMvc.perform(get("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseCategory").isEmpty())
                    .andExpect(jsonPath("$.data.courseDifficulty").value("INTERMEDIATE"))
                    .andExpect(jsonPath("$.data.courseThumbnailUrl").isEmpty());
        }

        @Test
        @DisplayName("성공 - 과정 연결 없는 차수 조회")
        void getCourseTime_success_withoutCourse() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CourseTime courseTime = CourseTime.create(
                    "독립 차수",
                    "과정 연결 없는 차수",
                    DeliveryType.ONLINE,
                    DurationType.FIXED,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(30),
                    null,
                    30,
                    5,
                    EnrollmentMethod.FIRST_COME,
                    80,
                    new BigDecimal("0"),
                    true,
                    null,
                    false,
                    null,
                    1L
            );
            courseTime = courseTimeRepository.save(courseTime);

            // when & then
            mockMvc.perform(get("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("독립 차수"))
                    .andExpect(jsonPath("$.data.description").value("과정 연결 없는 차수"))
                    .andExpect(jsonPath("$.data.courseId").isEmpty())
                    .andExpect(jsonPath("$.data.courseTitle").isEmpty())
                    .andExpect(jsonPath("$.data.courseCategory").isEmpty())
                    .andExpect(jsonPath("$.data.courseDifficulty").isEmpty())
                    .andExpect(jsonPath("$.data.courseThumbnailUrl").isEmpty());
        }

        @Test
        @DisplayName("성공 - 모든 난이도 레벨 테스트 (ADVANCED)")
        void getCourseTime_success_advancedLevel() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            Course course = createCourse(
                    "고급 과정",
                    "고급 설명",
                    CourseLevel.ADVANCED,
                    null,
                    null
            );
            CourseTime courseTime = createTestCourseTime(course, "고급 차수", null);

            // when & then
            mockMvc.perform(get("/api/times/{id}", courseTime.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.courseDifficulty").value("ADVANCED"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 차수 조회")
        void getCourseTime_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/times/{id}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("TS001"));
        }
    }
}
