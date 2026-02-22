package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 防霉管控区域ID值对象
 */
@Getter
@EqualsAndHashCode
public class ZoneId {
    private final String value;

    private ZoneId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("防霉管控区域ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ZoneId of(String value) {
        return new ZoneId(value);
    }

    public static ZoneId generate() {
        return new ZoneId("ZONE-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
