package com.huakuangtong.ruleengine.ast;

/**
 * 一元表达式节点
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class UnaryExpression implements ASTNode {
    private final UnaryOperator operator;
    private final ASTNode operand;

    public UnaryExpression(UnaryOperator operator, ASTNode operand) {
        this.operator = operator;
        this.operand = operand;
    }

    public UnaryOperator getOperator() {
        return operator;
    }

    public ASTNode getOperand() {
        return operand;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return operator + "(" + operand + ")";
    }
}
