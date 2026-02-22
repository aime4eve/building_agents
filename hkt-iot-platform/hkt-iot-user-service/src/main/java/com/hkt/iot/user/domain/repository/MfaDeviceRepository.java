package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.MfaDevice;

import java.util.List;
import java.util.Optional;

/**
 * MFA设备仓储接口
 * 基于DDD设计，提供MFA设备实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface MfaDeviceRepository extends BaseRepository<MfaDevice, Long> {

    /**
     * 根据用户ID查找MFA设备
     *
     * @param userId 用户ID
     * @return MFA设备列表
     */
    List<MfaDevice> findByUserId(Long userId);

    /**
     * 根据用户ID和设备标识符查找
     *
     * @param userId           用户ID
     * @param deviceIdentifier 设备标识符
     * @return MFA设备
     */
    Optional<MfaDevice> findByUserIdAndDeviceIdentifier(Long userId, String deviceIdentifier);

    /**
     * 根据用户ID查找已验证的设备
     *
     * @param userId 用户ID
     * @return 已验证的设备列表
     */
    List<MfaDevice> findVerifiedByUserId(Long userId);

    /**
     * 根据用户ID查找活跃设备
     *
     * @param userId 用户ID
     * @return 活跃设备列表
     */
    List<MfaDevice> findActiveByUserId(Long userId);

    /**
     * 统计用户的设备数量
     *
     * @param userId 用户ID
     * @return 设备数量
     */
    long countByUserId(Long userId);

    /**
     * 删除用户的所有设备
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
}
