package com.hkt.iot.smartapps.smartlivestock.domain.repository;

import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceViolation;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.ViolationId;

import java.util.List;
import java.util.Optional;

/**
 * 电子围栏违规仓储接口
 *
 * 职责：电子围栏违规记录的持久化操作
 */
public interface GeofenceViolationRepository {

    /**
     * 保存违规记录
     *
     * @param violation 违规记录
     * @return 保存后的违规记录
     */
    GeofenceViolation save(GeofenceViolation violation);

    /**
     * 按ID查询违规记录
     *
     * @param id 违规ID
     * @return 违规记录
     */
    Optional<GeofenceViolation> findById(ViolationId id);

    /**
     * 按ID删除违规记录
     *
     * @param id 违规ID
     */
    void deleteById(ViolationId id);

    /**
     * 按围栏ID查询违规记录
     *
     * @param geofenceId 围栏ID
     * @return 违规记录列表
     */
    List<GeofenceViolation> findByGeofenceId(GeofenceId geofenceId);

    /**
     * 按围栏ID和状态查询违规记录
     *
     * @param geofenceId 围栏ID
     * @param status 违规状态
     * @return 违规记录列表
     */
    List<GeofenceViolation> findByGeofenceIdAndStatus(GeofenceId geofenceId, GeofenceViolation.ViolationStatus status);

    /**
     * 按牲畜ID查询违规记录
     *
     * @param livestockId 牲畜ID
     * @return 违规记录列表
     */
    List<GeofenceViolation> findByLivestockId(LivestockId livestockId);

    /**
     * 按牲畜ID和状态查询违规记录
     *
     * @param livestockId 牲畜ID
     * @param status 违规状态
     * @return 违规记录列表
     */
    List<GeofenceViolation> findByLivestockIdAndStatus(LivestockId livestockId, GeofenceViolation.ViolationStatus status);

    /**
     * 检查违规记录是否存在
     *
     * @param id 违规ID
     * @return 是否存在
     */
    boolean existsById(ViolationId id);

    /**
     * 统计围栏的活跃违规数量
     *
     * @param geofenceId 围栏ID
     * @return 活跃违规数量
     */
    long countByGeofenceIdAndStatus(GeofenceId geofenceId, GeofenceViolation.ViolationStatus status);
}
