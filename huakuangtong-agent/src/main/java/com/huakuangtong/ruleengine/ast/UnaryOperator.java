package com.huakuangtong.ruleengine.ast;

/**
 * 一元操作符
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public enum UnaryOperator {
    NOT("!"),
    NEGATE("-");

    private final String symbol;

    UnaryOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
