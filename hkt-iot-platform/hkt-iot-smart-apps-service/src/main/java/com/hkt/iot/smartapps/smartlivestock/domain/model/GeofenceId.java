package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 电子围栏ID值对象
 */
@Getter
@EqualsAndHashCode
public class GeofenceId {
    private final String value;

    private GeofenceId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("电子围栏ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static GeofenceId of(String value) {
        return new GeofenceId(value);
    }

    public static GeofenceId generate() {
        return new GeofenceId("GF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
