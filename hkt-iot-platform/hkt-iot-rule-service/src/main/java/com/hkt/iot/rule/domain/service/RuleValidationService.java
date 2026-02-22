package com.hkt.iot.rule.domain.service;

import com.hkt.iot.rule.engine.RuleEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * 规则验证领域服务
 * 负责验证DSL表达式的语法和提取变量
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleValidationService {

    private final RuleEngine ruleEngine;

    public RuleValidationService() {
        this.ruleEngine = new RuleEngine();
    }

    public RuleValidationService(RuleEngine ruleEngine) {
        this.ruleEngine = ruleEngine;
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final int errorPosition;
        private final List<String> errors;

        public ValidationResult(boolean valid, String errorMessage, int errorPosition, List<String> errors) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.errorPosition = errorPosition;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null, -1, List.of());
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage, -1, List.of(errorMessage));
        }

        public static ValidationResult invalid(String errorMessage, int errorPosition) {
            return new ValidationResult(false, errorMessage, errorPosition, List.of(errorMessage));
        }

        public static ValidationResult invalid(String errorMessage, List<String> errors) {
            return new ValidationResult(false, errorMessage, -1, errors);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public int getErrorPosition() {
            return errorPosition;
        }

        public List<String> getErrors() {
            return errors;
        }
    }

    /**
     * 验证DSL表达式
     */
    public ValidationResult validateExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return ValidationResult.invalid("Expression cannot be empty");
        }

        try {
            // 尝试解析表达式
            ruleEngine.parse(expression);
            return ValidationResult.valid();
        } catch (com.hkt.iot.rule.engine.parser.ParseException e) {
            return ValidationResult.invalid("Syntax error: " + e.getMessage());
        } catch (com.hkt.iot.rule.engine.lexer.LexicalException e) {
            return ValidationResult.invalid("Lexical error: " + e.getMessage());
        } catch (Exception e) {
            return ValidationResult.invalid("Validation error: " + e.getMessage());
        }
    }

    /**
     * 提取表达式中的变量
     */
    public Set<String> extractVariables(String expression) {
        Set<String> variables = new HashSet<>();

        if (expression == null || expression.isEmpty()) {
            return variables;
        }

        try {
            // 解析表达式并从AST中提取标识符
            com.hkt.iot.rule.engine.ast.ASTNode ast = ruleEngine.parse(expression);

            // 访问AST提取变量
            VariableExtractorVisitor visitor = new VariableExtractorVisitor();
            ast.accept(visitor);

            return visitor.getVariables();

        } catch (Exception e) {
            // 如果解析失败，使用简单的正则表达式提取
            return extractVariablesByRegex(expression);
        }
    }

    /**
     * 使用正则表达式提取变量
     */
    private Set<String> extractVariablesByRegex(String expression) {
        Set<String> variables = new HashSet<>();

        // 匹配标识符模式
        Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_.]*");
        java.util.regex.Matcher matcher = pattern.matcher(expression);

        while (matcher.find()) {
            String token = matcher.group();
            // 排除关键字
            if (!isKeyword(token)) {
                variables.add(token);
            }
        }

        return variables;
    }

    /**
     * 检查是否是关键字
     */
    private boolean isKeyword(String token) {
        return token.equals("true") || token.equals("false") || token.equals("null") ||
               token.equals("and") || token.equals("or") || token.equals("not") ||
               token.equals("avg") || token.equals("sum") || token.equals("max") ||
               token.equals("min") || token.equals("count") || token.equals("first") ||
               token.equals("last") || token.equals("diff") || token.equals("rate") ||
               token.equals("now") || token.equals("today") || token.equals("toUpper") ||
               token.equals("toLower") || token.equals("length") || token.equals("abs") ||
               token.equals("in") || token.equals("between") || token.equals("contains") ||
               token.equals("matches");
    }

    /**
     * 变量提取访问者
     */
    private static class VariableExtractorVisitor implements com.hkt.iot.rule.engine.ast.ASTVisitor<Object> {
        private final Set<String> variables = new HashSet<>();

        @Override
        public Object visit(com.hkt.iot.rule.engine.ast.BinaryExpression node) {
            node.getLeft().accept(this);
            node.getRight().accept(this);
            return null;
        }

        @Override
        public Object visit(com.hkt.iot.rule.engine.ast.UnaryExpression node) {
            node.getOperand().accept(this);
            return null;
        }

        @Override
        public Object visit(com.hkt.iot.rule.engine.ast.FunctionCall node) {
            for (com.hkt.iot.rule.engine.ast.ASTNode arg : node.getArguments()) {
                arg.accept(this);
            }
            return null;
        }

        @Override
        public Object visit(com.hkt.iot.rule.engine.ast.Identifier node) {
            String name = node.getName();
            // 排除关键字
            if (!isKeyword(name)) {
                variables.add(name);
            }
            return null;
        }

        @Override
        public Object visit(com.hkt.iot.rule.engine.ast.Literal node) {
            return null;
        }

        @Override
        public Object visit(com.hkt.iot.rule.engine.ast.ArrayLiteral node) {
            for (com.hkt.iot.rule.engine.ast.ASTNode element : node.getElements()) {
                element.accept(this);
            }
            return null;
        }

        public Set<String> getVariables() {
            return variables;
        }

        private boolean isKeyword(String token) {
            return token.equals("true") || token.equals("false") || token.equals("null") ||
                   token.equals("avg") || token.equals("sum") || token.equals("max") ||
                   token.equals("min") || token.equals("count") || token.equals("first") ||
                   token.equals("last") || token.equals("diff") || token.equals("rate") ||
                   token.equals("now") || token.equals("today");
        }
    }

    /**
     * 格式化错误信息
     */
    public String formatErrorMessage(ValidationResult result, String expression) {
        if (result.isValid()) {
            return "Expression is valid";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Expression error: ").append(result.getErrorMessage());

        if (result.getErrorPosition() >= 0) {
            sb.append("\nExpression: ").append(expression);
            sb.append("\n             ");
            for (int i = 0; i < result.getErrorPosition(); i++) {
                sb.append(" ");
            }
            sb.append("^");
        }

        return sb.toString();
    }

    /**
     * 评估表达式
     *
     * @param expression 表达式字符串
     * @param context    规则上下文
     * @return 表达式评估结果
     */
    public boolean evaluateExpression(String expression, RuleContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        try {
            return ruleEngine.evaluate(expression, context.toMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate expression: " + e.getMessage(), e);
        }
    }

    /**
     * 评估表达式（带上下文Map）
     *
     * @param expression 表达式字符串
     * @param context    上下文Map
     * @return 表达式评估结果
     */
    public boolean evaluateExpression(String expression, Map<String, Object> context) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        try {
            return ruleEngine.evaluate(expression, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate expression: " + e.getMessage(), e);
        }
    }
}
