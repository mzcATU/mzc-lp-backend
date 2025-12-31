package com.mzc.lp.domain.content.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.content.constant.ContentType;
import com.mzc.lp.domain.content.dto.request.CreateExternalLinkRequest;
import com.mzc.lp.domain.content.dto.request.UpdateContentRequest;
import com.mzc.lp.domain.content.entity.Content;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContentControllerTest extends TenantTestSupport {

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

    @BeforeEach
    void setUp() {
        // FK 제약조건: LearningObject -> Content, ContentVersion -> Content
        learningObjectRepository.deleteAll();
        contentFolderRepository.deleteAll();
        contentVersionRepository.deleteAll();
        contentRepository.deleteAll();
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // DESIGNER 권한 유저 생성
    private User createDesignerUser() {
        User user = User.create("designer@example.com", "디자이너", passwordEncoder.encode("Password123!"));
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

    // ==================== 파일 업로드 테스트 ====================

    @Nested
    @DisplayName("POST /api/contents/upload - 파일 업로드")
    class Upload {

        @Test
        @DisplayName("성공 - 비디오 파일 업로드")
        void upload_success_video() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-video.mp4",
                    "video/mp4",
                    "test video content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contentType").value("VIDEO"))
                    .andExpect(jsonPath("$.data.originalFileName").value("test-video.mp4"));
        }

        @Test
        @DisplayName("성공 - 문서 파일 업로드")
        void upload_success_document() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-document.pdf",
                    "application/pdf",
                    "test pdf content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contentType").value("DOCUMENT"));
        }

        @Test
        @DisplayName("성공 - 이미지 파일 업로드")
        void upload_success_image() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-image.png",
                    "image/png",
                    "test image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contentType").value("IMAGE"));
        }

        @Test
        @DisplayName("실패 - 지원하지 않는 파일 형식")
        void upload_fail_unsupportedType() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.exe",
                    "application/octet-stream",
                    "test content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CT002"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 업로드")
        void upload_fail_unauthorized() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.mp4",
                    "video/mp4",
                    "test content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 일반 유저 권한으로 업로드")
        void upload_fail_userRole() throws Exception {
            // given
            createNormalUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test.mp4",
                    "video/mp4",
                    "test content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 외부 링크 등록 테스트 ====================

    @Nested
    @DisplayName("POST /api/contents/external-link - 외부 링크 등록")
    class CreateExternalLink {

        @Test
        @DisplayName("성공 - YouTube 링크 등록")
        void createExternalLink_success_youtube() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "https://www.youtube.com/watch?v=abc123",
                    "테스트 유튜브 영상",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/contents/external-link")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contentType").value("EXTERNAL_LINK"))
                    .andExpect(jsonPath("$.data.originalFileName").value("테스트 유튜브 영상"));
        }

        @Test
        @DisplayName("성공 - Vimeo 링크 등록")
        void createExternalLink_success_vimeo() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "https://vimeo.com/123456789",
                    "테스트 Vimeo 영상",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/contents/external-link")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contentType").value("EXTERNAL_LINK"));
        }

        @Test
        @DisplayName("실패 - 지원하지 않는 URL")
        void createExternalLink_fail_unsupportedUrl() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "https://example.com/video",
                    "지원하지 않는 링크",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/contents/external-link")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CT004"));
        }
    }

    // ==================== 콘텐츠 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents - 콘텐츠 목록 조회")
    class GetContents {

        @Test
        @DisplayName("성공 - 전체 목록 조회")
        void getContents_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // 콘텐츠 생성
            Content content1 = Content.createFile("video1.mp4", "stored1.mp4", ContentType.VIDEO, 1000L, "/path/1");
            Content content2 = Content.createFile("doc1.pdf", "stored2.pdf", ContentType.DOCUMENT, 500L, "/path/2");
            contentRepository.save(content1);
            contentRepository.save(content2);

            // when & then
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - 타입 필터링")
        void getContents_success_filterByType() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            Content video = Content.createFile("video.mp4", "stored.mp4", ContentType.VIDEO, 1000L, "/path/1");
            Content doc = Content.createFile("doc.pdf", "stored.pdf", ContentType.DOCUMENT, 500L, "/path/2");
            contentRepository.save(video);
            contentRepository.save(doc);

            // when & then
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", "VIDEO"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 키워드 검색")
        void getContents_success_searchByKeyword() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            Content content1 = Content.createFile("spring-tutorial.mp4", "stored1.mp4", ContentType.VIDEO, 1000L, "/path/1");
            Content content2 = Content.createFile("java-basics.mp4", "stored2.mp4", ContentType.VIDEO, 500L, "/path/2");
            contentRepository.save(content1);
            contentRepository.save(content2);

            // when & then
            mockMvc.perform(get("/api/contents")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("keyword", "spring"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    // ==================== 콘텐츠 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/{contentId} - 콘텐츠 상세 조회")
    class GetContent {

        @Test
        @DisplayName("성공 - 상세 조회")
        void getContent_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            Content content = Content.createFile("test.mp4", "stored.mp4", ContentType.VIDEO, 1000L, "/path/1");
            Content saved = contentRepository.save(content);

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}", saved.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.originalFileName").value("test.mp4"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠")
        void getContent_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CT001"));
        }
    }

    // ==================== 콘텐츠 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/contents/{contentId} - 콘텐츠 메타데이터 수정")
    class UpdateContent {

        @Test
        @DisplayName("성공 - 메타데이터 수정")
        void updateContent_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            Content content = Content.createFile("original.mp4", "stored.mp4", ContentType.VIDEO, 1000L, "/path/1");
            Content saved = contentRepository.save(content);

            UpdateContentRequest request = new UpdateContentRequest(
                    "updated-name.mp4",
                    120,
                    "1920x1080",
                    null
            );

            // when & then
            mockMvc.perform(put("/api/contents/{contentId}", saved.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.originalFileName").value("updated-name.mp4"))
                    .andExpect(jsonPath("$.data.duration").value(120))
                    .andExpect(jsonPath("$.data.resolution").value("1920x1080"));
        }
    }

    // ==================== 콘텐츠 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/contents/{contentId} - 콘텐츠 삭제")
    class DeleteContent {

        @Test
        @DisplayName("성공 - 콘텐츠 삭제")
        void deleteContent_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // filePath를 null로 설정하여 파일 시스템 삭제 로직을 스킵 (DB 삭제만 테스트)
            Content content = Content.createFile("test.mp4", "stored.mp4", ContentType.VIDEO, 1000L, null);
            Content saved = contentRepository.save(content);

            // when & then
            mockMvc.perform(delete("/api/contents/{contentId}", saved.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/contents/{contentId}", saved.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠 삭제")
        void deleteContent_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/contents/{contentId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("CT001"));
        }
    }
}
