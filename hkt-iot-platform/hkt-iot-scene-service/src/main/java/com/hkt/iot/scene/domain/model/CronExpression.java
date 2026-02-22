package com.hkt.iot.scene.domain.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * Cron表达式值对象
 */
@Getter
@EqualsAndHashCode
public class CronExpression {
    /**
     * 简化的Cron表达式验证（5段或6段）
     * 实际应使用Quartz的CronExpression进行验证
     */
    private static final Pattern CRON_PATTERN = Pattern.compile(
            "^([0-9]|[1-5][0-9]|\\*|\\?|[0-9]*-[0-9]*|[0-9]*/[0-9]+)\\s+" +
            "([0-9]|1[0-9]|2[0-3]|\\*|\\?|[0-9]*-[0-9]*|[0-9]*/[0-9]+)\\s+" +
            "([1-9]|[1-2][0-9]|3[0-1]|\\*|\\?|[1-9]*-[1-2][0-9]*|[1-9]*/[0-9]+)\\s+" +
            "([1-9]|1[0-2]|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC|\\*|\\?|" +
            "[1-9]*-[1-0][0-9]*|[1-9]*/[0-9]+)\\s+" +
            "([1-7]|MON|TUE|WED|THU|FRI|SAT|SUN|\\*|\\?|[1-7]*-[1-7]*|[1-7]*/[0-9]+)" +
            "(\\s+([1-9][0-9]*|\\*|\\?|[1-9]*-[0-9]*|[0-9]*/[0-9]+))?$"
    );

    private final String value;

    private CronExpression(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron表达式不能为空");
        }
        String trimmed = value.trim();
        // 简化验证，实际应使用Quartz的CronExpression
        if (!CRON_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Cron表达式格式不正确: " + value);
        }
        this.value = trimmed;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CronExpression of(String value) {
        return new CronExpression(value);
    }

    /**
     * 创建常用的预定义Cron表达式
     */
    public static CronExpression daily(int hour, int minute) {
        return new CronExpression(String.format("0 %d %d * * ?", minute, hour));
    }

    public static CronExpression weekly(int dayOfWeek, int hour, int minute) {
        return new CronExpression(String.format("0 %d %d ? * %d", minute, hour, dayOfWeek));
    }

    public static CronExpression monthly(int dayOfMonth, int hour, int minute) {
        return new CronExpression(String.format("0 %d %d %d * ?", minute, hour, dayOfMonth));
    }

    public static CronExpression hourly(int minute) {
        return new CronExpression(String.format("0 %d * * * ?", minute));
    }

    @Override
    public String toString() {
        return value;
    }
}
