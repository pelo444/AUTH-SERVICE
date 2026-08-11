package com.mztarou.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS", schema = "AG_PELO")
@SequenceGenerator(
    name = "seq_users",
    sequenceName = "AG_PELO.SEQ_USERS",
    allocationSize = 1
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_users")
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "USERNAME", nullable = false, length = 32)
    private String username;

    @Column(name = "PERSON_ID", length = 32)
    private String personId;

    @Column(name = "REGISTER_TIME", nullable = false)
    private LocalDateTime registerTime;

    @Column(name = "INVALID_TIME")
    private LocalDateTime invalidTime;

    // ===== コンストラクタ =====

    public User() {}

    public User(String username, String personId, LocalDateTime registerTime) {
        this.username = username;
        this.personId = personId;
        this.registerTime = registerTime;
    }

    // ===== ゲッター・セッター =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public LocalDateTime getRegisterTime() { return registerTime; }
    public void setRegisterTime(LocalDateTime registerTime) { this.registerTime = registerTime; }

    public LocalDateTime getInvalidTime() { return invalidTime; }
    public void setInvalidTime(LocalDateTime invalidTime) { this.invalidTime = invalidTime; }
}