package com.hkt.iot.rule.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

/**
 * 设备遥测数据接收事件
 * 当设备上报遥测数据时发布
 *
 * @author HKT IoT Team
 */
@Getter
public class TelemetryReceivedEvent extends AbstractDomainEvent {

    private final String deviceId;
    private final String deviceSn;
    private final String deviceType;
    private final Long tenantId;
    private final Long spaceId;
    private final Map<String, Object> telemetryData;
    private final Instant timestamp;
    private final Map<String, Object> metadata;

    public TelemetryReceivedEvent(
            String deviceId,
            String deviceSn,
            String deviceType,
            Long tenantId,
            Long spaceId,
            Map<String, Object> telemetryData,
            Instant timestamp,
            Map<String, Object> metadata) {
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.deviceType = deviceType;
        this.tenantId = tenantId;
        this.spaceId = spaceId;
        this.telemetryData = telemetryData;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.metadata = metadata;
    }

    @Override
    public String getAggregateId() {
        return deviceId;
    }

    @Override
    public String getAggregateType() {
        return "Device";
    }

    @Override
    public String eventType() {
        return "TelemetryReceived";
    }

    /**
     * 获取指定遥测值
     */
    public Object getTelemetryValue(String key) {
        return telemetryData != null ? telemetryData.get(key) : null;
    }

    /**
     * 获取指定元数据值
     */
    public Object getMetadataValue(String key) {
        return metadata != null ? metadata.get(key) : null;
    }
}
