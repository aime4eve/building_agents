package com.hkt.iot.smartapps.moldprevention.domain.model;

import com.hkt.iot.domain.shared.DeviceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 控制设备实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlDevice {

    private ControllerId id;
    private DeviceId deviceId;
    private ControllerType type;
    private ControllerStatus status;
    private ControlMode mode;
    private LocalDateTime lastControlledAt;

    /**
     * 执行控制命令
     */
    public void execute(ControlCommand command) {
        this.lastControlledAt = LocalDateTime.now();
        // 实际执行由基础设施层处理
    }

    /**
     * 控制器状态枚举
     */
    public enum ControllerStatus {
        /**
         * 在线
         */
        ONLINE,

        /**
         * 离线
         */
        OFFLINE,

        /**
         * 运行中
         */
        RUNNING,

        /**
         * 待机
         */
        STANDBY,

        /**
         * 故障
         */
        FAULT
    }

    /**
     * 控制模式枚举
     */
    public enum ControlMode {
        /**
         * 自动模式
         */
        AUTO,

        /**
         * 手动模式
         */
        MANUAL
    }
}
