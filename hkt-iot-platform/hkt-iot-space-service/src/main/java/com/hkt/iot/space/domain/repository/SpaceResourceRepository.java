package com.hkt.iot.space.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.space.domain.model.SpaceResource;

import java.util.List;
import java.util.Optional;

/**
 * 空间资源仓储接口
 * 基于DDD设计，提供空间资源实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface SpaceResourceRepository extends BaseRepository<SpaceResource, Long> {

    /**
     * 根据空间ID查找资源
     *
     * @param spaceId 空间ID
     * @return 资源列表
     */
    List<SpaceResource> findBySpaceId(Long spaceId);

    /**
     * 根据租户ID和资源类型查找
     *
     * @param tenantId     租户ID
     * @param resourceType 资源类型
     * @return 资源列表
     */
    List<SpaceResource> findByTenantIdAndResourceType(Long tenantId, SpaceResource.ResourceType resourceType);

    /**
     * 根据资源ID查找关联空间
     *
     * @param resourceType 资源类型
     * @param resourceId   资源ID
     * @return 资源列表
     */
    List<SpaceResource> findByResourceTypeAndResourceId(SpaceResource.ResourceType resourceType, Long resourceId);

    /**
     * 根据租户ID、空间ID和资源类型查找
     *
     * @param tenantId     租户ID
     * @param spaceId      空间ID
     * @param resourceType 资源类型
     * @return 资源列表
     */
    List<SpaceResource> findByTenantIdAndSpaceIdAndResourceType(Long tenantId, Long spaceId, SpaceResource.ResourceType resourceType);

    /**
     * 查找空间的主关联资源
     *
     * @param spaceId 空间ID
     * @return 主关联资源列表
     */
    List<SpaceResource> findPrimaryBySpaceId(Long spaceId);

    /**
     * 查找有效的资源关联（在有效期内）
     *
     * @param resourceId   资源ID
     * @param resourceType 资源类型
     * @return 有效资源列表
     */
    List<SpaceResource> findValidByResourceIdAndResourceType(Long resourceId, SpaceResource.ResourceType resourceType);

    /**
     * 统计空间下的资源数量
     *
     * @param spaceId 空间ID
     * @return 资源数量
     */
    long countBySpaceId(Long spaceId);

    /**
     * 删除空间的所有资源关联
     *
     * @param spaceId 空间ID
     */
    void deleteBySpaceId(Long spaceId);
}
