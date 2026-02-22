package com.hkt.iot.scene.application.dto;

import com.hkt.iot.scene.domain.model.ActionExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 动作执行结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionExecutionResultDTO {

    private String actionId;
    private String result;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Duration duration;
    private String errorMessage;
    private Object output;

    public static ActionExecutionResultDTO from(ActionExecutionResult result) {
        return ActionExecutionResultDTO.builder()
                .actionId(result.getActionId().getValue())
                .result(result.getResult().name())
                .startedAt(result.getStartedAt())
                .completedAt(result.getCompletedAt())
                .duration(result.getDuration())
                .errorMessage(result.getErrorMessage())
                .output(result.getOutput())
                .build();
    }
}
