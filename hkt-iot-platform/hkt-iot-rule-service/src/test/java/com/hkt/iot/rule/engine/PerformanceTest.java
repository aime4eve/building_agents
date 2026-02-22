package com.hkt.iot.rule.engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能测试
 *
 * @author AI Engineer
 * @since 1.0.0
 */
class PerformanceTest {

    private final RuleEngine ruleEngine = new RuleEngine();

    @Test
    void testSimpleRulePerformance() {
        String rule = "temperature > 30 && humidity > 80";

        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 35);
        context.put("humidity", 85);

        int iterations = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            ruleEngine.evaluate(rule, context);
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeUs = (endTime - startTime) / (double) iterations / 1000;

        System.out.println("简单规则性能测试 (" + iterations + " 次迭代):");
        System.out.println("  总耗时: " + totalTimeMs + " ms");
        System.out.println("  平均耗时: " + String.format("%.2f", avgTimeUs) + " μs/次");
        System.out.println("  吞吐量: " + String.format("%.0f", iterations * 1000.0 / totalTimeMs) + " 规则/秒");

        // 单次规则评估应该在1ms内完成
        assertTrue(avgTimeUs < 1000, "单次规则评估应在1ms内完成");
    }

    @Test
    void testComplexRulePerformance() {
        String rule = "(temperature > 30 || humidity > 80) && device.online == true && !device.maintenance";

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> device = new HashMap<>();
        device.put("online", true);
        device.put("maintenance", false);
        context.put("device", device);
        context.put("temperature", 35);
        context.put("humidity", 75);

        int iterations = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            ruleEngine.evaluate(rule, context);
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeUs = (endTime - startTime) / (double) iterations / 1000;

        System.out.println("复杂规则性能测试 (" + iterations + " 次迭代):");
        System.out.println("  总耗时: " + totalTimeMs + " ms");
        System.out.println("  平均耗时: " + String.format("%.2f", avgTimeUs) + " μs/次");
        System.out.println("  吞吐量: " + String.format("%.0f", iterations * 1000.0 / totalTimeMs) + " 规则/秒");
    }

    @Test
    void testCompiledRulePerformance() {
        String rule = "temperature > 30 && humidity > 80";
        CompiledRule compiledRule = ruleEngine.compile(rule);

        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 35);
        context.put("humidity", 85);

        int iterations = 100000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            compiledRule.evaluate(context);
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeUs = (endTime - startTime) / (double) iterations / 1000;

        System.out.println("编译规则性能测试 (" + iterations + " 次迭代):");
        System.out.println("  总耗时: " + totalTimeMs + " ms");
        System.out.println("  平均耗时: " + String.format("%.2f", avgTimeUs) + " μs/次");
        System.out.println("  吞吐量: " + String.format("%.0f", iterations * 1000.0 / totalTimeMs) + " 规则/秒");

        // 编译后的规则应该更快
        assertTrue(avgTimeUs < 100, "编译后的单次规则评估应在100μs内完成");
    }

    @Test
    void testMultipleRulesPerformance() {
        List<String> rules = List.of(
            "temperature > 30",
            "humidity > 80",
            "temperature > 30 && humidity > 80",
            "(temperature > 30 || humidity > 80) && device.online == true",
            "avg(temperature, 1h) > 25"
        );

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> device = new HashMap<>();
        device.put("online", true);
        context.put("device", device);
        context.put("temperature", 35);
        context.put("humidity", 85);

        int iterations = 1000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            for (String rule : rules) {
                ruleEngine.evaluate(rule, context);
            }
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        int totalEvaluations = iterations * rules.size();
        double avgTimeUs = (endTime - startTime) / (double) totalEvaluations / 1000;

        System.out.println("多规则性能测试 (" + totalEvaluations + " 次评估):");
        System.out.println("  规则数量: " + rules.size());
        System.out.println("  总耗时: " + totalTimeMs + " ms");
        System.out.println("  平均耗时: " + String.format("%.2f", avgTimeUs) + " μs/次");
        System.out.println("  吞吐量: " + String.format("%.0f", totalEvaluations * 1000.0 / totalTimeMs) + " 规则/秒");
    }

    @Test
    void testFunctionCallPerformance() {
        String rule = "avg(values) > 25";

        Map<String, Object> context = new HashMap<>();
        context.put("values", List.of(10, 20, 30, 40, 50));

        int iterations = 10000;
        long startTime = System.nanoTime();

        for (int i = 0; i < iterations; i++) {
            ruleEngine.evaluate(rule, context);
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        double avgTimeUs = (endTime - startTime) / (double) iterations / 1000;

        System.out.println("函数调用性能测试 (" + iterations + " 次迭代):");
        System.out.println("  总耗时: " + totalTimeMs + " ms");
        System.out.println("  平均耗时: " + String.format("%.2f", avgTimeUs) + " μs/次");
        System.out.println("  吞吐量: " + String.format("%.0f", iterations * 1000.0 / totalTimeMs) + " 规则/秒");
    }

    @Test
    void testRealWorldScenario() {
        // 模拟1000个设备，每个设备评估5条规则
        int deviceCount = 1000;
        int rulesPerDevice = 5;

        List<String> rules = List.of(
            "temperature > 30 && humidity > 80",
            "vibration > 10 || temperature > 80",
            "healthScore < 70",
            "battery < 20",
            "!online"
        );

        long startTime = System.nanoTime();
        int totalAlerts = 0;

        for (int i = 0; i < deviceCount; i++) {
            Map<String, Object> context = new HashMap<>();
            context.put("temperature", 25 + (i % 20));
            context.put("humidity", 60 + (i % 40));
            context.put("vibration", i % 15);
            context.put("healthScore", 50 + (i % 60));
            context.put("battery", 10 + (i % 90));
            context.put("online", (i % 10) != 0);

            for (String rule : rules) {
                if (ruleEngine.evaluate(rule, context)) {
                    totalAlerts++;
                }
            }
        }

        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;
        int totalEvaluations = deviceCount * rulesPerDevice;

        System.out.println("真实场景性能测试:");
        System.out.println("  设备数量: " + deviceCount);
        System.out.println("  规则数量: " + rulesPerDevice);
        System.out.println("  总评估次数: " + totalEvaluations);
        System.out.println("  告警次数: " + totalAlerts);
        System.out.println("  总耗时: " + totalTimeMs + " ms");
        System.out.println("  平均耗时: " + String.format("%.2f", totalTimeMs * 1000.0 / totalEvaluations) + " μs/次");
        System.out.println("  吞吐量: " + String.format("%.0f", totalEvaluations * 1000.0 / totalTimeMs) + " 规则/秒");

        // 1000条规则评估应在100ms内完成
        assertTrue(totalTimeMs < 100, "1000条规则评估应在100ms内完成");
    }
}
