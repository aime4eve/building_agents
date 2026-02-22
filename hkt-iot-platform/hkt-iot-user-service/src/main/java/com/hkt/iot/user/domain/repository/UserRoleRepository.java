package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * 用户角色关联仓储接口
 * 基于DDD设计，提供用户角色关联实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface UserRoleRepository extends BaseRepository<UserRole, Long> {

    /**
     * 根据用户ID查找角色
     *
     * @param userId 用户ID
     * @return 用户角色列表
     */
    List<UserRole> findByUserId(Long userId);

    /**
     * 根据角色ID查找用户
     *
     * @param roleId 角色ID
     * @return 用户角色列表
     */
    List<UserRole> findByRoleId(Long roleId);

    /**
     * 根据租户ID和用户ID查找
     *
     * @param tenantId 租户ID
     * @param userId   用户ID
     * @return 用户角色列表
     */
    List<UserRole> findByTenantIdAndUserId(Long tenantId, Long userId);

    /**
     * 根据用户ID和角色ID查找
     *
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 用户角色
     */
    Optional<UserRole> findByUserIdAndRoleId(Long userId, Long roleId);

    /**
     * 统计用户的角色数量
     *
     * @param userId 用户ID
     * @return 角色数量
     */
    long countByUserId(Long userId);

    /**
     * 删除用户的所有角色
     *
     * @param userId 用户ID
     */
    void deleteByUserId(Long userId);

    /**
     * 查找过期的用户角色
     *
     * @return 过期的用户角色列表
     */
    List<UserRole> findExpired();
}
