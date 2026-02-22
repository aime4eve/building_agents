package com.hkt.iot.scene.interfaces.rest;

import com.hkt.iot.common.web.Result;
import com.hkt.iot.scene.application.dto.*;
import com.hkt.iot.scene.application.service.ScheduleApplicationService;
import com.hkt.iot.scene.domain.model.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时计划管理REST接口
 *
 * @author HKT IoT Team
 */
@Slf4j
@Tag(name = "定时计划管理", description = "定时计划相关接口")
@RequiredArgsConstructor
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleApplicationService scheduleApplicationService;

    @Operation(summary = "创建定时计划")
    @org.springframework.web.bind.annotation.PostMapping
    public Result<ScheduleDTO> createSchedule(
            @Valid @org.springframework.web.bind.annotation.RequestBody CreateScheduleRequest request,
            @Parameter(hidden = true) com.hkt.iot.domain.shared.TenantId tenantId) {
        ScheduleDTO schedule = scheduleApplicationService.createSchedule(request, tenantId);
        return Result.success(schedule);
    }

    @Operation(summary = "更新定时计划")
    @org.springframework.web.bind.annotation.PutMapping("/{scheduleId}")
    public Result<ScheduleDTO> updateSchedule(
            @PathVariable String scheduleId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateScheduleRequest request) {
        ScheduleDTO schedule = scheduleApplicationService.updateSchedule(ScheduleId.of(scheduleId), request);
        return Result.success(schedule);
    }

    @Operation(summary = "删除定时计划")
    @org.springframework.web.bind.annotation.DeleteMapping("/{scheduleId}")
    public Result<Void> deleteSchedule(
            @PathVariable String scheduleId) {
        scheduleApplicationService.deleteSchedule(ScheduleId.of(scheduleId));
        return Result.success();
    }

    @Operation(summary = "获取定时计划详情")
    @org.springframework.web.bind.annotation.GetMapping("/{scheduleId}")
    public Result<ScheduleDTO> getSchedule(
            @PathVariable String scheduleId) {
        ScheduleDTO schedule = scheduleApplicationService.getSchedule(ScheduleId.of(scheduleId));
        return Result.success(schedule);
    }

    @Operation(summary = "获取租户下的定时计划列表")
    @org.springframework.web.bind.annotationGetMapping
    public Result<List<ScheduleDTO>> getSchedulesByTenant(
            @Parameter(hidden = true) com.hkt.iot.domain.shared.TenantId tenantId) {
        List<ScheduleDTO> schedules = scheduleApplicationService.getSchedulesByTenant(tenantId);
        return Result.success(schedules);
    }

    @Operation(summary = "获取空间下的定时计划列表")
    @org.springframework.web.bind.annotation.GetMapping("/by-space/{spaceId}")
    public Result<List<ScheduleDTO>> getSchedulesBySpace(
            @PathVariable String spaceId) {
        List<ScheduleDTO> schedules = scheduleApplicationService.getSchedulesBySpace(
                com.hkt.iot.domain.shared.SpaceId.of(spaceId));
        return Result.success(schedules);
    }

    @Operation(summary = "获取待执行的定时计划列表")
    @org.springframework.web.bind.annotation.getMapping("/pending")
    public Result<List<ScheduleDTO>> getPendingSchedules(
            @org.springframework.web.bind.annotation.RequestParam LocalDateTime beforeTime) {
        List<ScheduleDTO> schedules = scheduleApplicationService.getPendingSchedules(beforeTime);
        return Result.success(schedules);
    }

    @Operation(summary = "激活定时计划")
    @org.springframework.web.bind.annotation.PostMapping("/{scheduleId}/activate")
    public Result<Void> activateSchedule(
            @PathVariable String scheduleId) {
        scheduleApplicationService.activateSchedule(ScheduleId.of(scheduleId));
        return Result.success();
    }

    @Operation(summary = "停用定时计划")
    @org.springframework.web.bind.annotation.PostMapping("/{scheduleId}/deactivate")
    public Result<Void> deactivateSchedule(
            @PathVariable String scheduleId) {
        scheduleApplicationService.deactivateSchedule(ScheduleId.of(scheduleId));
        return Result.success();
    }

    @Operation(summary = "更新Cron表达式")
    @org.springframework.web.bind.annotation.PutMapping("/{scheduleId}/cron")
    public Result<Void> updateCronExpression(
            @PathVariable String scheduleId,
            @org.springframework.web.bind.annotation.RequestBody String cronExpression) {
        scheduleApplicationService.updateCronExpression(
                ScheduleId.of(scheduleId),
                CronExpression.of(cronExpression));
        return Result.success();
    }

    @Operation(summary = "添加执行动作")
    @org.springframework.web.bind.annotation.PostMapping("/{scheduleId}/actions")
    public Result<Void> addAction(
            @PathVariable String scheduleId,
            @org.springframework.web.bind.annotation.RequestBody SceneActionDTO action) {
        scheduleApplicationService.addAction(ScheduleId.of(scheduleId), action);
        return Result.success();
    }

    @Operation(summary = "移除执行动作")
    @org.springframework.web.bind.annotation.DeleteMapping("/{scheduleId}/actions/{actionId}")
    public Result<Void> removeAction(
            @PathVariable String scheduleId,
            @PathVariable String actionId) {
        scheduleApplicationService.removeAction(ScheduleId.of(scheduleId), ActionId.of(actionId));
        return Result.success();
    }

    @Operation(summary = "手动执行定时计划")
    @org.springframework.web.bind.annotation.PostMapping("/{scheduleId}/execute")
    public Result<ScheduleExecutionResultDTO> executeSchedule(
            @PathVariable String scheduleId) {
        ScheduleExecutionResultDTO result = scheduleApplicationService.executeSchedule(ScheduleId.of(scheduleId));
        return Result.success(result);
    }

    @Operation(summary = "获取定时计划执行日志")
    @org.springframework.web.bind.annotation.GetMapping("/{scheduleId}/execution-logs")
    public Result<List<ScheduleExecutionLogDTO>> getScheduleExecutionLogs(
            @PathVariable String scheduleId,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "1") int page,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size) {
        List<ScheduleExecutionLogDTO> logs = scheduleApplicationService.getScheduleExecutionLogs(
                ScheduleId.of(scheduleId), page, size);
        return Result.success(logs);
    }
}
