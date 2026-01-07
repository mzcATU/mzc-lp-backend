package com.mzc.lp.domain.user.controller;
import com.mzc.lp.common.support.TenantTestSupport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.constant.UserStatus;
import com.mzc.lp.domain.user.dto.request.*;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        userCourseRoleRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // OPERATOR 사용자 생성 헬퍼
    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    // TENANT_ADMIN 사용자 생성 헬퍼
    private User createAdminUser() {
        User user = User.create("admin@example.com", "관리자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.TENANT_ADMIN);
        return userRepository.save(user);
    }

    // 특정 이메일로 로그인 후 토큰 추출 헬퍼
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

    // 테스트용 유저 생성 헬퍼
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

    // 로그인 후 액세스 토큰 추출 헬퍼
    private String loginAndGetAccessToken() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("data").get("accessToken").asText();
    }

    // ==================== 내 정보 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/users/me - 내 정보 조회")
    class GetMe {

        @Test
        @DisplayName("성공 - 내 정보 조회")
        void getMe_success() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();

            // when & then
            mockMvc.perform(get("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.name").value("홍길동"))
                    .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.role").value("USER"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getMe_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/me"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 내 정보 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/users/me - 내 정보 수정")
    class UpdateMe {

        @Test
        @DisplayName("성공 - 프로필 수정")
        void updateMe_success() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            UpdateProfileRequest request = new UpdateProfileRequest(
                    "김철수",
                    "010-9876-5432",
                    "https://cdn.example.com/profile.jpg",
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("김철수"))
                    .andExpect(jsonPath("$.data.phone").value("010-9876-5432"))
                    .andExpect(jsonPath("$.data.profileImageUrl").value("https://cdn.example.com/profile.jpg"));
        }

        @Test
        @DisplayName("성공 - 부분 수정 (이름만)")
        void updateMe_success_partial() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            UpdateProfileRequest request = new UpdateProfileRequest("김철수", null, null, null, null);

            // when & then
            mockMvc.perform(put("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("김철수"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void updateMe_fail_unauthorized() throws Exception {
            // given
            UpdateProfileRequest request = new UpdateProfileRequest("김철수", null, null, null, null);

            // when & then
            mockMvc.perform(put("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 비밀번호 변경 테스트 ====================

    @Nested
    @DisplayName("PUT /api/users/me/password - 비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공 - 비밀번호 변경")
        void changePassword_success() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "Password123!",
                    "NewPassword456!"
            );

            // when & then
            mockMvc.perform(put("/api/users/me/password")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // 변경된 비밀번호로 로그인 성공 확인
            LoginRequest loginRequest = new LoginRequest("test@example.com", "NewPassword456!");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("실패 - 현재 비밀번호 불일치")
        void changePassword_fail_wrongCurrentPassword() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "WrongPassword123!",
                    "NewPassword456!"
            );

            // when & then
            mockMvc.perform(put("/api/users/me/password")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U004"));
        }

        @Test
        @DisplayName("실패 - 새 비밀번호 형식 오류")
        void changePassword_fail_invalidNewPassword() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            ChangePasswordRequest request = new ChangePasswordRequest(
                    "Password123!",
                    "weak"
            );

            // when & then
            mockMvc.perform(put("/api/users/me/password")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ==================== 회원 탈퇴 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/users/me - 회원 탈퇴")
    class Withdraw {

        @Test
        @DisplayName("성공 - 회원 탈퇴")
        void withdraw_success() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            WithdrawRequest request = new WithdrawRequest(
                    "Password123!",
                    "더 이상 서비스를 이용하지 않습니다."
            );

            // when & then
            mockMvc.perform(delete("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 탈퇴 후 로그인 시도 - 실패해야 함 (WITHDRAWN 상태)
            LoginRequest loginRequest = new LoginRequest("test@example.com", "Password123!");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("실패 - 비밀번호 불일치")
        void withdraw_fail_wrongPassword() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            WithdrawRequest request = new WithdrawRequest(
                    "WrongPassword123!",
                    "탈퇴 사유"
            );

            // when & then
            mockMvc.perform(delete("/api/users/me")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U004"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void withdraw_fail_unauthorized() throws Exception {
            // given
            WithdrawRequest request = new WithdrawRequest("Password123!", null);

            // when & then
            mockMvc.perform(delete("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 프로필 이미지 업로드 테스트 ====================

    @Nested
    @DisplayName("POST /api/users/me/profile-image - 프로필 이미지 업로드")
    class UploadProfileImage {

        @Test
        @DisplayName("성공 - JPG 이미지 업로드")
        void uploadProfileImage_success_jpg() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/users/me/profile-image")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.profileImageUrl").exists())
                    .andExpect(jsonPath("$.data.profileImageUrl").value(org.hamcrest.Matchers.containsString("/uploads/profile-images/")));
        }

        @Test
        @DisplayName("성공 - PNG 이미지 업로드")
        void uploadProfileImage_success_png() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.png",
                    "image/png",
                    "test image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/users/me/profile-image")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.profileImageUrl").exists());
        }

        @Test
        @DisplayName("실패 - 잘못된 파일 형식 (txt)")
        void uploadProfileImage_fail_invalidFormat() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.txt",
                    "text/plain",
                    "test content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/users/me/profile-image")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U008"));
        }

        @Test
        @DisplayName("실패 - 파일 크기 초과 (6MB)")
        void uploadProfileImage_fail_sizeExceeded() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    "image/jpeg",
                    largeContent
            );

            // when & then
            mockMvc.perform(multipart("/api/users/me/profile-image")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U009"));
        }

        @Test
        @DisplayName("실패 - 빈 파일")
        void uploadProfileImage_fail_emptyFile() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    "image/jpeg",
                    new byte[0]
            );

            // when & then
            mockMvc.perform(multipart("/api/users/me/profile-image")
                            .file(file)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U008"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void uploadProfileImage_fail_unauthorized() throws Exception {
            // given
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "profile.jpg",
                    "image/jpeg",
                    "test image content".getBytes()
            );

            // when & then
            mockMvc.perform(multipart("/api/users/me/profile-image")
                            .file(file))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 사용자 목록 조회 테스트 (OPERATOR 권한) ====================

    @Nested
    @DisplayName("GET /api/users - 사용자 목록 조회")
    class GetUsers {

        @Test
        @DisplayName("성공 - OPERATOR가 사용자 목록 조회")
        void getUsers_success_operator() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 사용자 목록 조회")
        void getUsers_success_admin() throws Exception {
            // given
            createAdminUser();
            createTestUser();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("성공 - 키워드 검색")
        void getUsers_success_withKeyword() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("keyword", "홍길동"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("성공 - 역할 필터링")
        void getUsers_success_withRoleFilter() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("role", "USER"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 접근")
        void getUsers_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();

            // when & then
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 사용자 상세 조회 테스트 (OPERATOR 권한) ====================

    @Nested
    @DisplayName("GET /api/users/{userId} - 사용자 상세 조회")
    class GetUser {

        @Test
        @DisplayName("성공 - OPERATOR가 사용자 상세 조회")
        void getUser_success() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"))
                    .andExpect(jsonPath("$.data.name").value("홍길동"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void getUser_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/users/{userId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 접근")
        void getUser_fail_userRole() throws Exception {
            // given
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken();

            // when & then
            mockMvc.perform(get("/api/users/{userId}", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 역할 변경 테스트 (TENANT_ADMIN 권한) ====================

    @Nested
    @DisplayName("PUT /api/users/{userId}/role - 역할 변경")
    class ChangeUserRole {

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 역할 변경")
        void changeUserRole_success() throws Exception {
            // given
            createAdminUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            ChangeRoleRequest request = new ChangeRoleRequest(TenantRole.OPERATOR);

            // when & then
            mockMvc.perform(put("/api/users/{userId}/role", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("OPERATOR"));
        }

        @Test
        @DisplayName("실패 - OPERATOR 권한으로 역할 변경 시도")
        void changeUserRole_fail_operatorRole() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            ChangeRoleRequest request = new ChangeRoleRequest(TenantRole.OPERATOR);

            // when & then
            mockMvc.perform(put("/api/users/{userId}/role", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void changeUserRole_fail_notFound() throws Exception {
            // given
            createAdminUser();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            ChangeRoleRequest request = new ChangeRoleRequest(TenantRole.OPERATOR);

            // when & then
            mockMvc.perform(put("/api/users/{userId}/role", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("U001"));
        }
    }

    // ==================== 상태 변경 테스트 (OPERATOR 권한) ====================

    @Nested
    @DisplayName("PUT /api/users/{userId}/status - 상태 변경")
    class ChangeUserStatus {

        @Test
        @DisplayName("성공 - OPERATOR가 사용자 정지")
        void changeUserStatus_success_suspend() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            ChangeStatusRequest request = new ChangeStatusRequest(UserStatus.SUSPENDED, "정책 위반");

            // when & then
            mockMvc.perform(put("/api/users/{userId}/status", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("SUSPENDED"));

            // 정지된 사용자 로그인 시도 - 실패해야 함
            LoginRequest loginRequest = new LoginRequest("test@example.com", "Password123!");
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("성공 - 정지 사용자 활성화")
        void changeUserStatus_success_activate() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            targetUser.suspend();
            userRepository.save(targetUser);

            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            ChangeStatusRequest request = new ChangeStatusRequest(UserStatus.ACTIVE, null);

            // when & then
            mockMvc.perform(put("/api/users/{userId}/status", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 상태 변경 시도")
        void changeUserStatus_fail_userRole() throws Exception {
            // given
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken();
            ChangeStatusRequest request = new ChangeStatusRequest(UserStatus.SUSPENDED, "정책 위반");

            // when & then
            mockMvc.perform(put("/api/users/{userId}/status", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== CourseRole API 테스트 ====================

    @Nested
    @DisplayName("POST /api/users/me/course-roles/designer - DESIGNER 역할 요청")
    class RequestDesignerRole {

        @Test
        @DisplayName("성공 - DESIGNER 역할 요청")
        void requestDesignerRole_success() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();

            // when & then
            mockMvc.perform(post("/api/users/me/course-roles/designer")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("DESIGNER"))
                    .andExpect(jsonPath("$.data.courseId").isEmpty());
        }

        @Test
        @DisplayName("실패 - 이미 DESIGNER 역할 보유")
        void requestDesignerRole_fail_alreadyExists() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();

            // 첫 번째 요청 - 성공
            mockMvc.perform(post("/api/users/me/course-roles/designer")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated());

            // when & then - 두 번째 요청 - 실패
            mockMvc.perform(post("/api/users/me/course-roles/designer")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("U006"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void requestDesignerRole_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post("/api/users/me/course-roles/designer"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/users/me/course-roles - 내 강의 역할 목록 조회")
    class GetMyCourseRoles {

        @Test
        @DisplayName("성공 - 역할이 없는 경우 빈 배열 반환")
        void getMyCourseRoles_success_empty() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();

            // when & then
            mockMvc.perform(get("/api/users/me/course-roles")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("성공 - DESIGNER 역할 조회")
        void getMyCourseRoles_success_withDesigner() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken();

            // DESIGNER 역할 요청
            mockMvc.perform(post("/api/users/me/course-roles/designer")
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isCreated());

            // when & then
            mockMvc.perform(get("/api/users/me/course-roles")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].role").value("DESIGNER"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 접근")
        void getMyCourseRoles_fail_unauthorized() throws Exception {
            // when & then
            mockMvc.perform(get("/api/users/me/course-roles"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== CourseRole 관리 API 테스트 (OPERATOR 권한) ====================

    @Nested
    @DisplayName("POST /api/users/{userId}/course-roles - 역할 부여")
    class AssignCourseRole {

        @Test
        @DisplayName("성공 - OPERATOR가 DESIGNER 역할 부여")
        void assignCourseRole_success_designer() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            AssignCourseRoleRequest request = new AssignCourseRoleRequest(
                    null,
                    com.mzc.lp.domain.user.constant.CourseRole.DESIGNER,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("DESIGNER"));
        }

        @Test
        @DisplayName("성공 - OPERATOR가 강의별 INSTRUCTOR 역할 부여")
        void assignCourseRole_success_instructor() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            AssignCourseRoleRequest request = new AssignCourseRoleRequest(
                    100L,  // 가상의 courseId
                    com.mzc.lp.domain.user.constant.CourseRole.INSTRUCTOR,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("INSTRUCTOR"))
                    .andExpect(jsonPath("$.data.courseId").value(100));
        }

        @Test
        @DisplayName("성공 - OPERATOR가 OWNER 역할 부여 (수익 분배율 포함)")
        void assignCourseRole_success_owner_withRevenue() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            AssignCourseRoleRequest request = new AssignCourseRoleRequest(
                    200L,
                    com.mzc.lp.domain.user.constant.CourseRole.OWNER,
                    70
            );

            // when & then
            mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.role").value("OWNER"))
                    .andExpect(jsonPath("$.data.courseId").value(200))
                    .andExpect(jsonPath("$.data.revenueSharePercent").value(70));
        }

        @Test
        @DisplayName("실패 - 중복 역할 부여 시도")
        void assignCourseRole_fail_duplicate() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            AssignCourseRoleRequest request = new AssignCourseRoleRequest(
                    null,
                    com.mzc.lp.domain.user.constant.CourseRole.DESIGNER,
                    null
            );

            // 첫 번째 요청 - 성공
            mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // when & then - 두 번째 요청 - 실패
            mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("U006"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 역할 부여 시도")
        void assignCourseRole_fail_userRole() throws Exception {
            // given
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken();
            AssignCourseRoleRequest request = new AssignCourseRoleRequest(
                    null,
                    com.mzc.lp.domain.user.constant.CourseRole.DESIGNER,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void assignCourseRole_fail_userNotFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            AssignCourseRoleRequest request = new AssignCourseRoleRequest(
                    null,
                    com.mzc.lp.domain.user.constant.CourseRole.DESIGNER,
                    null
            );

            // when & then
            mockMvc.perform(post("/api/users/{userId}/course-roles", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("U001"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{userId}/course-roles/{courseRoleId} - 역할 회수")
    class RevokeCourseRole {

        @Test
        @DisplayName("성공 - OPERATOR가 역할 회수")
        void revokeCourseRole_success() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // 역할 부여
            AssignCourseRoleRequest assignRequest = new AssignCourseRoleRequest(
                    null,
                    com.mzc.lp.domain.user.constant.CourseRole.DESIGNER,
                    null
            );
            MvcResult assignResult = mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(assignRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long courseRoleId = objectMapper.readTree(assignResult.getResponse().getContentAsString())
                    .get("data").get("courseRoleId").asLong();

            // when & then - 역할 회수
            mockMvc.perform(delete("/api/users/{userId}/course-roles/{courseRoleId}", targetUser.getId(), courseRoleId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 역할이 삭제되었는지 확인
            String userAccessToken = loginAndGetAccessToken();
            mockMvc.perform(get("/api/users/me/course-roles")
                            .header("Authorization", "Bearer " + userAccessToken))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 역할 회수 시도")
        void revokeCourseRole_fail_userRole() throws Exception {
            // given
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken();

            // when & then
            mockMvc.perform(delete("/api/users/{userId}/course-roles/{courseRoleId}", targetUser.getId(), 1L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 역할 회수 시도")
        void revokeCourseRole_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            createTestUser();
            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/users/{userId}/course-roles/{courseRoleId}", targetUser.getId(), 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("U007"));
        }

        @Test
        @DisplayName("실패 - 다른 사용자의 역할 회수 시도")
        void revokeCourseRole_fail_wrongUser() throws Exception {
            // given
            createOperatorUser();
            createTestUser();

            // 다른 사용자 생성
            User anotherUser = User.create("another@example.com", "다른사용자", passwordEncoder.encode("Password123!"));
            userRepository.save(anotherUser);

            User targetUser = userRepository.findByEmail("test@example.com").orElseThrow();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // targetUser에게 역할 부여
            AssignCourseRoleRequest assignRequest = new AssignCourseRoleRequest(
                    null,
                    com.mzc.lp.domain.user.constant.CourseRole.DESIGNER,
                    null
            );
            MvcResult assignResult = mockMvc.perform(post("/api/users/{userId}/course-roles", targetUser.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(assignRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Long courseRoleId = objectMapper.readTree(assignResult.getResponse().getContentAsString())
                    .get("data").get("courseRoleId").asLong();

            // when & then - anotherUser의 ID로 targetUser의 역할 회수 시도
            mockMvc.perform(delete("/api/users/{userId}/course-roles/{courseRoleId}", anotherUser.getId(), courseRoleId)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("U007"));
        }
    }
}
