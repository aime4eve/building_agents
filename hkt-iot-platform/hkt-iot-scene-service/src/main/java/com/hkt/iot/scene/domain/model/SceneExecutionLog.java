package com.hkt.iot.scene.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场景执行日志实体
 *
 * 职责：记录场景执行的详细日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneExecutionLog {

    private ExecutionId id;
    private SceneId sceneId;
    private ExecutionResult result;
    private List<ActionExecutionResult> actionResults;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration duration;
    private SceneContext context;
    private String errorMessage;

    public static SceneExecutionLog from(SceneExecutionResult executionResult) {
        return SceneExecutionLog.builder()
                .id(executionResult.getExecutionId())
                .sceneId(executionResult.getSceneId())
                .result(executionResult.getResult())
                .actionResults(executionResult.getActionResults())
                .startedAt(executionResult.getStartedAt())
                .completedAt(executionResult.getCompletedAt())
                .duration(executionResult.getDuration())
                .context(executionResult.getContext())
                .errorMessage(executionResult.getErrorMessage())
                .build();
    }
}
