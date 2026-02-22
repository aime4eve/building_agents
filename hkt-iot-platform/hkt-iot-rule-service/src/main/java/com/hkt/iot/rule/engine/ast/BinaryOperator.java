package com.hkt.iot.rule.engine.ast;

/**
 * 二元操作符
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public enum BinaryOperator {
    // 比较操作符
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("=="),
    NEQ("!="),
    CONTAINS("contains"),
    MATCHES("matches"),
    IN("in"),
    BETWEEN("between"),

    // 逻辑操作符
    AND("&&"),
    OR("||"),

    // 算术操作符
    ADD("+"),
    SUB("-"),
    MUL("*"),
    DIV("/"),
    MOD("%");

    private final String symbol;

    BinaryOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    /**
     * 获取操作符优先级
     */
    public int getPrecedence() {
        switch (this) {
            case OR:
                return 1;
            case AND:
                return 2;
            case EQ:
            case NEQ:
                return 3;
            case GT:
            case GTE:
            case LT:
            case LTE:
                return 4;
            case ADD:
            case SUB:
                return 5;
            case MUL:
            case DIV:
            case MOD:
                return 6;
            case CONTAINS:
            case MATCHES:
            case IN:
            case BETWEEN:
                return 7;
            default:
                return 0;
        }
    }

    /**
     * 是否是左结合的
     */
    public boolean isLeftAssociative() {
        return true;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
