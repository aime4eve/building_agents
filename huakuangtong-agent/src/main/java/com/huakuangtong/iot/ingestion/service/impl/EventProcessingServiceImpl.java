package com.huakuangtong.iot.ingestion.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huakuangtong.iot.ingestion.model.EventMessage;
import com.huakuangtong.iot.ingestion.service.EventProcessingService;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 设备事件处理服务实现
 *
 * 负责处理设备上报的事件数据（告警、故障等）
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessingServiceImpl implements EventProcessingService {

    private final ObjectMapper objectMapper;
    private final InfluxDBClient influxDBClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String EVENT_BUCKET = "hkt_iot_event";
    private static final String ORG = "hkt_iot";
    private static final String ALARM_TOPIC = "device-alarm";
    private static final String IDEM_POTENCY_KEY_PREFIX = "event:idem:";

    @Override
    public void processEvent(EventMessage message) {
        try {
            // 1. 幂等性检查
            if (!checkIdempotency(message.getMsgId(), message.getDeviceId())) {
                log.debug("Duplicate event message ignored, msgId: {}", message.getMsgId());
                return;
            }

            // 2. 数据验证
            validateMessage(message);

            // 3. 写入时序数据库
            writeEventToInfluxDB(message);

            // 4. ERROR级别事件需要特殊处理
            if ("ERROR".equalsIgnoreCase(message.getEventLevel())) {
                handleAlarmEvent(message);
            }

            // 5. 发布设备事件
            publishDeviceEvent(message);

            log.trace("Event processed successfully, deviceId: {}, eventType: {}, level: {}",
                message.getDeviceId(), message.getEventType(), message.getEventLevel());

        } catch (Exception e) {
            log.error("Failed to process event, deviceId: {}, msgId: {}",
                message.getDeviceId(), message.getMsgId(), e);
            throw new EventProcessingException("Failed to process event: " + e.getMessage(), e);
        }
    }

    @Override
    public void processEventBatch(List<EventMessage> messages) {
        long startTime = System.currentTimeMillis();

        try {
            WriteApi writeApi = influxDBClient.getWriteApi();

            for (EventMessage message : messages) {
                try {
                    // 幂等性检查
                    if (!checkIdempotency(message.getMsgId(), message.getDeviceId())) {
                        continue;
                    }

                    // 创建写入点
                    Point point = convertEventToPoint(message);
                    writeApi.writePoint(EVENT_BUCKET, ORG, point);

                    // 处理告警事件
                    if ("ERROR".equalsIgnoreCase(message.getEventLevel())) {
                        handleAlarmEvent(message);
                    }

                } catch (Exception e) {
                    log.error("Failed to process event in batch, msgId: {}", message.getMsgId(), e);
                }
            }

            // 强制刷新
            writeApi.flush();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Batch event processing completed, count: {}, duration: {} ms",
                messages.size(), duration);

        } catch (Exception e) {
            log.error("Failed to process event batch", e);
            throw new EventProcessingException("Failed to process event batch: " + e.getMessage(), e);
        }
    }

    /**
     * 幂等性检查
     */
    private boolean checkIdempotency(String msgId, String deviceId) {
        if (msgId == null || msgId.isEmpty()) {
            return false;
        }

        String key = IDEM_POTENCY_KEY_PREFIX + deviceId + ":" + msgId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(key, "1", 7, TimeUnit.DAYS);

        return Boolean.TRUE.equals(isNew);
    }

    /**
     * 验证消息数据
     */
    private void validateMessage(EventMessage message) {
        if (message.getDeviceId() == null || message.getDeviceId().isEmpty()) {
            throw new IllegalArgumentException("deviceId is required");
        }
        if (message.getTimestamp() == null) {
            throw new IllegalArgumentException("timestamp is required");
        }
        if (message.getEventType() == null || message.getEventType().isEmpty()) {
            throw new IllegalArgumentException("eventType is required");
        }
        if (message.getEventLevel() == null || message.getEventLevel().isEmpty()) {
            throw new IllegalArgumentException("eventLevel is required");
        }
    }

    /**
     * 写入InfluxDB
     */
    private void writeEventToInfluxDB(EventMessage message) {
        Point point = convertEventToPoint(message);

        try {
            WriteApi writeApi = influxDBClient.getWriteApi();
            writeApi.writePoint(EVENT_BUCKET, ORG, point);
        } catch (Exception e) {
            log.error("Failed to write event to InfluxDB, deviceId: {}", message.getDeviceId(), e);
            throw e;
        }
    }

    /**
     * 转换事件为InfluxDB Point
     */
    private Point convertEventToPoint(EventMessage message) {
        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(
            Instant.ofEpochMilli(message.getTimestamp()),
            ZoneId.systemDefault()
        );

        Point point = Point.measurement("device_event")
            .time(zonedDateTime.toInstant(), WritePrecision.MS)
            .addTag("device_id", message.getDeviceId())
            .addTag("device_type", message.getDeviceType() != null ?
                message.getDeviceType().toLowerCase() : "unknown")
            .addTag("event_type", message.getEventType())
            .addTag("event_level", message.getEventLevel());

        // 添加事件数据字段
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
            for (Map.Entry<String, Object> entry : message.getMetadata().entrySet()) {
                point.addField("meta_" + entry.getKey(), entry.getValue().toString());
            }
        }

        return point;
    }

    /**
     * 处理告警事件
     * 发送到Kafka告警Topic，供告警服务消费
     */
    private void handleAlarmEvent(EventMessage message) {
        try {
            String alarmMessage = objectMapper.writeValueAsString(message);

            kafkaTemplate.send(ALARM_TOPIC, message.getDeviceId(), alarmMessage)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send alarm to Kafka, deviceId: {}",
                            message.getDeviceId(), ex);
                    } else {
                        log.debug("Alarm sent to Kafka, deviceId: {}, partition: {}, offset: {}",
                            message.getDeviceId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                    }
                });

        } catch (Exception e) {
            log.error("Failed to serialize alarm event, deviceId: {}", message.getDeviceId(), e);
        }
    }

    /**
     * 发布设备事件
     * TODO: 实现事件发布逻辑
     */
    private void publishDeviceEvent(EventMessage message) {
        log.debug("Device event would be published, deviceId: {}, eventType: {}",
            message.getDeviceId(), message.getEventType());
    }
}
