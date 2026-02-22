package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 触发条件ID值对象
 */
@Getter
@EqualsAndHashCode
public class TriggerId {
    private final String value;

    private TriggerId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("触发条件ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TriggerId of(String value) {
        return new TriggerId(value);
    }

    public static TriggerId generate() {
        return new TriggerId("TRIG-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
