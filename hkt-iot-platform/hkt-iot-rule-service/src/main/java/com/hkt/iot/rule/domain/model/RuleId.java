package com.hkt.iot.rule.domain.model;

import java.util.Objects;

/**
 * 规则ID值对象
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleId {
    private final Long value;

    public RuleId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Rule ID must be positive");
        }
        this.value = value;
    }

    public Long getValue() {
        return value;
    }

    public static RuleId of(Long value) {
        return new RuleId(value);
    }

    public static RuleId generate() {
        return new RuleId(IdGenerator.generateId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleId ruleId = (RuleId) o;
        return Objects.equals(value, ruleId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "RuleId{" + value + "}";
    }
}
