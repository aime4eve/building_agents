package com.huakuangtong.iot.ingestion.service;

import com.huakuangtong.iot.ingestion.model.TelemetryMessage;

/**
 * 遥测数据处理服务
 *
 * 负责处理设备上报的遥测数据，包括：
 * 1. 数据验证和清洗
 * 2. 写入时序数据库（InfluxDB/TDengine）
 * 3. 更新设备最新状态缓存
 * 4. 触发规则引擎检查
 * 5. 发送遥测上报事件
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public interface TelemetryProcessingService {

    /**
     * 处理遥测数据
     *
     * @param message 遥测数据消息
     */
    void processTelemetry(TelemetryMessage message);

    /**
     * 批量处理遥测数据
     *
     * @param messages 遥测数据消息列表
     */
    void processTelemetryBatch(java.util.List<TelemetryMessage> messages);
}
