package com.huakuangtong.ruleengine.ast;

/**
 * 二元表达式节点
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class BinaryExpression implements ASTNode {
    private final ASTNode left;
    private final BinaryOperator operator;
    private final ASTNode right;

    public BinaryExpression(ASTNode left, BinaryOperator operator, ASTNode right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ASTNode getLeft() {
        return left;
    }

    public BinaryOperator getOperator() {
        return operator;
    }

    public ASTNode getRight() {
        return right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "(" + left + " " + operator + " " + right + ")";
    }
}
