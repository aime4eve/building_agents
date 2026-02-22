package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.user.domain.model.Role;

import java.util.List;
import java.util.Optional;

/**
 * 角色仓储接口
 * 基于DDD设计，提供角色聚合根的持久化操作
 *
 * @author HKT IoT Team
 */
public interface RoleRepository extends OptimisticLockRepository<Role, Long> {

    /**
     * 根据租户ID和角色编码查找
     *
     * @param tenantId 租户ID
     * @param roleCode 角色编码
     * @return 角色
     */
    Optional<Role> findByTenantIdAndRoleCode(Long tenantId, String roleCode);

    /**
     * 根据租户ID查找所有角色
     *
     * @param tenantId 租户ID
     * @return 角色列表
     */
    List<Role> findByTenantId(Long tenantId);

    /**
     * 根据租户ID和角色类型查找
     *
     * @param tenantId 租户ID
     * @param roleType 角色类型
     * @return 角色列表
     */
    List<Role> findByTenantIdAndRoleType(Long tenantId, Role.RoleType roleType);

    /**
     * 根据租户ID查找已启用的角色
     *
     * @param tenantId 租户ID
     * @return 已启用的角色列表
     */
    List<Role> findActiveByTenantId(Long tenantId);

    /**
     * 根据租户ID查找默认角色
     *
     * @param tenantId 租户ID
     * @return 默认角色列表
     */
    List<Role> findDefaultByTenantId(Long tenantId);

    /**
     * 检查角色编码是否存在
     *
     * @param tenantId 租户ID
     * @param roleCode 角色编码
     * @return 是否存在
     */
    boolean existsByTenantIdAndRoleCode(Long tenantId, String roleCode);

    /**
     * 统计租户下的角色数量
     *
     * @param tenantId 租户ID
     * @return 角色数量
     */
    long countByTenantId(Long tenantId);
}
