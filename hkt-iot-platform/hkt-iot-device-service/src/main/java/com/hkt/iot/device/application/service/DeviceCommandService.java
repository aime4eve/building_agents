package com.hkt.iot.device.application.service;

import com.hkt.iot.device.domain.event.DeviceCommandExecutedEvent;
import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.domain.model.DeviceCommand;
import com.hkt.iot.device.domain.repository.DeviceCommandRepository;
import com.hkt.iot.device.domain.repository.DeviceRepository;
import com.hkt.iot.device.application.event.DeviceEventPublisher;
import com.hkt.iot.device.infrastructure.mqtt.MqttCommandSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备命令应用服务
 * 负责设备控制命令的发送与回执处理
 *
 * @author HKT IoT Team
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceCommandService {

    private final DeviceCommandRepository commandRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceEventPublisher eventPublisher;
    private final MqttCommandSender mqttCommandSender;

    /**
     * 创建并发送设备命令
     */
    @Transactional(rollbackFor = Exception.class)
    public DeviceCommand sendCommand(
            Long tenantId,
            Long deviceId,
            String commandCode,
            String commandName,
            DeviceCommand.CommandType commandType,
            Map<String, Object> inputParams,
            Integer priority,
            Long createdBy) {

        log.info("发送设备命令: deviceId={}, commandCode={}, commandType={}",
                deviceId, commandCode, commandType);

        // 查询设备
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + deviceId));

        // 检查设备是否在线
        if (!device.getOnlineStatus()) {
            throw new IllegalStateException("设备离线，无法发送命令");
        }

        // 检查设备是否被锁定
        if (device.getIsLocked()) {
            throw new IllegalStateException("设备已被锁定，无法发送命令");
        }

        // 创建命令
        DeviceCommand command = DeviceCommand.create(
                tenantId,
                deviceId,
                device.getDeviceSn(),
                commandCode,
                commandName,
                commandType,
                inputParams,
                priority,
                30,  // 默认超时30秒
                3,   // 默认重试3次
                createdBy
        );

        // 保存命令
        DeviceCommand savedCommand = commandRepository.save(command);

        // 发送命令到设备
        try {
            mqttCommandSender.sendCommand(device.getDeviceSn(), command);
            savedCommand.send();
            commandRepository.save(savedCommand);
            log.info("设备命令发送成功: commandId={}, requestId={}",
                    savedCommand.getId(), savedCommand.getRequestId());
        } catch (Exception e) {
            log.error("设备命令发送失败: commandId={}, error={}",
                    savedCommand.getId(), e.getMessage(), e);
            savedCommand.fail("SEND_FAILED", e.getMessage());
            commandRepository.save(savedCommand);
        }

        return savedCommand;
    }

    /**
     * 处理命令执行回执
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleCommandReceipt(
            String requestId,
            DeviceCommand.CommandStatus status,
            Map<String, Object> outputParams,
            String resultCode,
            String resultMessage) {

        log.info("处理命令回执: requestId={}, status={}, resultCode={}",
                requestId, status, resultCode);

        // 查询命令
        DeviceCommand command = commandRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("命令不存在: " + requestId));

        // 更新命令状态
        switch (status) {
            case SUCCESS -> command.success(outputParams, resultCode, resultMessage);
            case FAILED -> command.fail(resultCode, resultMessage);
            case TIMEOUT -> command.timeout();
            default -> log.warn("未知的命令状态: {}", status);
        }

        commandRepository.save(command);

        // 发布命令执行事件
        DeviceCommandExecutedEvent event = new DeviceCommandExecutedEvent(
                command.getId(),
                command.getRequestId(),
                command.getDeviceId(),
                command.getDeviceSn(),
                command.getTenantId(),
                command.getCommandType(),
                command.getCommandStatus(),
                command.getResultCode(),
                command.getResultMessage(),
                command.getExecutedTime() != null ? command.getExecutedTime() : LocalDateTime.now()
        );
        eventPublisher.publishEvent(event);

        log.info("命令回执处理完成: requestId={}, finalStatus={}",
                requestId, command.getCommandStatus());
    }

    /**
     * 取消命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelCommand(Long commandId) {
        log.info("取消命令: commandId={}", commandId);

        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("命令不存在: " + commandId));

        command.cancel();
        commandRepository.save(command);

        log.info("命令取消成功: commandId={}", commandId);
    }

    /**
     * 重试失败的命令
     */
    @Transactional(rollbackFor = Exception.class)
    public DeviceCommand retryCommand(Long commandId) {
        log.info("重试命令: commandId={}", commandId);

        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("命令不存在: " + commandId));

        if (!command.canRetry()) {
            throw new IllegalStateException("命令不可重试: " + commandId);
        }

        command.retry();
        commandRepository.save(command);

        // 重新发送命令
        Device device = deviceRepository.findById(command.getDeviceId())
                .orElseThrow(() -> new IllegalArgumentException("设备不存在: " + command.getDeviceId()));

        try {
            mqttCommandSender.sendCommand(device.getDeviceSn(), command);
            command.send();
            commandRepository.save(command);
            log.info("命令重试发送成功: commandId={}", commandId);
        } catch (Exception e) {
            log.error("命令重试发送失败: commandId={}, error={}",
                    commandId, e.getMessage(), e);
            command.fail("RETRY_FAILED", e.getMessage());
            commandRepository.save(command);
        }

        return command;
    }

    /**
     * 查询命令详情
     */
    @Transactional(readOnly = true)
    public DeviceCommand getCommandById(Long commandId) {
        return commandRepository.findById(commandId)
                .orElseThrow(() -> new IllegalArgumentException("命令不存在: " + commandId));
    }

    /**
     * 根据请求ID查询命令
     */
    @Transactional(readOnly = true)
    public Optional<DeviceCommand> getCommandByRequestId(String requestId) {
        return commandRepository.findByRequestId(requestId);
    }

    /**
     * 查询设备的命令列表
     */
    @Transactional(readOnly = true)
    public List<DeviceCommand> getCommandsByDeviceId(Long deviceId) {
        return commandRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    /**
     * 处理超时命令（定时任务调用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutCommands() {
        log.debug("开始处理超时命令...");

        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(5);
        List<DeviceCommand> timeoutCommands = commandRepository
                .findByCommandStatusAndSentTimeBefore(
                        DeviceCommand.CommandStatus.EXECUTING,
                        timeoutThreshold
                );

        for (DeviceCommand command : timeoutCommands) {
            try {
                if (command.isTimeout()) {
                    command.timeout();
                    commandRepository.save(command);

                    DeviceCommandExecutedEvent event = new DeviceCommandExecutedEvent(
                            command.getId(),
                            command.getRequestId(),
                            command.getDeviceId(),
                            command.getDeviceSn(),
                            command.getTenantId(),
                            command.getCommandType(),
                            command.getCommandStatus(),
                            command.getResultCode(),
                            command.getResultMessage(),
                            LocalDateTime.now()
                    );
                    eventPublisher.publishEvent(event);
                }
            } catch (Exception e) {
                log.error("处理超时命令失败: commandId={}, error={}",
                        command.getId(), e.getMessage(), e);
            }
        }

        log.debug("超时命令处理完成，处理数量: {}", timeoutCommands.size());
    }
}
