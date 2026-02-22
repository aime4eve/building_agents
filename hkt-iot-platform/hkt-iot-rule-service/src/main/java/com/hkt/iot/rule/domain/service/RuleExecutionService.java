package com.hkt.iot.rule.domain.service;

import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.model.RuleContext;
import com.hkt.iot.rule.domain.model.RuleExecutionResult;
import com.hkt.iot.rule.domain.repository.RuleRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则执行领域服务
 * 负责规则的评估和执行
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleExecutionService {

    private final RuleRepository ruleRepository;

    // 编译后的规则缓存
    private final Map<Long, CompiledRuleInfo> ruleCache = new ConcurrentHashMap<>();

    public RuleExecutionService(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    /**
     * 执行规则
     */
    public RuleExecutionResult execute(Long ruleId, RuleContext context) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        return execute(rule, context);
    }

    /**
     * 执行规则
     */
    public RuleExecutionResult execute(Rule rule, RuleContext context) {
        // 检查规则是否启用且生效
        if (!rule.getIsEnabled() || !rule.getRuleStatus().equals(Rule.RuleStatus.ACTIVE)) {
            return RuleExecutionResult.skipped("Rule is not active or enabled");
        }

        // 检查生效时间
        LocalDateTime now = LocalDateTime.now();
        if (rule.getEffectiveTime() != null && now.isBefore(rule.getEffectiveTime())) {
            return RuleExecutionResult.skipped("Rule is not yet effective");
        }
        if (rule.getExpireTime() != null && !now.isBefore(rule.getExpireTime())) {
            return RuleExecutionResult.skipped("Rule has expired");
        }

        // 获取规则配置并执行
        try {
            // 从rule_config中获取条件和动作配置
            Map<String, Object> ruleConfig = parseRuleConfig(rule);

            // 评估条件
            boolean conditionMatched = evaluateCondition(rule, context, ruleConfig);

            if (!conditionMatched) {
                return RuleExecutionResult.notMatched();
            }

            // 执行动作
            return executeActions(rule, context, ruleConfig);

        } catch (Exception e) {
            return RuleExecutionResult.failed("Rule execution failed: " + e.getMessage());
        }
    }

    /**
     * 批量执行规则（给定租户的所有激活规则）
     */
    public List<RuleExecutionResult> executeActiveRules(Long tenantId, RuleContext context) {
        List<Rule> activeRules = ruleRepository.findEnabledByTenantId(tenantId);

        return activeRules.stream()
                .map(rule -> execute(rule, context))
                .toList();
    }

    /**
     * 测试规则（不更新统计）
     */
    public RuleExecutionResult test(Long ruleId, RuleContext context) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        return test(rule, context);
    }

    /**
     * 测试规则
     */
    public RuleExecutionResult test(Rule rule, RuleContext context) {
        try {
            Map<String, Object> ruleConfig = parseRuleConfig(rule);
            boolean conditionMatched = evaluateCondition(rule, context, ruleConfig);

            if (!conditionMatched) {
                return RuleExecutionResult.notMatched();
            }

            return RuleExecutionResult.matched();

        } catch (Exception e) {
            return RuleExecutionResult.failed("Rule test failed: " + e.getMessage());
        }
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(Rule rule, RuleContext context, Map<String, Object> ruleConfig) {
        // 获取条件表达式
        String conditionExpression = extractConditionExpression(rule, ruleConfig);

        if (conditionExpression == null || conditionExpression.isEmpty()) {
            return true;
        }

        // 使用DSL解析器评估条件
        com.hkt.iot.rule.engine.RuleEngine engine = new com.hkt.iot.rule.engine.RuleEngine();
        return engine.evaluate(conditionExpression, context.toMap());
    }

    /**
     * 执行动作
     */
    private RuleExecutionResult executeActions(Rule rule, RuleContext context, Map<String, Object> ruleConfig) {
        // Phase 1简化实现：只返回成功，实际应该调用动作执行器
        rule.updateExecutionStats(true);
        return RuleExecutionResult.success(null);
    }

    /**
     * 解析规则配置
     */
    private Map<String, Object> parseRuleConfig(Rule rule) {
        // Phase 1简化实现：从rule_config字段解析
        // 实际应该从JSON字段解析
        return new ConcurrentHashMap<>();
    }

    /**
     * 提取条件表达式
     */
    private String extractConditionExpression(Rule rule, Map<String, Object> ruleConfig) {
        // 优先从triggerExpression获取
        if (rule.getTriggerExpression() != null && !rule.getTriggerExpression().isEmpty()) {
            return rule.getTriggerExpression();
        }

        // 从rule_config中获取
        Object expression = ruleConfig.get("expression");
        if (expression != null) {
            return expression.toString();
        }

        return null;
    }

    /**
     * 清除规则缓存
     */
    public void clearCache() {
        ruleCache.clear();
    }

    /**
     * 清除指定规则的缓存
     */
    public void clearCache(Long ruleId) {
        ruleCache.remove(ruleId);
    }

    /**
     * 编译规则信息
     */
    private static class CompiledRuleInfo {
        private final String expression;
        private final com.hkt.iot.rule.engine.CompiledRule compiledRule;
        private final LocalDateTime compiledAt;

        public CompiledRuleInfo(String expression, com.hkt.iot.rule.engine.CompiledRule compiledRule) {
            this.expression = expression;
            this.compiledRule = compiledRule;
            this.compiledAt = LocalDateTime.now();
        }
    }
}
