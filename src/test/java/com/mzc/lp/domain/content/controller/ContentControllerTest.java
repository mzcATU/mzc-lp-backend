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
import com.mzc.lp.domain.learning.entity.LearningObject;
import com.mzc.lp.domain.course.entity.Course;
import com.mzc.lp.domain.course.entity.CourseItem;
import com.mzc.lp.domain.course.repository.CourseRepository;
import com.mzc.lp.domain.course.repository.CourseItemRepository;
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
    private CourseRepository courseRepository;

    @Autowired
    private CourseItemRepository courseItemRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // FK 제약조건: CourseItem -> LearningObject -> Content, ContentVersion -> Content
        courseItemRepository.deleteAll();
        courseRepository.deleteAll();
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
        @DisplayName("성공 - 일반 URL도 등록 가능")
        void createExternalLink_success_anyUrl() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "https://example.com/video",
                    "일반 URL 링크",
                    null
            );

            // when & then - URL 제한 해제됨
            mockMvc.perform(post("/api/contents/external-link")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.contentType").value("EXTERNAL_LINK"));
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

    // ==================== 스트리밍 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/{contentId}/stream - 스트리밍")
    class StreamContent {

        @Test
        @DisplayName("성공 - 파일 스트리밍")
        void streamContent_success() throws Exception {
            // given
            User user = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // 실제 파일이 필요하므로 업로드 후 스트리밍 테스트
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-video.mp4",
                    "video/mp4",
                    "test video content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/stream", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠")
        void streamContent_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/stream", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== 다운로드 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/{contentId}/download - 다운로드")
    class DownloadContent {

        @Test
        @DisplayName("성공 - 파일 다운로드")
        void downloadContent_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-document.pdf",
                    "application/pdf",
                    "test pdf content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/download", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Content-Disposition"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠")
        void downloadContent_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/download", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== 미리보기 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/{contentId}/preview - 미리보기")
    class PreviewContent {

        @Test
        @DisplayName("성공 - 파일 미리보기")
        void previewContent_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-image.png",
                    "image/png",
                    "test image content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/preview", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "inline"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠")
        void previewContent_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/contents/{contentId}/preview", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== 파일 교체 테스트 ====================

    @Nested
    @DisplayName("PUT /api/contents/{contentId}/file - 파일 교체")
    class ReplaceFile {

        @Test
        @DisplayName("성공 - 파일 교체")
        void replaceFile_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // 먼저 파일 업로드
            MockMultipartFile originalFile = new MockMultipartFile(
                    "file",
                    "original.mp4",
                    "video/mp4",
                    "original content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(originalFile)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // 새 파일로 교체
            MockMultipartFile newFile = new MockMultipartFile(
                    "file",
                    "new-video.mp4",
                    "video/mp4",
                    "new content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/{contentId}/file", contentId)
                            .file(newFile)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.currentVersion").value(2));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 콘텐츠")
        void replaceFile_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "new-video.mp4",
                    "video/mp4",
                    "new content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/{contentId}/file", 99999L)
                            .file(file)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== 내 콘텐츠 목록 테스트 ====================

    @Nested
    @DisplayName("GET /api/contents/my - 내 콘텐츠 목록")
    class GetMyContents {

        @Test
        @DisplayName("성공 - 내 콘텐츠 목록 조회")
        void getMyContents_success() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // 콘텐츠 업로드
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "my-video.mp4",
                    "video/mp4",
                    "my content".getBytes()
            );

            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated());

            // when & then
            mockMvc.perform(get("/api/contents/my")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 타입 필터링")
        void getMyContents_filterByType() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // 비디오 업로드
            MockMultipartFile video = new MockMultipartFile(
                    "file", "video.mp4", "video/mp4", "video".getBytes()
            );
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(video)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated());

            // 문서 업로드
            MockMultipartFile doc = new MockMultipartFile(
                    "file", "doc.pdf", "application/pdf", "doc".getBytes()
            );
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(doc)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated());

            // when & then - VIDEO만 필터링
            mockMvc.perform(get("/api/contents/my")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("contentType", "VIDEO"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    // ==================== 보관/복원 테스트 ====================

    @Nested
    @DisplayName("POST /api/contents/{contentId}/archive, /restore - 보관/복원")
    class ArchiveRestore {

        @Test
        @DisplayName("성공 - 콘텐츠 보관")
        void archiveContent_success() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then
            mockMvc.perform(post("/api/contents/{contentId}/archive", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ARCHIVED"));
        }

        @Test
        @DisplayName("성공 - 콘텐츠 복원")
        void restoreContent_success() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // 먼저 보관
            mockMvc.perform(post("/api/contents/{contentId}/archive", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isOk());

            // when & then - 복원
            mockMvc.perform(post("/api/contents/{contentId}/restore", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("실패 - 다른 사용자 콘텐츠 보관 시도")
        void archiveContent_fail_unauthorized() throws Exception {
            // given
            User designer1 = createDesignerUser();
            designer1.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer1);
            String accessToken1 = loginAndGetAccessToken("designer@example.com", "Password123!");

            // designer1이 콘텐츠 업로드
            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken1))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // 다른 사용자 생성
            User designer2 = User.create("designer2@example.com", "디자이너2", passwordEncoder.encode("Password123!"));
            designer2.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer2);
            String accessToken2 = loginAndGetAccessToken("designer2@example.com", "Password123!");

            // when & then - designer2가 designer1의 콘텐츠 보관 시도
            mockMvc.perform(post("/api/contents/{contentId}/archive", contentId)
                            .header("Authorization", "Bearer " + accessToken2))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== downloadable 옵션 테스트 ====================

    @Nested
    @DisplayName("downloadable 옵션 테스트")
    class DownloadableOption {

        @Test
        @DisplayName("성공 - downloadable=true로 업로드")
        void upload_withDownloadableTrue() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .param("downloadable", "true")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.downloadable").value(true));
        }

        @Test
        @DisplayName("성공 - downloadable=false로 업로드")
        void upload_withDownloadableFalse() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .param("downloadable", "false")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.downloadable").value(false));
        }

        @Test
        @DisplayName("실패 - downloadable=false인 콘텐츠 다운로드 거부")
        void download_fail_downloadableDisabled() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            // downloadable=false로 업로드
            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .param("downloadable", "false")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then - 다운로드 시도
            mockMvc.perform(get("/api/contents/{contentId}/download", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("CT011"));
        }

        @Test
        @DisplayName("성공 - downloadable 옵션 수정")
        void updateDownloadable_success() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            // 기본값으로 업로드
            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // downloadable을 false로 수정
            UpdateContentRequest request = new UpdateContentRequest(null, null, null, false);

            // when & then
            mockMvc.perform(put("/api/contents/{contentId}", contentId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.downloadable").value(false));
        }
    }

    // ==================== 외부 링크 URL 검증 테스트 ====================

    @Nested
    @DisplayName("외부 링크 URL 검증 테스트")
    class ExternalLinkValidation {

        @Test
        @DisplayName("성공 - 일반 https URL 등록")
        void createExternalLink_success_genericHttps() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "https://example.com/learning-resource",
                    "외부 학습 자료",
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
        @DisplayName("성공 - http URL 등록")
        void createExternalLink_success_http() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "http://internal-server.local/resource",
                    "내부 서버 자료",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/contents/external-link")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - http/https로 시작하지 않는 URL")
        void createExternalLink_fail_invalidProtocol() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            CreateExternalLinkRequest request = new CreateExternalLinkRequest(
                    "ftp://server.com/file",
                    "FTP 링크",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/contents/external-link")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("CT004"));
        }
    }

    // ==================== 수정 제한 (inCourse) 테스트 ====================

    @Nested
    @DisplayName("수정 제한 (inCourse) 테스트")
    class ContentInCourseRestriction {

        @Test
        @DisplayName("성공 - 상세 조회 시 inCourse 필드 반환 (강의 미포함)")
        void getContent_inCourse_false() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then - 강의에 포함되지 않은 콘텐츠
            mockMvc.perform(get("/api/contents/{contentId}", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.inCourse").value(false));
        }

        @Test
        @DisplayName("성공 - 상세 조회 시 inCourse 필드 반환 (강의 포함)")
        void getContent_inCourse_true() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // Content -> LearningObject -> CourseItem 연결
            Content content = contentRepository.findById(contentId).orElseThrow();
            LearningObject lo = LearningObject.create("Test LO", content);
            LearningObject savedLo = learningObjectRepository.save(lo);

            Course course = Course.create("Test Course", designer.getId());
            Course savedCourse = courseRepository.save(course);

            CourseItem item = CourseItem.createItem(savedCourse, "Test Item", null, savedLo.getId());
            courseItemRepository.save(item);

            // when & then - 강의에 포함된 콘텐츠
            mockMvc.perform(get("/api/contents/{contentId}", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.inCourse").value(true));
        }

        @Test
        @DisplayName("실패 - 강의에 포함된 콘텐츠 수정 시도")
        void updateContent_fail_inCourse() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // Content -> LearningObject -> CourseItem 연결
            Content content = contentRepository.findById(contentId).orElseThrow();
            LearningObject lo = LearningObject.create("Test LO", content);
            LearningObject savedLo = learningObjectRepository.save(lo);

            Course course = Course.create("Test Course", designer.getId());
            Course savedCourse = courseRepository.save(course);

            CourseItem item = CourseItem.createItem(savedCourse, "Test Item", null, savedLo.getId());
            courseItemRepository.save(item);

            // 수정 요청
            UpdateContentRequest request = new UpdateContentRequest("updated-name.mp4", 120, "1920x1080", null);

            // when & then - 수정 불가
            mockMvc.perform(put("/api/contents/{contentId}", contentId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("CT010"));
        }

        @Test
        @DisplayName("실패 - 강의에 포함된 콘텐츠 삭제 시도")
        void deleteContent_fail_inCourse() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // Content -> LearningObject -> CourseItem 연결
            Content content = contentRepository.findById(contentId).orElseThrow();
            LearningObject lo = LearningObject.create("Test LO", content);
            LearningObject savedLo = learningObjectRepository.save(lo);

            Course course = Course.create("Test Course", designer.getId());
            Course savedCourse = courseRepository.save(course);

            CourseItem item = CourseItem.createItem(savedCourse, "Test Item", null, savedLo.getId());
            courseItemRepository.save(item);

            // when & then - 삭제 불가
            mockMvc.perform(delete("/api/contents/{contentId}", contentId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("CT010"));
        }

        @Test
        @DisplayName("실패 - 강의에 포함된 콘텐츠 파일 교체 시도")
        void replaceFile_fail_inCourse() throws Exception {
            // given
            User designer = createDesignerUser();
            designer.updateRole(TenantRole.DESIGNER);
            userRepository.save(designer);
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile originalFile = new MockMultipartFile(
                    "file", "original.mp4", "video/mp4", "original content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(originalFile)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // Content -> LearningObject -> CourseItem 연결
            Content content = contentRepository.findById(contentId).orElseThrow();
            LearningObject lo = LearningObject.create("Test LO", content);
            LearningObject savedLo = learningObjectRepository.save(lo);

            Course course = Course.create("Test Course", designer.getId());
            Course savedCourse = courseRepository.save(course);

            CourseItem item = CourseItem.createItem(savedCourse, "Test Item", null, savedLo.getId());
            courseItemRepository.save(item);

            // 새 파일로 교체 시도
            MockMultipartFile newFile = new MockMultipartFile(
                    "file", "new-video.mp4", "video/mp4", "new content".getBytes()
            );

            // when & then - 파일 교체 불가
            mockMvc.perform(multipart("/api/contents/{contentId}/file", contentId)
                            .file(newFile)
                            .with(request -> {
                                request.setMethod("PUT");
                                return request;
                            })
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("CT010"));
        }
    }

    // ==================== 낙관적 락 테스트 ====================

    @Nested
    @DisplayName("낙관적 락 (@Version) 테스트")
    class OptimisticLocking {

        @Test
        @DisplayName("성공 - 단일 수정 시 버전 증가")
        void updateContent_versionIncrement() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // 초기 버전 확인
            Content initialContent = contentRepository.findById(contentId).orElseThrow();
            Long initialVersion = initialContent.getVersion();

            // 수정
            UpdateContentRequest request = new UpdateContentRequest("updated-name.mp4", 120, "1920x1080", null);

            mockMvc.perform(put("/api/contents/{contentId}", contentId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            // when & then - 버전이 증가했는지 확인
            Content updatedContent = contentRepository.findById(contentId).orElseThrow();
            assert updatedContent.getVersion() > initialVersion;
        }

        @Test
        @DisplayName("엔티티에 @Version 필드 존재 확인")
        void content_hasVersionField() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            MockMultipartFile file = new MockMultipartFile(
                    "file", "test.mp4", "video/mp4", "content".getBytes()
            );

            MvcResult uploadResult = mockMvc.perform(multipart("/api/contents/upload")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long contentId = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                    .get("data").get("id").asLong();

            // when & then - Content 엔티티가 version 필드를 가지고 있는지 확인
            Content content = contentRepository.findById(contentId).orElseThrow();
            assert content.getVersion() != null;
            assert content.getVersion() >= 0;
        }
    }
}
