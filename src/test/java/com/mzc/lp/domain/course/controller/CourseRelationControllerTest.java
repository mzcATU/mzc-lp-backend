package com.mzc.lp.domain.course.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.course.constant.CourseLevel;
import com.mzc.lp.domain.course.constant.CourseType;
import com.mzc.lp.domain.course.dto.request.CreateRelationRequest;
import com.mzc.lp.domain.course.dto.request.SetStartItemRequest;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.entity.CourseRelation;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
import com.mzc.lp.domain.course.repository.CourseRelationRepository;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import com.mzc.lp.common.support.TenantTestSupport;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CourseRelationControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseItemRepository courseItemRepository;

    @Autowired
    private CourseRelationRepository courseRelationRepository;

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
        courseRelationRepository.deleteAll();
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

    private CourseRelation createTestRelation(CourseItem from, CourseItem to) {
        CourseRelation relation = from == null
                ? CourseRelation.createStartPoint(to)
                : CourseRelation.create(from, to);
        return courseRelationRepository.save(relation);
    }

    // ==================== 학습 순서 설정 테스트 ====================

    @Nested
    @DisplayName("POST /api/courses/{courseId}/relations - 학습 순서 설정")
    class CreateRelations {

        @Test
        @DisplayName("성공 - 학습 순서 설정")
        void createRelations_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "1-1. 환경설정", null, 100L);
            CourseItem item2 = createTestItem(course, "1-2. 기본 문법", null, 101L);
            CourseItem item3 = createTestItem(course, "1-3. 심화", null, 102L);

            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(
                            new CreateRelationRequest.RelationItem(null, item1.getId()),
                            new CreateRelationRequest.RelationItem(item1.getId(), item2.getId()),
                            new CreateRelationRequest.RelationItem(item2.getId(), item3.getId())
                    )
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.relationCount").value(3))
                    .andExpect(jsonPath("$.data.startItemId").value(item1.getId()));
        }

        @Test
        @DisplayName("실패 - 빈 순서 목록")
        void createRelations_fail_emptyList() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            CreateRelationRequest request = new CreateRelationRequest(List.of());

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void createRelations_fail_courseNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(new CreateRelationRequest.RelationItem(null, 1L))
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 폴더 포함 시도")
        void createRelations_fail_includeFolder() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "1주차", null);

            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(new CreateRelationRequest.RelationItem(null, folder.getId()))
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 접근")
        void createRelations_fail_userRole() throws Exception {
            // given
            createRegularUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "차시", null, 100L);

            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(new CreateRelationRequest.RelationItem(null, item.getId()))
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 중복 시작점")
        void createRelations_fail_duplicateStartPoint() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "차시1", null, 100L);
            CourseItem item2 = createTestItem(course, "차시2", null, 101L);

            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(
                            new CreateRelationRequest.RelationItem(null, item1.getId()),
                            new CreateRelationRequest.RelationItem(null, item2.getId())
                    )
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 순환 참조")
        void createRelations_fail_circularReference() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "차시1", null, 100L);
            CourseItem item2 = createTestItem(course, "차시2", null, 101L);
            CourseItem item3 = createTestItem(course, "차시3", null, 102L);

            // item1 -> item2 -> item3 -> item1 (순환)
            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(
                            new CreateRelationRequest.RelationItem(null, item1.getId()),
                            new CreateRelationRequest.RelationItem(item1.getId(), item2.getId()),
                            new CreateRelationRequest.RelationItem(item2.getId(), item3.getId()),
                            new CreateRelationRequest.RelationItem(item3.getId(), item1.getId())
                    )
            );

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 학습 순서 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/courses/{courseId}/relations - 학습 순서 조회")
    class GetRelations {

        @Test
        @DisplayName("성공 - 학습 순서 조회")
        void getRelations_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "1-1. 환경설정", null, 100L);
            CourseItem item2 = createTestItem(course, "1-2. 기본 문법", null, 101L);
            CourseItem item3 = createTestItem(course, "1-3. 심화", null, 102L);

            createTestRelation(null, item1);
            createTestRelation(item1, item2);
            createTestRelation(item2, item3);

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.orderedItems").isArray())
                    .andExpect(jsonPath("$.data.orderedItems.length()").value(3))
                    .andExpect(jsonPath("$.data.orderedItems[0].itemName").value("1-1. 환경설정"))
                    .andExpect(jsonPath("$.data.orderedItems[0].order").value(1))
                    .andExpect(jsonPath("$.data.orderedItems[1].itemName").value("1-2. 기본 문법"))
                    .andExpect(jsonPath("$.data.orderedItems[1].order").value(2))
                    .andExpect(jsonPath("$.data.orderedItems[2].itemName").value("1-3. 심화"))
                    .andExpect(jsonPath("$.data.orderedItems[2].order").value(3))
                    .andExpect(jsonPath("$.data.relations").isArray())
                    .andExpect(jsonPath("$.data.relations.length()").value(3));
        }

        @Test
        @DisplayName("성공 - 빈 순서 조회")
        void getRelations_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.orderedItems").isArray())
                    .andExpect(jsonPath("$.data.orderedItems.length()").value(0))
                    .andExpect(jsonPath("$.data.relations").isArray())
                    .andExpect(jsonPath("$.data.relations.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 강의")
        void getRelations_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/courses/{courseId}/relations", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 학습 순서 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/courses/{courseId}/relations - 학습 순서 수정")
    class UpdateRelations {

        @Test
        @DisplayName("성공 - 학습 순서 수정")
        void updateRelations_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "1-1. 환경설정", null, 100L);
            CourseItem item2 = createTestItem(course, "1-2. 기본 문법", null, 101L);
            CourseItem item3 = createTestItem(course, "1-3. 심화", null, 102L);

            // 기존 순서: item1 -> item2 -> item3
            createTestRelation(null, item1);
            createTestRelation(item1, item2);
            createTestRelation(item2, item3);

            // 새 순서: item3 -> item1 -> item2
            CreateRelationRequest request = new CreateRelationRequest(
                    List.of(
                            new CreateRelationRequest.RelationItem(null, item3.getId()),
                            new CreateRelationRequest.RelationItem(item3.getId(), item1.getId()),
                            new CreateRelationRequest.RelationItem(item1.getId(), item2.getId())
                    )
            );

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/relations", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.relationCount").value(3))
                    .andExpect(jsonPath("$.data.startItemId").value(item3.getId()));
        }
    }

    // ==================== 시작점 설정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/courses/{courseId}/relations/start - 시작점 설정")
    class SetStartItem {

        @Test
        @DisplayName("성공 - 시작점 설정")
        void setStartItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "차시1", null, 100L);
            CourseItem item2 = createTestItem(course, "차시2", null, 101L);

            createTestRelation(null, item1);
            createTestRelation(item1, item2);

            SetStartItemRequest request = new SetStartItemRequest(item2.getId());

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/relations/start", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.startItemId").value(item2.getId()))
                    .andExpect(jsonPath("$.data.message").value("시작점이 변경되었습니다."));
        }

        @Test
        @DisplayName("실패 - 폴더를 시작점으로 설정")
        void setStartItem_fail_folder() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "폴더", null);

            SetStartItemRequest request = new SetStartItemRequest(folder.getId());

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/relations/start", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 항목")
        void setStartItem_fail_itemNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            SetStartItemRequest request = new SetStartItemRequest(99999L);

            // when & then
            mockMvc.perform(put("/api/courses/{courseId}/relations/start", course.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 자동 순서 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/courses/{courseId}/relations/auto - 자동 순서 생성")
    class CreateAutoRelations {

        @Test
        @DisplayName("성공 - 자동 순서 생성")
        void createAutoRelations_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem folder = createTestFolder(course, "1주차", null);
            createTestItem(course, "1-1. 환경설정", folder, 100L);
            createTestItem(course, "1-2. 기본 문법", folder, 101L);
            createTestItem(course, "1-3. 심화", folder, 102L);

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations/auto", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.courseId").value(course.getId()))
                    .andExpect(jsonPath("$.data.relationCount").value(3))
                    .andExpect(jsonPath("$.data.message").value("자동 순서 생성 완료"));
        }

        @Test
        @DisplayName("성공 - 차시 없음 (빈 결과)")
        void createAutoRelations_success_noItems() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            createTestFolder(course, "빈 폴더", null);

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations/auto", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.relationCount").value(0));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 접근")
        void createAutoRelations_fail_userRole() throws Exception {
            // given
            createRegularUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // when & then
            mockMvc.perform(post("/api/courses/{courseId}/relations/auto", course.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 순서 연결 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/courses/{courseId}/relations/{relationId} - 연결 삭제")
    class DeleteRelation {

        @Test
        @DisplayName("성공 - 연결 삭제")
        void deleteRelation_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item1 = createTestItem(course, "차시1", null, 100L);
            CourseItem item2 = createTestItem(course, "차시2", null, 101L);

            createTestRelation(null, item1);
            CourseRelation relation = createTestRelation(item1, item2);

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/relations/{relationId}", course.getId(), relation.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 연결")
        void deleteRelation_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/relations/{relationId}", course.getId(), 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 삭제 시도")
        void deleteRelation_fail_userRole() throws Exception {
            // given
            createRegularUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            Course course = createTestCourse("테스트 강의");
            CourseItem item = createTestItem(course, "차시", null, 100L);
            CourseRelation relation = createTestRelation(null, item);

            // when & then
            mockMvc.perform(delete("/api/courses/{courseId}/relations/{relationId}", course.getId(), relation.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
