package com.hkt.iot.workflow.domain.model.domainevent;

import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * SLA 违规领域事件
 * 当 SLA 已经违规时触发
 *
 * @author HKT IoT Team
 */
@Getter
public class SLABreachEvent implements DomainEvent {

    private final String eventId;
    private final SLAMonitorId slaMonitorId;
    private final ProcessInstanceId processInstanceId;
    private final TaskId taskId;
    private final BreachType breachType;
    private final Duration breachDuration;
    private final LocalDateTime occurredAt;

    public SLABreachEvent(
            SLAMonitorId slaMonitorId,
            ProcessInstanceId processInstanceId,
            TaskId taskId,
            BreachType breachType,
            Duration breachDuration,
            LocalDateTime occurredAt) {
        this.eventId = UUID.randomUUID().toString();
        this.slaMonitorId = Objects.requireNonNull(slaMonitorId, "slaMonitorId cannot be null");
        this.processInstanceId = Objects.requireNonNull(processInstanceId, "processInstanceId cannot be null");
        this.taskId = taskId;
        this.breachType = Objects.requireNonNull(breachType, "breachType cannot be null");
        this.breachDuration = Objects.requireNonNull(breachDuration, "breachDuration cannot be null");
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }

    @Override
    public String getAggregateId() {
        return processInstanceId.getValue();
    }

    @Override
    public String getAggregateType() {
        return "SLAMonitor";
    }

    @Override
    public String getEventType() {
        return "SLABreach";
    }

    public long getBreachMinutes() {
        return breachDuration.toMinutes();
    }

    public boolean isResponseBreach() {
        return breachType == BreachType.RESPONSE;
    }

    public boolean isResolutionBreach() {
        return breachType == BreachType.RESOLUTION;
    }

    /**
     * 违规类型枚举
     */
    public enum BreachType {
        /**
         * 响应时间违规
         */
        RESPONSE("响应时间违规"),

        /**
         * 解决时间违规
         */
        RESOLUTION("解决时间违规");

        private final String description;

        BreachType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
