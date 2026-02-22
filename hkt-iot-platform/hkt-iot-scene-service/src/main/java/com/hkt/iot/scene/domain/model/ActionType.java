package com.hkt.iot.scene.domain.model;

/**
 * 动作类型枚举
 */
public enum ActionType {
    /**
     * 设备控制 - 调用设备服务
     */
    DEVICE_CONTROL,

    /**
     * 场景联动 - 触发另一个场景
     */
    SCENE_SWITCH,

    /**
     * 发送通知 - 通过通知中心发送消息
     */
    NOTIFY,

    /**
     * 延迟执行 - 等待指定时间后执行后续动作
     */
    DELAY
}
