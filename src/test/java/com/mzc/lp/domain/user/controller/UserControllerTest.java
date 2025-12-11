package com.mzc.lp.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.domain.user.dto.request.*;
import com.mzc.lp.domain.user.repository.RefreshTokenRepository;
import com.mzc.lp.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
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
                    "https://cdn.example.com/profile.jpg"
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
            UpdateProfileRequest request = new UpdateProfileRequest("김철수", null, null);

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
            UpdateProfileRequest request = new UpdateProfileRequest("김철수", null, null);

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
}
