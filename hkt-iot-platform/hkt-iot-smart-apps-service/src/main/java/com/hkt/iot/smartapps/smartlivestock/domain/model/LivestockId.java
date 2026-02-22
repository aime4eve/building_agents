package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 牲畜ID值对象
 */
@Getter
@EqualsAndHashCode
public class LivestockId {
    private final String value;

    private LivestockId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("牲畜ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static LivestockId of(String value) {
        return new LivestockId(value);
    }

    public static LivestockId generate() {
        return new LivestockId("LS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
