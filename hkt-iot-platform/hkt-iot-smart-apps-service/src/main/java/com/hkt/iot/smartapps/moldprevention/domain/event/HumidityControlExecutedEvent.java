package com.hkt.iot.smartapps.moldprevention.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 湿度控制执行领域事件
 */
@Getter
public class HumidityControlExecutedEvent extends DomainEvent {

    private final String zoneId;
    private final String zoneCode;
    private final Long tenantId;
    private final String riskLevel;
    private final List<ControlCommandInfo> commands;
    private final LocalDateTime executedAt;

    /**
     * 控制命令信息
     */
    @Getter
    public static class ControlCommandInfo {
        private final String controllerId;
        private final String controllerType;
        private final String commandType;

        public ControlCommandInfo(String controllerId, String controllerType, String commandType) {
            this.controllerId = controllerId;
            this.controllerType = controllerType;
            this.commandType = commandType;
        }
    }

    public HumidityControlExecutedEvent(
            String zoneId,
            String zoneCode,
            Long tenantId,
            String riskLevel,
            List<ControlCommandInfo> commands,
            LocalDateTime executedAt) {
        this.zoneId = zoneId;
        this.zoneCode = zoneCode;
        this.tenantId = tenantId;
        this.riskLevel = riskLevel;
        this.commands = commands;
        this.executedAt = executedAt;
    }

    @Override
    public String eventType() {
        return "HumidityControlExecuted";
    }
}
