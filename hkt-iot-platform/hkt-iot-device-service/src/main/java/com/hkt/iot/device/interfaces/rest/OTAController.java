package com.hkt.iot.device.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.device.application.service.OTAService;
import com.hkt.iot.device.application.service.OTAService.OTACreateRequest;
import com.hkt.iot.device.domain.model.OTATask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OTA升级REST控制器
 * 提供OTA升级任务的创建、执行、管理等接口
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/ota")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OTA升级", description = "设备固件升级相关操作接口")
public class OTAController {

    private final OTAService otaService;

    @PostMapping
    @Operation(summary = "创建OTA任务", description = "创建新的固件升级任务")
    public Result<OTATask> createTask(
            @Valid @RequestBody OTACreateRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId) {
        try {
            OTATask task = otaService.createTask(request, userId);
            return Result.success(task);
        } catch (Exception e) {
            log.error("创建OTA任务失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{taskId}/start")
    @Operation(summary = "开始执行任务", description = "开始执行OTA升级任务")
    public Result<Void> startTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            otaService.startTask(taskId);
            return Result.success();
        } catch (Exception e) {
            log.error("开始执行OTA任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{taskId}/pause")
    @Operation(summary = "暂停任务", description = "暂停正在执行的OTA任务")
    public Result<Void> pauseTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            otaService.pauseTask(taskId);
            return Result.success();
        } catch (Exception e) {
            log.error("暂停OTA任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{taskId}/resume")
    @Operation(summary = "恢复任务", description = "恢复已暂停的OTA任务")
    public Result<Void> resumeTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            otaService.resumeTask(taskId);
            return Result.success();
        } catch (Exception e) {
            log.error("恢复OTA任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{taskId}/cancel")
    @Operation(summary = "取消任务", description = "取消OTA升级任务")
    public Result<Void> cancelTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestParam String reason) {
        try {
            otaService.cancelTask(taskId, reason);
            return Result.success();
        } catch (Exception e) {
            log.error("取消OTA任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取任务详情", description = "根据ID查询OTA任务详细信息")
    public Result<OTATask> getTask(
            @Parameter(description = "任务ID") @PathVariable Long taskId) {
        try {
            OTATask task = otaService.getTaskById(taskId);
            return Result.success(task);
        } catch (Exception e) {
            log.error("获取OTA任务详情失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "获取租户任务列表", description = "查询指定租户的所有OTA任务")
    public Result<List<OTATask>> getTasksByTenant(
            @Parameter(description = "租户ID") @PathVariable Long tenantId) {
        try {
            List<OTATask> tasks = otaService.getTasksByTenantId(tenantId);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("获取租户OTA任务列表失败: tenantId={}, error={}", tenantId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/{taskId}/progress")
    @Operation(summary = "更新设备升级进度", description = "更新设备在OTA任务中的升级进度")
    public Result<Void> updateProgress(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @RequestParam boolean success,
            @RequestParam(required = false) String errorMessage) {
        try {
            otaService.updateDeviceProgress(taskId, success, errorMessage);
            return Result.success();
        } catch (Exception e) {
            log.error("更新OTA任务进度失败: taskId={}, error={}", taskId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }
}
