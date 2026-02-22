package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 防霉管控区域名称值对象
 */
@Getter
@EqualsAndHashCode
public class ZoneName {
    private final String value;

    private ZoneName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("防霉管控区域名称不能为空");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("防霉管控区域名称不能超过100个字符");
        }
        this.value = value.trim();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ZoneName of(String value) {
        return new ZoneName(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
