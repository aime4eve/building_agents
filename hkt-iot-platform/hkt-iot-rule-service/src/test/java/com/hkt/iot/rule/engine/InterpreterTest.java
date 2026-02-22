package com.hkt.iot.rule.engine;

import com.hkt.iot.rule.engine.ast.ASTNode;
import com.hkt.iot.rule.engine.parser.Parser;
import com.hkt.iot.rule.engine.interpreter.EvaluationException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 解释器测试
 *
 * @author AI Engineer
 * @since 1.0.0
 */
class InterpreterTest {

    private final RuleEngine ruleEngine = new RuleEngine();

    private Object evaluate(String rule, Map<String, Object> context) {
        return ruleEngine.execute(rule, context);
    }

    private boolean evaluateBoolean(String rule, Map<String, Object> context) {
        return ruleEngine.evaluate(rule, context);
    }

    @Test
    void testSimpleComparison() {
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 35);

        assertTrue(evaluateBoolean("temperature > 30", context));
        assertFalse(evaluateBoolean("temperature > 40", context));
    }

    @Test
    void testLogicalAnd() {
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 35);
        context.put("humidity", 85);

        assertTrue(evaluateBoolean("temperature > 30 && humidity > 80", context));
        assertFalse(evaluateBoolean("temperature > 30 && humidity > 90", context));
    }

    @Test
    void testLogicalOr() {
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 25);
        context.put("humidity", 85);

        assertTrue(evaluateBoolean("temperature > 30 || humidity > 80", context));
        assertFalse(evaluateBoolean("temperature > 30 || humidity < 70", context));
    }

    @Test
    void testLogicalNot() {
        Map<String, Object> context = new HashMap<>();
        context.put("active", false);

        assertTrue(evaluateBoolean("!active", context));
        assertFalse(evaluateBoolean("!true", context));
    }

    @Test
    void testComplexExpression() {
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> device = new HashMap<>();
        device.put("online", true);
        context.put("device", device);
        context.put("temperature", 35);
        context.put("humidity", 75);

        assertTrue(evaluateBoolean("(temperature > 30 || humidity > 80) && device.online == true", context));
    }

    @Test
    void testArithmeticOperations() {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 10);
        context.put("b", 5);

        assertEquals(15, evaluate("a + b", context));
        assertEquals(5, evaluate("a - b", context));
        assertEquals(50, evaluate("a * b", context));
        assertEquals(2.0, evaluate("a / b", context));
        assertEquals(0, evaluate("a % b", context));
    }

    @Test
    void testComparisonOperators() {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 10);

        assertTrue(evaluateBoolean("value > 5", context));
        assertTrue(evaluateBoolean("value >= 10", context));
        assertTrue(evaluateBoolean("value < 15", context));
        assertTrue(evaluateBoolean("value <= 10", context));
        assertTrue(evaluateBoolean("value == 10", context));
        assertTrue(evaluateBoolean("value != 5", context));
    }

    @Test
    void testStringComparison() {
        Map<String, Object> context = new HashMap<>();
        context.put("name", "test");

        assertTrue(evaluateBoolean("name == 'test'", context));
        assertFalse(evaluateBoolean("name == 'other'", context));
    }

    @Test
    void testContainsOperator() {
        Map<String, Object> context = new HashMap<>();
        context.put("message", "Hello World");

        assertTrue(evaluateBoolean("message contains 'Hello'", context));
        assertTrue(evaluateBoolean("message contains 'World'", context));
        assertFalse(evaluateBoolean("message contains 'Python'", context));
    }

    @Test
    void testInOperator() {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 2);

        assertTrue(evaluateBoolean("value in [1, 2, 3]", context));
        assertFalse(evaluateBoolean("value in [4, 5, 6]", context));
    }

    @Test
    void testBetweenOperator() {
        Map<String, Object> context = new HashMap<>();
        context.put("value", 5);

        assertTrue(evaluateBoolean("value between [1, 10]", context));
        assertFalse(evaluateBoolean("value between [6, 10]", context));
    }

    @Test
    void testNestedPropertyAccess() {
        Map<String, Object> context = new HashMap<>();
        Map<String, Object> device = new HashMap<>();
        device.put("temperature", 35);
        device.put("humidity", 80);
        context.put("device", device);

        assertTrue(evaluateBoolean("device.temperature > 30", context));
        assertTrue(evaluateBoolean("device.humidity == 80", context));
    }

    @Test
    void testAvgFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30, 40, 50));

        assertEquals(30.0, evaluate("avg(values)", context));
    }

    @Test
    void testSumFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30));

        assertEquals(60.0, evaluate("sum(values)", context));
    }

    @Test
    void testMaxFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30));

        assertEquals(30.0, evaluate("max(values)", context));
    }

    @Test
    void testMinFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30));

        assertEquals(10.0, evaluate("min(values)", context));
    }

    @Test
    void testCountFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30));

        assertEquals(3, evaluate("count(values)", context));
    }

    @Test
    void testFirstFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30));

        assertEquals(10, evaluate("first(values)", context));
    }

    @Test
    void testLastFunction() {
        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30));

        assertEquals(30, evaluate("last(values)", context));
    }

    @Test
    void testDiffFunction() {
        assertEquals(5.0, evaluate("diff(10, 5)", new HashMap<>()));
        assertEquals(-5.0, evaluate("diff(5, 10)", new HashMap<>()));
    }

    @Test
    void testAbsFunction() {
        assertEquals(5.0, evaluate("abs(-5)", new HashMap<>()));
        assertEquals(5.0, evaluate("abs(5)", new HashMap<>()));
    }

    @Test
    void testStringFunctions() {
        Map<String, Object> context = new HashMap<>();
        context.put("name", "hello");

        assertEquals("HELLO", evaluate("toUpper(name)", context));
        assertEquals("hello", evaluate("toLower(name)", context));
        assertEquals(5, evaluate("length(name)", context));
    }

    @Test
    void testNullLiteral() {
        Map<String, Object> context = new HashMap<>();
        context.put("value", null);

        assertTrue(evaluateBoolean("value == null", context));
        assertFalse(evaluateBoolean("value != null", context));
    }

    @Test
    void testDecimalNumber() {
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 25.5);

        assertTrue(evaluateBoolean("temperature > 25", context));
        assertTrue(evaluateBoolean("temperature < 26", context));
    }

    @Test
    void testUnaryNegation() {
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", -10);

        assertTrue(evaluateBoolean("temperature < 0", context));
        assertTrue(evaluateBoolean("temperature > -20", context));
    }

    @Test
    void testDivisionByZero() {
        assertThrows(RuleEngineException.class, () -> {
            evaluate("10 / 0", new HashMap<>());
        });
    }

    @Test
    void testUndefinedVariable() {
        assertThrows(RuleEngineException.class, () -> {
            evaluate("undefined > 10", new HashMap<>());
        });
    }

    @Test
    void testCompiledRule() {
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 35);
        context.put("humidity", 85);

        CompiledRule rule = ruleEngine.compile("temperature > 30 && humidity > 80");

        assertTrue(rule.evaluate(context));

        context.put("temperature", 25);
        assertFalse(rule.evaluate(context));
    }

    @Test
    void testCustomFunction() {
        RuleEngine customEngine = new RuleEngine();
        customEngine.registerFunction("double", args -> {
            if (args.length != 1 || !(args[0] instanceof Number)) {
                throw new IllegalArgumentException("double() requires a numeric argument");
            }
            return ((Number) args[0]).doubleValue() * 2;
        });

        Map<String, Object> context = new HashMap<>();
        context.put("value", 5);

        assertEquals(10.0, customEngine.execute("double(value)", context));
    }

    @Test
    void testComplexRealWorldRule() {
        // 防霉管控场景：温度告警规则
        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 32);
        context.put("humidity", 82);

        assertTrue(evaluateBoolean("temperature > 30 && humidity > 80", context));

        // 智慧畜牧场景：健康评分规则
        Map<String, Object> livestockContext = new HashMap<>();
        livestockContext.put("healthScore", 60);
        livestockContext.put("temperature", 40);

        assertTrue(evaluateBoolean("healthScore < 70 || temperature > 39", livestockContext));

        // 设备故障预测场景
        Map<String, Object> deviceContext = new HashMap<>();
        deviceContext.put("vibration", 15);
        deviceContext.put("temperature", 85);

        assertTrue(evaluateBoolean("vibration > 10 || temperature > 80", deviceContext));
    }

    @Test
    void testOperatorPrecedenceInEvaluation() {
        Map<String, Object> context = new HashMap<>();
        context.put("a", 5);
        context.put("b", 10);
        context.put("c", 2);

        // a + b * c = 5 + 10 * 2 = 25
        assertEquals(25.0, evaluate("a + b * c", context));

        // (a + b) * c = (5 + 10) * 2 = 30
        assertEquals(30.0, evaluate("(a + b) * c", context));
    }
}
