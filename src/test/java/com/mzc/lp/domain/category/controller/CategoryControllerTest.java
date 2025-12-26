package com.mzc.lp.domain.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.category.dto.request.CreateCategoryRequest;
import com.mzc.lp.domain.category.dto.request.UpdateCategoryRequest;
import com.mzc.lp.domain.category.entity.Category;
import com.mzc.lp.domain.category.repository.CategoryRepository;
import com.mzc.lp.domain.user.constant.TenantRole;
import com.mzc.lp.domain.user.dto.request.LoginRequest;
import com.mzc.lp.domain.user.dto.request.RegisterRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest extends TenantTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ===== 헬퍼 메서드 =====

    private User createOperatorUser() {
        User user = User.create("operator@example.com", "운영자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.OPERATOR);
        return userRepository.save(user);
    }

    private User createAdminUser() {
        User user = User.create("admin@example.com", "관리자", passwordEncoder.encode("Password123!"));
        user.updateRole(TenantRole.TENANT_ADMIN);
        return userRepository.save(user);
    }

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

    private Category createTestCategory(String name, String code) {
        Category category = Category.create(name, code);
        return categoryRepository.save(category);
    }

    private Category createTestCategory(String name, String code, int sortOrder) {
        Category category = Category.create(name, code, sortOrder);
        return categoryRepository.save(category);
    }

    // ==================== 카테고리 생성 테스트 ====================

    @Nested
    @DisplayName("POST /api/categories - 카테고리 생성")
    class CreateCategory {

        @Test
        @DisplayName("성공 - OPERATOR가 카테고리 생성")
        void createCategory_success_operator() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "프로그래밍",
                    "PROGRAMMING",
                    1
            );

            // when & then
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("프로그래밍"))
                    .andExpect(jsonPath("$.data.code").value("PROGRAMMING"))
                    .andExpect(jsonPath("$.data.sortOrder").value(1))
                    .andExpect(jsonPath("$.data.active").value(true));
        }

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 카테고리 생성")
        void createCategory_success_admin() throws Exception {
            // given
            createAdminUser();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "데이터베이스",
                    "DATABASE",
                    null
            );

            // when & then
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("데이터베이스"))
                    .andExpect(jsonPath("$.data.code").value("DATABASE"));
        }

        @Test
        @DisplayName("실패 - 이름 누락")
        void createCategory_fail_missingName() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCategoryRequest request = new CreateCategoryRequest(
                    null,
                    "CODE",
                    1
            );

            // when & then
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 코드 누락")
        void createCategory_fail_missingCode() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "카테고리명",
                    null,
                    1
            );

            // when & then
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("실패 - 중복 코드")
        void createCategory_fail_duplicateCode() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestCategory("기존 카테고리", "DUPLICATE");

            CreateCategoryRequest request = new CreateCategoryRequest(
                    "새 카테고리",
                    "DUPLICATE",
                    1
            );

            // when & then
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CAT002"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 생성 시도")
        void createCategory_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            CreateCategoryRequest request = new CreateCategoryRequest(
                    "테스트",
                    "TEST",
                    1
            );

            // when & then
            mockMvc.perform(post("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 카테고리 목록 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/categories - 카테고리 목록 조회")
    class GetCategories {

        @Test
        @DisplayName("성공 - 전체 카테고리 목록 조회")
        void getCategories_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestCategory("프로그래밍", "PROGRAMMING", 1);
            createTestCategory("데이터베이스", "DATABASE", 2);
            createTestCategory("인프라", "INFRA", 3);

            // when & then
            mockMvc.perform(get("/api/categories")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("성공 - 정렬순 확인")
        void getCategories_success_sorted() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestCategory("세번째", "THIRD", 3);
            createTestCategory("첫번째", "FIRST", 1);
            createTestCategory("두번째", "SECOND", 2);

            // when & then
            mockMvc.perform(get("/api/categories")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].code").value("FIRST"))
                    .andExpect(jsonPath("$.data[1].code").value("SECOND"))
                    .andExpect(jsonPath("$.data[2].code").value("THIRD"));
        }

        @Test
        @DisplayName("성공 - 활성 카테고리만 조회")
        void getCategories_success_activeOnly() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Category active = createTestCategory("활성", "ACTIVE", 1);
            Category inactive = createTestCategory("비활성", "INACTIVE", 2);
            inactive.deactivate();
            categoryRepository.save(inactive);

            // when & then
            mockMvc.perform(get("/api/categories")
                            .header("Authorization", "Bearer " + accessToken)
                            .param("activeOnly", "true"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].code").value("ACTIVE"));
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void getCategories_success_empty() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/categories")
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    // ==================== 카테고리 상세 조회 테스트 ====================

    @Nested
    @DisplayName("GET /api/categories/{categoryId} - 카테고리 상세 조회")
    class GetCategory {

        @Test
        @DisplayName("성공 - 카테고리 상세 조회")
        void getCategory_success() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Category category = createTestCategory("프로그래밍", "PROGRAMMING");

            // when & then
            mockMvc.perform(get("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(category.getId()))
                    .andExpect(jsonPath("$.data.name").value("프로그래밍"))
                    .andExpect(jsonPath("$.data.code").value("PROGRAMMING"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리")
        void getCategory_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(get("/api/categories/{categoryId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CAT001"));
        }
    }

    // ==================== 카테고리 수정 테스트 ====================

    @Nested
    @DisplayName("PUT /api/categories/{categoryId} - 카테고리 수정")
    class UpdateCategory {

        @Test
        @DisplayName("성공 - OPERATOR가 카테고리 수정")
        void updateCategory_success_operator() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Category category = createTestCategory("원래 이름", "ORIGINAL");
            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "수정된 이름",
                    "UPDATED",
                    10,
                    false
            );

            // when & then
            mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("수정된 이름"))
                    .andExpect(jsonPath("$.data.code").value("UPDATED"))
                    .andExpect(jsonPath("$.data.sortOrder").value(10))
                    .andExpect(jsonPath("$.data.active").value(false));
        }

        @Test
        @DisplayName("성공 - 부분 수정")
        void updateCategory_success_partial() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Category category = createTestCategory("원래 이름", "ORIGINAL");
            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "수정된 이름만",
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value("수정된 이름만"))
                    .andExpect(jsonPath("$.data.code").value("ORIGINAL"));
        }

        @Test
        @DisplayName("실패 - 중복 코드로 수정")
        void updateCategory_fail_duplicateCode() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            createTestCategory("기존 카테고리", "EXISTING");
            Category target = createTestCategory("수정 대상", "TARGET");

            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    null,
                    "EXISTING",
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/categories/{categoryId}", target.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CAT002"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리")
        void updateCategory_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "수정 시도",
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/categories/{categoryId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CAT001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 수정 시도")
        void updateCategory_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            Category category = createTestCategory("테스트", "TEST");
            UpdateCategoryRequest request = new UpdateCategoryRequest(
                    "수정 시도",
                    null,
                    null,
                    null
            );

            // when & then
            mockMvc.perform(put("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== 카테고리 삭제 테스트 ====================

    @Nested
    @DisplayName("DELETE /api/categories/{categoryId} - 카테고리 삭제")
    class DeleteCategory {

        @Test
        @DisplayName("성공 - OPERATOR가 카테고리 삭제")
        void deleteCategory_success_operator() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");
            Category category = createTestCategory("삭제할 카테고리", "DELETE");

            // when & then
            mockMvc.perform(delete("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // 삭제 확인
            mockMvc.perform(get("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("성공 - TENANT_ADMIN이 카테고리 삭제")
        void deleteCategory_success_admin() throws Exception {
            // given
            createAdminUser();
            String accessToken = loginAndGetAccessToken("admin@example.com", "Password123!");
            Category category = createTestCategory("삭제할 카테고리", "DELETE");

            // when & then
            mockMvc.perform(delete("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 카테고리")
        void deleteCategory_fail_notFound() throws Exception {
            // given
            createOperatorUser();
            String accessToken = loginAndGetAccessToken("operator@example.com", "Password123!");

            // when & then
            mockMvc.perform(delete("/api/categories/{categoryId}", 99999L)
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("CAT001"));
        }

        @Test
        @DisplayName("실패 - USER 권한으로 삭제 시도")
        void deleteCategory_fail_userRole() throws Exception {
            // given
            createTestUser();
            String accessToken = loginAndGetAccessToken("test@example.com", "Password123!");
            Category category = createTestCategory("삭제할 카테고리", "DELETE");

            // when & then
            mockMvc.perform(delete("/api/categories/{categoryId}", category.getId())
                            .header("Authorization", "Bearer " + accessToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}
