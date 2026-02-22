package com.hkt.iot.scene.application.dto;

import com.hkt.iot.scene.domain.model.ScheduleExecutionLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划执行日志DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExecutionLogDTO {

    private String executionId;
    private String scheduleId;
    private String result;
    private List<ActionExecutionResultDTO> actionResults;
    private LocalDateTime executedAt;
    private Duration duration;
    private String errorMessage;

    public static ScheduleExecutionLogDTO from(ScheduleExecutionLog log) {
        return ScheduleExecutionLogDTO.builder()
                .executionId(log.getId().getValue())
                .scheduleId(log.getScheduleId().getValue())
                .result(log.getResult().name())
                .actionResults(log.getActionResults() != null
                    ? log.getActionResults().stream()
                        .map(ActionExecutionResultDTO::from)
                        .toList()
                    : List.of())
                .executedAt(log.getExecutedAt())
                .duration(log.getDuration())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
