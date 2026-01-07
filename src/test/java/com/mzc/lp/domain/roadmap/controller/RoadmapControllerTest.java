package com.mzc.lp.domain.roadmap.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.repository.ProgramRepository;
import com.mzc.lp.domain.roadmap.constant.RoadmapStatus;
import com.mzc.lp.domain.roadmap.dto.request.CreateRoadmapRequest;
import com.mzc.lp.domain.roadmap.dto.request.SaveDraftRequest;
import com.mzc.lp.domain.roadmap.dto.request.UpdateRoadmapRequest;
import com.mzc.lp.domain.roadmap.entity.Roadmap;
import com.mzc.lp.domain.roadmap.repository.RoadmapProgramRepository;
import com.mzc.lp.domain.roadmap.repository.RoadmapRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.entity.UserCourseRole;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserCourseRoleRepository;
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

import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("RoadmapController 통합 테스트")
class RoadmapControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private RoadmapProgramRepository roadmapProgramRepository;

    @Autowired
    private ProgramRepository programRepository;

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
        roadmapProgramRepository.deleteAll();
        roadmapRepository.deleteAll();
        programRepository.deleteAll();
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ===== 헬퍼 메서드 =====

    private User createDesignerUser() {
        User user = User.create("designer@example.com", "설계자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.DESIGNER);
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

    private Program createProgram(String title, User creator) {
        Program program = Program.create(
                title,
                "설명",
                null,
                ProgramLevel.BEGINNER,
                ProgramType.ONLINE,
                null,
                creator.getId()
        );
        return programRepository.save(program);
    }

    private void assignDesignerRole(User user) {
        UserCourseRole role = UserCourseRole.createDesigner(user);
        userCourseRoleRepository.save(role);
    }

    // ===== 테스트 =====

    @Test
    @DisplayName("로드맵 생성 - DESIGNER 권한으로 성공")
    void createRoadmap_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Program program1 = createProgram("프로그램1", designer);
        Program program2 = createProgram("프로그램2", designer);

        CreateRoadmapRequest request = new CreateRoadmapRequest(
                "테스트 로드맵",
                "테스트 설명",
                Arrays.asList(program1.getId(), program2.getId()),
                RoadmapStatus.DRAFT
        );

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(post("/api/roadmaps")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("테스트 로드맵"))
                .andExpect(jsonPath("$.data.courseCount").value(2))
                .andExpect(jsonPath("$.data.status").value("draft"));
    }

    @Test
    @DisplayName("로드맵 생성 - 제목 누락 실패")
    void createRoadmap_MissingTitle_Fail() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Program program1 = createProgram("프로그램1", designer);

        CreateRoadmapRequest request = new CreateRoadmapRequest(
                null, // 제목 누락
                "테스트 설명",
                Arrays.asList(program1.getId()),
                RoadmapStatus.DRAFT
        );

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(post("/api/roadmaps")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 로드맵 목록 조회 - 성공")
    void getMyRoadmaps_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Program program1 = createProgram("프로그램1", designer);
        Program program2 = createProgram("프로그램2", designer);

        Roadmap roadmap1 = Roadmap.create("로드맵1", "설명1", designer.getId(), RoadmapStatus.DRAFT);
        Roadmap roadmap2 = Roadmap.create("로드맵2", "설명2", designer.getId(), RoadmapStatus.PUBLISHED);
        roadmapRepository.save(roadmap1);
        roadmapRepository.save(roadmap2);

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(get("/api/roadmaps")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("sortBy", "updatedAt"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("로드맵 상세 조회 - 성공")
    void getRoadmap_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Program program = createProgram("프로그램1", designer);
        Roadmap roadmap = Roadmap.create("테스트 로드맵", "테스트 설명", designer.getId(), RoadmapStatus.PUBLISHED);
        roadmapRepository.save(roadmap);

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(get("/api/roadmaps/{id}", roadmap.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(roadmap.getId()))
                .andExpect(jsonPath("$.data.title").value("테스트 로드맵"));
    }

    @Test
    @DisplayName("로드맵 상세 조회 - 존재하지 않음")
    void getRoadmap_NotFound() throws Exception {
        // given
        User designer = createDesignerUser();
        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(get("/api/roadmaps/{id}", 999L)
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로드맵 수정 - 성공")
    void updateRoadmap_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Program program1 = createProgram("프로그램1", designer);
        Program program2 = createProgram("프로그램2", designer);
        Program program3 = createProgram("프로그램3", designer);

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", designer.getId(), RoadmapStatus.DRAFT);
        roadmapRepository.save(roadmap);

        UpdateRoadmapRequest request = new UpdateRoadmapRequest(
                "수정된 제목",
                "수정된 설명",
                Arrays.asList(program1.getId(), program2.getId(), program3.getId()),
                RoadmapStatus.PUBLISHED
        );

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(patch("/api/roadmaps/{id}", roadmap.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                .andExpect(jsonPath("$.data.status").value("published"))
                .andExpect(jsonPath("$.data.courseCount").value(3));
    }

    @Test
    @DisplayName("임시저장 - 빈 프로그램 목록으로 성공")
    void saveDraft_EmptyProgramIds_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Roadmap roadmap = Roadmap.create("원본 제목", "원본 설명", designer.getId(), RoadmapStatus.DRAFT);
        roadmapRepository.save(roadmap);

        SaveDraftRequest request = new SaveDraftRequest(
                "임시저장 제목",
                "임시저장 설명",
                Arrays.asList() // 빈 목록
        );

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(patch("/api/roadmaps/{id}/draft", roadmap.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("임시저장 제목"))
                .andExpect(jsonPath("$.data.courseCount").value(0));
    }

    @Test
    @DisplayName("로드맵 삭제 - 성공")
    void deleteRoadmap_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Roadmap roadmap = Roadmap.create("테스트 로드맵", "테스트 설명", designer.getId(), RoadmapStatus.DRAFT);
        roadmapRepository.save(roadmap);

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(delete("/api/roadmaps/{id}", roadmap.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("로드맵 복제 - 성공")
    void duplicateRoadmap_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Program program = createProgram("프로그램1", designer);
        Roadmap roadmap = Roadmap.create("원본 로드맵", "원본 설명", designer.getId(), RoadmapStatus.PUBLISHED);
        roadmapRepository.save(roadmap);

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(post("/api/roadmaps/{id}/duplicate", roadmap.getId())
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value(containsString("원본 로드맵")))
                .andExpect(jsonPath("$.data.title").value(containsString("복사본")))
                .andExpect(jsonPath("$.data.status").value("draft"));
    }

    @Test
    @DisplayName("통계 조회 - 성공")
    void getStatistics_Success() throws Exception {
        // given
        User designer = createDesignerUser();
        assignDesignerRole(designer);

        Roadmap roadmap1 = Roadmap.create("로드맵1", "설명1", designer.getId(), RoadmapStatus.DRAFT);
        Roadmap roadmap2 = Roadmap.create("로드맵2", "설명2", designer.getId(), RoadmapStatus.PUBLISHED);
        roadmapRepository.save(roadmap1);
        roadmapRepository.save(roadmap2);

        String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

        // when & then
        mockMvc.perform(get("/api/roadmaps/statistics")
                        .header("Authorization", "Bearer " + accessToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRoadmaps").value(2))
                .andExpect(jsonPath("$.data.totalEnrollments").value(0));
    }
}
