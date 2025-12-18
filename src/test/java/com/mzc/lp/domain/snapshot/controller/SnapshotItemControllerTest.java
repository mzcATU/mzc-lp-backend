package com.mzc.lp.domain.snapshot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.snapshot.constant.SnapshotStatus;
import com.mzc.lp.domain.snapshot.dto.request.CreateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.MoveSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.dto.request.UpdateSnapshotItemRequest;
import com.mzc.lp.domain.snapshot.entity.CourseSnapshot;
import com.mzc.lp.domain.snapshot.entity.SnapshotItem;
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
class SnapshotItemControllerTest extends TenantTestSupport {

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

    private CourseSnapshot createTestSnapshotWithStatus(String name, Long createdBy, SnapshotStatus status) {
        CourseSnapshot snapshot = CourseSnapshot.create(name, "설명", "#태그", createdBy);
        snapshotRepository.save(snapshot);

        if (status == SnapshotStatus.ACTIVE) {
            snapshot.publish();
        } else if (status == SnapshotStatus.COMPLETED) {
            snapshot.publish();
            snapshot.complete();
        }

        return snapshotRepository.save(snapshot);
    }

    private SnapshotItem createTestItem(CourseSnapshot snapshot, String name, SnapshotItem parent) {
        SnapshotItem item = SnapshotItem.createItem(snapshot, name, parent, null, "VIDEO");
        return snapshotItemRepository.save(item);
    }

    private SnapshotItem createTestFolder(CourseSnapshot snapshot, String name, SnapshotItem parent) {
        SnapshotItem folder = SnapshotItem.createFolder(snapshot, name, parent);
        return snapshotItemRepository.save(folder);
    }

    // ==================== 계층 구조 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/snapshots/{snapshotId}/items - 아이템 계층 구조 조회")
    class GetHierarchy {

        @Test
        @DisplayName("성공 - 아이템 계층 구조 조회")
        void getHierarchy_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            SnapshotItem folder = createTestFolder(snapshot, "1장. 입문", null);
            createTestItem(snapshot, "1-1. 소개", folder);
            createTestItem(snapshot, "1-2. 설치", folder);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].itemName").value("1장. 입문"))
                    .andExpect(jsonPath("$.data[0].isFolder").value(true))
                    .andExpect(jsonPath("$.data[0].children").isArray())
                    .andExpect(jsonPath("$.data[0].children.length()").value(2));
        }

        @Test
        @DisplayName("성공 - 빈 스냅샷 조회")
        void getHierarchy_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("빈 스냅샷", 1L);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 스냅샷")
        void getHierarchy_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/items", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM006"));
        }
    }

    // ==================== 평면 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/snapshots/{snapshotId}/items/flat - 아이템 평면 목록 조회")
    class GetFlatItems {

        @Test
        @DisplayName("성공 - 평면 목록 조회")
        void getFlatItems_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            SnapshotItem folder = createTestFolder(snapshot, "폴더", null);
            createTestItem(snapshot, "아이템1", folder);
            createTestItem(snapshot, "아이템2", null);

            // when & then
            mockMvc.perform(get("/api/snapshots/{snapshotId}/items/flat", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    // ==================== 아이템 추가 테스트 ====================

    @Nested
    @DisplayName("POST /api/snapshots/{snapshotId}/items - 아이템 추가")
    class CreateItem {

        @Test
        @DisplayName("성공 - DRAFT 상태에서 차시 추가")
        void createItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            CreateSnapshotItemRequest request = new CreateSnapshotItemRequest(
                    "새 차시",
                    null,
                    100L,
                    "VIDEO"
            );

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("새 차시"))
                    .andExpect(jsonPath("$.data.isFolder").value(false))
                    .andExpect(jsonPath("$.data.itemType").value("VIDEO"));
        }

        @Test
        @DisplayName("성공 - 폴더 추가")
        void createItem_success_folder() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            CreateSnapshotItemRequest request = new CreateSnapshotItemRequest(
                    "새 폴더",
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("새 폴더"))
                    .andExpect(jsonPath("$.data.isFolder").value(true));
        }

        @Test
        @DisplayName("실패 - ACTIVE 상태에서 추가 시도")
        void createItem_fail_activeState() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("ACTIVE 스냅샷", 1L, SnapshotStatus.ACTIVE);
            CreateSnapshotItemRequest request = new CreateSnapshotItemRequest(
                    "추가 시도",
                    null,
                    100L,
                    "VIDEO"
            );

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 추가 시도")
        void createItem_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            CreateSnapshotItemRequest request = new CreateSnapshotItemRequest(
                    "추가 시도",
                    null,
                    100L,
                    "VIDEO"
            );

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 이름 누락")
        void createItem_fail_missingName() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            CreateSnapshotItemRequest request = new CreateSnapshotItemRequest(
                    null,
                    null,
                    100L,
                    "VIDEO"
            );

            // when & then
            mockMvc.perform(post("/api/snapshots/{snapshotId}/items", snapshot.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    // ==================== 아이템 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/snapshots/{snapshotId}/items/{itemId} - 아이템 수정")
    class UpdateItem {

        @Test
        @DisplayName("성공 - 아이템 이름 변경")
        void updateItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "원래 이름", null);
            UpdateSnapshotItemRequest request = new UpdateSnapshotItemRequest("새 이름");

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/items/{itemId}", snapshot.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.itemName").value("새 이름"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 아이템")
        void updateItem_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            UpdateSnapshotItemRequest request = new UpdateSnapshotItemRequest("새 이름");

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/items/{itemId}", snapshot.getId(), 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM009"));
        }
    }

    // ==================== 아이템 이동 테스트 ====================

    @Nested
    @DisplayName("PUT /api/snapshots/{snapshotId}/items/{itemId}/move - 아이템 이동")
    class MoveItem {

        @Test
        @DisplayName("성공 - 폴더로 아이템 이동")
        void moveItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem folder = createTestFolder(snapshot, "대상 폴더", null);
            SnapshotItem item = createTestItem(snapshot, "이동할 아이템", null);
            MoveSnapshotItemRequest request = new MoveSnapshotItemRequest(folder.getId());

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/items/{itemId}/move", snapshot.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.parentId").value(folder.getId()))
                    .andExpect(jsonPath("$.data.depth").value(1));
        }

        @Test
        @DisplayName("성공 - 루트로 이동")
        void moveItem_success_toRoot() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem folder = createTestFolder(snapshot, "폴더", null);
            SnapshotItem item = createTestItem(snapshot, "아이템", folder);
            MoveSnapshotItemRequest request = new MoveSnapshotItemRequest(null);

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/items/{itemId}/move", snapshot.getId(), item.getId())
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
        @DisplayName("실패 - ACTIVE 상태에서 이동 시도")
        void moveItem_fail_activeState() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("ACTIVE 스냅샷", 1L, SnapshotStatus.ACTIVE);
            SnapshotItem item = createTestItem(snapshot, "아이템", null);
            MoveSnapshotItemRequest request = new MoveSnapshotItemRequest(null);

            // when & then
            mockMvc.perform(put("/api/snapshots/{snapshotId}/items/{itemId}/move", snapshot.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }
    }

    // ==================== 아이템 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/snapshots/{snapshotId}/items/{itemId} - 아이템 삭제")
    class DeleteItem {

        @Test
        @DisplayName("성공 - 아이템 삭제")
        void deleteItem_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);
            SnapshotItem item = createTestItem(snapshot, "삭제할 아이템", null);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}/items/{itemId}", snapshot.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - ACTIVE 상태에서 삭제 시도")
        void deleteItem_fail_activeState() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshotWithStatus("ACTIVE 스냅샷", 1L, SnapshotStatus.ACTIVE);
            SnapshotItem item = createTestItem(snapshot, "아이템", null);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}/items/{itemId}", snapshot.getId(), item.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM007"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 아이템")
        void deleteItem_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CourseSnapshot snapshot = createTestSnapshot("테스트 스냅샷", 1L);

            // when & then
            mockMvc.perform(delete("/api/snapshots/{snapshotId}/items/{itemId}", snapshot.getId(), 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CM009"));
        }
    }
}
