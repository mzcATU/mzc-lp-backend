package com.mzc.lp.domain.certificate.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CertificateTemplateNotFoundException extends BusinessException {

    public CertificateTemplateNotFoundException() {
        super(ErrorCode.CERTIFICATE_TEMPLATE_NOT_FOUND);
    }

    public CertificateTemplateNotFoundException(Long templateId) {
        super(ErrorCode.CERTIFICATE_TEMPLATE_NOT_FOUND, "Certificate template not found with id: " + templateId);
    }

    public CertificateTemplateNotFoundException(String templateCode) {
        super(ErrorCode.CERTIFICATE_TEMPLATE_NOT_FOUND, "Certificate template not found with code: " + templateCode);
    }
}
