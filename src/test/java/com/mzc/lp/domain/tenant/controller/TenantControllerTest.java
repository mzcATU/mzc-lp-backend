package com.mzc.lp.domain.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.tenant.constant.PlanType;
import com.mzc.lp.domain.tenant.constant.TenantStatus;
import com.mzc.lp.domain.tenant.constant.TenantType;
import com.mzc.lp.domain.tenant.dto.request.CreateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantRequest;
import com.mzc.lp.domain.tenant.dto.request.UpdateTenantStatusRequest;
import com.mzc.lp.domain.tenant.entity.Tenant;
import com.mzc.lp.domain.tenant.repository.TenantRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.entity.User;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TenantControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        tenantRepository.deleteAll();
    }

    // ===== 헬퍼 메서드 =====

    private User createSystemAdmin() {
        User user = User.create("sysadmin@example.com", "시스템관리자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.SYSTEM_ADMIN);
        return userRepository.save(user);
    }

    private User createTenantAdmin() {
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

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("data").get("accessToken").asText();
    }

    private Tenant createTestTenant(String code, String name, String subdomain) {
        Tenant tenant = Tenant.create(code, name, TenantType.B2B, subdomain, PlanType.BASIC);
        return tenantRepository.save(tenant);
    }

    @Nested
    @DisplayName("POST /api/tenants - 테넌트 생성")
    class CreateTenant {

        @Test
        @DisplayName("시스템 관리자가 테넌트를 생성한다")
        void createTenant_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");

            CreateTenantRequest request = new CreateTenantRequest(
                    "NEWCOMPANY",
                    "새회사",
                    TenantType.B2B,
                    "newcompany",
                    PlanType.PRO,
                    "learn.newcompany.com",
                    "admin@newcompany.com",
                    "새회사관리자"
            );

            // when & then
            mockMvc.perform(post("/api/tenants")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("NEWCOMPANY"))
                    .andExpect(jsonPath("$.data.name").value("새회사"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("테넌트 관리자는 테넌트 생성 권한이 없다")
        void createTenant_forbidden() throws Exception {
            // given
            createTenantAdmin();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");

            CreateTenantRequest request = new CreateTenantRequest(
                    "NEWCOMPANY",
                    "새회사",
                    TenantType.B2B,
                    "newcompany",
                    PlanType.PRO,
                    null,
                    "admin@newcompany.com",
                    "새회사관리자"
            );

            // when & then
            mockMvc.perform(post("/api/tenants")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("중복된 코드로 생성 시 실패한다")
        void createTenant_duplicateCode() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            createTestTenant("DUPLICATE", "기존회사", "existing");

            CreateTenantRequest request = new CreateTenantRequest(
                    "DUPLICATE",
                    "새회사",
                    TenantType.B2B,
                    "newcompany",
                    PlanType.PRO,
                    null,
                    "admin@duplicate.com",
                    "중복회사관리자"
            );

            // when & then
            mockMvc.perform(post("/api/tenants")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("GET /api/tenants - 테넌트 목록 조회")
    class GetTenants {

        @Test
        @DisplayName("시스템 관리자가 테넌트 목록을 조회한다")
        void getTenants_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            createTestTenant("COMPANY_A", "회사A", "companya");
            createTestTenant("COMPANY_B", "회사B", "companyb");

            // when & then
            mockMvc.perform(get("/api/tenants")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("키워드로 테넌트를 검색한다")
        void getTenants_withKeyword() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            createTestTenant("SAMSUNG", "삼성전자", "samsung");
            createTestTenant("LG", "LG전자", "lg");

            // when & then
            mockMvc.perform(get("/api/tenants")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("keyword", "삼성"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/tenants/{tenantId} - 테넌트 상세 조회")
    class GetTenant {

        @Test
        @DisplayName("시스템 관리자가 테넌트 상세를 조회한다")
        void getTenant_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            Tenant tenant = createTestTenant("TESTCO", "테스트회사", "testco");

            // when & then
            mockMvc.perform(get("/api/tenants/{tenantId}", tenant.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("TESTCO"))
                    .andExpect(jsonPath("$.data.name").value("테스트회사"));
        }

        @Test
        @DisplayName("존재하지 않는 테넌트 조회 시 404를 반환한다")
        void getTenant_notFound() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/tenants/{tenantId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/tenants/{tenantId} - 테넌트 수정")
    class UpdateTenant {

        @Test
        @DisplayName("시스템 관리자가 테넌트를 수정한다")
        void updateTenant_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            Tenant tenant = createTestTenant("TESTCO", "테스트회사", "testco");

            UpdateTenantRequest request = new UpdateTenantRequest(
                    "수정된회사명",
                    "new.domain.com",
                    PlanType.ENTERPRISE,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/tenants/{tenantId}", tenant.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("수정된회사명"))
                    .andExpect(jsonPath("$.data.customDomain").value("new.domain.com"))
                    .andExpect(jsonPath("$.data.plan").value("ENTERPRISE"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/tenants/{tenantId}/status - 테넌트 상태 변경")
    class UpdateTenantStatus {

        @Test
        @DisplayName("시스템 관리자가 테넌트 상태를 변경한다")
        void updateTenantStatus_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            Tenant tenant = createTestTenant("TESTCO", "테스트회사", "testco");

            UpdateTenantStatusRequest request = new UpdateTenantStatusRequest(TenantStatus.ACTIVE);

            // when & then
            mockMvc.perform(patch("/api/tenants/{tenantId}/status", tenant.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/tenants/{tenantId} - 테넌트 삭제")
    class DeleteTenant {

        @Test
        @DisplayName("시스템 관리자가 테넌트를 삭제한다")
        void deleteTenant_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            Tenant tenant = createTestTenant("DELETEME", "삭제할회사", "deleteme");

            // when & then
            mockMvc.perform(delete("/api/tenants/{tenantId}", tenant.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /api/tenants/code/{code} - 코드로 테넌트 조회")
    class GetTenantByCode {

        @Test
        @DisplayName("시스템 관리자가 코드로 테넌트를 조회한다")
        void getTenantByCode_success() throws Exception {
            // given
            createSystemAdmin();
            String accessToken = loginAndGetAccessToken("sysadmin@example.com", "Password123!");
            createTestTenant("BYCODE", "코드조회회사", "bycode");

            // when & then
            mockMvc.perform(get("/api/tenants/code/{code}", "BYCODE")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("BYCODE"))
                    .andExpect(jsonPath("$.data.name").value("코드조회회사"));
        }
    }
}
