package com.hkt.iot.rule.domain.repository;

import com.hkt.iot.domain.repository.Repository;
import com.hkt.iot.rule.domain.model.RuleVersion;

import java.util.List;
import java.util.Optional;

/**
 * 规则版本仓储接口
 *
 * @author HKT IoT Team
 */
public interface RuleVersionRepository extends Repository<RuleVersion, Long> {

    /**
     * 根据规则ID查找所有版本
     */
    List<RuleVersion> findByRuleIdOrderByVersionNumberDesc(Long ruleId);

    /**
     * 根据规则ID查找当前版本
     */
    Optional<RuleVersion> findByRuleIdAndIsCurrentTrue(Long ruleId);

    /**
     * 根据规则ID和版本号查找
     */
    Optional<RuleVersion> findByRuleIdAndVersionNumber(Long ruleId, Integer versionNumber);

    /**
     * 将规则的所有版本标记为非当前
     */
    void markAllAsNotCurrentByRuleId(Long ruleId);

    /**
     * 统计规则的版本数量
     */
    long countByRuleId(Long ruleId);

    /**
     * 获取规则的最新版本号
     */
    Optional<Integer> findMaxVersionNumberByRuleId(Long ruleId);
}
