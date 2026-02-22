package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 动作ID值对象
 */
@Getter
@EqualsAndHashCode
public class ActionId {
    private final String value;

    private ActionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("动作ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ActionId of(String value) {
        return new ActionId(value);
    }

    public static ActionId generate() {
        return new ActionId("ACT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
