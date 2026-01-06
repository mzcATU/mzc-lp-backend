package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateCreateRequest;
import com.mzc.lp.domain.certificate.dto.request.CertificateTemplateUpdateRequest;
import com.mzc.lp.domain.certificate.dto.response.CertificateTemplateResponse;
import com.mzc.lp.domain.certificate.entity.CertificateTemplate;
import com.mzc.lp.domain.certificate.exception.CertificateTemplateCodeDuplicateException;
import com.mzc.lp.domain.certificate.exception.CertificateTemplateNotFoundException;
import com.mzc.lp.domain.certificate.repository.CertificateTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateTemplateServiceTest extends TenantTestSupport {

    @InjectMocks
    private CertificateTemplateServiceImpl certificateTemplateService;

    @Mock
    private CertificateTemplateRepository certificateTemplateRepository;

    private static final Long TENANT_ID = 1L;

    private CertificateTemplate createTestTemplate(String templateCode, String title) {
        CertificateTemplate template = CertificateTemplate.create(
                templateCode,
                title,
                "테스트 설명",
                1L
        );

        // tenantId, id 설정
        try {
            var tenantIdField = com.mzc.lp.common.entity.TenantEntity.class.getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(template, TENANT_ID);

            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(template, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return template;
    }

    // ==================== 템플릿 생성 테스트 ====================

    @Nested
    @DisplayName("createTemplate - 템플릿 생성")
    class CreateTemplate {

        @Test
        @DisplayName("성공 - 템플릿 생성")
        void createTemplate_success() {
            // given
            CertificateTemplateCreateRequest request = new CertificateTemplateCreateRequest(
                    "TEMPLATE-001",
                    "기본 수료증 템플릿",
                    "기본 템플릿 설명",
                    "https://example.com/bg.png",
                    "https://example.com/sign.png",
                    "<html>Certificate Body</html>",
                    12,
                    false
            );

            given(certificateTemplateRepository.existsByTemplateCodeAndTenantId("TEMPLATE-001", TENANT_ID))
                    .willReturn(false);
            given(certificateTemplateRepository.save(any(CertificateTemplate.class)))
                    .willAnswer(invocation -> {
                        CertificateTemplate template = invocation.getArgument(0);
                        try {
                            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
                            idField.setAccessible(true);
                            idField.set(template, 1L);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return template;
                    });

            // when
            CertificateTemplateResponse response = certificateTemplateService.createTemplate(request, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.templateCode()).isEqualTo("TEMPLATE-001");
            assertThat(response.title()).isEqualTo("기본 수료증 템플릿");
            assertThat(response.validityMonths()).isEqualTo(12);

            verify(certificateTemplateRepository).save(any(CertificateTemplate.class));
        }

        @Test
        @DisplayName("실패 - 중복 템플릿 코드")
        void createTemplate_fail_duplicateCode() {
            // given
            CertificateTemplateCreateRequest request = new CertificateTemplateCreateRequest(
                    "TEMPLATE-001",
                    "기본 수료증 템플릿",
                    null, null, null, null, null, false
            );

            given(certificateTemplateRepository.existsByTemplateCodeAndTenantId("TEMPLATE-001", TENANT_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> certificateTemplateService.createTemplate(request, 1L))
                    .isInstanceOf(CertificateTemplateCodeDuplicateException.class);

            verify(certificateTemplateRepository, never()).save(any());
        }

        @Test
        @DisplayName("성공 - 기본 템플릿으로 생성")
        void createTemplate_success_asDefault() {
            // given
            CertificateTemplateCreateRequest request = new CertificateTemplateCreateRequest(
                    "TEMPLATE-DEFAULT",
                    "기본 템플릿",
                    null, null, null, null, null,
                    true
            );

            CertificateTemplate existingDefault = createTestTemplate("TEMPLATE-OLD", "이전 기본 템플릿");

            given(certificateTemplateRepository.existsByTemplateCodeAndTenantId("TEMPLATE-DEFAULT", TENANT_ID))
                    .willReturn(false);
            given(certificateTemplateRepository.findByTenantIdAndIsDefaultTrue(TENANT_ID))
                    .willReturn(Optional.of(existingDefault));
            given(certificateTemplateRepository.save(any(CertificateTemplate.class)))
                    .willAnswer(invocation -> {
                        CertificateTemplate template = invocation.getArgument(0);
                        try {
                            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
                            idField.setAccessible(true);
                            idField.set(template, 2L);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return template;
                    });

            // when
            CertificateTemplateResponse response = certificateTemplateService.createTemplate(request, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.isDefault()).isTrue();
            assertThat(existingDefault.isDefault()).isFalse(); // 기존 기본 템플릿 해제 확인
        }
    }

    // ==================== 템플릿 목록 조회 테스트 ====================

    @Nested
    @DisplayName("getTemplates - 템플릿 목록 조회")
    class GetTemplates {

        @Test
        @DisplayName("성공 - 템플릿 목록 조회")
        void getTemplates_success() {
            // given
            Pageable pageable = PageRequest.of(0, 20);
            List<CertificateTemplate> templates = List.of(
                    createTestTemplate("TEMPLATE-001", "템플릿 1"),
                    createTestTemplate("TEMPLATE-002", "템플릿 2")
            );
            Page<CertificateTemplate> page = new PageImpl<>(templates, pageable, templates.size());

            given(certificateTemplateRepository.findByTenantIdOrderByCreatedAtDesc(TENANT_ID, pageable))
                    .willReturn(page);

            // when
            Page<CertificateTemplateResponse> response = certificateTemplateService.getTemplates(pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
        }
    }

    // ==================== 활성 템플릿 목록 조회 테스트 ====================

    @Nested
    @DisplayName("getActiveTemplates - 활성 템플릿 목록 조회")
    class GetActiveTemplates {

        @Test
        @DisplayName("성공 - 활성 템플릿 목록 조회")
        void getActiveTemplates_success() {
            // given
            List<CertificateTemplate> templates = List.of(
                    createTestTemplate("TEMPLATE-001", "활성 템플릿 1"),
                    createTestTemplate("TEMPLATE-002", "활성 템플릿 2")
            );

            given(certificateTemplateRepository.findByTenantIdAndIsActiveTrue(TENANT_ID))
                    .willReturn(templates);

            // when
            List<CertificateTemplateResponse> response = certificateTemplateService.getActiveTemplates();

            // then
            assertThat(response).hasSize(2);
        }
    }

    // ==================== 템플릿 상세 조회 테스트 ====================

    @Nested
    @DisplayName("getTemplate - 템플릿 상세 조회")
    class GetTemplate {

        @Test
        @DisplayName("성공 - 템플릿 상세 조회")
        void getTemplate_success() {
            // given
            Long templateId = 1L;
            CertificateTemplate template = createTestTemplate("TEMPLATE-001", "테스트 템플릿");

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.of(template));

            // when
            CertificateTemplateResponse response = certificateTemplateService.getTemplate(templateId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.templateCode()).isEqualTo("TEMPLATE-001");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 템플릿")
        void getTemplate_fail_notFound() {
            // given
            Long templateId = 999L;

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> certificateTemplateService.getTemplate(templateId))
                    .isInstanceOf(CertificateTemplateNotFoundException.class);
        }
    }

    // ==================== 템플릿 수정 테스트 ====================

    @Nested
    @DisplayName("updateTemplate - 템플릿 수정")
    class UpdateTemplate {

        @Test
        @DisplayName("성공 - 템플릿 수정")
        void updateTemplate_success() {
            // given
            Long templateId = 1L;
            CertificateTemplate template = createTestTemplate("TEMPLATE-001", "원래 제목");
            CertificateTemplateUpdateRequest request = new CertificateTemplateUpdateRequest(
                    "수정된 제목",
                    "수정된 설명",
                    "https://example.com/new-bg.png",
                    "https://example.com/new-sign.png",
                    "<html>Updated Body</html>",
                    24
            );

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.of(template));

            // when
            CertificateTemplateResponse response = certificateTemplateService.updateTemplate(templateId, request);

            // then
            assertThat(response.title()).isEqualTo("수정된 제목");
            assertThat(response.validityMonths()).isEqualTo(24);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 템플릿 수정")
        void updateTemplate_fail_notFound() {
            // given
            Long templateId = 999L;
            CertificateTemplateUpdateRequest request = new CertificateTemplateUpdateRequest(
                    "수정된 제목", null, null, null, null, null
            );

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> certificateTemplateService.updateTemplate(templateId, request))
                    .isInstanceOf(CertificateTemplateNotFoundException.class);
        }
    }

    // ==================== 기본 템플릿 설정 테스트 ====================

    @Nested
    @DisplayName("setAsDefault - 기본 템플릿 설정")
    class SetAsDefault {

        @Test
        @DisplayName("성공 - 기본 템플릿 설정")
        void setAsDefault_success() {
            // given
            Long templateId = 2L;
            CertificateTemplate newDefault = createTestTemplate("TEMPLATE-NEW", "새 기본 템플릿");
            CertificateTemplate oldDefault = createTestTemplate("TEMPLATE-OLD", "이전 기본 템플릿");
            oldDefault.setAsDefault();

            // newDefault의 id를 2로 설정
            try {
                var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(newDefault, 2L);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.of(newDefault));
            given(certificateTemplateRepository.findByTenantIdAndIsDefaultTrue(TENANT_ID))
                    .willReturn(Optional.of(oldDefault));

            // when
            certificateTemplateService.setAsDefault(templateId);

            // then
            assertThat(newDefault.isDefault()).isTrue();
            assertThat(oldDefault.isDefault()).isFalse();
        }
    }

    // ==================== 템플릿 활성화/비활성화 테스트 ====================

    @Nested
    @DisplayName("activateTemplate / deactivateTemplate")
    class ActivateDeactivate {

        @Test
        @DisplayName("성공 - 템플릿 활성화")
        void activateTemplate_success() {
            // given
            Long templateId = 1L;
            CertificateTemplate template = createTestTemplate("TEMPLATE-001", "테스트");
            template.deactivate();

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.of(template));

            // when
            certificateTemplateService.activateTemplate(templateId);

            // then
            assertThat(template.isActive()).isTrue();
        }

        @Test
        @DisplayName("성공 - 템플릿 비활성화")
        void deactivateTemplate_success() {
            // given
            Long templateId = 1L;
            CertificateTemplate template = createTestTemplate("TEMPLATE-001", "테스트");

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.of(template));

            // when
            certificateTemplateService.deactivateTemplate(templateId);

            // then
            assertThat(template.isActive()).isFalse();
        }
    }

    // ==================== 템플릿 삭제 테스트 ====================

    @Nested
    @DisplayName("deleteTemplate - 템플릿 삭제")
    class DeleteTemplate {

        @Test
        @DisplayName("성공 - 템플릿 삭제")
        void deleteTemplate_success() {
            // given
            Long templateId = 1L;
            CertificateTemplate template = createTestTemplate("TEMPLATE-001", "테스트");

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.of(template));

            // when
            certificateTemplateService.deleteTemplate(templateId);

            // then
            verify(certificateTemplateRepository).delete(template);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 템플릿 삭제")
        void deleteTemplate_fail_notFound() {
            // given
            Long templateId = 999L;

            given(certificateTemplateRepository.findByIdAndTenantId(templateId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> certificateTemplateService.deleteTemplate(templateId))
                    .isInstanceOf(CertificateTemplateNotFoundException.class);

            verify(certificateTemplateRepository, never()).delete(any());
        }
    }
}
