package com.hkt.iot.smartapps.moldprevention.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

/**
 * 防霉管控区域 JPA 实体
 * 对应数据库表：mold_prevention_zone
 */
@Entity
@Table(name = "mold_prevention_zone")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = 0")
public class MoldPreventionZoneEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "zone_code", nullable = false, length = 100)
    private String zoneCode;

    @Column(name = "zone_name", nullable = false, length = 200)
    private String zoneName;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "zone_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ZoneStatus zoneStatus;

    @Column(name = "risk_threshold", columnDefinition = "JSON")
    private String riskThreshold;

    @Column(name = "control_strategy", columnDefinition = "JSON")
    private String controlStrategy;

    @Column(name = "current_risk_level", length = 20)
    @Enumerated(EnumType.STRING)
    private String currentRiskLevel;

    @Column(name = "last_environment_data", columnDefinition = "JSON")
    private String lastEnvironmentData;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @Column(name = "description", length = 500)
    private String description;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 区域状态枚举
     */
    public enum ZoneStatus {
        ACTIVE, INACTIVE, MAINTENANCE
    }

    /**
     * 从领域模型创建实体
     */
    public static MoldPreventionZoneEntity fromDomain(
            com.hkt.iot.smartapps.moldprevention.domain.model.MoldPreventionZone zone) {
        MoldPreventionZoneEntity entity = new MoldPreventionZoneEntity();
        // 注意：实际实现中需要处理值对象的转换
        entity.setTenantId(zone.getTenantId().getValue());
        entity.setZoneCode(zone.getCode().getValue());
        entity.setZoneName(zone.getName().getValue());
        entity.setSpaceId(zone.getSpaceId().getValue());
        entity.setZoneStatus(ZoneStatus.valueOf(zone.getStatus().name()));
        entity.setCurrentRiskLevel(zone.getCurrentRiskLevel().name());
        entity.setDescription(zone.getDescription());
        entity.setVersion(zone.getVersion());
        entity.setCreatedAt(java.time.LocalDateTime.now());
        entity.setUpdatedAt(java.time.LocalDateTime.now());
        entity.setDeleted(false);
        return entity;
    }

    // Setters for builder pattern
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setZoneCode(String zoneCode) { this.zoneCode = zoneCode; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public void setZoneStatus(ZoneStatus zoneStatus) { this.zoneStatus = zoneStatus; }
    public void setRiskThreshold(String riskThreshold) { this.riskThreshold = riskThreshold; }
    public void setControlStrategy(String controlStrategy) { this.controlStrategy = controlStrategy; }
    public void setCurrentRiskLevel(String currentRiskLevel) { this.currentRiskLevel = currentRiskLevel; }
    public void setLastEnvironmentData(String lastEnvironmentData) { this.lastEnvironmentData = lastEnvironmentData; }
    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) { this.lastEvaluatedAt = lastEvaluatedAt; }
    public void setDescription(String description) { this.description = description; }
    public void setVersion(Long version) { this.version = version; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
}
