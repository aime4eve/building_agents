package com.hkt.iot.space.domain.repository;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.space.domain.model.LogicalSpaceGroup;

import java.util.List;
import java.util.Optional;

/**
 * 逻辑空间分组仓储接口
 * 基于DDD设计，提供逻辑空间分组聚合根的持久化操作
 *
 * @author HKT IoT Team
 */
public interface LogicalSpaceGroupRepository extends OptimisticLockRepository<LogicalSpaceGroup, Long> {

    /**
     * 根据租户ID和分组编码查找
     *
     * @param tenantId  租户ID
     * @param groupCode 分组编码
     * @return 分组
     */
    Optional<LogicalSpaceGroup> findByTenantIdAndGroupCode(Long tenantId, String groupCode);

    /**
     * 根据租户ID和分组类型查找
     *
     * @param tenantId  租户ID
     * @param groupType 分组类型
     * @return 分组列表
     */
    List<LogicalSpaceGroup> findByTenantIdAndGroupType(Long tenantId, LogicalSpaceGroup.GroupType groupType);

    /**
     * 根据租户ID查找已启用的分组
     *
     * @param tenantId 租户ID
     * @return 已启用的分组列表
     */
    List<LogicalSpaceGroup> findActiveByTenantId(Long tenantId);

    /**
     * 根据租户ID查找所有分组
     *
     * @param tenantId 租户ID
     * @return 分组列表
     */
    List<LogicalSpaceGroup> findByTenantId(Long tenantId);

    /**
     * 检查分组编码是否存在
     *
     * @param tenantId  租户ID
     * @param groupCode 分组编码
     * @return 是否存在
     */
    boolean existsByTenantIdAndGroupCode(Long tenantId, String groupCode);

    /**
     * 统计租户下的分组数量
     *
     * @param tenantId 租户ID
     * @return 分组数量
     */
    long countByTenantId(Long tenantId);
}
