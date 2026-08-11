package com.mztarou.auth_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "SSO_APPS", schema = "AG_PELO")
public class SsoApp {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_sso_apps")
    @SequenceGenerator(name = "seq_sso_apps", sequenceName = "AG_PELO.SEQ_SSO_APPS", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "APP_NAME", nullable = false, length = 64)
    private String appName;

    @Column(name = "APP_URL", nullable = false, length = 256)
    private String appUrl;

    @Column(name = "DESCRIPTION", length = 256)
    private String description;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Integer isActive;

    @Column(name = "SORT_ORDER", nullable = false)
    private Integer sortOrder;

    // ===== コンストラクタ =====

    public SsoApp() {}

    // ===== ゲッター・セッター =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public String getAppUrl() { return appUrl; }
    public void setAppUrl(String appUrl) { this.appUrl = appUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getIsActive() { return isActive; }
    public void setIsActive(Integer isActive) { this.isActive = isActive; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}