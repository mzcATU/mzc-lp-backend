package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.dto.request.ChangePasswordRequest;
import com.mzc.lp.domain.user.dto.request.UpdateProfileRequest;
import com.mzc.lp.domain.user.dto.request.WithdrawRequest;
import com.mzc.lp.domain.user.dto.response.UserDetailResponse;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.PasswordMismatchException;
import com.mzc.lp.domain.user.exception.UserNotFoundException;
import com.mzc.lp.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetailResponse getMe(Long userId) {
        log.debug("Getting user info: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return UserDetailResponse.from(user);
    }

    @Override
    @Transactional
    public UserDetailResponse updateMe(Long userId, UpdateProfileRequest request) {
        log.info("Updating user profile: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        user.updateProfile(request.name(), request.phone(), request.profileImageUrl());
        return UserDetailResponse.from(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Changing password: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new PasswordMismatchException();
        }

        user.changePassword(passwordEncoder.encode(request.newPassword()));
        log.info("Password changed: userId={}", userId);
    }

    @Override
    @Transactional
    public void withdraw(Long userId, WithdrawRequest request) {
        log.info("Withdrawing user: userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new PasswordMismatchException();
        }

        user.withdraw();
        log.info("User withdrawn: userId={}", userId);
    }
}
