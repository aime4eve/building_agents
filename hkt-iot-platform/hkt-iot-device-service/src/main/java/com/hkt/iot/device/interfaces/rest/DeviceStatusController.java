package com.hkt.iot.device.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.device.interfaces.dto.DeviceOnlineNotification;
import com.hkt.iot.device.interfaces.dto.DeviceOfflineNotification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 设备状态控制器
 * 处理设备上线/离线通知接口
 *
 * @author HKT IoT Team
 */
@Slf4j
@Tag(name = "设备状态", description = "设备上线、离线通知接口")
@RestController
@RequestMapping("/api/v1/device-status")
@RequiredArgsConstructor
public class DeviceStatusController {

    // private final DeviceStatusApplicationService deviceStatusApplicationService;

    /**
     * 设备上线通知
     * EMQX通过Webhook调用此接口，通知设备上线
     */
    @Operation(summary = "设备上线通知", description = "EMQX通过Webhook通知设备上线")
    @PostMapping("/online")
    public Result<Void> deviceOnline(@Valid @RequestBody DeviceOnlineNotification notification) {
        log.info("设备上线通知: deviceId={}, tenantId={}, connectedAt={}",
                notification.getDeviceId(), notification.getTenantId(), notification.getConnectedAt());

        // TODO: 处理设备上线
        // 1. 更新设备状态为在线
        // 2. 更新连接信息
        // 3. 发送设备上线事件

        return Result.success();
    }

    /**
     * 设备离线通知
     * EMQX通过Webhook调用此接口，通知设备离线
     */
    @Operation(summary = "设备离线通知", description = "EMQX通过Webhook通知设备离线")
    @PostMapping("/offline")
    public Result<Void> deviceOffline(@Valid @RequestBody DeviceOfflineNotification notification) {
        log.info("设备离线通知: deviceId={}, tenantId={}, reason={}",
                notification.getDeviceId(), notification.getTenantId(), notification.getReason());

        // TODO: 处理设备离线
        // 1. 更新设备状态为离线
        // 2. 记录离线原因和时间
        // 3. 发送设备离线事件
        // 4. 如果是网关设备，标记子设备离线

        return Result.success();
    }

    /**
     * 查询设备状态
     */
    @Operation(summary = "查询设备状态", description = "查询设备当前状态")
    @GetMapping("/{deviceId}")
    public Result<DeviceStatusDTO> getDeviceStatus(@PathVariable String deviceId) {
        log.info("查询设备状态: deviceId={}", deviceId);

        // TODO: 查询设备状态
        DeviceStatusDTO status = new DeviceStatusDTO();
        status.setDeviceId(deviceId);
        status.setStatus("ONLINE");

        return Result.success(status);
    }

    /**
     * 设备状态DTO
     */
    public static class DeviceStatusDTO {
        private String deviceId;
        private String status; // ONLINE, OFFLINE, FAULT
        private Long lastCommunicatedAt;

        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getLastCommunicatedAt() { return lastCommunicatedAt; }
        public void setLastCommunicatedAt(Long lastCommunicatedAt) { this.lastCommunicatedAt = lastCommunicatedAt; }
    }
}
