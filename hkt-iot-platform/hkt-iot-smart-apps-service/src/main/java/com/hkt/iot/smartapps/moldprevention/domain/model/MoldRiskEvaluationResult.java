package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 霉菌风险评估结果值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoldRiskEvaluationResult {

    private ZoneId zoneId;
    private MoldRiskLevel riskLevel;
    private MoldRiskLevel previousRiskLevel;
    private boolean riskChanged;
    private EnvironmentData environmentData;
    private LocalDateTime evaluatedAt;
    private List<ControlCommand> controlCommands;

    /**
     * 判断风险是否上升
     */
    public boolean isRiskIncreased() {
        return previousRiskLevel != null
                && riskLevel.getLevel() > previousRiskLevel.getLevel();
    }

    /**
     * 判断风险是否下降
     */
    public boolean isRiskDecreased() {
        return previousRiskLevel != null
                && riskLevel.getLevel() < previousRiskLevel.getLevel();
    }
}
