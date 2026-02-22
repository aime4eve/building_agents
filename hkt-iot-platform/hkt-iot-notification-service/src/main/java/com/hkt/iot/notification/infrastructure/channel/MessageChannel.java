package com.hkt.iot.notification.infrastructure.channel;

import java.util.Map;

/**
 * 消息渠道接口
 * 定义各种通知渠道的统一发送接口
 *
 * @author HKT IoT Team
 */
public interface MessageChannel {

    /**
     * 发送消息
     *
     * @param payload 消息负载
     * @return 响应结果
     * @throws Exception 发送异常
     */
    String send(Map<String, Object> payload) throws Exception;

    /**
     * 获取渠道类型
     *
     * @return 渠道类型
     */
    String getChannelType();

    /**
     * 检查渠道是否可用
     *
     * @return 是否可用
     */
    default boolean isAvailable() {
        return true;
    }
}
