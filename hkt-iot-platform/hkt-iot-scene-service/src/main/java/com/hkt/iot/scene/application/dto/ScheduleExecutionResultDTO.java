package com.hkt.iot.scene.application.dto;

import com.hkt.iot.scene.domain.model.ScheduleExecutionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划执行结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExecutionResultDTO {

    private String executionId;
    private String scheduleId;
    private String result;
    private List<ActionExecutionResultDTO> actionResults;
    private LocalDateTime executedAt;
    private Duration duration;
    private String errorMessage;

    public static ScheduleExecutionResultDTO from(ScheduleExecutionResult result) {
        return ScheduleExecutionResultDTO.builder()
                .executionId(result.getExecutionId().getValue())
                .scheduleId(result.getScheduleId().getValue())
                .result(result.getResult().name())
                .actionResults(result.getActionResults() != null
                    ? result.getActionResults().stream()
                        .map(ActionExecutionResultDTO::from)
                        .toList()
                    : List.of())
                .executedAt(result.getExecutedAt())
                .duration(result.getDuration())
                .errorMessage(result.getErrorMessage())
                .build();
    }
}
