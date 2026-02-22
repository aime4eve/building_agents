package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.hkt.iot.domain.shared.AuditLog;
import com.hkt.iot.domain.shared.TenantId;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 电子围栏聚合根
 *
 * 职责：管理电子围栏的定义和越界检测
 * 业务规则：
 * - 定义围栏边界坐标
 * - 检测牲畜位置是否在围栏内
 * - 记录越界事件
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Geofence {

    private GeofenceId id;
    private GeofenceName name;
    private GeofenceCode code;
    private GeofenceType type;
    private GeofenceStatus status;
    private List<Coordinate> boundary;
    private TenantId tenantId;
    private String description;
    private AuditLog auditLog;
    private Long version;

    /**
     * 激活电子围栏
     */
    public void activate() {
        this.status = GeofenceStatus.ACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "电子围栏激活");
    }

    /**
     * 停用电子围栏
     */
    public void deactivate() {
        this.status = GeofenceStatus.INACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "电子围栏停用");
    }

    /**
     * 更新围栏边界
     */
    public void updateBoundary(List<Coordinate> boundary) {
        if (boundary == null || boundary.size() < 3) {
            throw new IllegalArgumentException("围栏边界至少需要3个坐标点");
        }
        this.boundary = new ArrayList<>(boundary);
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新围栏边界");
    }

    /**
     * 检查位置是否在围栏内
     * 使用射线法（Ray Casting）判断点是否在多边形内
     */
    public boolean contains(Location location) {
        if (this.boundary == null || this.boundary.isEmpty()) {
            return false;
        }

        Coordinate point = location.getCoordinate();
        int crossings = 0;

        for (int i = 0; i < this.boundary.size(); i++) {
            Coordinate a = this.boundary.get(i);
            Coordinate b = this.boundary.get((i + 1) % this.boundary.size());

            // 检查射线是否与边相交
            if (rayCrossesBoundary(point, a, b)) {
                crossings++;
            }
        }

        // 奇数次相交表示在内部
        return crossings % 2 == 1;
    }

    /**
     * 射线法判断射线是否与边界相交
     */
    private boolean rayCrossesBoundary(Coordinate point, Coordinate a, Coordinate b) {
        // 射线从点向右水平发射
        if (a.getLatitude() > point.getLatitude() == b.getLatitude() > point.getLatitude()) {
            return false;
        }

        // 计算交点的经度
        double intersectionLongitude = (b.getLongitude() - a.getLongitude())
                * (point.getLatitude() - a.getLatitude())
                / (b.getLatitude() - a.getLatitude())
                + a.getLongitude();

        return point.getLongitude() < intersectionLongitude;
    }

    /**
     * 检测越界
     */
    public GeofenceViolation checkViolation(LivestockId livestockId, Location location) {
        if (this.status != GeofenceStatus.ACTIVE) {
            return null;
        }

        if (!contains(location)) {
            return GeofenceViolation.create(
                    livestockId,
                    this.id,
                    location,
                    ViolationType.OUT_OF_BOUNDS
            );
        }

        return null;
    }

    /**
     * 创建新电子围栏
     */
    public static Geofence create(
            GeofenceName name,
            GeofenceCode code,
            GeofenceType type,
            List<Coordinate> boundary,
            TenantId tenantId,
            String description) {

        return Geofence.builder()
                .id(GeofenceId.generate())
                .name(name)
                .code(code)
                .type(type)
                .status(GeofenceStatus.INACTIVE)
                .boundary(boundary != null ? new ArrayList<>(boundary) : new ArrayList<>())
                .tenantId(tenantId)
                .description(description)
                .auditLog(AuditLog.create(LocalDateTime.now(), "创建电子围栏"))
                .version(0L)
                .build();
    }
}
