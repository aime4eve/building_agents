package com.huakuangtong.ruleengine.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 数组字面量节点
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class ArrayLiteral implements ASTNode {
    private final List<ASTNode> elements;

    public ArrayLiteral(List<ASTNode> elements) {
        this.elements = elements != null ? new ArrayList<>(elements) : new ArrayList<>();
    }

    public ArrayLiteral() {
        this(Collections.emptyList());
    }

    public List<ASTNode> getElements() {
        return Collections.unmodifiableList(elements);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayLiteral that = (ArrayLiteral) o;
        return Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < elements.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(elements.get(i));
        }
        sb.append("]");
        return sb.toString();
    }
}
