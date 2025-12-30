package com.mzc.lp.domain.learning.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.entity.Content;
import com.mzc.lp.domain.content.repository.ContentRepository;
import com.mzc.lp.domain.learning.dto.request.CreateLearningObjectRequest;
import com.mzc.lp.domain.learning.dto.request.MoveFolderRequest;
import com.mzc.lp.domain.learning.dto.request.UpdateLearningObjectRequest;
import com.mzc.lp.domain.learning.entity.ContentFolder;
import com.mzc.lp.domain.learning.entity.LearningObject;
import com.mzc.lp.domain.learning.repository.ContentFolderRepository;
import com.mzc.lp.domain.learning.repository.LearningObjectRepository;
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
class LearningObjectControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LearningObjectRepository learningObjectRepository;

    @Autowired
    private ContentFolderRepository contentFolderRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        learningObjectRepository.deleteAll();
        contentFolderRepository.deleteAll();
        contentRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // OPERATOR 권한 유저 생성
    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    // 일반 유저 생성
    private User createNormalUser() {
        User user = User.create("user@example.com", "일반유저", passwordEncoder.encode("Password123!"));
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
    @DisplayName("학습객체 생성 성공")
    void createLearningObject_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        CreateLearningObjectRequest request = new CreateLearningObjectRequest(
                "테스트 학습객체", content.getId(), null);

        // When & Then
        mockMvc.perform(post("/api/learning-objects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트 학습객체"))
                .andExpect(jsonPath("$.data.contentId").value(content.getId()));
    }

    @Test
    @DisplayName("학습객체 생성 - 폴더 지정")
    void createLearningObject_WithFolder() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        ContentFolder folder = createTestFolder("테스트 폴더", null);
        CreateLearningObjectRequest request = new CreateLearningObjectRequest(
                "테스트 학습객체", content.getId(), folder.getId());

        // When & Then
        mockMvc.perform(post("/api/learning-objects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.folderId").value(folder.getId()))
                .andExpect(jsonPath("$.data.folderName").value("테스트 폴더"));
    }

    @Test
    @DisplayName("학습객체 목록 조회 성공")
    void getLearningObjects_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content1 = createTestContent();
        Content content2 = createTestContent();
        createTestLearningObject("학습객체1", content1, null);
        createTestLearningObject("학습객체2", content2, null);

        // When & Then
        mockMvc.perform(get("/api/learning-objects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("학습객체 목록 조회 - 폴더 필터")
    void getLearningObjects_WithFolderFilter() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        ContentFolder folder = createTestFolder("폴더A", null);
        Content content1 = createTestContent();
        Content content2 = createTestContent();
        createTestLearningObject("폴더내 학습객체", content1, folder);
        createTestLearningObject("루트 학습객체", content2, null);

        // When & Then
        mockMvc.perform(get("/api/learning-objects")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("folderId", folder.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].folderName").value("폴더A"));
    }

    @Test
    @DisplayName("학습객체 목록 조회 - 키워드 검색")
    void getLearningObjects_WithKeyword() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content1 = createTestContent();
        Content content2 = createTestContent();
        createTestLearningObject("자바 기초 강의", content1, null);
        createTestLearningObject("파이썬 고급", content2, null);

        // When & Then
        mockMvc.perform(get("/api/learning-objects")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("keyword", "자바"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("자바 기초 강의"));
    }

    @Test
    @DisplayName("학습객체 단건 조회 성공")
    void getLearningObject_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        LearningObject lo = createTestLearningObject("테스트 학습객체", content, null);

        // When & Then
        mockMvc.perform(get("/api/learning-objects/{id}", lo.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learningObjectId").value(lo.getId()))
                .andExpect(jsonPath("$.data.name").value("테스트 학습객체"));
    }

    @Test
    @DisplayName("학습객체 단건 조회 - 404")
    void getLearningObject_NotFound() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        // When & Then
        mockMvc.perform(get("/api/learning-objects/{id}", 99999L)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("콘텐츠 ID로 학습객체 조회")
    void getLearningObjectByContentId_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        LearningObject lo = createTestLearningObject("콘텐츠 연결 학습객체", content, null);

        // When & Then
        mockMvc.perform(get("/api/learning-objects/content/{contentId}", content.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learningObjectId").value(lo.getId()))
                .andExpect(jsonPath("$.data.contentId").value(content.getId()));
    }

    @Test
    @DisplayName("학습객체 수정 성공")
    void updateLearningObject_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        LearningObject lo = createTestLearningObject("원래 이름", content, null);
        UpdateLearningObjectRequest request = new UpdateLearningObjectRequest("수정된 이름");

        // When & Then
        mockMvc.perform(put("/api/learning-objects/{id}", lo.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("수정된 이름"));
    }

    @Test
    @DisplayName("학습객체 폴더 이동 성공")
    void moveToFolder_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        ContentFolder folder = createTestFolder("대상 폴더", null);
        LearningObject lo = createTestLearningObject("이동할 학습객체", content, null);
        MoveFolderRequest request = new MoveFolderRequest(folder.getId());

        // When & Then
        mockMvc.perform(put("/api/learning-objects/{id}/folder", lo.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.folderId").value(folder.getId()))
                .andExpect(jsonPath("$.data.folderName").value("대상 폴더"));
    }

    @Test
    @DisplayName("학습객체 삭제 성공")
    void deleteLearningObject_Success() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        Content content = createTestContent();
        LearningObject lo = createTestLearningObject("삭제할 학습객체", content, null);

        // When & Then
        mockMvc.perform(delete("/api/learning-objects/{id}", lo.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("학습객체 삭제 - 404")
    void deleteLearningObject_NotFound() throws Exception {
        // Given
        createOperatorUser();
        String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

        // When & Then
        mockMvc.perform(delete("/api/learning-objects/{id}", 99999L)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("인증 없이 접근 - 403")
    void accessWithoutAuth_Forbidden() throws Exception {
        mockMvc.perform(get("/api/learning-objects"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일반 유저 권한으로 접근 - 403")
    void accessWithUserRole_Forbidden() throws Exception {
        // Given
        createNormalUser();
        String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

        // When & Then
        mockMvc.perform(get("/api/learning-objects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    // Helper methods
    private Content createTestContent() {
        Content content = Content.createFile(
                "test.mp4",
                "uuid-test.mp4",
                ContentType.VIDEO,
                1024L,
                "/uploads/2025/01/uuid-test.mp4"
        );
        return contentRepository.save(content);
    }

    private ContentFolder createTestFolder(String name, ContentFolder parent) {
        ContentFolder folder = parent == null
                ? ContentFolder.createRoot(name)
                : ContentFolder.createChild(name, parent);
        return contentFolderRepository.save(folder);
    }

    private LearningObject createTestLearningObject(String name, Content content, ContentFolder folder) {
        LearningObject lo = LearningObject.create(name, content, folder);
        return learningObjectRepository.save(lo);
    }
}
