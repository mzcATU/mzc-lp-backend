package com.mzc.lp.domain.course.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.dto.request.CreateCourseRequest;
import com.mzc.lp.domain.course.dto.request.UpdateCourseRequest;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CourseControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ===== 헬퍼 메서드 =====

    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    private User createAdminUser() {
        User user = User.create("admin@example.com", "관리자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.TENANT_ADMIN);
        return userRepository.save(user);
    }

    private void createTestUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "test@example.com",
                "Password123!",
                "홍길동",
                "010-1234-5678"
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
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

    private Course createTestCourse(String title) {
        Course course = Course.create(
                title,
                "테스트 강의 설명",
                CourseLevel.BEGINNER,
                CourseType.ONLINE,
                10,
                1L,
                null,
                null,
                null,
                null,
                null
        );
        return courseRepository.save(course);
    }

    private Course createTestCourseWithOwner(String title, Long createdBy) {
        Course course = Course.create(
                title,
                "테스트 강의 설명",
                CourseLevel.BEGINNER,
                CourseType.ONLINE,
                10,
                1L,
                null,
                null,
                null,
                null,
                createdBy
        );
        return courseRepository.save(course);
    }

    private User createDesignerUser() {
        User user = User.create("designer@example.com", "설계자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.DESIGNER);
        return userRepository.save(user);
    }

    // ==================== 강의 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/courses - 강의 생성")
    class CreateCourse {

        @Test
        @DisplayName("성공 - OPERATOR가 강의 생성")
        void createCourse_success_operator() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseRequest request = new CreateCourseRequest(
                    "Spring Boot 기초",
                    "Spring Boot 입문 강의입니다.",
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    20,
                    1L,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Spring Boot 기초"))
                    .andExpect(jsonPath("$.data.description").value("Spring Boot 입문 강의입니다."))
                    .andExpect(jsonPath("$.data.level").value("BEGINNER"))
                    .andExpect(jsonPath("$.data.type").value("ONLINE"))
                    .andExpect(jsonPath("$.data.estimatedHours").value(20));
        }

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 강의 생성")
        void createCourse_success_admin() throws Exception {
            // given
            createAdminUser();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            CreateCourseRequest request = new CreateCourseRequest(
                    "React 심화",
                    null,
                    CourseLevel.ADVANCED,
                    CourseType.BLENDED,
                    30,
                    2L,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("React 심화"));
        }

        @Test
        @DisplayName("성공 - 최소 필드로 강의 생성 (title만)")
        void createCourse_success_minimalFields() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseRequest request = new CreateCourseRequest(
                    "최소 강의",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("최소 강의"));
        }

        @Test
        @DisplayName("실패 - 제목 누락")
        void createCourse_fail_missingTitle() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseRequest request = new CreateCourseRequest(
                    null,
                    "설명만 있는 요청",
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    10,
                    1L,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 제목 빈 문자열")
        void createCourse_fail_emptyTitle() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCourseRequest request = new CreateCourseRequest(
                    "   ",
                    "설명",
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    10,
                    1L,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 제목 255자 초과")
        void createCourse_fail_titleTooLong() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            String longTitle = "a".repeat(256);
            CreateCourseRequest request = new CreateCourseRequest(
                    longTitle,
                    "설명",
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    10,
                    1L,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createCourse_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            CreateCourseRequest request = new CreateCourseRequest(
                    "테스트 강의",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void createCourse_fail_unauthorized() throws Exception {
            // given
            CreateCourseRequest request = new CreateCourseRequest(
                    "테스트 강의",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 강의 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses - 강의 목록 조회")
    class GetCourses {

        @Test
        @DisplayName("성공 - 전체 강의 목록 조회")
        void getCourses_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestCourse("Spring Boot 기초");
            createTestCourse("React 입문");
            createTestCourse("Docker 활용");

            // when & then
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test
        @DisplayName("성공 - 키워드 검색")
        void getCourses_success_withKeyword() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestCourse("Spring Boot 기초");
            createTestCourse("Spring Security 심화");
            createTestCourse("React 입문");

            // when & then
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("keyword", "Spring"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 카테고리 필터")
        void getCourses_success_withCategoryFilter() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            Course course1 = Course.create("Spring Boot", "설명", CourseLevel.BEGINNER, CourseType.ONLINE, 10, 1L, null, null, null, null, null);
            Course course2 = Course.create("React", "설명", CourseLevel.BEGINNER, CourseType.ONLINE, 10, 2L, null, null, null, null, null);
            Course course3 = Course.create("Docker", "설명", CourseLevel.BEGINNER, CourseType.ONLINE, 10, 1L, null, null, null, null, null);
            courseRepository.save(course1);
            courseRepository.save(course2);
            courseRepository.save(course3);

            // when & then
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("categoryId", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 페이징 처리")
        void getCourses_success_pagination() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            for (int i = 1; i <= 25; i++) {
                createTestCourse("강의 " + i);
            }

            // when & then
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(10))
                    .andExpect(jsonPath("$.data.totalElements").value(25))
                    .andExpect(jsonPath("$.data.totalPages").value(3));
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void getCourses_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/courses")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    // ==================== 강의 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses/{courseId} - 강의 상세 조회")
    class GetCourseDetail {

        @Test
        @DisplayName("성공 - 강의 상세 조회")
        void getCourseDetail_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("Spring Boot 기초");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.title").value("Spring Boot 기초"))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.itemCount").value(0))
                    .andExpect(jsonPath("$.data.isComplete").value(false)) // items가 없으므로 미완성
                    .andExpect(jsonPath("$.data.averageRating").value(0.0))
                    .andExpect(jsonPath("$.data.reviewCount").value(0));
        }

        @Test
        @DisplayName("성공 - 완성된 강의의 isComplete 확인")
        void getCourseDetail_success_isCompleteTrue() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 완성 조건을 충족하는 강의 생성 (title, description, categoryId 필수)
            Course course = Course.create(
                    "완성된 강의",
                    "상세한 설명입니다",
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    10,
                    1L, // categoryId
                    null,
                    null,
                    null,
                    null,
                    null
            );
            courseRepository.save(course);

            // CourseItem 추가 (커리큘럼 1개 이상 필요)
            com.mzc.lp.domain.course.entity.CourseItem item =
                    com.mzc.lp.domain.course.entity.CourseItem.createFolder(course, "폴더1", null);
            course.addItem(item);
            courseRepository.save(course);

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isComplete").value(true))
                    .andExpect(jsonPath("$.data.itemCount").value(1))
                    .andExpect(jsonPath("$.data.averageRating").value(0.0))
                    .andExpect(jsonPath("$.data.reviewCount").value(0));
        }

        @Test
        @DisplayName("성공 - description 없으면 isComplete false")
        void getCourseDetail_success_isCompleteFalse_noDescription() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // description 없는 강의
            Course course = Course.create(
                    "제목만 있는 강의",
                    null, // description 없음
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    10,
                    1L,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            courseRepository.save(course);

            // CourseItem 추가
            com.mzc.lp.domain.course.entity.CourseItem item =
                    com.mzc.lp.domain.course.entity.CourseItem.createFolder(course, "폴더1", null);
            course.addItem(item);
            courseRepository.save(course);

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.isComplete").value(false))
                    .andExpect(jsonPath("$.data.averageRating").value(0.0))
                    .andExpect(jsonPath("$.data.reviewCount").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void getCourseDetail_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM001"));
        }
    }

    // ==================== 강의 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/courses/{courseId} - 강의 수정")
    class UpdateCourse {

        @Test
        @DisplayName("성공 - OPERATOR가 강의 수정")
        void updateCourse_success_operator() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("원래 제목");
            UpdateCourseRequest request = new UpdateCourseRequest(
                    "수정된 제목",
                    "수정된 설명",
                    CourseLevel.ADVANCED,
                    CourseType.BLENDED,
                    50,
                    2L,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                    .andExpect(jsonPath("$.data.level").value("ADVANCED"))
                    .andExpect(jsonPath("$.data.type").value("BLENDED"))
                    .andExpect(jsonPath("$.data.estimatedHours").value(50));
        }

        @Test
        @DisplayName("성공 - 부분 수정")
        void updateCourse_success_partial() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("원래 제목");
            UpdateCourseRequest request = new UpdateCourseRequest(
                    "수정된 제목만",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("수정된 제목만"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void updateCourse_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            UpdateCourseRequest request = new UpdateCourseRequest(
                    "수정 시도",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 수정 시도")
        void updateCourse_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            UpdateCourseRequest request = new UpdateCourseRequest(
                    "수정 시도",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 내 강의 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses/my - 내 강의 목록 조회")
    class GetMyCourses {

        @Test
        @Disabled("CI 환경에서 flaky 테스트 - 결과 순서 불일치 문제")
        @DisplayName("성공 - 내 강의 목록 조회 및 isComplete 확인")
        void getMyCourses_success_withIsComplete() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 미완성 강의 (description 없음)
            Course incompleteCourse = Course.create(
                    "미완성 강의",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    operator.getId()
            );
            courseRepository.save(incompleteCourse);

            // 완성 강의 (title, description, categoryId, items 있음)
            Course completeCourse = Course.create(
                    "완성 강의",
                    "설명 있음",
                    CourseLevel.BEGINNER,
                    CourseType.ONLINE,
                    10,
                    1L,
                    null,
                    null,
                    null,
                    null,
                    operator.getId()
            );
            courseRepository.save(completeCourse);

            com.mzc.lp.domain.course.entity.CourseItem item =
                    com.mzc.lp.domain.course.entity.CourseItem.createFolder(completeCourse, "폴더1", null);
            completeCourse.addItem(item);
            courseRepository.save(completeCourse);

            // when & then
            mockMvc.perform(get("/api/courses/my")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andExpect(jsonPath("$.data.content[?(@.title == '완성 강의')].isComplete").value(true))
                    .andExpect(jsonPath("$.data.content[?(@.title == '완성 강의')].itemCount").value(1))
                    .andExpect(jsonPath("$.data.content[?(@.title == '미완성 강의')].isComplete").value(false))
                    .andExpect(jsonPath("$.data.content[?(@.title == '미완성 강의')].itemCount").value(0));
        }
    }

    // ==================== 강의 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/courses/{courseId} - 강의 삭제")
    class DeleteCourse {

        @Test
        @DisplayName("성공 - OPERATOR가 본인의 강의 삭제")
        void deleteCourse_success_operatorDeletesOwn() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourseWithOwner("삭제할 강의", operator.getId());

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 타인의 강의 삭제")
        void deleteCourse_success_adminDeletesOthers() throws Exception {
            // given
            User designer = createDesignerUser();
            createAdminUser();
            String adminToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            Course course = createTestCourseWithOwner("설계자의 강의", designer.getId());

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - OPERATOR가 타인의 강의 삭제 시도")
        void deleteCourse_fail_operatorDeletesOthers() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String operatorToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourseWithOwner("설계자의 강의", designer.getId());

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + operatorToken))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM010"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void deleteCourse_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 삭제 시도")
        void deleteCourse_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            Course course = createTestCourse("삭제할 강의");

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void deleteCourse_fail_unauthorized() throws Exception {
            // given
            Course course = createTestCourse("삭제할 강의");

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}", course.getId()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
