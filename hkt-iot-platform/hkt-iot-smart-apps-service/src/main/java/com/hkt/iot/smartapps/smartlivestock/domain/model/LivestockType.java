package com.hkt.iot.smartapps.smartlivestock.domain.model;

/**
 * 牲畜类型枚举
 */
public enum LivestockType {

    /**
     * 牛
     */
    CATTLE("牛", 18),

    /**
     * 羊
     */
    SHEEP("羊", 12),

    /**
     * 猪
     */
    PIG("猪", 6),

    /**
     * 山羊
     */
    GOAT("山羊", 12),

    /**
     * 马
     */
    HORSE("马", 48);

    private final String displayName;
    private final int adultAgeMonths;  // 成年月龄

    LivestockType(String displayName, int adultAgeMonths) {
        this.displayName = displayName;
        this.adultAgeMonths = adultAgeMonths;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAdultAgeMonths() {
        return adultAgeMonths;
    }
}
