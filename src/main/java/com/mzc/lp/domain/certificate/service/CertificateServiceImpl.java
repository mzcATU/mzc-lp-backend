package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import com.mzc.lp.domain.certificate.entity.Certificate;
import com.mzc.lp.domain.certificate.exception.CertificateAlreadyIssuedException;
import com.mzc.lp.domain.certificate.exception.CertificateNotFoundException;
import com.mzc.lp.domain.certificate.repository.CertificateRepository;
import com.mzc.lp.domain.student.entity.Enrollment;
import com.mzc.lp.domain.student.exception.EnrollmentNotFoundException;
import com.mzc.lp.domain.student.exception.UnauthorizedEnrollmentAccessException;
import com.mzc.lp.domain.student.repository.EnrollmentRepository;
import com.mzc.lp.domain.ts.entity.CourseTime;
import com.mzc.lp.domain.ts.exception.CourseTimeNotFoundException;
import com.mzc.lp.domain.ts.repository.CourseTimeRepository;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateServiceImpl implements CertificateService {

    private final CertificateRepository certificateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseTimeRepository courseTimeRepository;
    private final UserRepository userRepository;
    private final CertificateNumberGenerator certificateNumberGenerator;
    private final CertificatePdfService certificatePdfService;

    @Override
    @Transactional
    public CertificateDetailResponse issueCertificate(Long enrollmentId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Issuing certificate: enrollmentId={}, tenantId={}", enrollmentId, tenantId);

        // 중복 발급 체크
        if (certificateRepository.existsByEnrollmentIdAndTenantId(enrollmentId, tenantId)) {
            throw new CertificateAlreadyIssuedException(enrollmentId);
        }

        // Enrollment 조회
        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // CourseTime 조회 (프로그램 정보 포함)
        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(
                enrollment.getCourseTimeId(), tenantId
        ).orElseThrow(() -> new CourseTimeNotFoundException(enrollment.getCourseTimeId()));

        // User 조회 (이름)
        User user = userRepository.findById(enrollment.getUserId())
                .orElseThrow(() -> new UserNotFoundException(enrollment.getUserId()));

        // 수료증 번호 생성
        String certificateNumber = certificateNumberGenerator.generate(tenantId);

        // Certificate 생성
        Certificate certificate = Certificate.create(
                certificateNumber,
                enrollment.getUserId(),
                user.getName(),
                enrollmentId,
                courseTime.getId(),
                courseTime.getTitle(),
                courseTime.getProgram() != null ? courseTime.getProgram().getId() : null,
                courseTime.getProgram() != null ? courseTime.getProgram().getTitle() : null,
                enrollment.getCompletedAt()
        );

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate issued: certificateId={}, certificateNumber={}",
                saved.getId(), saved.getCertificateNumber());

        return CertificateDetailResponse.from(saved);
    }

    @Override
    public Page<CertificateResponse> getMyCertificates(Long userId, Pageable pageable) {
        Long tenantId = TenantContext.getCurrentTenantId();
        return certificateRepository
                .findByUserIdAndTenantIdOrderByIssuedAtDesc(userId, tenantId, pageable)
                .map(CertificateResponse::from);
    }

    @Override
    public CertificateDetailResponse getCertificate(Long certificateId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        Certificate certificate = certificateRepository.findByIdAndTenantId(certificateId, tenantId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        // 본인 소유 확인
        if (!certificate.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(certificateId, userId);
        }

        return CertificateDetailResponse.from(certificate);
    }

    @Override
    public byte[] downloadCertificatePdf(Long certificateId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        Certificate certificate = certificateRepository.findByIdAndTenantId(certificateId, tenantId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        // 본인 소유 확인
        if (!certificate.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(certificateId, userId);
        }

        return certificatePdfService.generatePdf(certificate);
    }

    @Override
    public CertificateVerifyResponse verifyCertificate(String certificateNumber) {
        return certificateRepository.findByCertificateNumber(certificateNumber)
                .map(CertificateVerifyResponse::from)
                .orElse(CertificateVerifyResponse.notFound(certificateNumber));
    }

    @Override
    @Transactional
    public void revokeCertificate(Long certificateId, String reason) {
        Long tenantId = TenantContext.getCurrentTenantId();

        Certificate certificate = certificateRepository.findByIdAndTenantId(certificateId, tenantId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        certificate.revoke(reason);
        log.info("Certificate revoked: certificateId={}, reason={}", certificateId, reason);
    }
}
