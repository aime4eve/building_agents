package com.hkt.iot.device.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.device.application.service.DeviceCommandService;
import com.hkt.iot.device.domain.model.DeviceCommand;
import com.hkt.iot.device.interfaces.rest.dto.DeviceCommandRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 设备命令REST控制器
 * 提供设备控制命令的发送、取消、重试等接口
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/commands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "设备命令", description = "设备控制命令相关操作接口")
public class DeviceCommandController {

    private final DeviceCommandService commandService;

    @PostMapping
    @Operation(summary = "发送设备命令", description = "向设备发送控制命令")
    public Result<DeviceCommand> sendCommand(
            @Valid @RequestBody DeviceCommandRequest request,
            @RequestHeader(value = "X-Tenant-Id", defaultValue = "1") Long tenantId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            DeviceCommand command = commandService.sendCommand(
                    tenantId,
                    request.getDeviceId(),
                    request.getCommandCode(),
                    request.getCommandName(),
                    request.getCommandType(),
                    request.getInputParams(),
                    request.getPriority(),
                    userId
            );
            return Result.success(command);
        } catch (Exception e) {
            log.error("发送设备命令失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{commandId}/cancel")
    @Operation(summary = "取消命令", description = "取消指定的设备命令")
    public Result<Void> cancelCommand(
            @Parameter(description = "命令ID") @PathVariable Long commandId) {
        try {
            commandService.cancelCommand(commandId);
            return Result.success();
        } catch (Exception e) {
            log.error("取消命令失败: commandId={}, error={}", commandId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{commandId}/retry")
    @Operation(summary = "重试命令", description = "重试失败的设备命令")
    public Result<DeviceCommand> retryCommand(
            @Parameter(description = "命令ID") @PathVariable Long commandId) {
        try {
            DeviceCommand command = commandService.retryCommand(commandId);
            return Result.success(command);
        } catch (Exception e) {
            log.error("重试命令失败: commandId={}, error={}", commandId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/{commandId}")
    @Operation(summary = "获取命令详情", description = "根据ID查询命令详细信息")
    public Result<DeviceCommand> getCommand(
            @Parameter(description = "命令ID") @PathVariable Long commandId) {
        try {
            DeviceCommand command = commandService.getCommandById(commandId);
            return Result.success(command);
        } catch (Exception e) {
            log.error("获取命令详情失败: commandId={}, error={}", commandId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/request/{requestId}")
    @Operation(summary = "根据请求ID查询命令", description = "根据请求ID查询命令状态")
    public Result<DeviceCommand> getCommandByRequestId(
            @Parameter(description = "请求ID") @PathVariable String requestId) {
        try {
            return commandService.getCommandByRequestId(requestId)
                    .map(Result::success)
                    .orElse(Result.error(404, "命令不存在"));
        } catch (Exception e) {
            log.error("查询命令失败: requestId={}, error={}", requestId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/device/{deviceId}")
    @Operation(summary = "获取设备命令列表", description = "查询指定设备的所有命令")
    public Result<List<DeviceCommand>> getCommandsByDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            List<DeviceCommand> commands = commandService.getCommandsByDeviceId(deviceId);
            return Result.success(commands);
        } catch (Exception e) {
            log.error("获取设备命令列表失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/receipt")
    @Operation(summary = "处理命令回执", description = "处理设备返回的命令执行结果")
    public Result<Void> handleCommandReceipt(
            @RequestBody Map<String, Object> receipt) {
        try {
            String requestId = (String) receipt.get("requestId");
            String statusStr = (String) receipt.get("status");
            DeviceCommand.CommandStatus status = DeviceCommand.CommandStatus.valueOf(statusStr);

            @SuppressWarnings("unchecked")
            Map<String, Object> outputParams = (Map<String, Object>) receipt.get("outputParams");

            String resultCode = (String) receipt.get("resultCode");
            String resultMessage = (String) receipt.get("resultMessage");

            commandService.handleCommandReceipt(requestId, status, outputParams, resultCode, resultMessage);
            return Result.success();
        } catch (Exception e) {
            log.error("处理命令回执失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }
}
