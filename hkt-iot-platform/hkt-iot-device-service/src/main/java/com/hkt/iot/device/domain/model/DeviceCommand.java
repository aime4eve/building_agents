package com.hkt.iot.device.domain.model;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.device.domain.event.DeviceCommandExecutedEvent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备控制命令实体
 * 属于Device聚合根，管理设备远程控制命令的生命周期
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "device_command")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceCommand extends Entity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_sn", nullable = false, length = 100)
    private String deviceSn;

    @Column(name = "command_code", nullable = false, length = 100)
    private String commandCode;

    @Column(name = "command_name", nullable = false, length = 200)
    private String commandName;

    @Column(name = "command_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private CommandType commandType;

    @Column(name = "command_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private CommandStatus commandStatus;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "input_params", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> inputParams;

    @Column(name = "output_params", columnDefinition = "JSON")
    @Transient
    private Map<String, Object> outputParams;

    @Column(name = "result_code", length = 50)
    private String resultCode;

    @Column(name = "result_message", length = 500)
    private String resultMessage;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Column(name = "retry_times")
    private Integer retryTimes;

    @Column(name = "max_retry_times")
    private Integer maxRetryTimes;

    @Column(name = "sent_time")
    private LocalDateTime sentTime;

    @Column(name = "executed_time")
    private LocalDateTime executedTime;

    @Column(name = "expired_time")
    private LocalDateTime expiredTime;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 命令类型
     */
    public enum CommandType {
        PROPERTY_SET,      // 属性设置
        SERVICE_CALL,      // 服务调用
        PROPERTY_GET,      // 属性查询
        SYNC_REQUEST,      // 同步请求
        ASYNC_COMMAND      // 异步命令
    }

    /**
     * 命令状态
     */
    public enum CommandStatus {
        PENDING,       // 待发送
        SENT,          // 已发送
        EXECUTING,     // 执行中
        SUCCESS,       // 执行成功
        FAILED,        // 执行失败
        TIMEOUT,       // 超时
        CANCELLED      // 已取消
    }

    /**
     * 工厂方法：创建设备命令
     */
    public static DeviceCommand create(
            Long tenantId,
            Long deviceId,
            String deviceSn,
            String commandCode,
            String commandName,
            CommandType commandType,
            Map<String, Object> inputParams,
            Integer priority,
            Integer timeoutSeconds,
            Integer maxRetryTimes,
            Long createdBy) {

        DeviceCommand command = new DeviceCommand();
        command.tenantId = tenantId;
        command.deviceId = deviceId;
        command.deviceSn = deviceSn;
        command.commandCode = commandCode;
        command.commandName = commandName;
        command.commandType = commandType;
        command.commandStatus = CommandStatus.PENDING;
        command.inputParams = inputParams;
        command.priority = priority != null ? priority : 0;
        command.timeoutSeconds = timeoutSeconds != null ? timeoutSeconds : 30;
        command.retryTimes = 0;
        command.maxRetryTimes = maxRetryTimes != null ? maxRetryTimes : 3;
        command.deleted = false;
        command.createdAt = LocalDateTime.now();
        command.updatedAt = LocalDateTime.now();
        command.createdBy = createdBy;

        // 生成请求ID
        command.requestId = generateRequestId(deviceSn, commandCode);

        return command;
    }

    /**
     * 发送命令
     */
    public void send() {
        if (this.commandStatus != CommandStatus.PENDING) {
            throw new IllegalStateException("只有待发送的命令才能发送");
        }
        this.commandStatus = CommandStatus.SENT;
        this.sentTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 开始执行
     */
    public void startExecuting() {
        if (this.commandStatus != CommandStatus.SENT) {
            throw new IllegalStateException("只有已发送的命令才能开始执行");
        }
        this.commandStatus = CommandStatus.EXECUTING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 执行成功
     */
    public void success(Map<String, Object> outputParams, String resultCode, String resultMessage) {
        if (this.commandStatus != CommandStatus.EXECUTING) {
            throw new IllegalStateException("只有执行中的命令才能标记为成功");
        }
        this.commandStatus = CommandStatus.SUCCESS;
        this.outputParams = outputParams;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.executedTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 执行失败
     */
    public void fail(String resultCode, String resultMessage) {
        if (this.commandStatus != CommandStatus.EXECUTING && this.commandStatus != CommandStatus.SENT) {
            throw new IllegalStateException("只有执行中或已发送的命令才能标记为失败");
        }
        this.commandStatus = CommandStatus.FAILED;
        this.resultCode = resultCode;
        this.resultMessage = resultMessage;
        this.executedTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 命令超时
     */
    public void timeout() {
        if (this.commandStatus != CommandStatus.EXECUTING && this.commandStatus != CommandStatus.SENT) {
            throw new IllegalStateException("只有执行中或已发送的命令才能标记为超时");
        }
        this.commandStatus = CommandStatus.TIMEOUT;
        this.resultCode = "TIMEOUT";
        this.resultMessage = "命令执行超时";
        this.executedTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 取消命令
     */
    public void cancel() {
        if (this.commandStatus == CommandStatus.SUCCESS || this.commandStatus == CommandStatus.FAILED) {
            throw new IllegalStateException("已完成或失败的命令不能取消");
        }
        this.commandStatus = CommandStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 重试命令
     */
    public void retry() {
        if (this.commandStatus != CommandStatus.FAILED && this.commandStatus != CommandStatus.TIMEOUT) {
            throw new IllegalStateException("只有失败或超时的命令才能重试");
        }
        if (this.retryTimes >= this.maxRetryTimes) {
            throw new IllegalStateException("已达到最大重试次数");
        }
        this.retryTimes++;
        this.commandStatus = CommandStatus.PENDING;
        this.resultCode = null;
        this.resultMessage = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return (this.commandStatus == CommandStatus.FAILED || this.commandStatus == CommandStatus.TIMEOUT)
                && this.retryTimes < this.maxRetryTimes;
    }

    /**
     * 检查是否超时
     */
    public boolean isTimeout() {
        if (this.timeoutSeconds == null || this.sentTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.sentTime.plusSeconds(this.timeoutSeconds));
    }

    /**
     * 检查是否已完成
     */
    public boolean isCompleted() {
        return this.commandStatus == CommandStatus.SUCCESS
                || this.commandStatus == CommandStatus.FAILED
                || this.commandStatus == CommandStatus.TIMEOUT
                || this.commandStatus == CommandStatus.CANCELLED;
    }

    /**
     * 软删除
     */
    public void softDelete() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 生成请求ID
     */
    private static String generateRequestId(String deviceSn, String commandCode) {
        return String.format("%s-%s-%d", deviceSn, commandCode, System.currentTimeMillis());
    }
}
