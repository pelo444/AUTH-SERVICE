package com.mztarou.auth_service.repository;

import com.mztarou.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // person_idで有効なユーザーを検索（invalid_timeがnullのもの）
    Optional<User> findByPersonIdAndInvalidTimeIsNull(String personId);

    // person_idに紐づく全レコードを取得（履歴含む）
    List<User> findByPersonId(String personId);
}
