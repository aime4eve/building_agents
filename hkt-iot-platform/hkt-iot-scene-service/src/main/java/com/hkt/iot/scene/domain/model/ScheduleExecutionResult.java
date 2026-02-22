package com.hkt.iot.scene.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划执行结果
 *
 * 职责：记录定时计划执行的完整结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExecutionResult {

    private ExecutionId executionId;
    private ScheduleId scheduleId;
    private ExecutionResult result;
    private List<ActionExecutionResult> actionResults;
    private LocalDateTime executedAt;
    private Duration duration;
    private String errorMessage;

    public boolean isSuccess() {
        return this.result == ExecutionResult.SUCCESS;
    }
}
