package com.mzc.lp.domain.user.service;

import com.mzc.lp.domain.user.dto.request.RegisterRequest;
import com.mzc.lp.domain.user.dto.response.UserResponse;
import com.mzc.lp.domain.user.entity.User;
import com.mzc.lp.domain.user.exception.DuplicateEmailException;
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
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // 비밀번호 암호화 및 User 생성
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = User.create(
                request.email(),
                request.name(),
                encodedPassword,
                request.phone()
        );

        // 저장
        User savedUser = userRepository.save(user);
        log.info("User registered: {}", savedUser.getEmail());

        return UserResponse.from(savedUser);
    }
}
