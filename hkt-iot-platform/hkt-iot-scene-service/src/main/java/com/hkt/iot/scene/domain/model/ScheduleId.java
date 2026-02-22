package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 定时计划ID值对象
 */
@Getter
@EqualsAndHashCode
public class ScheduleId {
    private final String value;

    private ScheduleId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("定时计划ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ScheduleId of(String value) {
        return new ScheduleId(value);
    }

    public static ScheduleId generate() {
        return new ScheduleId("SCH-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
