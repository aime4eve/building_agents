package com.hkt.iot.rule.engine.ast;

/**
 * AST访问者接口
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public interface ASTVisitor<T> {
    /**
     * 访问二元表达式节点
     */
    T visit(BinaryExpression node);

    /**
     * 访问一元表达式节点
     */
    T visit(UnaryExpression node);

    /**
     * 访问函数调用节点
     */
    T visit(FunctionCall node);

    /**
     * 访问标识符节点
     */
    T visit(Identifier node);

    /**
     * 访问字面量节点
     */
    T visit(Literal node);

    /**
     * 访问数组节点
     */
    T visit(ArrayLiteral node);
}
