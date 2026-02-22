package com.hkt.iot.rule.domain.repository;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.model.Rule.RuleType;
import com.hkt.iot.rule.domain.model.Rule.RuleStatus;

import java.util.List;
import java.util.Optional;

/**
 * 规则仓储接口
 * 基于DDD设计，提供规则聚合根的持久化操作
 *
 * @author HKT IoT Team
 */
public interface RuleRepository extends OptimisticLockRepository<Rule, Long> {

    /**
     * 根据租户ID和规则编码查找
     *
     * @param tenantId  租户ID
     * @param ruleCode 规则编码
     * @return 规则
     */
    Optional<Rule> findByTenantIdAndRuleCode(Long tenantId, String ruleCode);

    /**
     * 根据租户ID和规则类型查找
     *
     * @param tenantId  租户ID
     * @param ruleType  规则类型
     * @return 规则列表
     */
    List<Rule> findByTenantIdAndRuleType(Long tenantId, RuleType ruleType);

    /**
     * 根据租户ID和规则状态查找
     *
     * @param tenantId    租户ID
     * @param ruleStatus  规则状态
     * @return 规则列表
     */
    List<Rule> findByTenantIdAndRuleStatus(Long tenantId, RuleStatus ruleStatus);

    /**
     * 根据租户ID查找已启用的规则
     *
     * @param tenantId 租户ID
     * @return 已启用的规则列表
     */
    List<Rule> findEnabledByTenantId(Long tenantId);

    /**
     * 根据空间ID查找规则
     *
     * @param spaceId 空间ID
     * @return 规则列表
     */
    List<Rule> findBySpaceId(Long spaceId);

    /**
     * 根据设备ID查找关联的规则
     *
     * @param deviceId 设备ID
     * @return 规则列表
     */
    List<Rule> findByDeviceId(Long deviceId);

    /**
     * 统计租户下的规则数量
     *
     * @param tenantId 租户ID
     * @return 规则数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 检查规则编码是否存在
     *
     * @param tenantId  租户ID
     * @param ruleCode 规则编码
     * @return 是否存在
     */
    boolean existsByTenantIdAndRuleCode(Long tenantId, String ruleCode);

    /**
     * 根据优先级排序查找规则
     *
     * @param tenantId 租户ID
     * @return 规则列表（按优先级降序）
     */
    List<Rule> findByTenantIdOrderByPriorityDesc(Long tenantId);

    /**
     * 查找定时规则
     *
     * @param tenantId 租户ID
     * @return 定时规则列表
     */
    List<Rule> findScheduledRulesByTenantId(Long tenantId);
}
