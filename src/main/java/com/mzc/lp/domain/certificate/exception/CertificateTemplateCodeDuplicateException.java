package com.mzc.lp.domain.certificate.exception;

import com.mzc.lp.common.constant.ErrorCode;
import com.mzc.lp.common.exception.BusinessException;

public class CertificateTemplateCodeDuplicateException extends BusinessException {

    public CertificateTemplateCodeDuplicateException(String templateCode) {
        super(ErrorCode.CERTIFICATE_TEMPLATE_CODE_DUPLICATE, "Template code already exists: " + templateCode);
    }
}
