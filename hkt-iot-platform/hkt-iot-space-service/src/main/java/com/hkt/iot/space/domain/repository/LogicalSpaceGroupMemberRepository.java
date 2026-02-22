package com.hkt.iot.space.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.space.domain.model.LogicalSpaceGroupMember;

import java.util.List;

/**
 * 逻辑空间组成员仓储接口
 * 基于DDD设计，提供逻辑空间组成员实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface LogicalSpaceGroupMemberRepository extends BaseRepository<LogicalSpaceGroupMember, Long> {

    /**
     * 根据分组ID查找成员
     *
     * @param groupId 分组ID
     * @return 成员列表
     */
    List<LogicalSpaceGroupMember> findByGroupId(Long groupId);

    /**
     * 根据空间ID查找所属分组
     *
     * @param spaceId 空间ID
     * @return 成员列表
     */
    List<LogicalSpaceGroupMember> findBySpaceId(Long spaceId);

    /**
     * 根据租户ID查找所有成员
     *
     * @param tenantId 租户ID
     * @return 成员列表
     */
    List<LogicalSpaceGroupMember> findByTenantId(Long tenantId);

    /**
     * 统计分组成员数量
     *
     * @param groupId 分组ID
     * @return 成员数量
     */
    long countByGroupId(Long groupId);

    /**
     * 删除分组的所有成员
     *
     * @param groupId 分组ID
     */
    void deleteByGroupId(Long groupId);

    /**
     * 删除空间的所有分组关联
     *
     * @param spaceId 空间ID
     */
    void deleteBySpaceId(Long spaceId);
}
