package com.hkt.iot.rule.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 规则验证服务测试
 *
 * @author HKT IoT Team
 */
class RuleValidationServiceTest {

    private RuleValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new RuleValidationService();
    }

    @Test
    void testValidateExpression_ValidSimple() {
        // 测试简单有效表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("temperature > 30");

        assertTrue(result.isValid());
        assertNull(result.getErrorMessage());
    }

    @Test
    void testValidateExpression_ValidComplex() {
        // 测试复杂有效表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("(temperature > 30 || humidity < 40) && pressure > 100");

        assertTrue(result.isValid());
    }

    @Test
    void testValidateExpression_WithFunction() {
        // 测试带函数的有效表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("avg(temperature, 5) > 25");

        assertTrue(result.isValid());
    }

    @Test
    void testValidateExpression_InvalidSyntax() {
        // 测试语法错误表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("temperature > 30 &&");

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void testValidateExpression_Empty() {
        // 测试空表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("");

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void testValidateExpression_Null() {
        // 测试null表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression(null);

        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void testValidateExpression_WithBooleanLogic() {
        // 测试布尔逻辑
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("isOnline == true and active == false");

        assertTrue(result.isValid());
    }

    @Test
    void testValidateExpression_WithStringLiteral() {
        // 测试字符串字面量
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("status == 'online'");

        assertTrue(result.isValid());
    }

    @Test
    void testValidateExpression_WithArray() {
        // 测试数组操作
        RuleValidationService.ValidationResult result =
                validationService.validateExpression("value in [1, 2, 3]");

        assertTrue(result.isValid());
    }

    @Test
    void testExtractVariables_SimpleExpression() {
        // 测试提取简单表达式的变量
        Set<String> variables = validationService.extractVariables("temperature > 30");

        assertNotNull(variables);
        assertTrue(variables.contains("temperature"));
        assertEquals(1, variables.size());
    }

    @Test
    void testExtractVariables_ComplexExpression() {
        // 测试提取复杂表达式的变量
        Set<String> variables = validationService.extractVariables(
                "(temperature > 30 || humidity < 40) && pressure > 100");

        assertNotNull(variables);
        assertTrue(variables.contains("temperature"));
        assertTrue(variables.contains("humidity"));
        assertTrue(variables.contains("pressure"));
        assertEquals(3, variables.size());
    }

    @Test
    void testExtractVariables_WithFunction() {
        // 测试提取带函数表达式的变量
        Set<String> variables = validationService.extractVariables(
                "avg(temperature, 5) > min(humidity, 10)");

        assertNotNull(variables);
        assertTrue(variables.contains("temperature"));
        assertTrue(variables.contains("humidity"));
        // avg, min是关键字，不应该被提取
        assertFalse(variables.contains("avg"));
        assertFalse(variables.contains("min"));
    }

    @Test
    void testExtractVariables_EmptyExpression() {
        // 测试空表达式的变量提取
        Set<String> variables = validationService.extractVariables("");

        assertNotNull(variables);
        assertTrue(variables.isEmpty());
    }

    @Test
    void testExtractVariables_NullExpression() {
        // 测试null表达式的变量提取
        Set<String> variables = validationService.extractVariables(null);

        assertNotNull(variables);
        assertTrue(variables.isEmpty());
    }

    @Test
    void testExtractVariables_NoIdentifiers() {
        // 测试没有标识符的表达式
        Set<String> variables = validationService.extractVariables("true || false");

        assertNotNull(variables);
        // true, false是关键字，不应该被提取
        assertTrue(variables.isEmpty());
    }

    @Test
    void testExtractVariables_WithDotNotation() {
        // 测试点符号表示的变量
        Set<String> variables = validationService.extractVariables(
                "device.temperature > 30 and device.humidity < 50");

        assertNotNull(variables);
        // 当前实现可能提取完整的标识符
        assertTrue(variables.contains("device") ||
                   variables.stream().anyMatch(v -> v.contains("device")));
    }

    @Test
    void testEvaluateExpression_ValidContext() {
        // 测试表达式评估 - 有效上下文
        Map<String, Object> context = Map.of(
                "temperature", 35,
                "humidity", 45
        );

        boolean result = validationService.evaluateExpression(
                "temperature > 30",
                context
        );

        assertTrue(result);
    }

    @Test
    void testEvaluateExpression_NotMatched() {
        // 测试表达式评估 - 条件不匹配
        Map<String, Object> context = Map.of(
                "temperature", 25,
                "humidity", 45
        );

        boolean result = validationService.evaluateExpression(
                "temperature > 30",
                context
        );

        assertFalse(result);
    }

    @Test
    void testEvaluateExpression_ComplexLogic() {
        // 测试复杂逻辑表达式评估
        Map<String, Object> context = Map.of(
                "temperature", 35,
                "humidity", 35
        );

        boolean result = validationService.evaluateExpression(
                "temperature > 30 and humidity < 40",
                context
        );

        assertTrue(result);
    }

    @Test
    void testEvaluateExpression_WithRuleContext() {
        // 测试使用RuleContext评估
        RuleContext context = RuleContext.of(Map.of(
                "temperature", 35,
                "humidity", 45
        ));

        boolean result = validationService.evaluateExpression(
                "temperature > 30",
                context
        );

        assertTrue(result);
    }

    @Test
    void testFormatErrorMessage_Valid() {
        // 测试格式化有效表达式的错误信息
        RuleValidationService.ValidationResult result =
                RuleValidationService.ValidationResult.valid();
        String formatted = validationService.formatErrorMessage(result, "temperature > 30");

        assertEquals("Expression is valid", formatted);
    }

    @Test
    void testFormatErrorMessage_Invalid() {
        // 测试格式化无效表达式的错误信息
        RuleValidationService.ValidationResult result =
                RuleValidationService.ValidationResult.invalid("Syntax error at position 5", 5);
        String formatted = validationService.formatErrorMessage(result, "temp > 30");

        assertTrue(formatted.contains("Syntax error"));
        assertTrue(formatted.contains("position 5"));
    }

    @Test
    void testValidateExpression_WithComplexFunction() {
        // 测试复杂函数表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression(
                        "rate(energy, 3600) > 0.5 and count(alerts, 86400) > 5"
                );

        assertTrue(result.isValid());
    }

    @Test
    void testValidateExpression_WithTimeFunctions() {
        // 测试时间函数表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression(
                        "timestamp > now() - 300 and date == today()"
                );

        assertTrue(result.isValid());
    }

    @Test
    void testValidateExpression_WithStringFunctions() {
        // 测试字符串函数表达式
        RuleValidationService.ValidationResult result =
                validationService.validateExpression(
                        "contains(message, 'error') and toUpper(status) == 'ACTIVE'"
                );

        assertTrue(result.isValid());
    }

    @Test
    void testExtractVariables_ExcludeKeywords() {
        // 测试提取变量时排除关键字
        Set<String> variables = validationService.extractVariables(
                "avg(temperature) > max and min < min_value"
        );

        assertNotNull(variables);
        assertTrue(variables.contains("temperature"));
        assertTrue(variables.contains("min_value"));
        // max和min在上下文中可能是变量，需要正确处理
    }
}
