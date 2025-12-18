package com.mzc.lp.domain.snapshot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotRelationRequest;
import com.mzc.lp.domain.snapshot.dto.request.SetStartSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
import com.mzc.lp.domain.snapshot.entity.SnapshotLearningObject;
import com.mzc.lp.domain.snapshot.entity.SnapshotRelation;
import com.mzc.lp.domain.snapshot.repository.CourseSnapshotRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotItemRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotLearningObjectRepository;
import com.mzc.lp.domain.snapshot.repository.SnapshotRelationRepository;
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
class SnapshotRelationControllerTest extends TenantTestSupport {

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
    private SnapshotRelationRepository snapshotRelationRepository;

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
        snapshotRelationRepository.deleteAll();
        snapshotItemRepository.deleteAll();
        snapshotLoRepository.deleteAll();
        snapshotRepository.deleteAll();
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

    private CourseSnapshot createTestSnapshot(String name, Long createdBy) {
        CourseSnapshot snapshot = CourseSnapshot.create(name, "설명", "#태그", createdBy);
        return snapshotRepository.save(snapshot);
    }

    private SnapshotItem createTestItem(CourseSnapshot snapshot, String name) {
        SnapshotLearningObject slo = SnapshotLearningObject.create(100L, name);
        slo = snapshotLoRepository.save(slo);
        SnapshotItem item = SnapshotItem.createItem(snapshot, name, null, slo, "VIDEO");
        return snapshotItemRepository.save(item);
    }

    private SnapshotRelation createTestRelation(CourseSnapshot snapshot, SnapshotItem from, SnapshotItem to) {
        SnapshotRelation relation;
        if (from == null) {
            relation = SnapshotRelation.createStartPoint(snapshot, to);
        } else {
            relation = SnapshotRelation.create(snapshot, from, to);
        }
        return snapshotRelationRepository.save(relation);
    }

    // ==================== 연결 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/snapshots/{snapshotId}/relations - 연결 목록 조회")
    class GetRelations {

        @Test
        @DisplayName("성공 - 연결 목록 조회")
        void getRelations_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            SnapshotItem item1 = createTestItem(snapshot, "1강");
            SnapshotItem item2 = createTestItem(snapshot, "2강");
            SnapshotItem item3 = createTestItem(snapshot, "3강");

            createTestRelation(snapshot, null, item1);  // 시작점
            createTestRelation(snapshot, item1, item2);
            createTestRelation(snapshot, item2, item3);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/relations", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.snapshotId").value(snapshot.getId()))
                    .andExpect(jsonPath("$.data.orderedItems").isArray())
                    .andExpect(jsonPath("$.data.orderedItems.length()").value(3))
                    .andExpect(jsonPath("$.data.orderedItems[0].itemName").value("1강"))
                    .andExpect(jsonPath("$.data.orderedItems[0].seq").value(1))
                    .andExpect(jsonPath("$.data.relations").isArray());
        }

        @Test
        @DisplayName("성공 - 빈 연결 목록")
        void getRelations_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("빈 스냅샷", 1L);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/relations", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderedItems").isArray())
                    .andExpect(jsonPath("$.data.orderedItems.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스냅샷")
        void getRelations_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/relations", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM006"));
        }
    }

    // ==================== 순서대로 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/snapshots/{snapshotId}/relations/ordered - 순서대로 조회")
    class GetOrderedItems {

        @Test
        @DisplayName("성공 - 순서대로 아이템 조회")
        void getOrderedItems_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            SnapshotItem item1 = createTestItem(snapshot, "1강");
            SnapshotItem item2 = createTestItem(snapshot, "2강");

            createTestRelation(snapshot, null, item1);
            createTestRelation(snapshot, item1, item2);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/relations/ordered", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].seq").value(1))
                    .andExpect(jsonPath("$.data[0].itemName").value("1강"))
                    .andExpect(jsonPath("$.data[1].seq").value(2))
                    .andExpect(jsonPath("$.data[1].itemName").value("2강"));
        }
    }

    // ==================== 연결 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/snapshots/{snapshotId}/relations - 연결 생성")
    class CreateRelation {

        @Test
        @DisplayName("성공 - 시작점 생성")
        void createRelation_success_startPoint() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "1강");

            CreateSnapshotRelationRequest request = new CreateSnapshotRelationRequest(null, item.getId());

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/relations", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isStartPoint").value(true))
                    .andExpect(jsonPath("$.data.toItemId").value(item.getId()));
        }

        @Test
        @DisplayName("성공 - 연결 생성")
        void createRelation_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item1 = createTestItem(snapshot, "1강");
            SnapshotItem item2 = createTestItem(snapshot, "2강");

            createTestRelation(snapshot, null, item1);  // 시작점 먼저 생성

            CreateSnapshotRelationRequest request = new CreateSnapshotRelationRequest(item1.getId(), item2.getId());

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/relations", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isStartPoint").value(false))
                    .andExpect(jsonPath("$.data.fromItemId").value(item1.getId()))
                    .andExpect(jsonPath("$.data.toItemId").value(item2.getId()));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createRelation_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "1강");

            CreateSnapshotRelationRequest request = new CreateSnapshotRelationRequest(null, item.getId());

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/relations", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 아이템")
        void createRelation_fail_itemNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            CreateSnapshotRelationRequest request = new CreateSnapshotRelationRequest(null, 99999L);

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/relations", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM009"));
        }
    }

    // ==================== 시작점 설정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/snapshots/{snapshotId}/relations/start - 시작점 설정")
    class SetStartItem {

        @Test
        @DisplayName("성공 - 시작점 설정")
        void setStartItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "새 시작점");

            SetStartSnapshotItemRequest request = new SetStartSnapshotItemRequest(item.getId());

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/relations/start", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isStartPoint").value(true))
                    .andExpect(jsonPath("$.data.toItemId").value(item.getId()));
        }

        @Test
        @DisplayName("성공 - 기존 시작점 변경")
        void setStartItem_success_change() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item1 = createTestItem(snapshot, "기존 시작점");
            SnapshotItem item2 = createTestItem(snapshot, "새 시작점");

            createTestRelation(snapshot, null, item1);  // 기존 시작점

            SetStartSnapshotItemRequest request = new SetStartSnapshotItemRequest(item2.getId());

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/relations/start", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.toItemId").value(item2.getId()));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 아이템")
        void setStartItem_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            SetStartSnapshotItemRequest request = new SetStartSnapshotItemRequest(99999L);

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/relations/start", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM009"));
        }
    }

    // ==================== 연결 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/snapshots/{snapshotId}/relations/{relationId} - 연결 삭제")
    class DeleteRelation {

        @Test
        @DisplayName("성공 - 연결 삭제")
        void deleteRelation_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "1강");
            SnapshotRelation relation = createTestRelation(snapshot, null, item);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}/relations/{relationId}",
                            snapshot.getId(), relation.getId())
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
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}/relations/{relationId}",
                            snapshot.getId(), 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("실패 - USER 권한으로 삭제 시도")
        void deleteRelation_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "1강");
            SnapshotRelation relation = createTestRelation(snapshot, null, item);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}/relations/{relationId}",
                            snapshot.getId(), relation.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
