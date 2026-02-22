package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * 防霉管控区域编码值对象
 */
@Getter
@EqualsAndHashCode
public class ZoneCode {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    private final String value;

    private ZoneCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("防霉管控区域编码不能为空");
        }
        String trimmed = value.trim();
        if (!CODE_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("防霉管控区域编码只能包含字母、数字、下划线和连字符");
        }
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("防霉管控区域编码不能超过50个字符");
        }
        this.value = trimmed.toUpperCase();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ZoneCode of(String value) {
        return new ZoneCode(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
