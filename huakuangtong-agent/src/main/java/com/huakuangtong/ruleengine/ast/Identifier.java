package com.huakuangtong.ruleengine.ast;

import java.util.Objects;

/**
 * 标识符节点
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class Identifier implements ASTNode {
    private final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier identifier = (Identifier) o;
        return Objects.equals(name, identifier.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
