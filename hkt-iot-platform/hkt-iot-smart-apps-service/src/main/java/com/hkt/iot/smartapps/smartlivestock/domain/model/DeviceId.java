package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 设备ID值对象（智慧畜牧专用，避免与domain.shared冲突）
 */
@Getter
@EqualsAndHashCode
public class DeviceId {
    private final String value;

    private DeviceId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("设备ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static DeviceId of(String value) {
        return new DeviceId(value);
    }

    public static DeviceId generate() {
        return new DeviceId("DEV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
