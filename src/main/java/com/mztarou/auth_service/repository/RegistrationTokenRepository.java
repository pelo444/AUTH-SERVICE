package com.mztarou.auth_service.repository;

import com.mztarou.auth_service.entity.RegistrationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationTokenRepository extends JpaRepository<RegistrationToken, String> {

    // tokenで検索
    Optional<RegistrationToken> findByToken(String token);

    // emailで未使用・有効なtokenを検索
    Optional<RegistrationToken> findByEmailAndUsedAtIsNull(String email);
}