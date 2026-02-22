package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 电子围栏违规记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeofenceViolation {

    private ViolationId id;
    private LivestockId livestockId;
    private GeofenceId geofenceId;
    private Location violationLocation;
    private ViolationType violationType;
    private LocalDateTime occurredAt;
    private ViolationStatus status;
    private LocalDateTime resolvedAt;
    private String notes;

    public static GeofenceViolation create(
            LivestockId livestockId,
            GeofenceId geofenceId,
            Location location,
            ViolationType type) {

        return GeofenceViolation.builder()
                .id(ViolationId.generate())
                .livestockId(livestockId)
                .geofenceId(geofenceId)
                .violationLocation(location)
                .violationType(type)
                .occurredAt(LocalDateTime.now())
                .status(ViolationStatus.PENDING)
                .build();
    }

    /**
     * 标记为已解决
     */
    public void markAsResolved(String notes) {
        this.status = ViolationStatus.RESOLVED;
        this.resolvedAt = LocalDateTime.now();
        this.notes = notes;
    }

    /**
     * 判断是否未解决
     */
    public boolean isPending() {
        return this.status == ViolationStatus.PENDING;
    }

    /**
     * 违规类型枚举
     */
    public enum ViolationType {
        /**
         * 超出边界
         */
        OUT_OF_BOUNDS,

        /**
         * 进入限制区
         */
        ENTER_RESTRICTED,

        /**
         * 离开放牧区
         */
        LEAVE_GRAZING
    }

    /**
     * 违规状态枚举
     */
    public enum ViolationStatus {
        /**
         * 待处理
         */
        PENDING,

        /**
         * 已解决
         */
        RESOLVED,

        /**
         * 已忽略
         */
        DISMISSED
    }
}
