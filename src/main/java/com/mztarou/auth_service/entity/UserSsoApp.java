package com.mztarou.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_SSO_APPS", schema = "AG_PELO")
public class UserSsoApp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_user_sso_apps")
    @SequenceGenerator(name = "seq_user_sso_apps", sequenceName = "AG_PELO.SEQ_USER_SSO_APPS", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "PERSON_ID", nullable = false, length = 32)
    private String personId;

    @Column(name = "APP_ID", nullable = false)
    private Long appId;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    // ===== コンストラクタ =====

    public UserSsoApp() {}

    // ===== ゲッター・セッター =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public Long getAppId() { return appId; }
    public void setAppId(Long appId) { this.appId = appId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}