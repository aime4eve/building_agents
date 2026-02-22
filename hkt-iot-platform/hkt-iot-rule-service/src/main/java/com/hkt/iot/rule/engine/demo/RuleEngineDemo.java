package com.hkt.iot.rule.engine.demo;

import com.hkt.iot.rule.engine.RuleEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 规则引擎演示类
 * 展示DSL解析器的使用方法
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleEngineDemo {

    public static void main(String[] args) {
        RuleEngine ruleEngine = new RuleEngine();

        System.out.println("=== 规则引擎DSL解析器演示 ===\n");

        // 演示1: 简单温度告警规则
        demoSimpleAlert(ruleEngine);

        // 演示2: 复合条件规则
        demoComplexCondition(ruleEngine);

        // 演示3: 使用函数的规则
        demoFunctionCall(ruleEngine);

        // 演示4: 嵌套属性访问
        demoNestedProperty(ruleEngine);

        // 演示5: 编译规则优化性能
        demoCompiledRule(ruleEngine);

        System.out.println("\n=== 演示完成 ===");
    }

    /**
     * 演示1: 简单温度告警规则
     * DSL: temperature > 30
     */
    private static void demoSimpleAlert(RuleEngine ruleEngine) {
        System.out.println("【演示1】简单温度告警规则");
        System.out.println("DSL: temperature > 30");

        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 35);

        boolean result = ruleEngine.evaluate("temperature > 30", context);
        System.out.println("温度: " + context.get("temperature") + "°C");
        System.out.println("结果: " + (result ? "告警触发" : "正常"));
        System.out.println();
    }

    /**
     * 演示2: 复合条件规则（防霉管控场景）
     * DSL: temperature > 30 && humidity > 80
     */
    private static void demoComplexCondition(RuleEngine ruleEngine) {
        System.out.println("【演示2】防霉管控复合条件");
        System.out.println("DSL: temperature > 30 && humidity > 80");

        Map<String, Object> context = new HashMap<>();
        context.put("temperature", 32);
        context.put("humidity", 82);

        boolean result = ruleEngine.evaluate("temperature > 30 && humidity > 80", context);
        System.out.println("温度: " + context.get("temperature") + "°C");
        System.out.println("湿度: " + context.get("humidity") + "%");
        System.out.println("结果: " + (result ? "霉菌高风险" : "霉菌低风险"));
        System.out.println();
    }

    /**
     * 演示3: 使用内置函数
     * DSL: avg(values) > 25
     */
    private static void demoFunctionCall(RuleEngine ruleEngine) {
        System.out.println("【演示3】使用内置函数");
        System.out.println("DSL: avg(temperatures) > 25");

        Map<String, Object> context = new HashMap<>();
        context.put("temperatures", List.of(22, 24, 26, 28, 30));

        boolean result = ruleEngine.evaluate("avg(temperatures) > 25", context);
        System.out.println("温度序列: " + context.get("temperatures"));
        System.out.println("结果: " + (result ? "平均温度超标" : "平均温度正常"));
        System.out.println();
    }

    /**
     * 演示4: 嵌套属性访问（设备状态检查）
     * DSL: device.online == true && device.temperature < 80
     */
    private static void demoNestedProperty(RuleEngine ruleEngine) {
        System.out.println("【演示4】嵌套属性访问");
        System.out.println("DSL: device.online == true && device.temperature < 80");

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> device = new HashMap<>();
        device.put("online", true);
        device.put("temperature", 75);
        context.put("device", device);

        boolean result = ruleEngine.evaluate("device.online == true && device.temperature < 80", context);
        System.out.println("设备在线: " + device.get("online"));
        System.out.println("设备温度: " + device.get("temperature") + "°C");
        System.out.println("结果: " + (result ? "设备状态正常" : "设备异常"));
        System.out.println();
    }

    /**
     * 演示5: 编译规则（性能优化）
     */
    private static void demoCompiledRule(RuleEngine ruleEngine) {
        System.out.println("【演示5】编译规则（性能优化）");
        System.out.println("DSL: (temperature > 30 || humidity > 80) && device.online == true");

        // 编译规则
        var compiledRule = ruleEngine.compile("(temperature > 30 || humidity > 80) && device.online == true");

        Map<String, Object> context = new HashMap<>();
        Map<String, Object> device = new HashMap<>();
        device.put("online", true);
        context.put("device", device);

        // 测试多种场景
        int[][] testCases = {
            {35, 75},  // 温度高，湿度低
            {25, 85},  // 温度低，湿度高
            {35, 85},  // 温度和湿度都高
            {25, 75}   // 温度和湿度都低
        };

        for (int i = 0; i < testCases.length; i++) {
            context.put("temperature", testCases[i][0]);
            context.put("humidity", testCases[i][1]);

            boolean result = compiledRule.evaluate(context);
            System.out.println("场景" + (i + 1) + ": 温度=" + testCases[i][0] + "°C, 湿度=" + testCases[i][1] + "% -> " +
                (result ? "告警" : "正常"));
        }
        System.out.println();
    }
}
