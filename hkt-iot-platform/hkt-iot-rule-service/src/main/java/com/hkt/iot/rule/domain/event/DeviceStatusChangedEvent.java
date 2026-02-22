package com.hkt.iot.rule.domain.event;

import com.hkt.iot.domain.event.AbstractDomainEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * 设备状态变化事件
 * 当设备状态发生变化时发布
 *
 * @author HKT IoT Team
 */
@Getter
public class DeviceStatusChangedEvent extends AbstractDomainEvent {

    private final Long deviceId;
    private final String deviceSn;
    private final Long tenantId;
    private final String oldStatus;
    private final String newStatus;
    private final Instant changedAt;

    public DeviceStatusChangedEvent(
            Long deviceId,
            String deviceSn,
            Long tenantId,
            String oldStatus,
            String newStatus,
            Instant changedAt) {
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.tenantId = tenantId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt != null ? changedAt : Instant.now();
    }

    @Override
    public String getAggregateId() {
        return deviceId != null ? deviceId.toString() : null;
    }

    @Override
    public String getAggregateType() {
        return "Device";
    }

    @Override
    public String eventType() {
        return "DeviceStatusChanged";
    }

    /**
     * 判断是否为上线事件
     */
    public boolean isOnline() {
        return "ONLINE".equalsIgnoreCase(newStatus);
    }

    /**
     * 判断是否为离线事件
     */
    public boolean isOffline() {
        return "OFFLINE".equalsIgnoreCase(newStatus);
    }
}
