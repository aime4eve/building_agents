package com.hkt.iot.rule.domain.model;

/**
 * 优先级值对象
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class Priority {
    private final int value;

    public Priority(int value) {
        if (value < 1 || value > 10) {
            throw new IllegalArgumentException("Priority must be between 1 and 10");
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Priority of(int value) {
        return new Priority(value);
    }

    public static Priority low() {
        return new Priority(1);
    }

    public static Priority medium() {
        return new Priority(5);
    }

    public static Priority high() {
        return new Priority(10);
    }

    public boolean isHigherThan(Priority other) {
        return this.value > other.value;
    }

    public boolean isLowerThan(Priority other) {
        return this.value < other.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Priority priority = (Priority) o;
        return value == priority.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
