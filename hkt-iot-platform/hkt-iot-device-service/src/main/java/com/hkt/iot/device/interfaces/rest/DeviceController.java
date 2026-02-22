package com.hkt.iot.device.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.device.application.service.DeviceApplicationService;
import com.hkt.iot.device.domain.model.Device;
import com.hkt.iot.device.interfaces.rest.dto.DeviceCreateDTO;
import com.hkt.iot.device.interfaces.rest.dto.DeviceUpdateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 设备管理REST控制器
 * 提供设备CRUD、激活、停用、锁定、解锁等接口
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "设备管理", description = "设备相关操作接口")
public class DeviceController {

    private final DeviceApplicationService deviceApplicationService;

    @PostMapping
    @Operation(summary = "创建设备", description = "创建新的设备")
    public Result<Device> createDevice(
            @Valid @RequestBody DeviceCreateDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            Device device = deviceApplicationService.createDevice(dto, userId);
            return Result.success(device);
        } catch (Exception e) {
            log.error("创建设备失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PutMapping("/{deviceId}")
    @Operation(summary = "更新设备", description = "更新设备信息")
    public Result<Device> updateDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Valid @RequestBody DeviceUpdateDTO dto,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            Device device = deviceApplicationService.updateDevice(deviceId, dto, userId);
            return Result.success(device);
        } catch (Exception e) {
            log.error("更新设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @DeleteMapping("/{deviceId}")
    @Operation(summary = "删除设备", description = "软删除设备")
    public Result<Void> deleteDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            deviceApplicationService.deleteDevice(deviceId, userId);
            return Result.success();
        } catch (Exception e) {
            log.error("删除设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "获取设备详情", description = "根据ID查询设备详细信息")
    public Result<Device> getDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            Device device = deviceApplicationService.getDeviceById(deviceId);
            return Result.success(device);
        } catch (Exception e) {
            log.error("获取设备详情失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "获取租户设备列表", description = "查询指定租户下的所有设备")
    public Result<List<Device>> getDevicesByTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        try {
            List<Device> devices = deviceApplicationService.getDevicesByTenantId(tenantId);
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取租户设备列表失败: tenantId={}, error={}", tenantId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/space/{spaceId}")
    @Operation(summary = "获取空间设备列表", description = "查询指定空间下的所有设备")
    public Result<List<Device>> getDevicesBySpace(
            @Parameter(description = "空间ID") @PathVariable Long spaceId) {
        try {
            List<Device> devices = deviceApplicationService.getDevicesBySpaceId(spaceId);
            return Result.success(devices);
        } catch (Exception e) {
            log.error("获取空间设备列表失败: spaceId={}, error={}", spaceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{deviceId}/activate")
    @Operation(summary = "激活设备", description = "激活指定的设备")
    public Result<Void> activateDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            deviceApplicationService.activateDevice(deviceId);
            return Result.success();
        } catch (Exception e) {
            log.error("激活设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{deviceId}/deactivate")
    @Operation(summary = "停用设备", description = "停用指定的设备")
    public Result<Void> deactivateDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            deviceApplicationService.deactivateDevice(deviceId);
            return Result.success();
        } catch (Exception e) {
            log.error("停用设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{deviceId}/lock")
    @Operation(summary = "锁定设备", description = "锁定指定的设备")
    public Result<Void> lockDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @RequestParam String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            deviceApplicationService.lockDevice(deviceId, userId, reason);
            return Result.success();
        } catch (Exception e) {
            log.error("锁定设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{deviceId}/unlock")
    @Operation(summary = "解锁设备", description = "解锁指定的设备")
    public Result<Void> unlockDevice(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            deviceApplicationService.unlockDevice(deviceId);
            return Result.success();
        } catch (Exception e) {
            log.error("解锁设备失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/tenant/{tenantId}/count")
    @Operation(summary = "统计设备数量", description = "统计租户下的设备数量")
    public Result<Long> countDevices(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        try {
            long count = deviceApplicationService.countDevicesByTenantId(tenantId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("统计设备数量失败: tenantId={}, error={}", tenantId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }
}
