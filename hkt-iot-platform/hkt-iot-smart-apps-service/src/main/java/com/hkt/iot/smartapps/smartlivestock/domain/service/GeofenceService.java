package com.hkt.iot.smartapps.smartlivestock.domain.service;

import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceViolation;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockLocation;
import com.hkt.iot.smartapps.smartlivestock.domain.model.Location;
import com.hkt.iot.smartapps.smartlivestock.domain.model.ViolationId;

import java.util.List;
import java.util.Map;

/**
 * 电子围栏领域服务接口
 *
 * 职责：处理电子围栏相关的业务逻辑
 */
public interface GeofenceService {

    /**
     * 检查越界
     *
     * @param geofenceId 围栏ID
     * @param livestockId 牲畜ID
     * @param location 位置
     * @return 违规记录，如果未越界返回null
     */
    GeofenceViolation checkViolation(GeofenceId geofenceId, LivestockId livestockId, Location location);

    /**
     * 批量检查越界
     *
     * @param geofenceId 围栏ID
     * @param locations 牲畜位置列表
     * @return 违规记录列表（牲畜ID -> 违规记录）
     */
    Map<LivestockId, GeofenceViolation> batchCheckViolation(GeofenceId geofenceId, List<LivestockLocation> locations);

    /**
     * 获取活跃违规
     *
     * @param geofenceId 围栏ID
     * @return 活跃违规列表
     */
    List<GeofenceViolation> getActiveViolations(GeofenceId geofenceId);

    /**
     * 解决违规
     *
     * @param violationId 违规ID
     * @return 是否解决成功
     */
    boolean resolveViolation(ViolationId violationId);

    /**
     * 解决违规（带备注）
     *
     * @param violationId 违规ID
     * @param notes 备注
     * @return 是否解决成功
     */
    boolean resolveViolation(ViolationId violationId, String notes);

    /**
     * 获取牲畜的所有活跃违规
     *
     * @param livestockId 牲畜ID
     * @return 活跃违规列表
     */
    List<GeofenceViolation> getActiveViolationsByLivestock(LivestockId livestockId);

    /**
     * 检查位置是否在围栏内
     *
     * @param geofenceId 围栏ID
     * @param location 位置
     * @return 是否在围栏内
     */
    boolean isInsideGeofence(GeofenceId geofenceId, Location location);
}
