package com.mzc.lp.domain.certificate.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CertificateAlreadyIssuedException extends BusinessException {

    public CertificateAlreadyIssuedException(Long enrollmentId) {
        super(ErrorCode.CERTIFICATE_ALREADY_ISSUED,
                "Certificate already issued for enrollment: " + enrollmentId);
    }
}
