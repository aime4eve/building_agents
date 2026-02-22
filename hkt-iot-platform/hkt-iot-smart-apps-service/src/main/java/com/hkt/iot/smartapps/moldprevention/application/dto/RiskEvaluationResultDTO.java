package com.hkt.iot.smartapps.moldprevention.application.dto;

import com.hkt.iot.smartapps.moldprevention.domain.model.ControlCommand;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldRiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险评估结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskEvaluationResultDTO {

    /**
     * 区域ID
     */
    private String zoneId;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 风险等级描述
     */
    private String riskLevelDescription;

    /**
     * 风险分数
     */
    private Integer riskScore;

    /**
     * 风险是否变化
     */
    private Boolean riskChanged;

    /**
     * 当前温度
     */
    private Double temperature;

    /**
     * 当前湿度
     */
    private Double humidity;

    /**
     * 评估时间
     */
    private LocalDateTime evaluatedAt;

    /**
     * 控制命令列表
     */
    private List<ControlCommandDTO> controlCommands;

    /**
     * 是否需要控制
     */
    private Boolean requiresControl;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ControlCommandDTO {
        private String controllerId;
        private String commandType;
        private Object parameters;
    }

    public static RiskEvaluationResultDTO fromDomain(
            com.hkt.iot.smartapps.moldprevention.domain.model.MoldRiskEvaluationResult result,
            int riskScore) {
        if (result == null) {
            return null;
        }

        MoldRiskLevel level = result.getRiskLevel();
        List<ControlCommandDTO> commandDTOs = null;
        
        if (result.getControlCommands() != null) {
            commandDTOs = result.getControlCommands().stream()
                    .map(c -> ControlCommandDTO.builder()
                            .controllerId(c.getControllerId().getValue())
                            .commandType(c.getCommandType())
                            .parameters(c.getParameters())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        }

        return RiskEvaluationResultDTO.builder()
                .zoneId(result.getZoneId().getValue())
                .riskLevel(level.name())
                .riskLevelDescription(level.getDescription())
                .riskScore(riskScore)
                .riskChanged(result.isRiskChanged())
                .temperature(result.getEnvironmentData().getTemperature())
                .humidity(result.getEnvironmentData().getHumidity())
                .evaluatedAt(result.getEvaluatedAt())
                .controlCommands(commandDTOs)
                .requiresControl(level.requiresControl())
                .build();
    }
}
