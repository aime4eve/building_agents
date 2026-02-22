package com.hkt.iot.device.domain.event;

import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 设备状态变更领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class DeviceStatusChangedEvent extends DomainEvent {

    private final Long deviceId;
    private final String deviceSn;
    private final Long tenantId;
    private final Device.DeviceStatus oldStatus;
    private final Device.DeviceStatus newStatus;
    private final Boolean onlineStatus;
    private final LocalDateTime changedAt;

    public DeviceStatusChangedEvent(
            Long deviceId,
            String deviceSn,
            Long tenantId,
            Device.DeviceStatus oldStatus,
            Device.DeviceStatus newStatus,
            Boolean onlineStatus,
            LocalDateTime changedAt) {
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.tenantId = tenantId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.onlineStatus = onlineStatus;
        this.changedAt = changedAt;
    }

    @Override
    public String eventType() {
        return "DeviceStatusChanged";
    }
}
