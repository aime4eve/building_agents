package com.hkt.iot.device.domain.repository;

import com.hkt.iot.device.domain.model.DeviceLicense;
import com.hkt.iot.device.domain.model.DeviceLicense.LicenseStatus;
import com.hkt.iot.device.domain.model.DeviceLicense.LicenseType;
import com.hkt.iot.domain.repository.BaseRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 设备License仓储接口
 * 基于DDD设计，提供设备License实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface DeviceLicenseRepository extends BaseRepository<DeviceLicense, Long> {

    /**
     * 根据租户ID和License Key查找
     *
     * @param tenantId   租户ID
     * @param licenseKey License Key
     * @return License
     */
    Optional<DeviceLicense> findByTenantIdAndLicenseKey(Long tenantId, String licenseKey);

    /**
     * 根据License Key查找
     *
     * @param licenseKey License Key
     * @return License
     */
    Optional<DeviceLicense> findByLicenseKey(String licenseKey);

    /**
     * 根据设备ID查找
     *
     * @param deviceId 设备ID
     * @return License列表
     */
    List<DeviceLicense> findByDeviceId(Long deviceId);

    /**
     * 根据租户ID和设备ID查找
     *
     * @param tenantId 租户ID
     * @param deviceId 设备ID
     * @return License列表
     */
    List<DeviceLicense> findByTenantIdAndDeviceId(Long tenantId, Long deviceId);

    /**
     * 根据设备序列号查找
     *
     * @param deviceSn 设备序列号
     * @return License列表
     */
    List<DeviceLicense> findByDeviceSn(String deviceSn);

    /**
     * 根据租户ID查找所有License
     *
     * @param tenantId 租户ID
     * @return License列表
     */
    List<DeviceLicense> findByTenantId(Long tenantId);

    /**
     * 根据License类型查找
     *
     * @param tenantId    租户ID
     * @param licenseType License类型
     * @return License列表
     */
    List<DeviceLicense> findByTenantIdAndLicenseType(Long tenantId, LicenseType licenseType);

    /**
     * 根据License状态查找
     *
     * @param tenantId      租户ID
     * @param licenseStatus License状态
     * @return License列表
     */
    List<DeviceLicense> findByTenantIdAndLicenseStatus(Long tenantId, LicenseStatus licenseStatus);

    /**
     * 查找即将过期的License
     *
     * @param tenantId  租户ID
     * @param beforeDate 过期日期
     * @return License列表
     */
    List<DeviceLicense> findByTenantIdAndEndDateBefore(Long tenantId, LocalDate beforeDate);

    /**
     * 查找已过期的License
     *
     * @param tenantId 租户ID
     * @return License列表
     */
    List<DeviceLicense> findExpiredByTenantId(Long tenantId);

    /**
     * 统计租户下的License数量
     *
     * @param tenantId 租户ID
     * @return License数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查License Key是否存在
     *
     * @param licenseKey License Key
     * @return 是否存在
     */
    boolean existsByLicenseKey(String licenseKey);

    /**
     * 检查租户下License Key是否存在
     *
     * @param tenantId   租户ID
     * @param licenseKey License Key
     * @return 是否存在
     */
    boolean existsByTenantIdAndLicenseKey(Long tenantId, String licenseKey);
}
