package com.hkt.iot.smartapps.smartlivestock.domain.model;

/**
 * 性别枚举
 */
public enum Gender {

    /**
     * 雄性
     */
    MALE("公"),

    /**
     * 雌性
     */
    FEMALE("母");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
