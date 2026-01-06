package com.mzc.lp.domain.certificate.service;

import com.mzc.lp.common.context.TenantContext;
import com.mzc.lp.domain.certificate.constant.CertificateStatus;
import com.mzc.lp.domain.certificate.dto.response.CertificateDetailResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateResponse;
import com.mzc.lp.domain.certificate.dto.response.CertificateVerifyResponse;
import com.mzc.lp.domain.certificate.entity.Certificate;
import com.mzc.lp.domain.certificate.exception.CertificateAlreadyIssuedException;
import com.mzc.lp.domain.certificate.exception.CertificateNotFoundException;
import com.mzc.lp.domain.certificate.repository.CertificateRepository;
import com.mzc.lp.domain.student.constant.EnrollmentStatus;
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

        // 유효한 수료증 중복 발급 체크 (ISSUED 상태인 것만)
        if (certificateRepository.existsByEnrollmentIdAndTenantIdAndStatus(
                enrollmentId, tenantId, CertificateStatus.ISSUED)) {
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
    @Transactional
    public CertificateDetailResponse issueCertificateByUser(Long enrollmentId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Issuing certificate by user: enrollmentId={}, userId={}, tenantId={}",
                enrollmentId, userId, tenantId);

        // Enrollment 조회
        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 수강 확인
        if (!enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(enrollmentId, userId);
        }

        // 수강 완료 상태 확인
        if (enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new IllegalStateException("수강을 완료해야 수료증을 발급받을 수 있습니다.");
        }

        // 유효한 수료증 중복 발급 체크
        if (certificateRepository.existsByEnrollmentIdAndTenantIdAndStatus(
                enrollmentId, tenantId, CertificateStatus.ISSUED)) {
            throw new CertificateAlreadyIssuedException(enrollmentId);
        }

        // CourseTime 조회
        CourseTime courseTime = courseTimeRepository.findByIdAndTenantId(
                enrollment.getCourseTimeId(), tenantId
        ).orElseThrow(() -> new CourseTimeNotFoundException(enrollment.getCourseTimeId()));

        // User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 수료증 번호 생성
        String certificateNumber = certificateNumberGenerator.generate(tenantId);

        // Certificate 생성
        Certificate certificate = Certificate.create(
                certificateNumber,
                userId,
                user.getName(),
                enrollmentId,
                courseTime.getId(),
                courseTime.getTitle(),
                courseTime.getProgram() != null ? courseTime.getProgram().getId() : null,
                courseTime.getProgram() != null ? courseTime.getProgram().getTitle() : null,
                enrollment.getCompletedAt()
        );

        Certificate saved = certificateRepository.save(certificate);
        log.info("Certificate issued by user: certificateId={}, certificateNumber={}",
                saved.getId(), saved.getCertificateNumber());

        return CertificateDetailResponse.from(saved);
    }

    @Override
    @Transactional
    public CertificateDetailResponse reissueCertificate(Long certificateId, String reason, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();
        log.info("Reissuing certificate: certificateId={}, userId={}, reason={}",
                certificateId, userId, reason);

        // 기존 수료증 조회
        Certificate original = certificateRepository.findByIdAndTenantId(certificateId, tenantId)
                .orElseThrow(() -> new CertificateNotFoundException(certificateId));

        // 본인 소유 확인
        if (!original.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(certificateId, userId);
        }

        // 유효한 수료증인지 확인
        if (!original.isValid()) {
            throw new IllegalStateException("유효하지 않은 수료증은 재발급할 수 없습니다.");
        }

        // 기존 수료증 무효화
        original.invalidateForReissue(reason);

        // 새 수료증 번호 생성
        String newCertificateNumber = certificateNumberGenerator.generate(tenantId);

        // 재발급 수료증 생성
        Certificate reissued = Certificate.createReissue(newCertificateNumber, original, reason);

        Certificate saved = certificateRepository.save(reissued);
        log.info("Certificate reissued: originalId={}, newId={}, newCertificateNumber={}, reissueCount={}",
                certificateId, saved.getId(), saved.getCertificateNumber(), saved.getReissueCount());

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
    public CertificateDetailResponse getCertificateByEnrollment(Long enrollmentId, Long userId) {
        Long tenantId = TenantContext.getCurrentTenantId();

        // Enrollment 조회
        Enrollment enrollment = enrollmentRepository.findByIdAndTenantId(enrollmentId, tenantId)
                .orElseThrow(() -> new EnrollmentNotFoundException(enrollmentId));

        // 본인 수강 확인
        if (!enrollment.getUserId().equals(userId)) {
            throw new UnauthorizedEnrollmentAccessException(enrollmentId, userId);
        }

        // 유효한 수료증 조회
        Certificate certificate = certificateRepository.findByEnrollmentIdAndTenantIdAndStatus(
                enrollmentId, tenantId, CertificateStatus.ISSUED
        ).orElseThrow(() -> CertificateNotFoundException.withMessage("해당 수강에 대한 유효한 수료증이 없습니다."));

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
