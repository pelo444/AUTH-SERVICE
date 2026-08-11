package com.mztarou.auth_service.repository;

import com.mztarou.auth_service.entity.UserSsoApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSsoAppRepository extends JpaRepository<UserSsoApp, Long> {

    List<UserSsoApp> findByPersonId(String personId);
}