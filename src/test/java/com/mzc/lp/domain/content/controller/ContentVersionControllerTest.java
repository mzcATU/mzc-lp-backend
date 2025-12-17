package com.mzc.lp.domain.content.controller;

import com.mzc.lp.common.support.TenantTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.constant.VersionChangeType;
import com.mzc.lp.domain.content.dto.request.RestoreVersionRequest;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.entity.ContentVersion;
import com.mzc.lp.domain.content.repository.ContentRepository;
import com.mzc.lp.domain.content.repository.ContentVersionRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
import com.mzc.lp.domain.learning.repository.ContentFolderRepository;
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
class ContentVersionControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private ContentVersionRepository contentVersionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserCourseRoleRepository userCourseRoleRepository;

    @Autowired
    private LearningObjectRepository learningObjectRepository;

    @Autowired
    private ContentFolderRepository contentFolderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User designerUser;

    @BeforeEach
    void setUp() {
        learningObjectRepository.deleteAll();
        contentFolderRepository.deleteAll();
        contentVersionRepository.deleteAll();
        contentRepository.deleteAll();
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();

        designerUser = createDesignerUser();
    }

    private User createDesignerUser() {
        User user = User.create("designer@example.com", "디자이너", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    private User createAnotherUser() {
        User user = User.create("another@example.com", "다른유저", passwordEncoder.encode("Password123!"));
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

    private Content createContentWithOwner(Long ownerId) {
        Content content = Content.createFile("test.mp4", "stored.mp4", ContentType.VIDEO, 1000L, "/path/test.mp4", ownerId);
        return contentRepository.save(content);
    }

    private ContentVersion createVersion(Content content, int versionNumber, VersionChangeType changeType) {
        ContentVersion version = ContentVersion.createFrom(
                content, versionNumber, changeType, content.getCreatedBy(), "Version " + versionNumber
        );
        return contentVersionRepository.save(version);
    }

    // ==================== 버전 이력 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/{contentId}/versions - 버전 이력 조회")
    class GetVersionHistory {

        @Test
        @DisplayName("성공 - 버전 이력 조회")
        void getVersionHistory_success() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);
            createVersion(content, 2, VersionChangeType.METADATA_UPDATE);

            mockMvc.perform(get("/api/contents/{contentId}/versions", content.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].versionNumber").value(2))
                    .andExpect(jsonPath("$.data[1].versionNumber").value(1));
        }

        @Test
        @DisplayName("성공 - 버전이 없는 경우 빈 배열 반환")
        void getVersionHistory_empty() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());

            mockMvc.perform(get("/api/contents/{contentId}/versions", content.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠")
        void getVersionHistory_contentNotFound() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            mockMvc.perform(get("/api/contents/{contentId}/versions", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CT001"));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 콘텐츠")
        void getVersionHistory_unauthorized() throws Exception {
            createAnotherUser();
            String accessToken = loginAndGetAccessToken("another@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());

            mockMvc.perform(get("/api/contents/{contentId}/versions", content.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CT008"));
        }
    }

    // ==================== 특정 버전 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/{contentId}/versions/{versionNumber} - 특정 버전 조회")
    class GetVersion {

        @Test
        @DisplayName("성공 - 특정 버전 조회")
        void getVersion_success() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);
            createVersion(content, 2, VersionChangeType.FILE_REPLACE);

            mockMvc.perform(get("/api/contents/{contentId}/versions/{versionNumber}", content.getId(), 1)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.versionNumber").value(1))
                    .andExpect(jsonPath("$.data.changeType").value("FILE_UPLOAD"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 버전")
        void getVersion_versionNotFound() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);

            mockMvc.perform(get("/api/contents/{contentId}/versions/{versionNumber}", content.getId(), 99)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CT009"));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 콘텐츠")
        void getVersion_unauthorized() throws Exception {
            createAnotherUser();
            String accessToken = loginAndGetAccessToken("another@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);

            mockMvc.perform(get("/api/contents/{contentId}/versions/{versionNumber}", content.getId(), 1)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("CT008"));
        }
    }

    // ==================== 버전 복원 테스트 ====================

    @Nested
    @DisplayName("POST /api/contents/{contentId}/versions/{versionNumber}/restore - 버전 복원")
    class RestoreVersion {

        @Test
        @DisplayName("성공 - 버전 복원")
        void restoreVersion_success() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);

            RestoreVersionRequest request = new RestoreVersionRequest("Restoring to version 1");

            mockMvc.perform(post("/api/contents/{contentId}/versions/{versionNumber}/restore", content.getId(), 1)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(content.getId()));
        }

        @Test
        @DisplayName("성공 - changeSummary 없이 복원")
        void restoreVersion_withoutSummary() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);

            mockMvc.perform(post("/api/contents/{contentId}/versions/{versionNumber}/restore", content.getId(), 1)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 버전 복원")
        void restoreVersion_versionNotFound() throws Exception {
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());

            mockMvc.perform(post("/api/contents/{contentId}/versions/{versionNumber}/restore", content.getId(), 99)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("CT009"));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 콘텐츠 복원")
        void restoreVersion_unauthorized() throws Exception {
            createAnotherUser();
            String accessToken = loginAndGetAccessToken("another@example.com", "Password123!");
            Content content = createContentWithOwner(designerUser.getId());
            createVersion(content, 1, VersionChangeType.FILE_UPLOAD);

            mockMvc.perform(post("/api/contents/{contentId}/versions/{versionNumber}/restore", content.getId(), 1)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("CT008"));
        }
    }
}
