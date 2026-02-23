package com.hkt.iot.workflow.domain.model.valueobject;

/**
 * SLA 预警级别枚举
 *
 * @author HKT IoT Team
 */
public enum SLAWarningLevel {
    /**
     * 正常状态
     */
    NORMAL("正常", 0),

    /**
     * 预警状态 - 接近阈值
     */
    WARNING("预警", 1),

    /**
     * 严重状态 - 即将违规
     */
    CRITICAL("严重", 2),

    /**
     * 已违规状态
     */
    BREACHED("已违规", 3);

    private final String description;
    private final int severity;

    SLAWarningLevel(String description, int severity) {
        this.description = description;
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isMoreSevereThan(SLAWarningLevel other) {
        return this.severity > other.severity;
    }

    public static SLAWarningLevel fromSeverity(int severity) {
        for (SLAWarningLevel level : values()) {
            if (level.severity == severity) {
                return level;
            }
        }
        return NORMAL;
    }
}
