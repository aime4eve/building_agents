package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 执行ID值对象
 */
@Getter
@EqualsAndHashCode
public class ExecutionId {
    private final String value;

    private ExecutionId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("执行ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ExecutionId of(String value) {
        return new ExecutionId(value);
    }

    public static ExecutionId generate() {
        return new ExecutionId("EXEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
