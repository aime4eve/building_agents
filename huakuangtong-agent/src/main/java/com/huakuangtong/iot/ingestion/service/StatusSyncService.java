package com.huakuangtong.iot.ingestion.service;

import com.huakuangtong.iot.ingestion.model.StatusMessage;

/**
 * 设备状态同步服务
 *
 * 负责同步设备状态，包括：
 * 1. 更新设备状态（MySQL）
 * 2. 更新设备缓存（Redis）
 * 3. 发送状态变更事件
 * 4. 触发相关业务逻辑
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
public interface StatusSyncService {

    /**
     * 同步设备状态
     *
     * @param message 状态消息
     */
    void syncStatus(StatusMessage message);

    /**
     * 批量同步设备状态
     *
     * @param messages 状态消息列表
     */
    void syncStatusBatch(java.util.List<StatusMessage> messages);

    /**
     * 检查设备心跳超时
     *
     * 定时任务调用，检查所有在线设备的心跳时间
     * 超时设备标记为离线
     */
    void checkHeartbeatTimeout();
}
