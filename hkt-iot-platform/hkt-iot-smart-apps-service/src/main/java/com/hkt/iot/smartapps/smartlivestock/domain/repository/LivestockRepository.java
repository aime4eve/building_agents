package com.hkt.iot.smartapps.smartlivestock.domain.repository;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.Livestock;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockStatus;

import java.util.List;
import java.util.Optional;

/**
 * 牲畜仓储接口
 *
 * 职责：牲畜聚合根的持久化操作
 */
public interface LivestockRepository {

    /**
     * 保存牲畜
     *
     * @param livestock 牲畜实体
     * @return 保存后的牲畜
     */
    Livestock save(Livestock livestock);

    /**
     * 按ID查询牲畜
     *
     * @param id 牲畜ID
     * @return 牲畜实体
     */
    Optional<Livestock> findById(LivestockId id);

    /**
     * 按ID删除牲畜
     *
     * @param id 牲畜ID
     */
    void deleteById(LivestockId id);

    /**
     * 按租户查询牲畜列表
     *
     * @param tenantId 租户ID
     * @return 牲畜列表
     */
    List<Livestock> findByTenantId(TenantId tenantId);

    /**
     * 按围栏ID查询牲畜列表
     *
     * @param geofenceId 围栏ID
     * @return 牲畜列表
     */
    List<Livestock> findByGeofenceId(GeofenceId geofenceId);

    /**
     * 按状态查询牲畜列表
     *
     * @param status 牲畜状态
     * @return 牲畜列表
     */
    List<Livestock> findByStatus(LivestockStatus status);

    /**
     * 按租户和状态查询牲畜列表
     *
     * @param tenantId 租户ID
     * @param status 牲畜状态
     * @return 牲畜列表
     */
    List<Livestock> findByTenantIdAndStatus(TenantId tenantId, LivestockStatus status);

    /**
     * 检查牲畜是否存在
     *
     * @param id 牲畜ID
     * @return 是否存在
     */
    boolean existsById(LivestockId id);

    /**
     * 统计租户下的牲畜数量
     *
     * @param tenantId 租户ID
     * @return 牲畜数量
     */
    long countByTenantId(TenantId tenantId);
}
