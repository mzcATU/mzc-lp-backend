package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.dto.request.ChangePasswordRequest;
import com.mzc.lp.domain.user.dto.request.UpdateProfileRequest;
import com.mzc.lp.domain.user.dto.request.WithdrawRequest;
import com.mzc.lp.domain.user.dto.response.UserDetailResponse;

public interface UserService {

    UserDetailResponse getMe(Long userId);

    UserDetailResponse updateMe(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void withdraw(Long userId, WithdrawRequest request);
}
