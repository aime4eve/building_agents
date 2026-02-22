package com.hkt.iot.scene.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场景执行结果
 *
 * 职责：记录场景执行的完整结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneExecutionResult {

    private ExecutionId executionId;
    private SceneId sceneId;
    private ExecutionResult result;
    private List<ActionExecutionResult> actionResults;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration duration;
    private SceneContext context;
    private String errorMessage;

    public boolean isSuccess() {
        return this.result == ExecutionResult.SUCCESS;
    }

    public boolean isPartialSuccess() {
        return this.result == ExecutionResult.PARTIAL_SUCCESS;
    }

    public boolean isFailed() {
        return this.result == ExecutionResult.FAILED;
    }
}
