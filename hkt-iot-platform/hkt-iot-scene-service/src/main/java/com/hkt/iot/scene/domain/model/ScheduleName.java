package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 定时计划名称值对象
 */
@Getter
@EqualsAndHashCode
public class ScheduleName {
    private final String value;

    private ScheduleName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("定时计划名称不能为空");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("定时计划名称不能超过100个字符");
        }
        this.value = value.trim();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ScheduleName of(String value) {
        return new ScheduleName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
