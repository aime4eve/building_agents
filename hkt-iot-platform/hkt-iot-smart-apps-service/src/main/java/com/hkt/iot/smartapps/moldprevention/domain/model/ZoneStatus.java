package com.hkt.iot.smartapps.moldprevention.domain.model;

/**
 * 防霉管控区域状态枚举
 */
public enum ZoneStatus {
    /**
     * 激活 - 正常运行
     */
    ACTIVE,

    /**
     * 停用 - 暂停运行
     */
    INACTIVE,

    /**
     * 维护 - 维护模式
     */
    MAINTENANCE
}
