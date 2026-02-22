package com.hkt.iot.smartapps.smartlivestock.application.dto;

import com.hkt.iot.smartapps.smartlivestock.domain.model.HealthLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 健康评分DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScoreDTO {
    private Integer value;
    private String level;
    private String levelDescription;
    private Integer temperatureScore;
    private Integer heartRateScore;
    private Integer respiratoryRateScore;
    private Integer activityScore;
    private Integer feedIntakeScore;
    private Boolean abnormal;
    private String abnormalNote;

    public static HealthScoreDTO fromDomain(com.hkt.iot.smartapps.smartlivestock.domain.model.HealthScore score) {
        if (score == null) {
            return null;
        }
        HealthLevel level = score.getLevel();
        return HealthScoreDTO.builder()
                .value(score.getValue())
                .level(level != null ? level.name() : null)
                .levelDescription(level != null ? level.getDescription() : null)
                .abnormal(score.isAbnormal())
                .build();
    }
}
