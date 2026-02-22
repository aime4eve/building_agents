package com.hkt.iot.smartapps.smartlivestock.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

/**
 * 报告ID值对象
 */
@Getter
@EqualsAndHashCode
public class ReportId {
    private final String value;

    private ReportId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("报告ID不能为空");
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static ReportId of(String value) {
        return new ReportId(value);
    }

    public static ReportId generate() {
        return new ReportId("LRPT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
    }

    @Override
    public String toString() {
        return value;
    }
}
