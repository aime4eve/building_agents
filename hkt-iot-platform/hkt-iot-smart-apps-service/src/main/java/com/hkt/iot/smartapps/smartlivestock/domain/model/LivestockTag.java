package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 牲畜耳标号值对象
 */
@Getter
@EqualsAndHashCode
public class LivestockTag {
    private final String value;

    private LivestockTag(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("牲畜耳标号不能为空");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("牲畜耳标号不能超过50个字符");
        }
        this.value = value.trim().toUpperCase();
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static LivestockTag of(String value) {
        return new LivestockTag(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
