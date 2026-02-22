package com.hkt.iot.scene.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划执行日志实体
 *
 * 职责：记录定时计划执行的详细日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExecutionLog {

    private ExecutionId id;
    private ScheduleId scheduleId;
    private ExecutionResult result;
    private List<ActionExecutionResult> actionResults;
    private LocalDateTime executedAt;
    private Duration duration;
    private String errorMessage;

    public static ScheduleExecutionLog from(ScheduleExecutionResult executionResult) {
        return ScheduleExecutionLog.builder()
                .id(executionResult.getExecutionId())
                .scheduleId(executionResult.getScheduleId())
                .result(executionResult.getResult())
                .actionResults(executionResult.getActionResults())
                .executedAt(executionResult.getExecutedAt())
                .duration(executionResult.getDuration())
                .errorMessage(executionResult.getErrorMessage())
                .build();
    }
}
