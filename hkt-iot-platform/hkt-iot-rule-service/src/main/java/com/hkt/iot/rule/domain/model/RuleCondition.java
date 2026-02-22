package com.hkt.iot.rule.domain.model;

import com.hkt.iot.rule.engine.CompiledRule;
import com.hkt.iot.rule.engine.RuleEngine;

import java.util.Map;

/**
 * 规则条件实体
 * 集成DSL解析器，支持条件表达式解析和评估
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleCondition {
    private Long id;
    private String code;
    private String name;
    private String expression; // DSL表达式

    // 编译后的规则（缓存）
    private transient CompiledRule compiledRule;

    // 规则引擎实例
    private static final RuleEngine RULE_ENGINE = new RuleEngine();

    /**
     * 私有构造函数
     */
    private RuleCondition() {
    }

    /**
     * 创建条件
     */
    public static RuleCondition create(String code, String name, String expression) {
        RuleCondition condition = new RuleCondition();
        condition.code = code;
        condition.name = name;
        condition.expression = expression;
        return condition;
    }

    /**
     * 编译DSL表达式
     */
    public void compile() {
        if (expression != null && !expression.isEmpty()) {
            this.compiledRule = RULE_ENGINE.compile(expression);
        }
    }

    /**
     * 评估条件
     */
    public boolean evaluate(RuleContext context) {
        if (compiledRule == null) {
            compile();
        }
        return compiledRule.evaluate(context.toMap());
    }

    /**
     * 验证表达式
     */
    public ValidationResult validate() {
        try {
            RULE_ENGINE.parse(expression);
            return ValidationResult.valid();
        } catch (Exception e) {
            return ValidationResult.invalid(e.getMessage());
        }
    }

    /**
     * 获取表达式中的变量
     */
    public java.util.Set<String> extractVariables() {
        // 简单实现：提取标识符
        // 实际应该从AST中提取
        java.util.Set<String> variables = new java.util.HashSet<>();
        if (expression != null) {
            // 简单的变量提取逻辑
            String[] tokens = expression.split("[^a-zA-Z0-9_.]");
            for (String token : tokens) {
                if (!token.isEmpty() && Character.isLetter(token.charAt(0))) {
                    // 排除关键字
                    if (!isKeyword(token)) {
                        variables.add(token);
                    }
                }
            }
        }
        return variables;
    }

    private boolean isKeyword(String token) {
        return token.equals("true") || token.equals("false") || token.equals("null") ||
               token.equals("and") || token.equals("or") || token.equals("not") ||
               token.equals("avg") || token.equals("sum") || token.equals("max") ||
               token.equals("min") || token.equals("count") || token.equals("in") ||
               token.equals("between") || token.equals("contains") || token.equals("matches");
    }

    // ==================== Getters and Setters ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
        // 重置编译缓存
        this.compiledRule = null;
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
