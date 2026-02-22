package com.hkt.iot.rule.domain.model;

/**
 * 规则状态枚举
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public enum RuleStatus {
    /**
     * 草稿 - 规则正在编辑中，尚未激活
     */
    DRAFT("草稿"),

    /**
     * 激活 - 规则已激活，可以被执行
     */
    ACTIVE("激活"),

    /**
     * 暂停 - 规则已暂停执行
     */
    SUSPENDED("暂停"),

    /**
     * 归档 - 规则已归档，不再使用
     */
    ARCHIVED("归档");

    private final String description;

    RuleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean canActivate() {
        return this == DRAFT || this == SUSPENDED;
    }

    public boolean canSuspend() {
        return this == ACTIVE;
    }

    public boolean canArchive() {
        return this == DRAFT || this == SUSPENDED;
    }
}
