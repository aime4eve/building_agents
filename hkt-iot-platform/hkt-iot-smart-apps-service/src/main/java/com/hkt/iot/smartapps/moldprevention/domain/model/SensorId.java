package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 传感器ID值对象
 */
@Getter
@EqualsAndHashCode
public class SensorId {
    private final String value;

    private SensorId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("传感器ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static SensorId of(String value) {
        return new SensorId(value);
    }

    public static SensorId generate() {
        return new SensorId("SENSOR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
