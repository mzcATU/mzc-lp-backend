package com.mzc.lp.domain.certificate.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CertificateNotFoundException extends BusinessException {

    public CertificateNotFoundException() {
        super(ErrorCode.CERTIFICATE_NOT_FOUND);
    }

    public CertificateNotFoundException(Long certificateId) {
        super(ErrorCode.CERTIFICATE_NOT_FOUND, "Certificate not found with id: " + certificateId);
    }

    public CertificateNotFoundException(String certificateNumber) {
        super(ErrorCode.CERTIFICATE_NOT_FOUND, "Certificate not found with number: " + certificateNumber);
    }

    public static CertificateNotFoundException withMessage(String message) {
        return new CertificateNotFoundException(ErrorCode.CERTIFICATE_NOT_FOUND, message);
    }

    private CertificateNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
