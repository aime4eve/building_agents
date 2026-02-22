package com.hkt.iot.rule.interfaces.rest;

import com.hkt.iot.rule.application.service.RuleApplicationService;
import com.hkt.iot.rule.domain.model.RuleContext;
import com.hkt.iot.rule.domain.model.RuleExecutionResult;
import com.hkt.iot.rule.domain.service.RuleValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 规则测试REST API控制器
 * 提供规则测试与调试功能
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rules/test")
public class RuleTestController {

    private final RuleApplicationService ruleApplicationService;
    private final RuleValidationService validationService;

    public RuleTestController(
            RuleApplicationService ruleApplicationService,
            RuleValidationService validationService) {
        this.ruleApplicationService = ruleApplicationService;
        this.validationService = validationService;
    }

    /**
     * 测试规则条件
     * 只验证条件是否匹配，不执行动作
     */
    @PostMapping("/{ruleId}/condition")
    public ConditionTestResult testCondition(
            @PathVariable Long ruleId,
            @RequestBody Map<String, Object> context) {
        log.info("测试规则条件: ruleId={}", ruleId);

        try {
            RuleContext ruleContext = RuleContext.of(context);
            RuleExecutionResult result = ruleApplicationService.testRule(ruleId, context);

            return new ConditionTestResult(
                    result.isMatched(),
                    result.getStatus().name(),
                    result.getMessage(),
                    null
            );
        } catch (Exception e) {
            return new ConditionTestResult(
                    false,
                    "ERROR",
                    e.getMessage(),
                    e.getClass().getSimpleName()
            );
        }
    }

    /**
     * 调试规则执行
     * 返回详细的执行过程信息
     */
    @PostMapping("/{ruleId}/debug")
    public DebugResult debugRule(
            @PathVariable Long ruleId,
            @RequestBody DebugRequest request) {
        log.info("调试规则: ruleId={}", ruleId);

        try {
            RuleContext context = RuleContext.of(request.getContext());

            // 验证表达式
            RuleValidationService.ValidationResult validationResult =
                    validationService.validateExpression(request.getExpression());

            // 评估表达式
            boolean matched = validationService.evaluateExpression(
                    request.getExpression(),
                    context
            );

            return new DebugResult(
                    true,
                    "Debug completed",
                    validationResult.isValid(),
                    matched,
                    context.toMap(),
                    validationResult.getErrors()
            );

        } catch (Exception e) {
            return new DebugResult(
                    false,
                    e.getMessage(),
                    false,
                    false,
                    request.getContext(),
                    List.of(e.getClass().getSimpleName() + ": " + e.getMessage())
            );
        }
    }

    /**
     * 批量测试规则
     * 使用多个测试用例测试同一规则
     */
    @PostMapping("/{ruleId}/batch-test")
    public List<BatchTestResult> batchTestRule(
            @PathVariable Long ruleId,
            @RequestBody BatchTestRequest request) {
        log.info("批量测试规则: ruleId={}, testCases={}", ruleId, request.getTestCases().size());

        return request.getTestCases().stream()
                .map(testCase -> {
                    try {
                        RuleExecutionResult result = ruleApplicationService.testRule(
                                ruleId,
                                testCase.getContext()
                        );
                        return new BatchTestResult(
                                testCase.getName(),
                                true,
                                result.isMatched(),
                                result.getStatus().name(),
                                null
                        );
                    } catch (Exception e) {
                        return new BatchTestResult(
                                testCase.getName(),
                                false,
                                false,
                                "ERROR",
                                e.getMessage()
                        );
                    }
                })
                .toList();
    }

    /**
     * 获取规则表达式的变量列表
     */
    @GetMapping("/{ruleId}/variables")
    public VariableListResult getRuleVariables(@PathVariable Long ruleId) {
        log.info("获取规则变量: ruleId={}", ruleId);

        var ruleDetail = ruleApplicationService.getRuleDetail(ruleId);
        Set<String> variables = ruleApplicationService.extractVariables(
                ruleDetail.getTriggerExpression()
        );

        return new VariableListResult(
                ruleId,
                ruleDetail.getTriggerExpression(),
                variables
        );
    }

    /**
     * 条件测试结果
     */
    public static class ConditionTestResult {
        private final boolean matched;
        private final String status;
        private final String message;
        private final String errorType;

        public ConditionTestResult(boolean matched, String status, String message, String errorType) {
            this.matched = matched;
            this.status = status;
            this.message = message;
            this.errorType = errorType;
        }

        public boolean isMatched() { return matched; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getErrorType() { return errorType; }
    }

    /**
     * 调试请求
     */
    public static class DebugRequest {
        private String expression;
        private Map<String, Object> context;

        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
        public Map<String, Object> getContext() { return context; }
        public void setContext(Map<String, Object> context) { this.context = context; }
    }

    /**
     * 调试结果
     */
    public static class DebugResult {
        private final boolean success;
        private final String message;
        private final boolean expressionValid;
        private final boolean conditionMatched;
        private final Map<String, Object> contextSnapshot;
        private final List<String> errors;

        public DebugResult(boolean success, String message, boolean expressionValid,
                          boolean conditionMatched, Map<String, Object> contextSnapshot,
                          List<String> errors) {
            this.success = success;
            this.message = message;
            this.expressionValid = expressionValid;
            this.conditionMatched = conditionMatched;
            this.contextSnapshot = contextSnapshot;
            this.errors = errors;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public boolean isExpressionValid() { return expressionValid; }
        public boolean isConditionMatched() { return conditionMatched; }
        public Map<String, Object> getContextSnapshot() { return contextSnapshot; }
        public List<String> getErrors() { return errors; }
    }

    /**
     * 批量测试请求
     */
    public static class BatchTestRequest {
        private List<TestCase> testCases;

        public List<TestCase> getTestCases() { return testCases; }
        public void setTestCases(List<TestCase> testCases) { this.testCases = testCases; }

        public static class TestCase {
            private String name;
            private Map<String, Object> context;
            private boolean expectedMatch;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public Map<String, Object> getContext() { return context; }
            public void setContext(Map<String, Object> context) { this.context = context; }
            public boolean isExpectedMatch() { return expectedMatch; }
            public void setExpectedMatch(boolean expectedMatch) { this.expectedMatch = expectedMatch; }
        }
    }

    /**
     * 批量测试结果
     */
    public static class BatchTestResult {
        private final String testName;
        private final boolean success;
        private final boolean matched;
        private final String status;
        private final String error;

        public BatchTestResult(String testName, boolean success, boolean matched,
                              String status, String error) {
            this.testName = testName;
            this.success = success;
            this.matched = matched;
            this.status = status;
            this.error = error;
        }

        public String getTestName() { return testName; }
        public boolean isSuccess() { return success; }
        public boolean isMatched() { return matched; }
        public String getStatus() { return status; }
        public String getError() { return error; }
    }

    /**
     * 变量列表结果
     */
    public static class VariableListResult {
        private final Long ruleId;
        private final String expression;
        private final Set<String> variables;
        private final LocalDateTime extractedAt;

        public VariableListResult(Long ruleId, String expression, Set<String> variables) {
            this.ruleId = ruleId;
            this.expression = expression;
            this.variables = variables;
            this.extractedAt = LocalDateTime.now();
        }

        public Long getRuleId() { return ruleId; }
        public String getExpression() { return expression; }
        public Set<String> getVariables() { return variables; }
        public LocalDateTime getExtractedAt() { return extractedAt; }
    }
}
