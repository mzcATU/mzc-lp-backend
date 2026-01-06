package com.mzc.lp.domain.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.course.dto.request.CreateReviewRequest;
import com.mzc.lp.domain.course.dto.request.UpdateReviewRequest;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseReview;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.course.repository.CourseReviewRepository;
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
@DisplayName("코스 리뷰 API 테스트")
class CourseReviewControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseReviewRepository reviewRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseTimeRepository courseTimeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Course testCourse;
    private CourseTime testCourseTime;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        reviewRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseTimeRepository.deleteAll();
        courseRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        // 테스트 사용자 생성 및 로그인
        testUser = User.create("student@example.com", "학생", passwordEncoder.encode("Password123!"));
        testUser.updateRole(TenantRole.USER);
        testUser = userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest("student@example.com", "Password123!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response).get("data").get("accessToken").asText();

        // 테스트 코스 생성
        testCourse = Course.create("테스트 코스", testUser.getId());
        testCourse = courseRepository.save(testCourse);

        // 테스트 차수 생성
        testCourseTime = CourseTime.create(
                "테스트 차수",
                DeliveryType.ONLINE,
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                100,
                0,
                EnrollmentMethod.FIRST_COME,
                80,
                BigDecimal.ZERO,
                true,
                null,
                false,
                testUser.getId()
        );
        testCourseTime.linkCourse(testCourse.getId(), 1L);
        testCourseTime = courseTimeRepository.save(testCourseTime);
    }

    @Test
    @DisplayName("리뷰 작성 성공 - 수강 완료한 코스")
    void createReview_Success() throws Exception {
        // Given - 수강 완료 상태 생성
        Enrollment enrollment = Enrollment.createVoluntary(testUser.getId(), testCourseTime.getId());
        enrollment.complete(100);
        enrollmentRepository.save(enrollment);

        CreateReviewRequest request = new CreateReviewRequest(5, "정말 좋은 강의였습니다!");

        // When & Then
        mockMvc.perform(post("/api/courses/{courseId}/reviews", testCourse.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.content").value("정말 좋은 강의였습니다!"));
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 수강 완료하지 않음")
    void createReview_Fail_NotCompleted() throws Exception {
        // Given - 수강 중 상태
        Enrollment enrollment = Enrollment.createVoluntary(testUser.getId(), testCourseTime.getId());
        enrollmentRepository.save(enrollment);

        CreateReviewRequest request = new CreateReviewRequest(5, "좋아요");

        // When & Then
        mockMvc.perform(post("/api/courses/{courseId}/reviews", testCourse.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공")
    void getReviews_Success() throws Exception {
        // Given - 다른 사용자 생성
        User otherUser = User.create("other@example.com", "다른사용자", passwordEncoder.encode("Password123!"));
        otherUser.updateRole(TenantRole.USER);
        otherUser = userRepository.save(otherUser);

        // Given - 리뷰 생성
        CourseReview review1 = CourseReview.create(testCourse.getId(), testUser.getId(), 5, "좋아요");
        CourseReview review2 = CourseReview.create(testCourse.getId(), otherUser.getId(), 4, "괜찮아요");
        reviewRepository.save(review1);
        reviewRepository.save(review2);

        // When & Then
        mockMvc.perform(get("/api/courses/{courseId}/reviews", testCourse.getId())
                        .param("page", "0")
                        .param("pageSize", "20")
                        .param("sortBy", "latest"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviews").isArray())
                .andExpect(jsonPath("$.data.averageRating").exists())
                .andExpect(jsonPath("$.data.reviewCount").exists());
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_Success() throws Exception {
        // Given
        Enrollment enrollment = Enrollment.createVoluntary(testUser.getId(), testCourseTime.getId());
        enrollment.complete(100);
        enrollmentRepository.save(enrollment);

        CourseReview review = CourseReview.create(testCourse.getId(), testUser.getId(), 4, "괜찮아요");
        review = reviewRepository.save(review);

        UpdateReviewRequest request = new UpdateReviewRequest(5, "수정: 정말 좋아요!");

        // When & Then
        mockMvc.perform(put("/api/courses/{courseId}/reviews/{reviewId}", testCourse.getId(), review.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(5))
                .andExpect(jsonPath("$.data.content").value("수정: 정말 좋아요!"));
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() throws Exception {
        // Given
        CourseReview review = CourseReview.create(testCourse.getId(), testUser.getId(), 5, "좋아요");
        review = reviewRepository.save(review);

        // When & Then
        mockMvc.perform(delete("/api/courses/{courseId}/reviews/{reviewId}", testCourse.getId(), review.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("리뷰 통계 조회 성공")
    void getReviewStats_Success() throws Exception {
        // Given - 다른 사용자 생성
        User otherUser = User.create("other3@example.com", "다른사용자3", passwordEncoder.encode("Password123!"));
        otherUser.updateRole(TenantRole.USER);
        otherUser = userRepository.save(otherUser);

        // Given - 리뷰 생성
        CourseReview review1 = CourseReview.create(testCourse.getId(), testUser.getId(), 5, "좋아요");
        CourseReview review2 = CourseReview.create(testCourse.getId(), otherUser.getId(), 4, "괜찮아요");
        reviewRepository.saveAndFlush(review1);
        reviewRepository.saveAndFlush(review2);

        // When & Then
        mockMvc.perform(get("/api/courses/{courseId}/reviews/stats", testCourse.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseId").value(testCourse.getId()))
                .andExpect(jsonPath("$.data.averageRating").exists())
                .andExpect(jsonPath("$.data.reviewCount").exists());
    }
}
