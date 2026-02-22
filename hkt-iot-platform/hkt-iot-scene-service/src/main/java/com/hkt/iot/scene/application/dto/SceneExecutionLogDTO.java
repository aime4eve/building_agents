package com.hkt.iot.scene.application.dto;

import com.hkt.iot.scene.domain.model.SceneExecutionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场景执行日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneExecutionLogDTO {

    private String executionId;
    private String sceneId;
    private String result;
    private List<ActionExecutionResultDTO> actionResults;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration duration;
    private String errorMessage;

    public static SceneExecutionLogDTO from(SceneExecutionLog log) {
        return SceneExecutionLogDTO.builder()
                .executionId(log.getId().getValue())
                .sceneId(log.getSceneId().getValue())
                .result(log.getResult().name())
                .actionResults(log.getActionResults() != null
                    ? log.getActionResults().stream()
                        .map(ActionExecutionResultDTO::from)
                        .toList()
                    : List.of())
                .startedAt(log.getStartedAt())
                .completedAt(log.getCompletedAt())
                .duration(log.getDuration())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
