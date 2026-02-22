package com.huakuangtong.iot.ingestion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakuangtong.iot.ingestion.model.StatusMessage;
import com.huakuangtong.iot.ingestion.service.StatusSyncService;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 设备状态同步服务实现
 *
 * 负责同步设备状态到数据库和缓存
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatusSyncServiceImpl implements StatusSyncService {

    private final ObjectMapper objectMapper;
    private final InfluxDBClient influxDBClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JdbcTemplate jdbcTemplate;

    private static final String STATUS_BUCKET = "hkt_iot_status";
    private static final String ORG = "hkt_iot";
    private static final String DEVICE_CACHE_KEY_PREFIX = "device:status:";
    private static final String HEARTBEAT_KEY_PREFIX = "device:heartbeat:";

    // 设备状态枚举
    private static final String STATUS_ONLINE = "ONLINE";
    private static final String STATUS_OFFLINE = "OFFLINE";
    private static final String STATUS_FAULT = "FAULT";
    private static final String STATUS_MAINTENANCE = "MAINTENANCE";
    private static final String STATUS_INACTIVE = "INACTIVE";

    @Override
    @Transactional
    public void syncStatus(StatusMessage message) {
        try {
            // 1. 验证消息
            validateMessage(message);

            // 2. 更新数据库
            updateDeviceStatusInDB(message);

            // 3. 更新缓存
            updateDeviceStatusCache(message);

            // 4. 写入时序数据库（状态变更历史）
            writeStatusHistory(message);

            // 5. 更新心跳时间
            updateHeartbeat(message.getDeviceId(), message.getTimestamp());

            // 6. 发布状态变更事件
            publishStatusChangeEvent(message);

            log.debug("Device status synced, deviceId: {}, status: {}",
                message.getDeviceId(), message.getStatus());

        } catch (Exception e) {
            log.error("Failed to sync device status, deviceId: {}, status: {}",
                message.getDeviceId(), message.getStatus(), e);
            throw new StatusSyncException("Failed to sync device status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void syncStatusBatch(List<StatusMessage> messages) {
        long startTime = System.currentTimeMillis();

        try {
            WriteApi writeApi = influxDBClient.getWriteApi();

            for (StatusMessage message : messages) {
                try {
                    // 更新数据库
                    updateDeviceStatusInDB(message);

                    // 更新缓存
                    updateDeviceStatusCache(message);

                    // 创建写入点
                    Point point = convertStatusToPoint(message);
                    writeApi.writePoint(STATUS_BUCKET, ORG, point);

                    // 更新心跳时间
                    updateHeartbeat(message.getDeviceId(), message.getTimestamp());

                } catch (Exception e) {
                    log.error("Failed to sync status in batch, deviceId: {}", message.getDeviceId(), e);
                }
            }

            // 强制刷新
            writeApi.flush();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Batch status sync completed, count: {}, duration: {} ms",
                messages.size(), duration);

        } catch (Exception e) {
            log.error("Failed to sync status batch", e);
            throw new StatusSyncException("Failed to sync status batch: " + e.getMessage(), e);
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void checkHeartbeatTimeout() {
        log.debug("Checking device heartbeat timeout...");

        try {
            // 查询所有在线设备
            String sql = "SELECT device_id, last_communicated_at FROM device WHERE status = 'ONLINE'";
            List<Map<String, Object>> onlineDevices = jdbcTemplate.queryForList(sql);

            int timeoutCount = 0;
            long timeoutThreshold = System.currentTimeMillis() - 180000; // 3分钟前

            for (Map<String, Object> device : onlineDevices) {
                String deviceId = (String) device.get("device_id");
                Object lastCommObj = device.get("last_communicated_at");

                if (lastCommObj instanceof java.sql.Timestamp) {
                    java.sql.Timestamp lastComm = (java.sql.Timestamp) lastCommObj;
                    if (lastComm.getTime() < timeoutThreshold) {
                        // 设备心跳超时，标记为离线
                        markDeviceOffline(deviceId, "heartbeat_timeout");
                        timeoutCount++;
                    }
                }
            }

            if (timeoutCount > 0) {
                log.warn("Heartbeat timeout detected, {} devices marked as offline", timeoutCount);
            }

        } catch (Exception e) {
            log.error("Failed to check heartbeat timeout", e);
        }
    }

    /**
     * 验证消息数据
     */
    private void validateMessage(StatusMessage message) {
        if (message.getDeviceId() == null || message.getDeviceId().isEmpty()) {
            throw new IllegalArgumentException("deviceId is required");
        }
        if (message.getStatus() == null || message.getStatus().isEmpty()) {
            throw new IllegalArgumentException("status is required");
        }
        if (message.getTimestamp() == null) {
            message.setTimestamp(System.currentTimeMillis());
        }
    }

    /**
     * 更新数据库中的设备状态
     */
    private void updateDeviceStatusInDB(StatusMessage message) {
        String sql = """
            UPDATE device
            SET status = ?,
                last_communicated_at = FROM_UNIXTIME(? / 1000),
                updated_at = NOW()
            WHERE device_id = ?
            """;

        int updated = jdbcTemplate.update(
            sql,
            message.getStatus(),
            message.getTimestamp() / 1000,
            message.getDeviceId()
        );

        if (updated == 0) {
            log.warn("Device not found in database, deviceId: {}", message.getDeviceId());
        }
    }

    /**
     * 更新设备状态缓存
     */
    private void updateDeviceStatusCache(StatusMessage message) {
        String key = DEVICE_CACHE_KEY_PREFIX + message.getDeviceId();

        Map<String, Object> status = Map.of(
            "deviceId", message.getDeviceId(),
            "status", message.getStatus(),
            "lastCommunicatedAt", message.getTimestamp(),
            "updatedAt", System.currentTimeMillis()
        );

        redisTemplate.opsForValue().set(key, status, 5, TimeUnit.MINUTES);
    }

    /**
     * 写入状态变更历史到InfluxDB
     */
    private void writeStatusHistory(StatusMessage message) {
        Point point = convertStatusToPoint(message);

        try {
            WriteApi writeApi = influxDBClient.getWriteApi();
            writeApi.writePoint(STATUS_BUCKET, ORG, point);
        } catch (Exception e) {
            log.error("Failed to write status history to InfluxDB, deviceId: {}",
                message.getDeviceId(), e);
        }
    }

    /**
     * 转换状态为InfluxDB Point
     */
    private Point convertStatusToPoint(StatusMessage message) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(message.getTimestamp()),
            ZoneId.systemDefault()
        );

        Point point = Point.measurement("device_status")
            .time(zonedDateTime.toInstant(), WritePrecision.MS)
            .addTag("device_id", message.getDeviceId())
            .addTag("device_type", message.getDeviceType() != null ?
                message.getDeviceType().toLowerCase() : "unknown")
            .addTag("status", message.getStatus());

        // 添加原因字段
        if (message.getReason() != null) {
            point.addField("reason", message.getReason());
        }

        // 添加连接信息字段
        if (message.getConnectionInfo() != null) {
            if (message.getConnectionInfo().getIpAddress() != null) {
                point.addField("ip_address", message.getConnectionInfo().getIpAddress());
            }
            if (message.getConnectionInfo().getProtocol() != null) {
                point.addField("protocol", message.getConnectionInfo().getProtocol());
            }
        }

        return point;
    }

    /**
     * 更新心跳时间
     */
    private void updateHeartbeat(String deviceId, Long timestamp) {
        String key = HEARTBEAT_KEY_PREFIX + deviceId;
        redisTemplate.opsForValue().set(key, timestamp.toString(), 5, TimeUnit.MINUTES);
    }

    /**
     * 标记设备离线
     */
    private void markDeviceOffline(String deviceId, String reason) {
        StatusMessage message = new StatusMessage();
        message.setDeviceId(deviceId);
        message.setStatus(STATUS_OFFLINE);
        message.setTimestamp(System.currentTimeMillis());
        message.setReason(reason);

        syncStatus(message);

        // 发布离线事件
        log.info("Device marked as offline, deviceId: {}, reason: {}", deviceId, reason);
    }

    /**
     * 发布状态变更事件
     * TODO: 实现事件发布逻辑
     */
    private void publishStatusChangeEvent(StatusMessage message) {
        log.debug("Status change event would be published, deviceId: {}, status: {}",
            message.getDeviceId(), message.getStatus());
    }
}
