package com.hkt.iot.rule.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.rule.domain.model.RuleConditionEntity;

import java.util.List;

/**
 * 规则条件仓储接口
 * 基于DDD设计，提供规则条件实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface RuleConditionEntityRepository extends BaseRepository<RuleConditionEntity, Long> {

    /**
     * 根据规则ID查找条件
     *
     * @param ruleId 规则ID
     * @return 条件列表（按顺序排序）
     */
    List<RuleConditionEntity> findByRuleIdOrderByConditionOrderAsc(Long ruleId);

    /**
     * 根据租户ID和规则ID查找
     *
     * @param tenantId 租户ID
     * @param ruleId   规则ID
     * @return 条件列表
     */
    List<RuleConditionEntity> findByTenantIdAndRuleId(Long tenantId, Long ruleId);

    /**
     * 根据设备ID查找关联条件
     *
     * @param deviceId 设备ID
     * @return 条件列表
     */
    List<RuleConditionEntity> findByDeviceId(Long deviceId);

    /**
     * 统计规则的条件数量
     *
     * @param ruleId 规则ID
     * @return 条件数量
     */
    long countByRuleId(Long ruleId);

    /**
     * 删除规则的所有条件
     *
     * @param ruleId 规则ID
     */
    void deleteByRuleId(Long ruleId);
}
