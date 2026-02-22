package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 违规ID值对象
 */
@Getter
@EqualsAndHashCode
public class ViolationId {
    private final String value;

    private ViolationId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("违规ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ViolationId of(String value) {
        return new ViolationId(value);
    }

    public static ViolationId generate() {
        return new ViolationId("VIOL-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
