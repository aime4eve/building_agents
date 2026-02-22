package com.hkt.iot.rule.domain.service;

import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.model.RuleVersion;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import com.hkt.iot.rule.domain.repository.RuleVersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 规则版本管理服务
 * 负责规则版本的创建、查询和恢复
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
public class RuleVersionService {

    private final RuleVersionRepository versionRepository;
    private final RuleRepository ruleRepository;

    public RuleVersionService(
            RuleVersionRepository versionRepository,
            RuleRepository ruleRepository) {
        this.versionRepository = versionRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * 创建规则版本
     *
     * @param rule             规则
     * @param changeDescription 变更描述
     * @param changeType        变更类型
     * @param createdBy         创建者
     * @return 版本记录
     */
    @Transactional
    public RuleVersion createVersion(
            Rule rule,
            String changeDescription,
            RuleVersion.ChangeType changeType,
            Long createdBy) {
        // 获取下一版本号
        Integer nextVersion = getNextVersionNumber(rule.getId());

        // 将之前的当前版本标记为非当前
        versionRepository.markAllAsNotCurrentByRuleId(rule.getId());

        // 创建新版本
        RuleVersion version = RuleVersion.create(
                rule.getId(),
                rule.getRuleCode(),
                nextVersion,
                rule.getTriggerExpression(),
                rule.getRuleConfig(),
                rule.getTriggerExpression(),
                changeDescription,
                changeType,
                createdBy
        );

        return versionRepository.save(version);
    }

    /**
     * 获取规则的所有版本
     *
     * @param ruleId 规则ID
     * @return 版本列表（按版本号降序）
     */
    @Transactional(readOnly = true)
    public List<RuleVersion> getRuleVersions(Long ruleId) {
        return versionRepository.findByRuleIdOrderByVersionNumberDesc(ruleId);
    }

    /**
     * 获取规则的当前版本
     *
     * @param ruleId 规则ID
     * @return 当前版本
     */
    @Transactional(readOnly = true)
    public Optional<RuleVersion> getCurrentVersion(Long ruleId) {
        return versionRepository.findByRuleIdAndIsCurrentTrue(ruleId);
    }

    /**
     * 获取指定版本
     *
     * @param ruleId       规则ID
     * @param versionNumber 版本号
     * @return 版本记录
     */
    @Transactional(readOnly = true)
    public Optional<RuleVersion> getVersion(Long ruleId, Integer versionNumber) {
        return versionRepository.findByRuleIdAndVersionNumber(ruleId, versionNumber);
    }

    /**
     * 比较两个版本的差异
     *
     * @param ruleId        规则ID
     * @param versionNumber1 版本号1
     * @param versionNumber2 版本号2
     * @return 版本差异
     */
    @Transactional(readOnly = true)
    public VersionDiff compareVersions(Long ruleId, Integer versionNumber1, Integer versionNumber2) {
        Optional<RuleVersion> version1Opt = getVersion(ruleId, versionNumber1);
        Optional<RuleVersion> version2Opt = getVersion(ruleId, versionNumber2);

        if (version1Opt.isEmpty() || version2Opt.isEmpty()) {
            throw new IllegalArgumentException("指定的版本不存在");
        }

        RuleVersion v1 = version1Opt.get();
        RuleVersion v2 = version2Opt.get();

        return new VersionDiff(
                v1.getVersionNumber(),
                v2.getVersionNumber(),
                !v1.getTriggerExpression().equals(v2.getTriggerExpression()),
                v1.getTriggerExpression(),
                v2.getTriggerExpression(),
                v1.getChangeDescription(),
                v2.getChangeDescription()
        );
    }

    /**
     * 恢复到指定版本
     *
     * @param ruleId       规则ID
     * @param versionNumber 目标版本号
     * @param restoredBy   恢复者
     */
    @Transactional
    public void restoreToVersion(Long ruleId, Integer versionNumber, Long restoredBy) {
        // 获取目标版本
        RuleVersion targetVersion = getVersion(ruleId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("版本不存在: " + versionNumber));

        // 获取当前规则
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + ruleId));

        // 保存当前状态为版本
        createVersion(rule, "恢复前备份", RuleVersion.ChangeType.UPDATE, restoredBy);

        // 恢复规则配置
        rule.setTriggerExpression(targetVersion.getTriggerExpression());
        rule.setRuleConfig(targetVersion.getRuleConfig());
        rule.setUpdatedBy(restoredBy);

        ruleRepository.save(rule);

        // 创建恢复版本
        createVersion(rule,
                "恢复到版本 " + versionNumber,
                RuleVersion.ChangeType.UPDATE,
                restoredBy);

        log.info("规则已恢复到版本: ruleId={}, version={}", ruleId, versionNumber);
    }

    /**
     * 获取下一版本号
     */
    private Integer getNextVersionNumber(Long ruleId) {
        return versionRepository.findMaxVersionNumberByRuleId(ruleId)
                .map(max -> max + 1)
                .orElse(1);
    }

    /**
     * 版本差异
     */
    public static class VersionDiff {
        private final Integer version1;
        private final Integer version2;
        private final boolean hasExpressionChanged;
        private final String expression1;
        private final String expression2;
        private final String description1;
        private final String description2;

        public VersionDiff(Integer version1, Integer version2, boolean hasExpressionChanged,
                          String expression1, String expression2,
                          String description1, String description2) {
            this.version1 = version1;
            this.version2 = version2;
            this.hasExpressionChanged = hasExpressionChanged;
            this.expression1 = expression1;
            this.expression2 = expression2;
            this.description1 = description1;
            this.description2 = description2;
        }

        public Integer getVersion1() { return version1; }
        public Integer getVersion2() { return version2; }
        public boolean hasExpressionChanged() { return hasExpressionChanged; }
        public String getExpression1() { return expression1; }
        public String getExpression2() { return expression2; }
        public String getDescription1() { return description1; }
        public String getDescription2() { return description2; }
    }
}
