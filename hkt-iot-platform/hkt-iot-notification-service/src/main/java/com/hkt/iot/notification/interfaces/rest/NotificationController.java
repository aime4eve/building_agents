package com.hkt.iot.notification.interfaces.rest;

import com.hkt.iot.common.web.Result;
import com.hkt.iot.notification.application.dto.NotificationLogQueryDTO;
import com.hkt.iot.notification.application.dto.NotificationSendDTO;
import com.hkt.iot.notification.application.service.NotificationApplicationService;
import com.hkt.iot.notification.domain.repository.NotificationLogRepository;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationLogPO;
import com.hkt.iot.notification.infrastructure.persistence.po.NotificationRequestPO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "通知发送与管理相关接口")
public class NotificationController {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationRequestRepository requestRepository;
    private final NotificationLogRepository logRepository;

    /**
     * 发送通知
     */
    @PostMapping("/send")
    @Operation(summary = "发送通知", description = "发送单条通知")
    public Result<Long> sendNotification(
            @RequestBody @Valid NotificationSendDTO dto,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        dto.setTenantId(tenantId);
        return notificationApplicationService.sendNotification(dto);
    }

    /**
     * 批量发送通知
     */
    @PostMapping("/batch-send")
    @Operation(summary = "批量发送通知", description = "批量发送多条通知")
    public Result<List<Long>> batchSendNotifications(
            @RequestBody @Valid List<NotificationSendDTO> dtos,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        List<Long> requestIds = dtos.stream()
                .map(dto -> {
                    dto.setTenantId(tenantId);
                    Result<Long> result = notificationApplicationService.sendNotification(dto);
                    return result.getData();
                })
                .toList();

        return Result.success(requestIds);
    }

    /**
     * 获取通知请求状态
     */
    @GetMapping("/requests/{requestId}")
    @Operation(summary = "获取通知请求状态", description = "根据请求ID获取通知状态")
    public Result<NotificationRequestPO> getRequestStatus(
            @PathVariable Long requestId,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        // TODO: 实现获取请求状态
        return Result.success();
    }

    /**
     * 取消通知
     */
    @PostMapping("/requests/{requestId}/cancel")
    @Operation(summary = "取消通知", description = "取消待发送的通知")
    public Result<Void> cancelNotification(
            @PathVariable Long requestId,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        // TODO: 实现取消通知
        return Result.success();
    }

    /**
     * 查询通知日志
     */
    @PostMapping("/logs/query")
    @Operation(summary = "查询通知日志", description = "根据条件查询通知发送日志")
    public Result<List<NotificationLogPO>> queryLogs(
            @RequestBody NotificationLogQueryDTO dto,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        dto.setTenantId(tenantId);
        // TODO: 实现日志查询
        return Result.success();
    }

    /**
     * 获取通知统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取通知统计", description = "获取租户的通知发送统计")
    public Result<Object> getStatistics(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "统计类型: daily/weekly/monthly")
            @RequestParam(defaultValue = "daily") String type
    ) {
        // TODO: 实现统计功能
        return Result.success();
    }
}
