package com.mztarou.auth_service.service;

import com.mztarou.auth_service.entity.User;
import com.mztarou.auth_service.entity.UserAuth;
import com.mztarou.auth_service.repository.UserAuthRepository;
import com.mztarou.auth_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       UserAuthRepository userAuthRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===========================
    // 認証
    // ===========================
    public Optional<User> authenticate(String personId, String rawPassword) {

        // 有効なUSERSレコードを取得
        Optional<User> userOpt = userRepository
            .findByPersonIdAndInvalidTimeIsNull(personId);

        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        // USER_AUTHSからハッシュ化パスワードを取得
        Optional<UserAuth> userAuthOpt = userAuthRepository
            .findByPersonId(user.getPersonId());

        if (userAuthOpt.isEmpty()) {
            return Optional.empty();
        }

        // パスワードを検証
        boolean matches = passwordEncoder.matches(
            rawPassword,
            userAuthOpt.get().getIdentifier()
        );

        return matches ? Optional.of(user) : Optional.empty();
    }
}
