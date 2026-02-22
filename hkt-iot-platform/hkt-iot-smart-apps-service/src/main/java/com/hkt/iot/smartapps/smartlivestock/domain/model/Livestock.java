package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.hkt.iot.domain.shared.AuditLog;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.domain.shared.UserId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 牲畜聚合根
 *
 * 职责：管理牲畜的基本信息和健康状态
 * 业务规则：
 * - 记录牲畜的基本信息、位置、健康评分
 * - 支持电子围栏越界检测
 * - 记录健康状态变化历史
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Livestock {

    private LivestockId id;
    private LivestockTag tag;
    private LivestockType type;
    private LivestockStatus status;
    private Gender gender;
    private LocalDate birthDate;
    private Weight currentWeight;
    private HealthScore healthScore;
    private Location currentLocation;
    private GeofenceId geofenceId;
    private DeviceId rumenCapsuleId;  // 瘤胃胶囊ID
    private DeviceId trackerId;        // 追踪器ID
    private TenantId tenantId;
    private String breed;              // 品种
    private String notes;
    private AuditLog auditLog;
    private Long version;

    /**
     * 更新位置
     */
    public void updateLocation(Location location) {
        this.currentLocation = location;

        // 检查是否越界
        if (this.geofenceId != null) {
            GeofenceViolation violation = checkGeofenceViolation(location);
            if (violation != null) {
                this.status = LivestockStatus.QUARANTINE;  // 越界自动隔离
                this.auditLog = AuditLog.create(LocalDateTime.now(), "牲畜越界，已自动隔离");
            }
        }
    }

    /**
     * 检查电子围栏越界
     */
    public GeofenceViolation checkGeofenceViolation(Location location) {
        if (this.geofenceId == null) {
            return null;
        }
        // 实际检测由Geofence聚合根完成
        // 这里只是标记可能越界
        return null;
    }

    /**
     * 更新健康评分
     */
    public void updateHealthScore(HealthScore score) {
        HealthScore previous = this.healthScore;
        this.healthScore = score;

        // 根据健康评分更新状态
        if (score.getLevel() == HealthLevel.CRITICAL) {
            this.status = LivestockStatus.SICK;
        } else if (this.status == LivestockStatus.SICK
                && score.getLevel().ordinal() <= HealthLevel.GOOD.ordinal()) {
            this.status = LivestockStatus.HEALTHY;
        }

        this.auditLog = AuditLog.create(LocalDateTime.now(),
                String.format("健康评分更新: %d -> %d",
                        previous != null ? previous.getValue() : 0, score.getValue()));
    }

    /**
     * 更新体重
     */
    public void updateWeight(Weight weight) {
        this.currentWeight = weight;
        this.auditLog = AuditLog.create(LocalDateTime.now(),
                "体重更新: " + weight.getValue() + weight.getUnit());
    }

    /**
     * 标记为生病
     */
    public void markSick(String diagnosis) {
        this.status = LivestockStatus.SICK;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "诊断: " + diagnosis;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "标记为生病: " + diagnosis);
    }

    /**
     * 标记为康复
     */
    public void markRecovered() {
        this.status = LivestockStatus.HEALTHY;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "牲畜康复");
    }

    /**
     * 隔离
     */
    public void quarantine() {
        this.status = LivestockStatus.QUARANTINE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "牲畜隔离");
    }

    /**
     * 解除隔离
     */
    public void releaseFromQuarantine() {
        if (this.status == LivestockStatus.QUARANTINE) {
            this.status = LivestockStatus.HEALTHY;
            this.auditLog = AuditLog.create(LocalDateTime.now(), "解除隔离");
        }
    }

    /**
     * 标记为死亡
     */
    public void markDeceased(String cause) {
        this.status = LivestockStatus.DECEASED;
        this.notes = (this.notes != null ? this.notes + "\n" : "") + "死亡原因: " + cause;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "牲畜死亡: " + cause);
    }

    /**
     * 分配电子围栏
     */
    public void assignGeofence(GeofenceId geofenceId) {
        this.geofenceId = geofenceId;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "分配电子围栏: " + geofenceId.getValue());
    }

    /**
     * 绑定瘤胃胶囊
     */
    public void bindRumenCapsule(DeviceId deviceId) {
        this.rumenCapsuleId = deviceId;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "绑定瘤胃胶囊: " + deviceId.getValue());
    }

    /**
     * 绑定追踪器
     */
    public void bindTracker(DeviceId deviceId) {
        this.trackerId = deviceId;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "绑定追踪器: " + deviceId.getValue());
    }

    /**
     * 计算年龄（月）
     */
    public int getAgeInMonths() {
        if (this.birthDate == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.MONTHS.between(this.birthDate, LocalDate.now());
    }

    /**
     * 检查是否成年
     */
    public boolean isAdult() {
        if (this.birthDate == null) {
            return false;
        }
        // 默认18个月成年
        return getAgeInMonths() >= 18;
    }

    /**
     * 创建新牲畜记录
     */
    public static Livestock create(
            LivestockTag tag,
            LivestockType type,
            Gender gender,
            LocalDate birthDate,
            Weight weight,
            String breed,
            TenantId tenantId) {

        return Livestock.builder()
                .id(LivestockId.generate())
                .tag(tag)
                .type(type)
                .status(LivestockStatus.HEALTHY)
                .gender(gender)
                .birthDate(birthDate)
                .currentWeight(weight)
                .healthScore(HealthScore.initial())
                .tenantId(tenantId)
                .breed(breed)
                .auditLog(AuditLog.create(LocalDateTime.now(), "创建牲畜记录"))
                .version(0L)
                .build();
    }
}
