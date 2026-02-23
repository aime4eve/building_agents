package com.hkt.iot.space.domain.repository;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.space.domain.model.Space;
import com.hkt.iot.space.domain.model.Space.SpaceType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 空间仓储接口
 * 基于DDD设计，提供空间聚合根的持久化操作
 *
 * @author HKT IoT Team
 */
public interface SpaceRepository extends OptimisticLockRepository<Space, Long> {

    /**
     * 根据空间编码查找
     *
     * @param tenantId  租户ID
     * @param spaceCode 空间编码
     * @return 空间
     */
    Optional<Space> findByTenantIdAndSpaceCode(Long tenantId, String spaceCode);

    /**
     * 根据空间类型查找
     *
     * @param tenantId  租户ID
     * @param spaceType 空间类型
     * @return 空间列表
     */
    List<Space> findByTenantIdAndSpaceType(Long tenantId, SpaceType spaceType);

    /**
     * 根据父空间ID查找子空间
     *
     * @param parentSpaceId 父空间ID
     * @return 子空间列表
     */
    List<Space> findByParentSpaceId(Long parentSpaceId);

    /**
     * 根据空间路径前缀查询
     *
     * @param tenantId   租户ID
     * @param spacePath  空间路径前缀
     * @return 空间列表
     */
    List<Space> findByTenantIdAndSpacePathStartingWith(Long tenantId, String spacePath);

    /**
     * 根据根空间ID查找所有子空间
     *
     * @param rootSpaceId 根空间ID
     * @return 空间列表
     */
    List<Space> findByRootSpaceId(Long rootSpaceId);

    /**
     * 统计租户下的空间数量
     *
     * @param tenantId 租户ID
     * @return 空间数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查空间编码是否存在
     *
     * @param tenantId  租户ID
     * @param spaceCode 空间编码
     * @return 是否存在
     */
    boolean existsByTenantIdAndSpaceCode(Long tenantId, String spaceCode);

    /**
     * 根据空间层级查找
     *
     * @param tenantId   租户ID
     * @param spaceLevel 空间层级
     * @return 空间列表
     */
    List<Space> findByTenantIdAndSpaceLevel(Long tenantId, Integer spaceLevel);

    /**
     * 统计各类型空间数量
     *
     * @param tenantId 租户ID
     * @return 按类型分组的数量统计
     */
    Map<String, Long> countGroupByType(Long tenantId);

    /**
     * 统计各状态空间数量
     *
     * @param tenantId 租户ID
     * @return 按状态分组的数量统计
     */
    Map<String, Long> countGroupByStatus(Long tenantId);

    /**
     * 统计各层级空间数量
     *
     * @param tenantId 租户ID
     * @return 按层级分组的数量统计
     */
    Map<Integer, Long> countGroupByLevel(Long tenantId);
}
