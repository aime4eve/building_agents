package com.hkt.iot.rule.domain.repository;

import com.hkt.iot.domain.repository.OptimisticLockRepository;
import com.hkt.iot.rule.domain.model.RuleSet;
import com.hkt.iot.rule.domain.model.RuleSet.RuleSetStatus;

import java.util.List;
import java.util.Optional;

/**
 * 规则集仓储接口
 *
 * @author HKT IoT Team
 */
public interface RuleSetRepository extends OptimisticLockRepository<RuleSet, Long> {

    /**
     * 根据租户ID和规则集编码查找
     */
    Optional<RuleSet> findByTenantIdAndSetCode(Long tenantId, String setCode);

    /**
     * 根据租户ID查找所有规则集
     */
    List<RuleSet> findByTenantIdOrderByPriorityDesc(Long tenantId);

    /**
     * 根据租户ID和状态查找规则集
     */
    List<RuleSet> findByTenantIdAndSetStatus(Long tenantId, RuleSetStatus status);

    /**
     * 根据空间ID查找规则集
     */
    List<RuleSet> findBySpaceId(Long spaceId);

    /**
     * 根据租户ID和类别查找规则集
     */
    List<RuleSet> findByTenantIdAndSetCategory(Long tenantId, String setCategory);

    /**
     * 检查规则集编码是否存在
     */
    boolean existsByTenantIdAndSetCode(Long tenantId, String setCode);

    /**
     * 统计租户下的规则集数量
     */
    long countByTenantId(Long tenantId);

    /**
     * 查找包含指定规则的规则集
     */
    List<RuleSet> findByRuleIdsContaining(Long ruleId);
}
