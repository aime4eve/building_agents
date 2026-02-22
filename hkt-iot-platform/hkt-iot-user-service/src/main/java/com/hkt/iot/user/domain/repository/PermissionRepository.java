package com.hkt.iot.user.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.user.domain.model.Permission;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓储接口
 * 基于DDD设计，提供权限实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface PermissionRepository extends BaseRepository<Permission, Long> {

    /**
     * 根据权限编码查找
     *
     * @param permissionCode 权限编码
     * @return 权限
     */
    Optional<Permission> findByPermissionCode(String permissionCode);

    /**
     * 根据资源类型查找
     *
     * @param resourceType 资源类型
     * @return 权限列表
     */
    List<Permission> findByResourceType(Permission.ResourceType resourceType);

    /**
     * 根据父权限ID查找子权限
     *
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    List<Permission> findByParentId(Long parentId);

    /**
     * 根据资源类型和操作查找
     *
     * @param resourceType 资源类型
     * @param action       操作
     * @return 权限列表
     */
    List<Permission> findByResourceTypeAndAction(Permission.ResourceType resourceType, Permission.Action action);

    /**
     * 检查权限编码是否存在
     *
     * @param permissionCode 权限编码
     * @return 是否存在
     */
    boolean existsByPermissionCode(String permissionCode);

    /**
     * 统计权限数量
     *
     * @return 权限数量
     */
    long count();
}
