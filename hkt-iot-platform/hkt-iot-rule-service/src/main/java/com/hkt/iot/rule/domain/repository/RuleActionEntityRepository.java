package com.hkt.iot.rule.domain.repository;

import com.hkt.iot.domain.repository.BaseRepository;
import com.hkt.iot.rule.domain.model.RuleActionEntity;

import java.util.List;

/**
 * 规则动作仓储接口
 * 基于DDD设计，提供规则动作实体的持久化操作
 *
 * @author HKT IoT Team
 */
public interface RuleActionEntityRepository extends BaseRepository<RuleActionEntity, Long> {

    /**
     * 根据规则ID查找动作
     *
     * @param ruleId 规则ID
     * @return 动作列表（按顺序排序）
     */
    List<RuleActionEntity> findByRuleIdOrderByActionOrderAsc(Long ruleId);

    /**
     * 根据租户ID和规则ID查找
     *
     * @param tenantId 租户ID
     * @param ruleId   规则ID
     * @return 动作列表
     */
    List<RuleActionEntity> findByTenantIdAndRuleId(Long tenantId, Long ruleId);

    /**
     * 根据目标设备ID查找关联动作
     *
     * @param targetDeviceId 目标设备ID
     * @return 动作列表
     */
    List<RuleActionEntity> findByTargetDeviceId(Long targetDeviceId);

    /**
     * 根据动作类型查找
     *
     * @param actionType 动作类型
     * @return 动作列表
     */
    List<RuleActionEntity> findByActionType(RuleActionEntity.ActionType actionType);

    /**
     * 统计规则的动作数量
     *
     * @param ruleId 规则ID
     * @return 动作数量
     */
    long countByRuleId(Long ruleId);

    /**
     * 删除规则的所有动作
     *
     * @param ruleId 规则ID
     */
    void deleteByRuleId(Long ruleId);
}
