package com.hkt.iot.device.domain.event;

import com.hkt.iot.device.domain.model.DeviceCommand;
import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 设备命令执行领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class DeviceCommandExecutedEvent extends DomainEvent {

    private final Long commandId;
    private final String requestId;
    private final Long deviceId;
    private final String deviceSn;
    private final Long tenantId;
    private final DeviceCommand.CommandType commandType;
    private final DeviceCommand.CommandStatus commandStatus;
    private final String resultCode;
    private final String resultMessage;
    private final LocalDateTime executedAt;

    public DeviceCommandExecutedEvent(
            Long commandId,
            String requestId,
            Long deviceId,
            String deviceSn,
            Long tenantId,
            DeviceCommand.CommandType commandType,
            DeviceCommand.CommandStatus commandStatus,
            String resultCode,
            String resultMessage,
            LocalDateTime executedAt) {
        this.commandId = commandId;
        this.requestId = requestId;
        this.deviceId = deviceId;
        this.deviceSn = deviceSn;
        this.tenantId = tenantId;
        this.commandType = commandType;
        this.commandStatus = commandStatus;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.executedAt = executedAt;
    }

    @Override
    public String eventType() {
        return "DeviceCommandExecuted";
    }
}
