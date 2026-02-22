package com.hkt.iot.rule.engine.ast;

/**
 * AST节点基类
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public interface ASTNode {
    /**
     * 接受访问者
     */
    <T> T accept(ASTVisitor<T> visitor);
}
