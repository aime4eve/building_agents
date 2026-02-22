package com.hkt.iot.smartapps.smartlivestock.domain.model;

/**
 * 健康等级枚举
 */
public enum HealthLevel {

    /**
     * 优秀 - 90-100分
     */
    EXCELLENT(5, "优秀", 90, 100),

    /**
     * 良好 - 75-89分
     */
    GOOD(4, "良好", 75, 89),

    /**
     * 一般 - 60-74分
     */
    FAIR(3, "一般", 60, 74),

    /**
     * 较差 - 40-59分
     */
    POOR(2, "较差", 40, 59),

    /**
     * 危急 - 0-39分
     */
    CRITICAL(1, "危急", 0, 39);

    private final int level;
    private final String description;
    private final int minScore;
    private final int maxScore;

    HealthLevel(int level, String description, int minScore, int maxScore) {
        this.level = level;
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
