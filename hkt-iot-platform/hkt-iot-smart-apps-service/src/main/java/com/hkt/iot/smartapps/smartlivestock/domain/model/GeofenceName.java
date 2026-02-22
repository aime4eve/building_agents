package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 电子围栏名称值对象
 */
@Getter
@EqualsAndHashCode
public class GeofenceName {
    private final String value;

    private GeofenceName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("电子围栏名称不能为空");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("电子围栏名称不能超过100个字符");
        }
        this.value = value.trim();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static GeofenceName of(String value) {
        return new GeofenceName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
