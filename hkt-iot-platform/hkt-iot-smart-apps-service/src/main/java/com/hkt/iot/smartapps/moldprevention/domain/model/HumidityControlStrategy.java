package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 湿度控制策略值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HumidityControlStrategy {

    /**
     * 策略ID
     */
    private String id;

    /**
     * 策略名称
     */
    private String name;

    /**
     * 目标湿度（%）
     */
    private double targetHumidity;

    /**
     * 湿度容差范围（±%）
     */
    private double humidityTolerance;

    /**
     * 控制模式
     */
    private ControlMode mode;

    /**
     * 除湿机开启湿度阈值（%）
     */
    private double dehumidifierOnThreshold;

    /**
     * 除湿机关闭湿度阈值（%）
     */
    private double dehumidifierOffThreshold;

    /**
     * 通风开启湿度阈值（%）
     */
    private double ventilationOnThreshold;

    /**
     * 通风关闭湿度阈值（%）
     */
    private double ventilationOffThreshold;

    /**
     * 控制优先级（1-10，数字越大优先级越高）
     */
    private int priority;

    /**
     * 控制延迟（秒）
     */
    private int controlDelay;

    /**
     * 生成控制命令
     */
    public List<ControlCommand> generateCommands(EnvironmentData data, MoldRiskLevel riskLevel) {
        List<ControlCommand> commands = new java.util.ArrayList<>();

        double currentHumidity = data.getHumidity();

        // 根据风险等级调整控制策略
        if (riskLevel == MoldRiskLevel.CRITICAL) {
            // 极高风险：开启所有除湿设备
            commands.add(createDehumidifierCommand(true, 100));  // 全功率运行
            commands.add(createVentilationCommand(true));  // 开启通风
        } else if (riskLevel == MoldRiskLevel.HIGH) {
            // 高风险：开启除湿
            if (currentHumidity > dehumidifierOnThreshold) {
                commands.add(createDehumidifierCommand(true, 80));  // 80%功率
            }
        } else if (riskLevel == MoldRiskLevel.MEDIUM) {
            // 中风险：根据当前湿度决定
            if (currentHumidity > targetHumidity + humidityTolerance) {
                commands.add(createDehumidifierCommand(true, 60));  // 60%功率
            }
        } else {
            // 低风险：关闭除湿设备（如果湿度已降至目标以下）
            if (currentHumidity < dehumidifierOffThreshold) {
                commands.add(createDehumidifierCommand(false, 0));
            }
        }

        return commands;
    }

    private ControlCommand createDehumidifierCommand(boolean on, int power) {
        return ControlCommand.builder()
                .commandId(UUID.randomUUID().toString())
                .commandType(on ? CommandType.TURN_ON : CommandType.TURN_OFF)
                .targetType(ControllerType.DEHUMIDIFIER)
                .parameters(java.util.Map.of(
                        "power", power,
                        "mode", "auto"
                ))
                .priority(this.priority)
                .delaySeconds(this.controlDelay)
                .build();
    }

    private ControlCommand createVentilationCommand(boolean on) {
        return ControlCommand.builder()
                .commandId(UUID.randomUUID().toString())
                .commandType(on ? CommandType.TURN_ON : CommandType.TURN_OFF)
                .targetType(ControllerType.VENTILATION)
                .parameters(java.util.Map.of(
                        "speed", "medium"
                ))
                .priority(this.priority - 1)
                .delaySeconds(this.controlDelay)
                .build();
    }

    /**
     * 控制模式枚举
     */
    public enum ControlMode {
        /**
         * 自动模式 - 根据传感器数据自动控制
         */
        AUTO,

        /**
         * 手动模式 - 需要手动触发
         */
        MANUAL,

        /**
         * 定时模式 - 按时间计划控制
         */
        SCHEDULED
    }
}
