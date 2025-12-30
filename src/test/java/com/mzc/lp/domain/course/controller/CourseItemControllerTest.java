package com.mzc.lp.domain.course.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.dto.request.CreateFolderRequest;
import com.mzc.lp.domain.course.dto.request.CreateItemRequest;
import com.mzc.lp.domain.course.dto.request.MoveItemRequest;
import com.mzc.lp.domain.course.dto.request.UpdateItemNameRequest;
import com.mzc.lp.domain.course.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CourseItemControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private User createRegularUser() {
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

    private CourseItem createTestFolder(Course course, String name, CourseItem parent) {
        CourseItem folder = CourseItem.createFolder(course, name, parent);
        return courseItemRepository.save(folder);
    }

    private CourseItem createTestItem(Course course, String name, CourseItem parent, Long loId) {
        CourseItem item = CourseItem.createItem(course, name, parent, loId);
        return courseItemRepository.save(item);
    }

    // ==================== 차시 추가 테스트 ====================

    @Nested
    @DisplayName("POST /api/courses/{courseId}/items - 차시 추가")
    class CreateItem {

        @Test
        @DisplayName("성공 - 최상위에 차시 추가")
        void createItem_success_rootLevel() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CreateItemRequest request = new CreateItemRequest(
                    "1-1. 환경설정",
                    null,
                    100L,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/items", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("1-1. 환경설정"))
                    .andExpect(jsonPath("$.data.depth").value(0))
                    .andExpect(jsonPath("$.data.parentId").isEmpty())
                    .andExpect(jsonPath("$.data.learningObjectId").value(100))
                    .andExpect(jsonPath("$.data.isFolder").value(false));
        }

        @Test
        @DisplayName("성공 - 폴더 하위에 차시 추가")
        void createItem_success_underFolder() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "1주차", null);
            CreateItemRequest request = new CreateItemRequest(
                    "1-1. 환경설정",
                    folder.getId(),
                    100L,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/items", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("1-1. 환경설정"))
                    .andExpect(jsonPath("$.data.depth").value(1))
                    .andExpect(jsonPath("$.data.parentId").value(folder.getId()))
                    .andExpect(jsonPath("$.data.isFolder").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void createItem_fail_courseNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateItemRequest request = new CreateItemRequest(
                    "차시",
                    null,
                    100L,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/items", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM001"));
        }

        @Test
        @DisplayName("실패 - 항목 이름 누락")
        void createItem_fail_missingName() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CreateItemRequest request = new CreateItemRequest(
                    null,
                    null,
                    100L,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/items", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - learningObjectId 누락")
        void createItem_fail_missingLearningObjectId() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CreateItemRequest request = new CreateItemRequest(
                    "차시 이름",
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/items", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 접근")
        void createItem_fail_userRole() throws Exception {
            // given
            createRegularUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CreateItemRequest request = new CreateItemRequest(
                    "차시",
                    null,
                    100L,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/items", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 폴더 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/courses/{courseId}/folders - 폴더 생성")
    class CreateFolder {

        @Test
        @DisplayName("성공 - 최상위에 폴더 생성")
        void createFolder_success_rootLevel() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CreateFolderRequest request = new CreateFolderRequest(
                    "1주차",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/folders", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("1주차"))
                    .andExpect(jsonPath("$.data.depth").value(0))
                    .andExpect(jsonPath("$.data.parentId").isEmpty())
                    .andExpect(jsonPath("$.data.learningObjectId").isEmpty())
                    .andExpect(jsonPath("$.data.isFolder").value(true));
        }

        @Test
        @DisplayName("성공 - 중첩 폴더 생성")
        void createFolder_success_nested() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem parentFolder = createTestFolder(course, "1주차", null);
            CreateFolderRequest request = new CreateFolderRequest(
                    "1주차-실습",
                    parentFolder.getId()
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/folders", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("1주차-실습"))
                    .andExpect(jsonPath("$.data.depth").value(1))
                    .andExpect(jsonPath("$.data.parentId").value(parentFolder.getId()))
                    .andExpect(jsonPath("$.data.isFolder").value(true));
        }

        @Test
        @DisplayName("실패 - 폴더 이름 누락")
        void createFolder_fail_missingName() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CreateFolderRequest request = new CreateFolderRequest(
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/folders", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 계층 구조 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses/{courseId}/items/hierarchy - 계층 구조 조회")
    class GetHierarchy {

        @Test
        @DisplayName("성공 - 계층 구조 조회")
        void getHierarchy_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "1주차", null);
            createTestItem(course, "1-1. 환경설정", folder, 100L);
            createTestItem(course, "1-2. 기본 문법", folder, 101L);

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/items/hierarchy", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].itemName").value("1주차"))
                    .andExpect(jsonPath("$.data[0].isFolder").value(true))
                    .andExpect(jsonPath("$.data[0].children").isArray())
                    .andExpect(jsonPath("$.data[0].children.length()").value(2));
        }

        @Test
        @DisplayName("성공 - 빈 구조 조회")
        void getHierarchy_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/items/hierarchy", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void getHierarchy_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/items/hierarchy", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 순서대로 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses/{courseId}/items/ordered - 순서대로 조회")
    class GetOrderedItems {

        @Test
        @DisplayName("성공 - 차시만 조회 (폴더 제외)")
        void getOrderedItems_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "1주차", null);
            createTestItem(course, "1-1. 환경설정", folder, 100L);
            createTestItem(course, "1-2. 기본 문법", folder, 101L);

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/items/ordered", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].isFolder").value(false))
                    .andExpect(jsonPath("$.data[1].isFolder").value(false));
        }
    }

    // ==================== 항목 이동 테스트 ====================

    @Nested
    @DisplayName("PUT /api/courses/{courseId}/items/move - 항목 이동")
    class MoveItem {

        @Test
        @DisplayName("성공 - 다른 폴더로 이동")
        void moveItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder1 = createTestFolder(course, "1주차", null);
            CourseItem folder2 = createTestFolder(course, "2주차", null);
            CourseItem item = createTestItem(course, "이동할 차시", folder1, 100L);
            MoveItemRequest request = new MoveItemRequest(
                    item.getId(),
                    folder2.getId(),
                    0
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/items/move", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.parentId").value(folder2.getId()));
        }

        @Test
        @DisplayName("성공 - 최상위로 이동")
        void moveItem_success_toRoot() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "1주차", null);
            CourseItem item = createTestItem(course, "이동할 차시", folder, 100L);
            MoveItemRequest request = new MoveItemRequest(
                    item.getId(),
                    null,
                    0
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/items/move", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.parentId").isEmpty())
                    .andExpect(jsonPath("$.data.depth").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 항목")
        void moveItem_fail_itemNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            MoveItemRequest request = new MoveItemRequest(
                    99999L,
                    null,
                    0
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/items/move", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 항목 이름 변경 테스트 ====================

    @Nested
    @DisplayName("PATCH /api/courses/{courseId}/items/{itemId}/name - 이름 변경")
    class UpdateItemName {

        @Test
        @DisplayName("성공 - 항목 이름 변경")
        void updateItemName_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "원래 이름", null, 100L);
            UpdateItemNameRequest request = new UpdateItemNameRequest("변경된 이름");

            // when & then
            mockMvc.perform(patch("/api/courses/{courseId}/items/{itemId}/name", course.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("변경된 이름"));
        }

        @Test
        @DisplayName("실패 - 빈 이름")
        void updateItemName_fail_emptyName() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "원래 이름", null, 100L);
            UpdateItemNameRequest request = new UpdateItemNameRequest("   ");

            // when & then
            mockMvc.perform(patch("/api/courses/{courseId}/items/{itemId}/name", course.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 학습 객체 변경 테스트 ====================

    @Nested
    @DisplayName("PATCH /api/courses/{courseId}/items/{itemId}/learning-object - LO 변경")
    class UpdateLearningObject {

        @Test
        @DisplayName("성공 - 학습 객체 변경")
        void updateLearningObject_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "차시", null, 100L);
            UpdateLearningObjectRequest request = new UpdateLearningObjectRequest(200L);

            // when & then
            mockMvc.perform(patch("/api/courses/{courseId}/items/{itemId}/learning-object", course.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.learningObjectId").value(200));
        }

        @Test
        @DisplayName("실패 - 폴더에 LO 연결 시도")
        void updateLearningObject_fail_folder() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "폴더", null);
            UpdateLearningObjectRequest request = new UpdateLearningObjectRequest(200L);

            // when & then
            mockMvc.perform(patch("/api/courses/{courseId}/items/{itemId}/learning-object", course.getId(), folder.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 항목 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/courses/{courseId}/items/{itemId} - 항목 삭제")
    class DeleteItem {

        @Test
        @DisplayName("성공 - 차시 삭제")
        void deleteItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "삭제할 차시", null, 100L);

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/items/{itemId}", course.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("성공 - 폴더 삭제 (하위 항목 포함)")
        void deleteItem_success_folderWithChildren() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "삭제할 폴더", null);
            createTestItem(course, "하위 차시 1", folder, 100L);
            createTestItem(course, "하위 차시 2", folder, 101L);

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/items/{itemId}", course.getId(), folder.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 계층 구조 확인 - 빈 목록이어야 함
            mockMvc.perform(get("/api/courses/{courseId}/items/hierarchy", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 항목")
        void deleteItem_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/items/{itemId}", course.getId(), 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 삭제 시도")
        void deleteItem_fail_userRole() throws Exception {
            // given
            createRegularUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "삭제할 차시", null, 100L);

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/items/{itemId}", course.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
