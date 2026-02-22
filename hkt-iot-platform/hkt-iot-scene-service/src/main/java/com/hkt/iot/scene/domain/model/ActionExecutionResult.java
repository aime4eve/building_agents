package com.hkt.iot.scene.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 动作执行结果
 *
 * 职责：记录单个动作的执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionExecutionResult {

    private ActionId actionId;
    private ExecutionResult result;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration duration;
    private String errorMessage;
    private Object output;

    public ActionExecutionResult(ActionId actionId, ExecutionResult result,
                                  LocalDateTime startedAt, LocalDateTime completedAt) {
        this.actionId = actionId;
        this.result = result;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.duration = Duration.between(startedAt, completedAt);
    }

    public boolean isSuccess() {
        return this.result == ExecutionResult.SUCCESS;
    }
}
