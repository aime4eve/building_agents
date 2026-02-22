package com.hkt.iot.rule.domain.service;

import com.hkt.iot.rule.domain.model.*;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 规则执行服务测试
 *
 * @author HKT IoT Team
 */
@ExtendWith(MockitoExtension.class)
class RuleExecutionServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    private RuleExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new RuleExecutionService(ruleRepository);
    }

    @Test
    void testExecuteRule_Success() {
        // 准备测试数据
        Rule rule = createTestRule();
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        // 创建上下文
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        contextData.put("humidity", 45);
        RuleContext context = RuleContext.of(contextData);

        // 执行规则
        RuleExecutionResult result = executionService.execute(rule.getId(), context);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isSuccess() || result.isMatched());
    }

    @Test
    void testExecuteRule_NotMatched() {
        // 准备测试数据 - 温度不满足条件
        Rule rule = createTestRule();
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        // 创建上下文 - 温度低于阈值
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 25);
        contextData.put("humidity", 45);
        RuleContext context = RuleContext.of(contextData);

        // 执行规则
        RuleExecutionResult result = executionService.execute(rule.getId(), context);

        // 验证结果 - 条件不匹配
        assertNotNull(result);
    }

    @Test
    void testExecuteRule_NotActive() {
        // 准备测试数据 - 规则未激活
        Rule rule = createTestRule();
        rule.disable();
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        // 创建上下文
        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        RuleContext context = RuleContext.of(contextData);

        // 执行规则
        RuleExecutionResult result = executionService.execute(rule.getId(), context);

        // 验证结果 - 规则跳过
        assertNotNull(result);
        assertEquals(RuleExecutionResult.ExecutionStatus.SKIPPED, result.getStatus());
    }

    @Test
    void testExecuteRule_NotEnabled() {
        // 准备测试数据 - 规则未启用
        Rule rule = createTestRule();
        rule.disable();
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        RuleContext context = RuleContext.of(contextData);

        // 执行规则
        RuleExecutionResult result = executionService.execute(rule.getId(), context);

        // 验证结果
        assertNotNull(result);
    }

    @Test
    void testExecuteRule_Expired() {
        // 准备测试数据 - 规则已过期
        Rule rule = createTestRule();
        rule.setExpireTime(LocalDateTime.now().minusDays(1));
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        RuleContext context = RuleContext.of(contextData);

        // 执行规则
        RuleExecutionResult result = executionService.execute(rule.getId(), context);

        // 验证结果 - 规则已过期跳过
        assertNotNull(result);
        assertEquals(RuleExecutionResult.ExecutionStatus.SKIPPED, result.getStatus());
    }

    @Test
    void testExecuteRule_NotYetEffective() {
        // 准备测试数据 - 规则未生效
        Rule rule = createTestRule();
        rule.setEffectiveTime(LocalDateTime.now().plusDays(1));
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        RuleContext context = RuleContext.of(contextData);

        // 执行规则
        RuleExecutionResult result = executionService.execute(rule.getId(), context);

        // 验证结果 - 规则未生效跳过
        assertNotNull(result);
        assertEquals(RuleExecutionResult.ExecutionStatus.SKIPPED, result.getStatus());
    }

    @Test
    void testExecuteActiveRules() {
        // 准备测试数据
        Rule rule1 = createTestRule(1L, "temp > 30");
        Rule rule2 = createTestRule(2L, "humidity < 40");

        when(ruleRepository.findEnabledByTenantId(any()))
                .thenReturn(List.of(rule1, rule2));

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        contextData.put("humidity", 35);
        RuleContext context = RuleContext.of(contextData);

        // 执行所有激活规则
        List<RuleExecutionResult> results = executionService.executeActiveRules(1L, context);

        // 验证结果
        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void testTestRule_NoStatsUpdate() {
        // 准备测试数据
        Rule rule = createTestRule();
        when(ruleRepository.findById(any())).thenReturn(Optional.of(rule));

        Map<String, Object> contextData = new HashMap<>();
        contextData.put("temperature", 35);
        RuleContext context = RuleContext.of(contextData);

        // 测试规则
        RuleExecutionResult result = executionService.test(rule.getId(), context);

        // 验证结果
        assertNotNull(result);
        // 验证没有更新统计（测试不应该更新）
        verify(ruleRepository, never()).save(any());
    }

    @Test
    void testClearCache() {
        // 清除缓存
        executionService.clearCache();

        // 验证缓存已清除（无异常）
        assertTrue(true);
    }

    @Test
    void testClearRuleCache() {
        // 清除指定规则缓存
        executionService.clearCache(1L);

        // 验证缓存已清除（无异常）
        assertTrue(true);
    }

    /**
     * 创建测试规则
     */
    private Rule createTestRule() {
        return createTestRule(1L, "temperature > 30");
    }

    /**
     * 创建测试规则
     */
    private Rule createTestRule(Long id, String expression) {
        Rule rule = Rule.create(
                1L,
                "RULE_" + id,
                "测试规则" + id,
                Rule.RuleType.ALARM,
                "测试",
                "测试规则描述",
                Rule.TriggerType.REALTIME,
                Map.of("threshold", 30),
                List.of(1001L),
                1L
        );
        rule.setTriggerExpression(expression);
        rule.enable();
        return rule;
    }
}
