package com.mzc.lp.domain.snapshot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotRequest;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotLearningObjectRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SnapshotControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseSnapshotRepository snapshotRepository;

    @Autowired
    private SnapshotItemRepository snapshotItemRepository;

    @Autowired
    private SnapshotLearningObjectRepository snapshotLoRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseItemRepository courseItemRepository;

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
        snapshotItemRepository.deleteAll();
        snapshotLoRepository.deleteAll();
        snapshotRepository.deleteAll();
        courseItemRepository.deleteAll();
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
                null
        );
        return courseRepository.save(course);
    }

    private Course createTestCourseWithItems(String title) {
        Course course = createTestCourse(title);

        CourseItem folder = CourseItem.createFolder(course, "1장. 입문", null);
        courseItemRepository.save(folder);

        CourseItem item1 = CourseItem.createItem(course, "1-1. 소개", folder, 100L);
        CourseItem item2 = CourseItem.createItem(course, "1-2. 설치", folder, 101L);
        courseItemRepository.save(item1);
        courseItemRepository.save(item2);

        return course;
    }

    private CourseSnapshot createTestSnapshot(String name, Long createdBy) {
        CourseSnapshot snapshot = CourseSnapshot.create(name, "설명", "#태그", createdBy);
        return snapshotRepository.save(snapshot);
    }

    private CourseSnapshot createTestSnapshotWithStatus(String name, Long createdBy, SnapshotStatus status) {
        CourseSnapshot snapshot = CourseSnapshot.create(name, "설명", "#태그", createdBy);
        snapshotRepository.save(snapshot);

        if (status == SnapshotStatus.ACTIVE) {
            snapshot.publish();
        } else if (status == SnapshotStatus.COMPLETED) {
            snapshot.publish();
            snapshot.complete();
        } else if (status == SnapshotStatus.ARCHIVED) {
            snapshot.publish();
            snapshot.complete();
            snapshot.archive();
        }

        return snapshotRepository.save(snapshot);
    }

    // ==================== Course에서 스냅샷 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/courses/{courseId}/snapshots - Course에서 스냅샷 생성")
    class CreateSnapshotFromCourse {

        @Test
        @DisplayName("성공 - OPERATOR가 Course에서 스냅샷 생성")
        void createSnapshotFromCourse_success() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourseWithItems("Spring Boot 기초");

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/snapshots", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", operator.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.snapshotName").value("Spring Boot 기초"))
                    .andExpect(jsonPath("$.data.sourceCourseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andExpect(jsonPath("$.data.version").value(1));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 Course")
        void createSnapshotFromCourse_fail_courseNotFound() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/snapshots", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", operator.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createSnapshotFromCourse_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/snapshots", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", "1"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 신규 스냅샷 직접 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/snapshots - 신규 스냅샷 직접 생성")
    class CreateSnapshot {

        @Test
        @DisplayName("성공 - OPERATOR가 스냅샷 직접 생성")
        void createSnapshot_success() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateSnapshotRequest request = new CreateSnapshotRequest(
                    "새 강의",
                    "강의 설명입니다.",
                    "#spring #java"
            );

            // when & then
            mockMvc.perform(post("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", operator.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.snapshotName").value("새 강의"))
                    .andExpect(jsonPath("$.data.description").value("강의 설명입니다."))
                    .andExpect(jsonPath("$.data.hashtags").value("#spring #java"))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andExpect(jsonPath("$.data.sourceCourseId").isEmpty());
        }

        @Test
        @DisplayName("실패 - 스냅샷 이름 누락")
        void createSnapshot_fail_missingName() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateSnapshotRequest request = new CreateSnapshotRequest(
                    null,
                    "설명만 있음",
                    "#태그"
            );

            // when & then
            mockMvc.perform(post("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", operator.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createSnapshot_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            CreateSnapshotRequest request = new CreateSnapshotRequest(
                    "테스트 강의",
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 스냅샷 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/snapshots - 스냅샷 목록 조회")
    class GetSnapshots {

        @Test
        @DisplayName("성공 - 전체 스냅샷 목록 조회")
        void getSnapshots_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestSnapshot("스냅샷1", 1L);
            createTestSnapshot("스냅샷2", 1L);
            createTestSnapshot("스냅샷3", 2L);

            // when & then
            mockMvc.perform(get("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test
        @DisplayName("성공 - 상태 필터링")
        void getSnapshots_success_statusFilter() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestSnapshotWithStatus("DRAFT 스냅샷", 1L, SnapshotStatus.DRAFT);
            createTestSnapshotWithStatus("ACTIVE 스냅샷", 1L, SnapshotStatus.ACTIVE);
            createTestSnapshotWithStatus("COMPLETED 스냅샷", 1L, SnapshotStatus.COMPLETED);

            // when & then
            mockMvc.perform(get("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("status", "ACTIVE"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 생성자 필터링")
        void getSnapshots_success_createdByFilter() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestSnapshot("스냅샷1", 1L);
            createTestSnapshot("스냅샷2", 1L);
            createTestSnapshot("스냅샷3", 2L);

            // when & then
            mockMvc.perform(get("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", "1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void getSnapshots_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/snapshots")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    // ==================== 스냅샷 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/snapshots/{snapshotId} - 스냅샷 상세 조회")
    class GetSnapshotDetail {

        @Test
        @DisplayName("성공 - 스냅샷 상세 조회")
        void getSnapshotDetail_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.snapshotId").value(snapshot.getId()))
                    .andExpect(jsonPath("$.data.snapshotName").value("테스트 스냅샷"))
                    .andExpect(jsonPath("$.data.items").isArray());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스냅샷")
        void getSnapshotDetail_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM006"));
        }
    }

    // ==================== Course의 스냅샷 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses/{courseId}/snapshots - Course의 스냅샷 목록")
    class GetSnapshotsByCourse {

        @Test
        @DisplayName("성공 - Course의 스냅샷 목록 조회")
        void getSnapshotsByCourse_success() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // Course에서 스냅샷 생성
            mockMvc.perform(post("/api/courses/{courseId}/snapshots", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", operator.getId().toString()))
                    .andExpect(status().isCreated());

            mockMvc.perform(post("/api/courses/{courseId}/snapshots", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", operator.getId().toString()))
                    .andExpect(status().isCreated());

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/snapshots", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 Course")
        void getSnapshotsByCourse_fail_courseNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/snapshots", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM001"));
        }
    }

    // ==================== 스냅샷 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/snapshots/{snapshotId} - 스냅샷 수정")
    class UpdateSnapshot {

        @Test
        @DisplayName("성공 - DRAFT 상태에서 수정")
        void updateSnapshot_success_draft() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("원래 이름", 1L);
            UpdateSnapshotRequest request = new UpdateSnapshotRequest(
                    "수정된 이름",
                    "수정된 설명",
                    "#updated"
            );

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.snapshotName").value("수정된 이름"))
                    .andExpect(jsonPath("$.data.description").value("수정된 설명"))
                    .andExpect(jsonPath("$.data.hashtags").value("#updated"));
        }

        @Test
        @DisplayName("성공 - ACTIVE 상태에서 메타데이터 수정")
        void updateSnapshot_success_active() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("원래 이름", 1L, SnapshotStatus.ACTIVE);
            UpdateSnapshotRequest request = new UpdateSnapshotRequest(
                    "수정된 이름",
                    "수정된 설명",
                    "#updated"
            );

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.snapshotName").value("수정된 이름"));
        }

        @Test
        @DisplayName("실패 - COMPLETED 상태에서 수정 시도")
        void updateSnapshot_fail_completed() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("원래 이름", 1L, SnapshotStatus.COMPLETED);
            UpdateSnapshotRequest request = new UpdateSnapshotRequest(
                    "수정 시도",
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스냅샷")
        void updateSnapshot_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            UpdateSnapshotRequest request = new UpdateSnapshotRequest(
                    "수정 시도",
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM006"));
        }
    }

    // ==================== 스냅샷 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/snapshots/{snapshotId} - 스냅샷 삭제")
    class DeleteSnapshot {

        @Test
        @DisplayName("성공 - 스냅샷 삭제")
        void deleteSnapshot_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("삭제할 스냅샷", 1L);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/snapshots/{snapshotId}", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스냅샷")
        void deleteSnapshot_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM006"));
        }
    }

    // ==================== 상태 변경 테스트 ====================

    @Nested
    @DisplayName("POST /api/snapshots/{snapshotId}/publish - 스냅샷 발행")
    class PublishSnapshot {

        @Test
        @DisplayName("성공 - DRAFT → ACTIVE 발행")
        void publishSnapshot_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("발행할 스냅샷", 1L);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/publish", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("실패 - ACTIVE 상태에서 발행 시도")
        void publishSnapshot_fail_alreadyActive() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("이미 발행됨", 1L, SnapshotStatus.ACTIVE);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/publish", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }
    }

    @Nested
    @DisplayName("POST /api/snapshots/{snapshotId}/complete - 스냅샷 완료")
    class CompleteSnapshot {

        @Test
        @DisplayName("성공 - ACTIVE → COMPLETED 완료")
        void completeSnapshot_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("완료할 스냅샷", 1L, SnapshotStatus.ACTIVE);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/complete", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("실패 - DRAFT 상태에서 완료 시도")
        void completeSnapshot_fail_draft() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("DRAFT 스냅샷", 1L);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/complete", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }
    }

    @Nested
    @DisplayName("POST /api/snapshots/{snapshotId}/archive - 스냅샷 보관")
    class ArchiveSnapshot {

        @Test
        @DisplayName("성공 - COMPLETED → ARCHIVED 보관")
        void archiveSnapshot_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("보관할 스냅샷", 1L, SnapshotStatus.COMPLETED);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/archive", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
        }

        @Test
        @DisplayName("실패 - ACTIVE 상태에서 보관 시도")
        void archiveSnapshot_fail_active() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("ACTIVE 스냅샷", 1L, SnapshotStatus.ACTIVE);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/archive", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }
    }
}
