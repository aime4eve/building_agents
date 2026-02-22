package com.hkt.iot.device.domain.event;

import com.hkt.iot.device.domain.model.TelemetryData;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 遥测数据接收领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class TelemetryReceivedEvent extends DomainEvent {

    private final Long telemetryId;
    private final Long deviceId;
    private final String deviceSn;
    private final Long tenantId;
    private final TelemetryData.DataType dataType;
    private final Map<String, Object> data;
    private final LocalDateTime dataTime;
    private final LocalDateTime receivedAt;
    private final String eventId;
    private final String batchId;

    public TelemetryReceivedEvent(
            Long telemetryId,
            Long deviceId,
            String deviceSn,
            Long tenantId,
            TelemetryData.DataType dataType,
            Map<String, Object> data,
            LocalDateTime dataTime,
            LocalDateTime receivedAt,
            String eventId,
            String batchId) {
        this.telemetryId = telemetryId;
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.tenantId = tenantId;
        this.dataType = dataType;
        this.data = data;
        this.dataTime = dataTime;
        this.receivedAt = receivedAt;
        this.eventId = eventId;
        this.batchId = batchId;
    }

    @Override
    public String eventType() {
        return "TelemetryReceived";
    }
}
