package com.hkt.iot.scene.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场景执行结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneExecutionResultDTO {

    private String executionId;
    private String sceneId;
    private String result;
    private List<ActionExecutionResultDTO> actionResults;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration duration;
    private String errorMessage;

    public static SceneExecutionResultDTO from(com.hkt.iot.scene.domain.model.SceneExecutionResult result) {
        return SceneExecutionResultDTO.builder()
                .executionId(result.getExecutionId().getValue())
                .sceneId(result.getSceneId().getValue())
                .result(result.getResult().name())
                .actionResults(result.getActionResults() != null
                    ? result.getActionResults().stream()
                        .map(ActionExecutionResultDTO::from)
                        .toList()
                    : List.of())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .duration(result.getDuration())
                .errorMessage(result.getErrorMessage())
                .build();
    }
}
