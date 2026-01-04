package com.mzc.lp.domain.certificate.listener;

import com.mzc.lp.domain.certificate.event.EnrollmentCompletedEvent;
import com.mzc.lp.domain.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateEventListener {

    private final CertificateService certificateService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleEnrollmentCompleted(EnrollmentCompletedEvent event) {
        log.info("EnrollmentCompletedEvent received: enrollmentId={}, userId={}, courseTimeId={}",
                event.getEnrollmentId(), event.getUserId(), event.getCourseTimeId());

        try {
            certificateService.issueCertificate(event.getEnrollmentId());
        } catch (Exception e) {
            log.error("Failed to issue certificate for enrollment: {}", event.getEnrollmentId(), e);
            throw e;
        }
    }
}
