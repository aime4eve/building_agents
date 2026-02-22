package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 体重值对象
 */
@Getter
@EqualsAndHashCode
public class Weight {

    private final BigDecimal value;
    private final String unit;  // kg, lb, etc.

    private Weight(BigDecimal value, String unit) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("体重必须大于0");
        }
        this.value = value;
        this.unit = unit != null ? unit : "kg";
    }

    @JsonValue
    public String getValue() {
        return value + unit;
    }

    public static Weight of(BigDecimal value, String unit) {
        return new Weight(value, unit);
    }

    public static Weight ofKilograms(double value) {
        return new Weight(BigDecimal.valueOf(value), "kg");
    }

    /**
     * 计算增重
     */
    public Weight gain(Weight other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("单位不一致");
        }
        return new Weight(this.value.add(other.value), this.unit);
    }

    /**
     * 转换为公斤
     */
    public Weight toKilograms() {
        if ("kg".equals(this.unit)) {
            return this;
        }
        if ("lb".equals(this.unit)) {
            return new Weight(this.value.multiply(BigDecimal.valueOf(0.453592)), "kg");
        }
        throw new UnsupportedOperationException("不支持的单位转换: " + this.unit);
    }

    @Override
    public String toString() {
        return value + unit;
    }
}
