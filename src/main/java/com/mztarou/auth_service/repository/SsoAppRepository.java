package com.mztarou.auth_service.repository;

import com.mztarou.auth_service.entity.SsoApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SsoAppRepository extends JpaRepository<SsoApp, Long> {

    // PERSON_IDに紐づくアクティブなアプリを取得
    @Query("SELECT a FROM SsoApp a " +
           "JOIN UserSsoApp ua ON ua.appId = a.id " +
           "WHERE ua.personId = :personId " +
           "AND a.isActive = 1 " +
           "ORDER BY a.sortOrder")
    List<SsoApp> findActiveAppsByPersonId(String personId);
}