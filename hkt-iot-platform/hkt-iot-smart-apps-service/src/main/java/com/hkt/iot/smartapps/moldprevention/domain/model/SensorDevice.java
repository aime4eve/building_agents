package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.hkt.iot.domain.shared.DeviceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 传感器设备实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorDevice {

    private SensorId id;
    private DeviceId deviceId;
    private SensorType type;
    private SensorStatus status;
    private CalibrationInfo calibration;
    private LocalDateTime lastCalibratedAt;

    /**
     * 传感器类型枚举
     */
    public enum SensorType {
        /**
         * 温度传感器
         */
        TEMPERATURE,

        /**
         * 湿度传感器
         */
        HUMIDITY,

        /**
         * 温湿度一体传感器
         */
        TEMPERATURE_HUMIDITY,

        /**
         * 霉菌检测传感器
         */
        MOLD_DETECTOR
    }

    /**
     * 传感器状态枚举
     */
    public enum SensorStatus {
        /**
         * 在线
         */
        ONLINE,

        /**
         * 离线
         */
        OFFLINE,

        /**
         * 故障
         */
        FAULT,

        /**
         * 校准中
         */
        CALIBRATING
    }
}
