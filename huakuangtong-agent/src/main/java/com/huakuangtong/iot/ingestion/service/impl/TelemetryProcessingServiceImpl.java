package com.huakuangtong.iot.ingestion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakuangtong.iot.ingestion.model.TelemetryMessage;
import com.huakuangtong.iot.ingestion.service.TelemetryProcessingService;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 遥测数据处理服务实现
 *
 * 负责处理设备上报的遥测数据
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryProcessingServiceImpl implements TelemetryProcessingService {

    private final ObjectMapper objectMapper;
    private final InfluxDBClient influxDBClient;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TELEMETRY_BUCKET = "hkt_iot_raw";
    private static final String ORG = "hkt_iot";
    private static final String IDEM_POTENCY_KEY_PREFIX = "telemetry:idem:";
    private static final String DEVICE_STATUS_KEY_PREFIX = "device:status:";

    @Override
    public void processTelemetry(TelemetryMessage message) {
        try {
            // 1. 幂等性检查
            if (!checkIdempotency(message.getMsgId(), message.getDeviceId())) {
                log.debug("Duplicate telemetry message ignored, msgId: {}", message.getMsgId());
                return;
            }

            // 2. 数据验证
            validateMessage(message);

            // 3. 写入时序数据库（InfluxDB）
            writeToInfluxDB(message);

            // 4. 更新设备状态缓存
            updateDeviceStatusCache(message);

            // 5. 发布遥测上报事件（可选，用于触发规则引擎）
            publishTelemetryEvent(message);

            log.trace("Telemetry processed successfully, deviceId: {}, msgId: {}",
                message.getDeviceId(), message.getMsgId());

        } catch (Exception e) {
            log.error("Failed to process telemetry, deviceId: {}, msgId: {}",
                message.getDeviceId(), message.getMsgId(), e);
            throw new TelemetryProcessingException("Failed to process telemetry: " + e.getMessage(), e);
        }
    }

    @Override
    public void processTelemetryBatch(List<TelemetryMessage> messages) {
        long startTime = System.currentTimeMillis();

        try {
            WriteApi writeApi = influxDBClient.getWriteApi();

            for (TelemetryMessage message : messages) {
                try {
                    // 幂等性检查
                    if (!checkIdempotency(message.getMsgId(), message.getDeviceId())) {
                        continue;
                    }

                    // 创建写入点
                    Point point = convertToPoint(message);
                    writeApi.writePoint(TELEMETRY_BUCKET, ORG, point);

                    // 更新缓存
                    updateDeviceStatusCache(message);

                } catch (Exception e) {
                    log.error("Failed to process telemetry in batch, msgId: {}", message.getMsgId(), e);
                }
            }

            // 强制刷新
            writeApi.flush();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Batch telemetry processing completed, count: {}, duration: {} ms",
                messages.size(), duration);

        } catch (Exception e) {
            log.error("Failed to process telemetry batch", e);
            throw new TelemetryProcessingException("Failed to process telemetry batch: " + e.getMessage(), e);
        }
    }

    /**
     * 幂等性检查
     * 使用Redis记录已处理的消息ID，防止重复处理
     */
    private boolean checkIdempotency(String msgId, String deviceId) {
        if (msgId == null || msgId.isEmpty()) {
            return false;
        }

        String key = IDEM_POTENCY_KEY_PREFIX + deviceId + ":" + msgId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", 1, TimeUnit.HOURS);

        return Boolean.TRUE.equals(isNew);
    }

    /**
     * 验证消息数据
     */
    private void validateMessage(TelemetryMessage message) {
        if (message.getDeviceId() == null || message.getDeviceId().isEmpty()) {
            throw new IllegalArgumentException("deviceId is required");
        }
        if (message.getTimestamp() == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
        if (message.getData() == null || message.getData().isEmpty()) {
            throw new IllegalArgumentException("data is required");
        }
    }

    /**
     * 写入InfluxDB
     */
    private void writeToInfluxDB(TelemetryMessage message) {
        Point point = convertToPoint(message);

        try {
            WriteApi writeApi = influxDBClient.getWriteApi();
            writeApi.writePoint(TELEMETRY_BUCKET, ORG, point);
        } catch (Exception e) {
            log.error("Failed to write to InfluxDB, deviceId: {}", message.getDeviceId(), e);
            throw e;
        }
    }

    /**
     * 转换为InfluxDB Point
     */
    private Point convertToPoint(TelemetryMessage message) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(message.getTimestamp()),
            ZoneId.systemDefault()
        );

        Point point = Point.measurement("device_telemetry")
            .time(zonedDateTime.toInstant(), WritePrecision.MS)
            .addTag("device_id", message.getDeviceId())
            .addTag("device_type", message.getDeviceType() != null ?
                message.getDeviceType().toLowerCase() : "unknown")
            .addTag("tenant_id", extractTenantId(message.getDeviceId()));

        // 添加数据字段
        if (message.getData() != null) {
            for (Map.Entry<String, Object> entry : message.getData().entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Number) {
                    point.addField(entry.getKey(), ((Number) value).doubleValue());
                } else if (value instanceof Boolean) {
                    point.addField(entry.getKey(), (Boolean) value);
                } else {
                    point.addField(entry.getKey(), value.toString());
                }
            }
        }

        // 添加元数据字段
        if (message.getMetadata() != null) {
            if (message.getMetadata().getBattery() != null) {
                point.addField("battery", message.getMetadata().getBattery());
            }
            if (message.getMetadata().getRssi() != null) {
                point.addField("rssi", message.getMetadata().getRssi());
            }
        }

        return point;
    }

    /**
     * 更新设备状态缓存
     */
    private void updateDeviceStatusCache(TelemetryMessage message) {
        String key = DEVICE_STATUS_KEY_PREFIX + message.getDeviceId();

        Map<String, Object> status = Map.of(
            "deviceId", message.getDeviceId(),
            "lastCommunicatedAt", message.getTimestamp(),
            "status", "ONLINE"
        );

        redisTemplate.opsForValue().set(key, status, 5, TimeUnit.MINUTES);
    }

    /**
     * 发布遥测上报事件
     * TODO: 实现事件发布逻辑
     */
    private void publishTelemetryEvent(TelemetryMessage message) {
        // 将通过Kafka或Spring Events发布
        log.debug("Telemetry event would be published for deviceId: {}", message.getDeviceId());
    }

    /**
     * 从设备ID提取租户ID
     * TODO: 根据实际租户ID生成规则调整
     */
    private String extractTenantId(String deviceId) {
        // 假设设备ID格式为: tenant_xxx_dev_xxx
        if (deviceId.contains("_")) {
            return deviceId.substring(0, deviceId.indexOf("_", deviceId.indexOf("_") + 1));
        }
        return "default";
    }
}
