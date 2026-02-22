package com.hkt.iot.smartapps.smartlivestock.domain.repository;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.Geofence;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceStatus;

import java.util.List;
import java.util.Optional;

/**
 * 电子围栏仓储接口
 *
 * 职责：电子围栏聚合根的持久化操作
 */
public interface GeofenceRepository {

    /**
     * 保存电子围栏
     *
     * @param geofence 电子围栏实体
     * @return 保存后的电子围栏
     */
    Geofence save(Geofence geofence);

    /**
     * 按ID查询电子围栏
     *
     * @param id 电子围栏ID
     * @return 电子围栏实体
     */
    Optional<Geofence> findById(GeofenceId id);

    /**
     * 按ID删除电子围栏
     *
     * @param id 电子围栏ID
     */
    void deleteById(GeofenceId id);

    /**
     * 按租户查询电子围栏列表
     *
     * @param tenantId 租户ID
     * @return 电子围栏列表
     */
    List<Geofence> findByTenantId(TenantId tenantId);

    /**
     * 查询租户下的活跃电子围栏
     *
     * @param tenantId 租户ID
     * @return 活跃电子围栏列表
     */
    List<Geofence> findActiveGeofences(TenantId tenantId);

    /**
     * 按租户和状态查询电子围栏列表
     *
     * @param tenantId 租户ID
     * @param status 电子围栏状态
     * @return 电子围栏列表
     */
    List<Geofence> findByTenantIdAndStatus(TenantId tenantId, GeofenceStatus status);

    /**
     * 检查电子围栏是否存在
     *
     * @param id 电子围栏ID
     * @return 是否存在
     */
    boolean existsById(GeofenceId id);

    /**
     * 统计租户下的电子围栏数量
     *
     * @param tenantId 租户ID
     * @return 电子围栏数量
     */
    long countByTenantId(TenantId tenantId);
}
