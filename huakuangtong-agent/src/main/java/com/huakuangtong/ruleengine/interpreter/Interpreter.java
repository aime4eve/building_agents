package com.huakuangtong.ruleengine.interpreter;

import com.huakuangtong.ruleengine.ast.*;
import com.huakuangtong.ruleengine.ast.BinaryOperator;
import com.huakuangtong.ruleengine.ast.UnaryOperator;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 规则引擎解释器
 * 执行AST并返回结果
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class Interpreter implements ASTVisitor<Object> {

    /**
     * 上下文变量
     */
    private final Map<String, Object> context;

    /**
     * 函数注册表
     */
    private final FunctionRegistry functionRegistry;

    public Interpreter(Map<String, Object> context) {
        this.context = context;
        this.functionRegistry = new FunctionRegistry();
    }

    public Interpreter(Map<String, Object> context, FunctionRegistry functionRegistry) {
        this.context = context;
        this.functionRegistry = functionRegistry;
    }

    /**
     * 解释执行AST节点
     */
    public Object interpret(ASTNode node) {
        return node.accept(this);
    }

    @Override
    public Object visit(BinaryExpression node) {
        Object left = interpret(node.getLeft());
        Object right = interpret(node.getRight());

        switch (node.getOperator()) {
            // 算术运算
            case ADD:
                return arithmeticAdd(left, right);
            case SUB:
                return arithmeticSub(left, right);
            case MUL:
                return arithmeticMul(left, right);
            case DIV:
                return arithmeticDiv(left, right);
            case MOD:
                return arithmeticMod(left, right);

            // 比较运算
            case GT:
                return compare(left, right) > 0;
            case GTE:
                return compare(left, right) >= 0;
            case LT:
                return compare(left, right) < 0;
            case LTE:
                return compare(left, right) <= 0;
            case EQ:
                return equals(left, right);
            case NEQ:
                return !equals(left, right);

            // 字符串运算
            case CONTAINS:
                return contains(left, right);
            case MATCHES:
                return matches(left, right);
            case IN:
                return in(left, right);
            case BETWEEN:
                return between(left, right);

            // 逻辑运算
            case AND:
                return isTruthy(left) && isTruthy(right);
            case OR:
                return isTruthy(left) || isTruthy(right);

            default:
                throw new EvaluationException("Unknown binary operator: " + node.getOperator());
        }
    }

    @Override
    public Object visit(UnaryExpression node) {
        Object operand = interpret(node.getOperand());

        switch (node.getOperator()) {
            case NOT:
                return !isTruthy(operand);
            case NEGATE:
                if (!(operand instanceof Number)) {
                    throw new EvaluationException("Cannot negate non-number: " + operand);
                }
                return -((Number) operand).doubleValue();
            default:
                throw new EvaluationException("Unknown unary operator: " + node.getOperator());
        }
    }

    @Override
    public Object visit(FunctionCall node) {
        // 查找注册的函数
        Function function = functionRegistry.getFunction(node.getFunctionName());
        if (function == null) {
            throw new EvaluationException("Unknown function: " + node.getFunctionName());
        }

        // 计算参数值
        Object[] args = new Object[node.getArguments().size()];
        for (int i = 0; i < node.getArguments().size(); i++) {
            args[i] = interpret(node.getArguments().get(i));
        }

        // 调用函数
        try {
            return function.apply(args);
        } catch (Exception e) {
            throw new EvaluationException("Error executing function: " + node.getFunctionName(), e);
        }
    }

    @Override
    public Object visit(Identifier node) {
        String name = node.getName();

        // 处理嵌套属性访问，如 device.temperature
        if (name.contains(".")) {
            return getNestedValue(name);
        }

        if (!context.containsKey(name)) {
            throw new EvaluationException("Undefined variable: " + name);
        }

        return context.get(name);
    }

    @Override
    public Object visit(Literal node) {
        return node.getValue();
    }

    @Override
    public Object visit(ArrayLiteral node) {
        Object[] result = new Object[node.getElements().size()];
        for (int i = 0; i < node.getElements().size(); i++) {
            result[i] = interpret(node.getElements().get(i));
        }
        return result;
    }

    // ==================== 辅助方法 ====================

    private Object arithmeticAdd(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() + ((Number) right).doubleValue();
        }
        if (left instanceof String || right instanceof String) {
            return stringify(left) + stringify(right);
        }
        throw new EvaluationException("Cannot add " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private Object arithmeticSub(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() - ((Number) right).doubleValue();
        }
        throw new EvaluationException("Cannot subtract " + right.getClass().getSimpleName() + " from " + left.getClass().getSimpleName());
    }

    private Object arithmeticMul(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() * ((Number) right).doubleValue();
        }
        throw new EvaluationException("Cannot multiply " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private Object arithmeticDiv(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            double divisor = ((Number) right).doubleValue();
            if (divisor == 0) {
                throw new EvaluationException("Division by zero");
            }
            return ((Number) left).doubleValue() / divisor;
        }
        throw new EvaluationException("Cannot divide " + left.getClass().getSimpleName() + " by " + right.getClass().getSimpleName());
    }

    private Object arithmeticMod(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return ((Number) left).doubleValue() % ((Number) right).doubleValue();
        }
        throw new EvaluationException("Cannot modulo " + left.getClass().getSimpleName() + " by " + right.getClass().getSimpleName());
    }

    @SuppressWarnings("unchecked")
    private int compare(Object left, Object right) {
        if (left instanceof Number && right instanceof Number) {
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue());
        }
        if (left instanceof String && right instanceof String) {
            return ((String) left).compareTo((String) right);
        }
        if (left instanceof Comparable && right instanceof Comparable) {
            return ((Comparable<Object>) left).compareTo(right);
        }
        throw new EvaluationException("Cannot compare " + left.getClass().getSimpleName() + " and " + right.getClass().getSimpleName());
    }

    private boolean equals(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;
        return left.equals(right);
    }

    private boolean contains(Object left, Object right) {
        String leftStr = stringify(left);
        String rightStr = stringify(right);
        return leftStr.contains(rightStr);
    }

    private boolean matches(Object left, Object right) {
        String text = stringify(left);
        String pattern = stringify(right);
        try {
            return Pattern.compile(pattern).matcher(text).find();
        } catch (PatternSyntaxException e) {
            throw new EvaluationException("Invalid regex pattern: " + pattern, e);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean in(Object left, Object right) {
        if (right.getClass().isArray()) {
            Object[] array = (Object[]) right;
            for (Object item : array) {
                if (equals(left, item)) {
                    return true;
                }
            }
            return false;
        }
        if (right instanceof List) {
            return ((List<Object>) right).contains(left);
        }
        if (right instanceof String) {
            return stringify(right).contains(stringify(left));
        }
        return false;
    }

    private boolean between(Object value, Object range) {
        if (range.getClass().isArray()) {
            Object[] array = (Object[]) range;
            if (array.length != 2) {
                throw new EvaluationException("BETWEEN requires exactly 2 values, got: " + array.length);
            }
            try {
                int cmpLow = compare(value, array[0]);
                int cmpHigh = compare(value, array[1]);
                return cmpLow >= 0 && cmpHigh <= 0;
            } catch (EvaluationException e) {
                throw new EvaluationException("Cannot use BETWEEN with non-comparable values", e);
            }
        }
        throw new EvaluationException("BETWEEN requires an array of 2 values");
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof Number) return ((Number) value).doubleValue() != 0;
        if (value instanceof String) return !((String) value).isEmpty();
        return true;
    }

    private String stringify(Object value) {
        if (value == null) return "";
        return value.toString();
    }

    /**
     * 获取嵌套属性值，如 device.temperature
     */
    private Object getNestedValue(String path) {
        String[] parts = path.split("\\.");
        Object current = context.get(parts[0]);

        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(parts[i]);
            } else {
                // 尝试使用反射获取属性
                try {
                    java.lang.reflect.Field field = current.getClass().getDeclaredField(parts[i]);
                    field.setAccessible(true);
                    current = field.get(current);
                } catch (Exception e) {
                    throw new EvaluationException("Cannot access property: " + parts[i] + " on " + current.getClass().getSimpleName(), e);
                }
            }
        }

        return current;
    }
}
