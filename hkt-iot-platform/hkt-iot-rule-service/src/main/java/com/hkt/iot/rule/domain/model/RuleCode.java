package com.hkt.iot.rule.domain.model;

import java.util.Objects;

/**
 * 规则编码值对象
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleCode {
    private final String value;

    public RuleCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule code cannot be null or empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Rule code length cannot exceed 100 characters");
        }
        this.value = value.trim();
    }

    public String getValue() {
        return value;
    }

    public static RuleCode of(String value) {
        return new RuleCode(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleCode ruleCode = (RuleCode) o;
        return Objects.equals(value, ruleCode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
