package com.hkt.iot.device.domain.repository;

import com.hkt.iot.device.domain.model.DeviceThingModel;
import com.hkt.iot.domain.repository.BaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * 设备物模型仓储接口
 * 基于DDD设计，提供设备物模型实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface DeviceThingModelRepository extends BaseRepository<DeviceThingModel, Long> {

    /**
     * 根据租户ID和设备型号查找
     *
     * @param tenantId    租户ID
     * @param deviceModel 设备型号
     * @return 物模型
     */
    Optional<DeviceThingModel> findByTenantIdAndDeviceModel(Long tenantId, String deviceModel);

    /**
     * 根据设备型号查找
     *
     * @param deviceModel 设备型号
     * @return 物模型
     */
    Optional<DeviceThingModel> findByDeviceModel(String deviceModel);

    /**
     * 根据租户ID查找所有物模型
     *
     * @param tenantId 租户ID
     * @return 物模型列表
     */
    List<DeviceThingModel> findByTenantId(Long tenantId);

    /**
     * 根据设备分类查找
     *
     * @param category 设备分类
     * @return 物模型列表
     */
    List<DeviceThingModel> findByCategory(String category);

    /**
     * 根据制造商查找
     *
     * @param manufacturer 制造商
     * @return 物模型列表
     */
    List<DeviceThingModel> findByManufacturer(String manufacturer);

    /**
     * 根据状态查找
     *
     * @param status 状态
     * @return 物模型列表
     */
    List<DeviceThingModel> findByStatus(Integer status);

    /**
     * 统计租户下的物模型数量
     *
     * @param tenantId 租户ID
     * @return 物模型数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查租户下设备型号是否存在
     *
     * @param tenantId    租户ID
     * @param deviceModel 设备型号
     * @return 是否存在
     */
    boolean existsByTenantIdAndDeviceModel(Long tenantId, String deviceModel);
}
