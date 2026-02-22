package com.huakuangtong.iot.ingestion.service;

import com.huakuangtong.iot.ingestion.model.EventMessage;

/**
 * 设备事件处理服务
 *
 * 负责处理设备上报的事件数据，包括：
 * 1. 事件验证和分类
 * 2. 写入时序数据库
 * 3. 触发告警（ERROR级别事件）
 * 4. 通知规则引擎
 * 5. 发送事件通知
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public interface EventProcessingService {

    /**
     * 处理设备事件
     *
     * @param message 事件消息
     */
    void processEvent(EventMessage message);

    /**
     * 批量处理设备事件
     *
     * @param messages 事件消息列表
     */
    void processEventBatch(java.util.List<EventMessage> messages);
}
