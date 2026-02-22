package com.hkt.iot.device.domain.repository;

import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.model.Device.DeviceStatus;
import com.hkt.iot.domain.repository.OptimisticLockRepository;

import java.util.List;
import java.util.Optional;

/**
 * 设备仓储接口
 * 基于DDD设计，提供设备聚合根的持久化操作
 *
 * @author HKT IoT Team
 */
public interface DeviceRepository extends OptimisticLockRepository<Device, Long> {

    /**
     * 根据设备序列号查找
     *
     * @param tenantId 租户ID
     * @param deviceSn 设备序列号
     * @return 设备
     */
    Optional<Device> findByTenantIdAndDeviceSn(Long tenantId, String deviceSn);

    /**
     * 根据设备编码查找
     *
     * @param tenantId  租户ID
     * @param deviceCode 设备编码
     * @return 设备
     */
    Optional<Device> findByTenantIdAndDeviceCode(Long tenantId, String deviceCode);

    /**
     * 根据设备类型查找
     *
     * @param tenantId    租户ID
     * @param deviceType  设备类型
     * @return 设备列表
     */
    List<Device> findByTenantIdAndDeviceType(Long tenantId, String deviceType);

    /**
     * 根据设备状态查找
     *
     * @param tenantId      租户ID
     * @param deviceStatus  设备状态
     * @return 设备列表
     */
    List<Device> findByTenantIdAndDeviceStatus(Long tenantId, DeviceStatus deviceStatus);

    /**
     * 根据在线状态查找
     *
     * @param tenantId      租户ID
     * @param onlineStatus  在线状态
     * @return 设备列表
     */
    List<Device> findByTenantIdAndOnlineStatus(Long tenantId, Boolean onlineStatus);

    /**
     * 根据空间ID查找
     *
     * @param spaceId 空间ID
     * @return 设备列表
     */
    List<Device> findBySpaceId(Long spaceId);

    /**
     * 根据父设备ID查找子设备
     *
     * @param parentDeviceId 父设备ID
     * @return 子设备列表
     */
    List<Device> findByParentDeviceId(Long parentDeviceId);

    /**
     * 统计租户下的设备数量
     *
     * @param tenantId 租户ID
     * @return 设备数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查设备序列号是否存在
     *
     * @param tenantId  租户ID
     * @param deviceSn  设备序列号
     * @return 是否存在
     */
    boolean existsByTenantIdAndDeviceSn(Long tenantId, String deviceSn);
}
