package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.MfaConfig;

import java.util.List;
import java.util.Optional;

/**
 * MFA配置仓储接口
 * 基于DDD设计，提供MFA配置实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface MfaConfigRepository extends BaseRepository<MfaConfig, Long> {

    /**
     * 根据用户ID查找MFA配置
     *
     * @param userId 用户ID
     * @return MFA配置列表
     */
    List<MfaConfig> findByUserId(Long userId);

    /**
     * 根据用户ID和MFA类型查找
     *
     * @param userId 用户ID
     * @param mfaType MFA类型
     * @return MFA配置
     */
    Optional<MfaConfig> findByUserIdAndMfaType(Long userId, MfaConfig.MfaType mfaType);

    /**
     * 根据用户ID查找已启用的MFA配置
     *
     * @param userId 用户ID
     * @return 已启用的MFA配置列表
     */
    List<MfaConfig> findEnabledByUserId(Long userId);

    /**
     * 根据用户ID查找主要MFA方式
     *
     * @param userId 用户ID
     * @return 主要MFA配置
     */
    Optional<MfaConfig> findPrimaryByUserId(Long userId);

    /**
     * 检查用户是否启用MFA
     *
     * @param userId 用户ID
     * @return 是否启用
     */
    boolean existsByUserIdAndIsEnabledTrue(Long userId);

    /**
     * 删除用户的所有MFA配置
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);
}
