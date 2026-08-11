package com.mztarou.auth_service.repository;

import com.mztarou.auth_service.entity.UserAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long> {

    // IDに紐づく認証情報を取得
    Optional<UserAuth> findByPersonId(String personId);  // Long → String に変更
}