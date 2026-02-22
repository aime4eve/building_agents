package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 健康评分值对象
 */
@Getter
@EqualsAndHashCode
public class HealthScore {

    private final int value;  // 0-100
    private final HealthLevel level;

    private HealthScore(int value, HealthLevel level) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("健康评分必须在0-100之间");
        }
        this.value = value;
        this.level = level != null ? level : calculateLevel(value);
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    public static HealthScore of(int value) {
        return new HealthScore(value, calculateLevel(value));
    }

    public static HealthScore initial() {
        return new HealthScore(80, HealthLevel.GOOD);
    }

    /**
     * 根据评分计算等级
     */
    private static HealthLevel calculateLevel(int value) {
        if (value >= 90) {
            return HealthLevel.EXCELLENT;
        } else if (value >= 75) {
            return HealthLevel.GOOD;
        } else if (value >= 60) {
            return HealthLevel.FAIR;
        } else if (value >= 40) {
            return HealthLevel.POOR;
        } else {
            return HealthLevel.CRITICAL;
        }
    }

    /**
     * 判断是否健康
     */
    public boolean isHealthy() {
        return level.ordinal() <= HealthLevel.GOOD.ordinal();
    }

    /**
     * 判断是否需要关注
     */
    public boolean needsAttention() {
        return level.ordinal() >= HealthLevel.POOR.ordinal();
    }

    /**
     * 改善评分
     */
    public HealthScore improve(int amount) {
        int newValue = Math.min(100, this.value + amount);
        return new HealthScore(newValue, calculateLevel(newValue));
    }

    /**
     * 降低评分
     */
    public HealthScore decline(int amount) {
        int newValue = Math.max(0, this.value - amount);
        return new HealthScore(newValue, calculateLevel(newValue));
    }

    @Override
    public String toString() {
        return value + " (" + level.getDescription() + ")";
    }
}
