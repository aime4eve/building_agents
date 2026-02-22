package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 校准信息值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalibrationInfo {

    /**
     * 温度偏移量（°C）
     */
    private double temperatureOffset;

    /**
     * 湿度偏移量（%）
     */
    private double humidityOffset;

    /**
     * 上次校准时间
     */
    private LocalDateTime lastCalibratedAt;

    /**
     * 下次校准时间
     */
    private LocalDateTime nextCalibrationDueAt;

    /**
     * 校准有效期（天）
     */
    private int calibrationValidityDays;

    /**
     * 检查是否需要重新校准
     */
    public boolean needsRecalibration() {
        return nextCalibrationDueAt != null
                && LocalDateTime.now().isAfter(nextCalibrationDueAt);
    }
}
