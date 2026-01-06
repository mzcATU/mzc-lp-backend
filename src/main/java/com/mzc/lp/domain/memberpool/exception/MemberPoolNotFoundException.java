package com.mzc.lp.domain.memberpool.exception;

import com.mzc.lp.common.exception.BusinessException;
import com.mzc.lp.common.constant.ErrorCode;

public class MemberPoolNotFoundException extends BusinessException {

    public MemberPoolNotFoundException() {
        super(ErrorCode.MEMBER_POOL_NOT_FOUND);
    }

    public MemberPoolNotFoundException(Long poolId) {
        super(ErrorCode.MEMBER_POOL_NOT_FOUND, "멤버 풀을 찾을 수 없습니다. ID: " + poolId);
    }
}
