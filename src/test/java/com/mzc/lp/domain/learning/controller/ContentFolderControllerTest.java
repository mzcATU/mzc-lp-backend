package com.mzc.lp.domain.learning.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.learning.dto.request.CreateContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.MoveContentFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateContentFolderRequest;
import com.mzc.lp.domain.learning.entity.ContentFolder;
import com.mzc.lp.domain.learning.repository.ContentFolderRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContentFolderControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ContentFolderRepository contentFolderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        contentFolderRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // OPERATOR 권한 유저 생성
    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    // 로그인 후 토큰 반환
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

    @Test
    @DisplayName("폴더 생성 성공 - 루트 폴더")
    void createFolder_Root_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        CreateContentFolderRequest request = new CreateContentFolderRequest("루트 폴더", null);

        // When & Then
        mockMvc.perform(post("/api/content-folders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.folderName").value("루트 폴더"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.parentId").isEmpty());
    }

    @Test
    @DisplayName("폴더 생성 성공 - 하위 폴더")
    void createFolder_Child_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder parent = createTestFolder("부모 폴더", null);
        CreateContentFolderRequest request = new CreateContentFolderRequest("하위 폴더", parent.getId());

        // When & Then
        mockMvc.perform(post("/api/content-folders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.folderName").value("하위 폴더"))
                .andExpect(jsonPath("$.data.depth").value(1))
                .andExpect(jsonPath("$.data.parentId").value(parent.getId()));
    }

    @Test
    @DisplayName("폴더 생성 실패 - 최대 깊이 초과")
    void createFolder_MaxDepthExceeded() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        // 3단계 폴더 생성 (depth 0, 1, 2)
        ContentFolder level0 = createTestFolder("레벨0", null);
        ContentFolder level1 = createTestFolder("레벨1", level0);
        ContentFolder level2 = createTestFolder("레벨2", level1);

        // depth 3 시도
        CreateContentFolderRequest request = new CreateContentFolderRequest("레벨3", level2.getId());

        // When & Then
        mockMvc.perform(post("/api/content-folders")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("폴더 트리 조회 성공")
    void getFolderTree_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder root1 = createTestFolder("루트1", null);
        createTestFolder("루트2", null);
        createTestFolder("루트1-자식", root1);

        // When & Then
        mockMvc.perform(get("/api/content-folders/tree")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("폴더 단건 조회 성공")
    void getFolder_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder folder = createTestFolder("조회할 폴더", null);

        // When & Then
        mockMvc.perform(get("/api/content-folders/{id}", folder.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.folderId").value(folder.getId()))
                .andExpect(jsonPath("$.data.folderName").value("조회할 폴더"));
    }

    @Test
    @DisplayName("폴더 단건 조회 - 404")
    void getFolder_NotFound() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        // When & Then
        mockMvc.perform(get("/api/content-folders/{id}", 99999L)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("하위 폴더 목록 조회 성공")
    void getChildFolders_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder parent = createTestFolder("부모", null);
        createTestFolder("자식1", parent);
        createTestFolder("자식2", parent);

        // When & Then
        mockMvc.perform(get("/api/content-folders/{id}/children", parent.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("폴더 수정 성공")
    void updateFolder_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder folder = createTestFolder("원래 이름", null);
        UpdateContentFolderRequest request = new UpdateContentFolderRequest("수정된 이름");

        // When & Then
        mockMvc.perform(put("/api/content-folders/{id}", folder.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.folderName").value("수정된 이름"));
    }

    @Test
    @DisplayName("폴더 이동 성공")
    void moveFolder_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder folder = createTestFolder("이동할 폴더", null);
        ContentFolder newParent = createTestFolder("새 부모", null);
        MoveContentFolderRequest request = new MoveContentFolderRequest(newParent.getId());

        // When & Then
        mockMvc.perform(put("/api/content-folders/{id}/move", folder.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.parentId").value(newParent.getId()))
                .andExpect(jsonPath("$.data.depth").value(1));
    }

    @Test
    @DisplayName("폴더 삭제 성공")
    void deleteFolder_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder folder = createTestFolder("삭제할 폴더", null);

        // When & Then
        mockMvc.perform(delete("/api/content-folders/{id}", folder.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("폴더 삭제 - 404")
    void deleteFolder_NotFound() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        // When & Then
        mockMvc.perform(delete("/api/content-folders/{id}", 99999L)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("인증 없이 접근 - 403")
    void accessWithoutAuth_Forbidden() throws Exception {
        mockMvc.perform(get("/api/content-folders/tree"))
                .andExpect(status().isForbidden());
    }

    // Helper method
    private ContentFolder createTestFolder(String name, ContentFolder parent) {
        ContentFolder folder = parent == null
                ? ContentFolder.createRoot(name)
                : ContentFolder.createChild(name, parent);
        return contentFolderRepository.save(folder);
    }
}
