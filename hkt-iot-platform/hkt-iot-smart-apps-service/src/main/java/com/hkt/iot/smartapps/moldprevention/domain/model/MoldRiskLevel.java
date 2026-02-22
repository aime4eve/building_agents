package com.hkt.iot.smartapps.moldprevention.domain.model;

/**
 * 霉菌风险等级枚举
 */
public enum MoldRiskLevel {

    /**
     * 低风险 - 温湿度条件不利于霉菌滋生
     */
    LOW(1, "低风险", 40, 50),

    /**
     * 中风险 - 温湿度条件可能滋生霉菌
     */
    MEDIUM(2, "中风险", 55, 65),

    /**
     * 高风险 - 温湿度条件利于霉菌滋生
     */
    HIGH(3, "高风险", 65, 75),

    /**
     * 极高风险 - 温湿度条件非常利于霉菌快速滋生
     */
    CRITICAL(4, "极高风险", 75, 85);

    private final int level;
    private final String description;
    private final double humidityThresholdLow;
    private final double humidityThresholdHigh;

    MoldRiskLevel(int level, String description, double humidityThresholdLow, double humidityThresholdHigh) {
        this.level = level;
        this.description = description;
        this.humidityThresholdLow = humidityThresholdLow;
        this.humidityThresholdHigh = humidityThresholdHigh;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public double getHumidityThresholdLow() {
        return humidityThresholdLow;
    }

    public double getHumidityThresholdHigh() {
        return humidityThresholdHigh;
    }

    /**
     * 判断是否为高风险
     */
    public boolean isHighRisk() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * 判断是否需要采取控制措施
     */
    public boolean requiresControl() {
        return this == MEDIUM || this == HIGH || this == CRITICAL;
    }
}
