package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.common.support.TenantTestSupport;
import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import com.mzc.lp.domain.certificate.entity.Certificate;
import com.mzc.lp.domain.certificate.exception.CertificateAlreadyIssuedException;
import com.mzc.lp.domain.certificate.exception.CertificateNotFoundException;
import com.mzc.lp.domain.certificate.repository.CertificateRepository;
import com.mzc.lp.domain.program.entity.Program;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.exception.UnauthorizedEnrollmentAccessException;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.repository.UserRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest extends TenantTestSupport {

    @InjectMocks
    private CertificateServiceImpl certificateService;

    @Mock
    private CertificateRepository certificateRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseTimeRepository courseTimeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CertificateNumberGenerator certificateNumberGenerator;

    @Mock
    private CertificatePdfService certificatePdfService;

    private static final Long TENANT_ID = 1L;

    private Certificate createTestCertificate(Long userId, String certificateNumber) {
        Certificate certificate = Certificate.create(
                certificateNumber,
                userId,
                "테스트 사용자",
                1L,
                1L,
                "테스트 차수",
                1L,
                "테스트 프로그램",
                Instant.now()
        );

        // tenantId 설정
        try {
            var tenantIdField = com.mzc.lp.common.entity.TenantEntity.class.getDeclaredField("tenantId");
            tenantIdField.setAccessible(true);
            tenantIdField.set(certificate, TENANT_ID);

            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(certificate, 1L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return certificate;
    }

    // ==================== 수료증 발급 테스트 ====================

    @Nested
    @DisplayName("issueCertificate - 수료증 발급")
    class IssueCertificate {

        @Test
        @DisplayName("성공 - 수료증 자동 발급")
        void issueCertificate_success() {
            // given
            Long enrollmentId = 1L;
            Long userId = 1L;
            Long courseTimeId = 1L;

            Enrollment enrollment = mock(Enrollment.class);
            given(enrollment.getUserId()).willReturn(userId);
            given(enrollment.getCourseTimeId()).willReturn(courseTimeId);
            given(enrollment.getCompletedAt()).willReturn(Instant.now());

            CourseTime courseTime = mock(CourseTime.class);
            given(courseTime.getId()).willReturn(courseTimeId);
            given(courseTime.getTitle()).willReturn("테스트 차수");
            Program program = mock(Program.class);
            given(program.getId()).willReturn(1L);
            given(program.getTitle()).willReturn("테스트 프로그램");
            given(courseTime.getProgram()).willReturn(program);

            User user = mock(User.class);
            given(user.getName()).willReturn("테스트 사용자");

            given(certificateRepository.existsByEnrollmentIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(false);
            given(enrollmentRepository.findByIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(Optional.of(enrollment));
            given(courseTimeRepository.findByIdAndTenantId(courseTimeId, TENANT_ID))
                    .willReturn(Optional.of(courseTime));
            given(userRepository.findById(userId))
                    .willReturn(Optional.of(user));
            given(certificateNumberGenerator.generate(TENANT_ID))
                    .willReturn("CERT-001-2026-000001");
            given(certificateRepository.save(any(Certificate.class)))
                    .willAnswer(invocation -> {
                        Certificate cert = invocation.getArgument(0);
                        try {
                            var idField = com.mzc.lp.common.entity.BaseEntity.class.getDeclaredField("id");
                            idField.setAccessible(true);
                            idField.set(cert, 1L);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        return cert;
                    });

            // when
            CertificateDetailResponse response = certificateService.issueCertificate(enrollmentId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.certificateNumber()).isEqualTo("CERT-001-2026-000001");
            assertThat(response.userName()).isEqualTo("테스트 사용자");
            assertThat(response.status()).isEqualTo(CertificateStatus.ISSUED);

            verify(certificateRepository).save(any(Certificate.class));
        }

        @Test
        @DisplayName("실패 - 이미 발급된 수료증")
        void issueCertificate_fail_alreadyIssued() {
            // given
            Long enrollmentId = 1L;

            given(certificateRepository.existsByEnrollmentIdAndTenantId(enrollmentId, TENANT_ID))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> certificateService.issueCertificate(enrollmentId))
                    .isInstanceOf(CertificateAlreadyIssuedException.class);

            verify(certificateRepository, never()).save(any());
        }
    }

    // ==================== 내 수료증 목록 조회 테스트 ====================

    @Nested
    @DisplayName("getMyCertificates - 내 수료증 목록 조회")
    class GetMyCertificates {

        @Test
        @DisplayName("성공 - 내 수료증 목록 조회")
        void getMyCertificates_success() {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 20);
            List<Certificate> certificates = List.of(
                    createTestCertificate(userId, "CERT-001-2026-000001"),
                    createTestCertificate(userId, "CERT-001-2026-000002")
            );
            Page<Certificate> page = new PageImpl<>(certificates, pageable, certificates.size());

            given(certificateRepository.findByUserIdAndTenantIdOrderByIssuedAtDesc(userId, TENANT_ID, pageable))
                    .willReturn(page);

            // when
            Page<CertificateResponse> response = certificateService.getMyCertificates(userId, pageable);

            // then
            assertThat(response.getContent()).hasSize(2);
        }
    }

    // ==================== 수료증 상세 조회 테스트 ====================

    @Nested
    @DisplayName("getCertificate - 수료증 상세 조회")
    class GetCertificate {

        @Test
        @DisplayName("성공 - 본인 수료증 조회")
        void getCertificate_success() {
            // given
            Long certificateId = 1L;
            Long userId = 1L;
            Certificate certificate = createTestCertificate(userId, "CERT-001-2026-000001");

            given(certificateRepository.findByIdAndTenantId(certificateId, TENANT_ID))
                    .willReturn(Optional.of(certificate));

            // when
            CertificateDetailResponse response = certificateService.getCertificate(certificateId, userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.certificateNumber()).isEqualTo("CERT-001-2026-000001");
        }

        @Test
        @DisplayName("실패 - 다른 사용자 수료증 조회 시도")
        void getCertificate_fail_unauthorized() {
            // given
            Long certificateId = 1L;
            Long ownerUserId = 1L;
            Long otherUserId = 999L;
            Certificate certificate = createTestCertificate(ownerUserId, "CERT-001-2026-000001");

            given(certificateRepository.findByIdAndTenantId(certificateId, TENANT_ID))
                    .willReturn(Optional.of(certificate));

            // when & then
            assertThatThrownBy(() -> certificateService.getCertificate(certificateId, otherUserId))
                    .isInstanceOf(UnauthorizedEnrollmentAccessException.class);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수료증")
        void getCertificate_fail_notFound() {
            // given
            Long certificateId = 999L;

            given(certificateRepository.findByIdAndTenantId(certificateId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> certificateService.getCertificate(certificateId, 1L))
                    .isInstanceOf(CertificateNotFoundException.class);
        }
    }

    // ==================== PDF 다운로드 테스트 ====================

    @Nested
    @DisplayName("downloadCertificatePdf - PDF 다운로드")
    class DownloadCertificatePdf {

        @Test
        @DisplayName("성공 - PDF 다운로드")
        void downloadCertificatePdf_success() {
            // given
            Long certificateId = 1L;
            Long userId = 1L;
            Certificate certificate = createTestCertificate(userId, "CERT-001-2026-000001");
            byte[] pdfBytes = "PDF_CONTENT".getBytes();

            given(certificateRepository.findByIdAndTenantId(certificateId, TENANT_ID))
                    .willReturn(Optional.of(certificate));
            given(certificatePdfService.generatePdf(certificate))
                    .willReturn(pdfBytes);

            // when
            byte[] result = certificateService.downloadCertificatePdf(certificateId, userId);

            // then
            assertThat(result).isEqualTo(pdfBytes);
            verify(certificatePdfService).generatePdf(certificate);
        }
    }

    // ==================== 수료증 진위 확인 테스트 ====================

    @Nested
    @DisplayName("verifyCertificate - 수료증 진위 확인")
    class VerifyCertificate {

        @Test
        @DisplayName("성공 - 유효한 수료증 확인")
        void verifyCertificate_success_valid() {
            // given
            String certificateNumber = "CERT-001-2026-000001";
            Certificate certificate = createTestCertificate(1L, certificateNumber);

            given(certificateRepository.findByCertificateNumber(certificateNumber))
                    .willReturn(Optional.of(certificate));

            // when
            CertificateVerifyResponse response = certificateService.verifyCertificate(certificateNumber);

            // then
            assertThat(response.valid()).isTrue();
            assertThat(response.certificateNumber()).isEqualTo(certificateNumber);
            assertThat(response.message()).contains("유효한");
        }

        @Test
        @DisplayName("성공 - 폐기된 수료증 확인")
        void verifyCertificate_success_revoked() {
            // given
            String certificateNumber = "CERT-001-2026-000001";
            Certificate certificate = createTestCertificate(1L, certificateNumber);
            certificate.revoke("테스트 폐기 사유");

            given(certificateRepository.findByCertificateNumber(certificateNumber))
                    .willReturn(Optional.of(certificate));

            // when
            CertificateVerifyResponse response = certificateService.verifyCertificate(certificateNumber);

            // then
            assertThat(response.valid()).isFalse();
            assertThat(response.status()).isEqualTo(CertificateStatus.REVOKED);
            assertThat(response.message()).contains("폐기");
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 수료증 번호")
        void verifyCertificate_success_notFound() {
            // given
            String certificateNumber = "CERT-999-2026-999999";

            given(certificateRepository.findByCertificateNumber(certificateNumber))
                    .willReturn(Optional.empty());

            // when
            CertificateVerifyResponse response = certificateService.verifyCertificate(certificateNumber);

            // then
            assertThat(response.valid()).isFalse();
            assertThat(response.certificateNumber()).isEqualTo(certificateNumber);
            assertThat(response.message()).contains("존재하지 않는");
        }
    }

    // ==================== 수료증 폐기 테스트 ====================

    @Nested
    @DisplayName("revokeCertificate - 수료증 폐기")
    class RevokeCertificate {

        @Test
        @DisplayName("성공 - 수료증 폐기")
        void revokeCertificate_success() {
            // given
            Long certificateId = 1L;
            String reason = "부정 수료";
            Certificate certificate = createTestCertificate(1L, "CERT-001-2026-000001");

            given(certificateRepository.findByIdAndTenantId(certificateId, TENANT_ID))
                    .willReturn(Optional.of(certificate));

            // when
            certificateService.revokeCertificate(certificateId, reason);

            // then
            assertThat(certificate.isRevoked()).isTrue();
            assertThat(certificate.getRevokedReason()).isEqualTo(reason);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수료증 폐기 시도")
        void revokeCertificate_fail_notFound() {
            // given
            Long certificateId = 999L;

            given(certificateRepository.findByIdAndTenantId(certificateId, TENANT_ID))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> certificateService.revokeCertificate(certificateId, "테스트"))
                    .isInstanceOf(CertificateNotFoundException.class);
        }
    }
}
