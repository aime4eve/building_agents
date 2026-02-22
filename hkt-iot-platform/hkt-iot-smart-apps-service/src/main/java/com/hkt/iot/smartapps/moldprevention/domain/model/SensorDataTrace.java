package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 传感器数据追踪值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDataTrace {

    private String id;
    private SensorId sensorId;
    private String deviceId;
    private LocalDateTime collectedAt;
    private LocalDateTime collectedFrom;
    private LocalDateTime collectedTo;
    private int dataPointCount;
    private DataQuality quality;

    /**
     * 数据质量枚举
     */
    public enum DataQuality {
        /**
         * 优秀 - 数据完整，无异常
         */
        EXCELLENT,

        /**
         * 良好 - 数据基本完整，少量异常
         */
        GOOD,

        /**
         * 一般 - 部分数据缺失
         */
        FAIR,

        /**
         * 差 - 数据大量缺失或异常
         */
        POOR
    }
}
