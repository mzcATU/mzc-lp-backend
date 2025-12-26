package com.mzc.lp.domain.program.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.program.constant.ProgramLevel;
import com.mzc.lp.domain.program.constant.ProgramStatus;
import com.mzc.lp.domain.program.constant.ProgramType;
import com.mzc.lp.domain.program.dto.request.ApproveRequest;
import com.mzc.lp.domain.program.dto.request.CreateProgramRequest;
import com.mzc.lp.domain.program.dto.request.RejectRequest;
import com.mzc.lp.domain.program.dto.request.UpdateProgramRequest;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.program.repository.ProgramRepository;
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
class ProgramControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    private User createRegularUser() {
        User user = User.create("user@example.com", "일반사용자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.USER);
        return userRepository.save(user);
    }

    private User createTenantAdminUser() {
        User user = User.create("admin@example.com", "테넌트관리자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.TENANT_ADMIN);
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

    private Program createTestProgram(String title, Long createdBy) {
        Program program = Program.create(
                title,
                "테스트 프로그램 설명",
                null,
                ProgramLevel.BEGINNER,
                ProgramType.ONLINE,
                10,
                createdBy
        );
        return programRepository.save(program);
    }

    private Program createTestProgramWithStatus(String title, Long createdBy, ProgramStatus status) {
        Program program = createTestProgram(title, createdBy);

        if (status == ProgramStatus.PENDING) {
            program.submit();
        } else if (status == ProgramStatus.APPROVED) {
            program.submit();
            program.approve(1L, "승인합니다");
        } else if (status == ProgramStatus.REJECTED) {
            program.submit();
            program.reject(1L, "반려 사유");
        } else if (status == ProgramStatus.CLOSED) {
            program.submit();
            program.approve(1L, "승인합니다");
            program.close();
        }

        return programRepository.save(program);
    }

    // ==================== 프로그램 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/programs - 프로그램 생성")
    class CreateProgram {

        @Test
        @DisplayName("성공 - DESIGNER가 프로그램 생성")
        void createProgram_success() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            CreateProgramRequest request = new CreateProgramRequest(
                    "Spring Boot 기초",
                    "Spring Boot 기초 강의입니다.",
                    null,
                    ProgramLevel.BEGINNER,
                    ProgramType.ONLINE,
                    20,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/programs")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Spring Boot 기초"))
                    .andExpect(jsonPath("$.data.status").value("DRAFT"))
                    .andExpect(jsonPath("$.data.createdBy").value(designer.getId()));
        }

        @Test
        @DisplayName("성공 - OPERATOR가 프로그램 생성")
        void createProgram_success_operator() throws Exception {
            // given
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateProgramRequest request = new CreateProgramRequest(
                    "Java 심화",
                    "Java 심화 강의입니다.",
                    null,
                    ProgramLevel.ADVANCED,
                    ProgramType.BLENDED,
                    30,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/programs")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("Java 심화"))
                    .andExpect(jsonPath("$.data.level").value("ADVANCED"))
                    .andExpect(jsonPath("$.data.type").value("BLENDED"));
        }

        @Test
        @DisplayName("실패 - 제목 누락")
        void createProgram_fail_missingTitle() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            CreateProgramRequest request = new CreateProgramRequest(
                    null,
                    "설명만 있음",
                    null,
                    ProgramLevel.BEGINNER,
                    ProgramType.ONLINE,
                    10,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/programs")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createProgram_fail_userRole() throws Exception {
            // given
            createRegularUser();
            String accessToken = loginAndGetAccessToken("user@example.com", "Password123!");
            CreateProgramRequest request = new CreateProgramRequest(
                    "테스트 프로그램",
                    "설명",
                    null,
                    ProgramLevel.BEGINNER,
                    ProgramType.ONLINE,
                    10,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/programs")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 프로그램 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/programs - 프로그램 목록 조회")
    class GetPrograms {

        @Test
        @DisplayName("성공 - 전체 프로그램 목록 조회")
        void getPrograms_success() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            createTestProgram("프로그램1", designer.getId());
            createTestProgram("프로그램2", designer.getId());
            createTestProgram("프로그램3", designer.getId());

            // when & then
            mockMvc.perform(get("/api/programs")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(3));
        }

        @Test
        @DisplayName("성공 - 상태 필터링")
        void getPrograms_success_statusFilter() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            createTestProgramWithStatus("DRAFT 프로그램", designer.getId(), ProgramStatus.DRAFT);
            createTestProgramWithStatus("PENDING 프로그램", designer.getId(), ProgramStatus.PENDING);
            createTestProgramWithStatus("APPROVED 프로그램", designer.getId(), ProgramStatus.APPROVED);

            // when & then
            mockMvc.perform(get("/api/programs")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("status", "PENDING"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 생성자 필터링")
        void getPrograms_success_creatorFilter() throws Exception {
            // given
            User designer = createDesignerUser();
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            createTestProgram("디자이너 프로그램1", designer.getId());
            createTestProgram("디자이너 프로그램2", designer.getId());
            createTestProgram("운영자 프로그램", operator.getId());

            // when & then
            mockMvc.perform(get("/api/programs")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("createdBy", designer.getId().toString()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }
    }

    // ==================== 프로그램 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/programs/{programId} - 프로그램 상세 조회")
    class GetProgramDetail {

        @Test
        @DisplayName("성공 - 프로그램 상세 조회")
        void getProgramDetail_success() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgram("테스트 프로그램", designer.getId());

            // when & then
            mockMvc.perform(get("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(program.getId()))
                    .andExpect(jsonPath("$.data.title").value("테스트 프로그램"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 프로그램")
        void getProgramDetail_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/programs/{programId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG001"));
        }
    }

    // ==================== 프로그램 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/programs/{programId} - 프로그램 수정")
    class UpdateProgram {

        @Test
        @DisplayName("성공 - DRAFT 상태에서 수정")
        void updateProgram_success_draft() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgram("원래 제목", designer.getId());
            UpdateProgramRequest request = new UpdateProgramRequest(
                    "수정된 제목",
                    "수정된 설명",
                    null,
                    ProgramLevel.INTERMEDIATE,
                    ProgramType.OFFLINE,
                    15
            );

            // when & then
            mockMvc.perform(put("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.level").value("INTERMEDIATE"))
                    .andExpect(jsonPath("$.data.type").value("OFFLINE"));
        }

        @Test
        @DisplayName("실패 - APPROVED 상태에서 수정 시도")
        void updateProgram_fail_approved() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgramWithStatus("승인된 프로그램", designer.getId(), ProgramStatus.APPROVED);
            UpdateProgramRequest request = new UpdateProgramRequest(
                    "수정 시도",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG003"));
        }
    }

    // ==================== 프로그램 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/programs/{programId} - 프로그램 삭제")
    class DeleteProgram {

        @Test
        @DisplayName("성공 - DRAFT 상태 프로그램 삭제")
        void deleteProgram_success() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgram("삭제할 프로그램", designer.getId());

            // when & then
            mockMvc.perform(delete("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 프로그램")
        void deleteProgram_fail_notFound() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/programs/{programId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG001"));
        }

        @Test
        @DisplayName("실패 - 다른 사용자가 생성한 프로그램 삭제 시도")
        void deleteProgram_fail_notOwner() throws Exception {
            // given
            User designer = createDesignerUser();
            User operator = createOperatorUser();
            String operatorToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgram("디자이너 프로그램", designer.getId());

            // when & then
            mockMvc.perform(delete("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + operatorToken))
                    .andDo(print())
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG006"));
        }

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 다른 사용자의 프로그램 삭제")
        void deleteProgram_success_tenantAdmin() throws Exception {
            // given
            User designer = createDesignerUser();
            createTenantAdminUser();
            String adminToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            Program program = createTestProgram("디자이너 프로그램", designer.getId());

            // when & then
            mockMvc.perform(delete("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/programs/{programId}", program.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== 프로그램 제출 테스트 ====================

    @Nested
    @DisplayName("POST /api/programs/{programId}/submit - 프로그램 제출")
    class SubmitProgram {

        @Test
        @DisplayName("성공 - DRAFT → PENDING 제출")
        void submitProgram_success() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgram("제출할 프로그램", designer.getId());

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/submit", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("실패 - 이미 PENDING 상태에서 제출 시도")
        void submitProgram_fail_alreadyPending() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgramWithStatus("이미 제출됨", designer.getId(), ProgramStatus.PENDING);

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/submit", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG002"));
        }
    }

    // ==================== 검토 대기 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/programs/pending - 검토 대기 목록 조회")
    class GetPendingPrograms {

        @Test
        @DisplayName("성공 - OPERATOR가 검토 대기 목록 조회")
        void getPendingPrograms_success() throws Exception {
            // given
            User designer = createDesignerUser();
            User operator = createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestProgramWithStatus("PENDING 프로그램1", designer.getId(), ProgramStatus.PENDING);
            createTestProgramWithStatus("PENDING 프로그램2", designer.getId(), ProgramStatus.PENDING);
            createTestProgramWithStatus("DRAFT 프로그램", designer.getId(), ProgramStatus.DRAFT);

            // when & then
            mockMvc.perform(get("/api/programs/pending")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("실패 - DESIGNER는 검토 대기 목록 접근 불가")
        void getPendingPrograms_fail_designerRole() throws Exception {
            // given
            createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/programs/pending")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 프로그램 승인 테스트 ====================

    @Nested
    @DisplayName("POST /api/programs/{programId}/approve - 프로그램 승인")
    class ApproveProgram {

        @Test
        @DisplayName("성공 - PENDING → APPROVED 승인")
        void approveProgram_success() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgramWithStatus("승인 대기 프로그램", designer.getId(), ProgramStatus.PENDING);
            ApproveRequest request = new ApproveRequest("잘 작성되었습니다.");

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/approve", program.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"))
                    .andExpect(jsonPath("$.data.approvalComment").value("잘 작성되었습니다."));
        }

        @Test
        @DisplayName("성공 - 코멘트 없이 승인")
        void approveProgram_success_noComment() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgramWithStatus("승인 대기 프로그램", designer.getId(), ProgramStatus.PENDING);

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/approve", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }

        @Test
        @DisplayName("실패 - DRAFT 상태에서 승인 시도")
        void approveProgram_fail_draft() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgram("DRAFT 프로그램", designer.getId());

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/approve", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG002"));
        }

        @Test
        @DisplayName("실패 - DESIGNER는 승인 불가")
        void approveProgram_fail_designerRole() throws Exception {
            // given
            User designer = createDesignerUser();
            String accessToken = loginAndGetAccessToken("designer@example.com", "Password123!");
            Program program = createTestProgramWithStatus("승인 대기 프로그램", designer.getId(), ProgramStatus.PENDING);

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/approve", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 프로그램 반려 테스트 ====================

    @Nested
    @DisplayName("POST /api/programs/{programId}/reject - 프로그램 반려")
    class RejectProgram {

        @Test
        @DisplayName("성공 - PENDING → REJECTED 반려")
        void rejectProgram_success() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgramWithStatus("반려 대기 프로그램", designer.getId(), ProgramStatus.PENDING);
            RejectRequest request = new RejectRequest("내용이 부족합니다. 보완해주세요.");

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/reject", program.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"))
                    .andExpect(jsonPath("$.data.rejectionReason").value("내용이 부족합니다. 보완해주세요."));
        }

        @Test
        @DisplayName("실패 - 반려 사유 누락")
        void rejectProgram_fail_missingReason() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgramWithStatus("반려 대기 프로그램", designer.getId(), ProgramStatus.PENDING);
            RejectRequest request = new RejectRequest(null);

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/reject", program.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - DRAFT 상태에서 반려 시도")
        void rejectProgram_fail_draft() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgram("DRAFT 프로그램", designer.getId());
            RejectRequest request = new RejectRequest("반려 사유");

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/reject", program.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG002"));
        }
    }

    // ==================== 프로그램 종료 테스트 ====================

    @Nested
    @DisplayName("POST /api/programs/{programId}/close - 프로그램 종료")
    class CloseProgram {

        @Test
        @DisplayName("성공 - APPROVED → CLOSED 종료")
        void closeProgram_success() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgramWithStatus("승인된 프로그램", designer.getId(), ProgramStatus.APPROVED);

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/close", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));
        }

        @Test
        @DisplayName("실패 - PENDING 상태에서 종료 시도")
        void closeProgram_fail_pending() throws Exception {
            // given
            User designer = createDesignerUser();
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Program program = createTestProgramWithStatus("승인 대기 프로그램", designer.getId(), ProgramStatus.PENDING);

            // when & then
            mockMvc.perform(post("/api/programs/{programId}/close", program.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("PG002"));
        }
    }
}
