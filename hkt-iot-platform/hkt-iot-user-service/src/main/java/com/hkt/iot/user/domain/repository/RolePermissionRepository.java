package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.RolePermission;

import java.util.List;
import java.util.Optional;

/**
 * 角色权限关联仓储接口
 * 基于DDD设计，提供角色权限关联实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface RolePermissionRepository extends BaseRepository<RolePermission, Long> {

    /**
     * 根据角色ID查找权限
     *
     * @param roleId 角色ID
     * @return 角色权限列表
     */
    List<RolePermission> findByRoleId(Long roleId);

    /**
     * 根据权限ID查找角色
     *
     * @param permissionId 权限ID
     * @return 角色权限列表
     */
    List<RolePermission> findByPermissionId(Long permissionId);

    /**
     * 根据租户ID和角色ID查找
     *
     * @param tenantId 租户ID
     * @param roleId   角色ID
     * @return 角色权限列表
     */
    List<RolePermission> findByTenantIdAndRoleId(Long tenantId, Long roleId);

    /**
     * 根据角色ID和权限ID查找
     *
     * @param roleId       角色ID
     * @param permissionId 权限ID
     * @return 角色权限
     */
    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);

    /**
     * 统计角色的权限数量
     *
     * @param roleId 角色ID
     * @return 权限数量
     */
    long countByRoleId(Long roleId);

    /**
     * 删除角色的所有权限
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(Long roleId);

    /**
     * 删除权限的所有角色关联
     *
     * @param permissionId 权限ID
     */
    void deleteByPermissionId(Long permissionId);
}
