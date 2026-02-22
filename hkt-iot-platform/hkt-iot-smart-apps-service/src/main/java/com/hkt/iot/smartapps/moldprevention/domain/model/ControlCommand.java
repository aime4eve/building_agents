package com.hkt.iot.smartapps.moldprevention.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 控制命令值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlCommand {

    private String commandId;
    private CommandType commandType;
    private ControllerId controllerId;
    private ControllerType targetType;
    private Map<String, Object> parameters;
    private int priority;
    private int delaySeconds;
    private LocalDateTime createdAt;

    /**
     * 命令类型枚举
     */
    public enum CommandType {
        /**
         * 开启
         */
        TURN_ON,

        /**
         * 关闭
         */
        TURN_OFF,

        /**
         * 设置参数
         */
        SET_PARAMETER,

        /**
         * 调整功率
         */
        ADJUST_POWER
    }

    /**
     * 创建新命令
     */
    public static ControlCommand create(
            CommandType commandType,
            ControllerType targetType,
            Map<String, Object> parameters) {
        return ControlCommand.builder()
                .commandId(UUID.randomUUID().toString())
                .commandType(commandType)
                .targetType(targetType)
                .parameters(parameters)
                .priority(5)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
