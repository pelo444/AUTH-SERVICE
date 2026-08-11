package com.mztarou.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USER_AUTHS", schema = "AG_PELO")
public class UserAuth {

    @Id
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "PERSON_ID", nullable = false)
    private String personId;  // Long → String に変更

    @Column(name = "IDENTITY_TYPE", nullable = false, length = 8)
    private String identityType;

    @Column(name = "IDENTIFIER", nullable = false, length = 512)
    private String identifier;

    @Column(name = "REGISTER_TIME", nullable = false)
    private LocalDateTime registerTime;

    // ===== コンストラクタ =====

    public UserAuth() {}

    public UserAuth(Long id, String personId, String identityType,
                    String identifier, LocalDateTime registerTime) {
        this.id = id;
        this.personId = personId;
        this.identityType = identityType;
        this.identifier = identifier;
        this.registerTime = registerTime;
    }

    // ===== ゲッター・セッター =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }
    
    public String getIdentityType() { return identityType; }
    public void setIdentityType(String identityType) { this.identityType = identityType; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public LocalDateTime getRegisterTime() { return registerTime; }
    public void setRegisterTime(LocalDateTime registerTime) { this.registerTime = registerTime; }
}