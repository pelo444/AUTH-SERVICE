package com.mztarou.auth_service.service;

import com.mztarou.auth_service.entity.User;
import com.mztarou.auth_service.entity.UserAuth;
import com.mztarou.auth_service.repository.UserAuthRepository;
import com.mztarou.auth_service.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserAuthRepository userAuthRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserAuthRepository userAuthRepository) {
        this.userRepository = userRepository;
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String personId)
            throws UsernameNotFoundException {

        // 有効なユーザーを取得
        User user = userRepository
            .findByPersonIdAndInvalidTimeIsNull(personId)
            .orElseThrow(() -> new UsernameNotFoundException(
                "ユーザーが見つかりません: " + personId));

        // 認証情報を取得
        UserAuth userAuth = userAuthRepository
            .findByPersonId(user.getPersonId())  // user.getId() → user.getPersonId()
            .orElseThrow(() -> new UsernameNotFoundException(
                "認証情報が見つかりません: " + personId));

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getPersonId())
            .password(userAuth.getIdentifier())
            .roles("USER")
            .build();
    }
}