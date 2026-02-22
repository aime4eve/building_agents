package com.hkt.iot.notification.interfaces.rest;

import com.hkt.iot.common.web.Result;
import com.hkt.iot.notification.application.dto.NotificationLogQueryDTO;
import com.hkt.iot.notification.application.dto.NotificationSendDTO;
import com.hkt.iot.notification.application.dto.NotificationStatisticsDTO;
import com.hkt.iot.notification.application.service.NotificationApplicationService;
import com.hkt.iot.notification.application.service.NotificationStatisticsService;
import com.hkt.iot.notification.domain.model.NotificationLog;
import com.hkt.iot.notification.domain.model.NotificationRequest;
import com.hkt.iot.notification.domain.repository.NotificationLogRepository;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    private final NotificationStatisticsService statisticsService;
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
    public Result<NotificationRequest> getRequestStatus(
            @PathVariable Long requestId,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        log.info("获取通知请求状态: requestId={}, tenantId={}", requestId, tenantId);
        
        Optional<NotificationRequest> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            return Result.error(404, "通知请求不存在");
        }
        
        NotificationRequest request = requestOpt.get();
        if (!tenantId.equals(request.getTenantId())) {
            return Result.error(403, "无权访问该通知请求");
        }
        
        return Result.success(request);
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
        log.info("取消通知: requestId={}, tenantId={}", requestId, tenantId);
        
        Optional<NotificationRequest> requestOpt = requestRepository.findById(requestId);
        if (requestOpt.isEmpty()) {
            return Result.error(404, "通知请求不存在");
        }
        
        NotificationRequest request = requestOpt.get();
        if (!tenantId.equals(request.getTenantId())) {
            return Result.error(403, "无权操作该通知请求");
        }
        
        if (request.getStatus() != NotificationRequest.NotificationStatus.PENDING) {
            return Result.error(400, "只有待发送状态的通知可以取消，当前状态: " + request.getStatus().getDescription());
        }
        
        request.cancel();
        requestRepository.save(request);
        
        log.info("通知已取消: requestId={}", requestId);
        return Result.success();
    }

    /**
     * 查询通知日志
     */
    @PostMapping("/logs/query")
    @Operation(summary = "查询通知日志", description = "根据条件查询通知发送日志")
    public Result<List<NotificationLog>> queryLogs(
            @RequestBody NotificationLogQueryDTO dto,
            @RequestHeader("X-Tenant-Id") String tenantId
    ) {
        log.info("查询通知日志: tenantId={}, dto={}", tenantId, dto);
        
        dto.setTenantId(tenantId);
        
        int page = dto.getPage() != null ? dto.getPage() - 1 : 0;
        int size = dto.getSize() != null ? dto.getSize() : 20;
        
        List<NotificationLog> logs = logRepository.findByTenantId(tenantId, page, size);
        
        return Result.success(logs);
    }

    /**
     * 获取通知统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取通知统计", description = "获取租户的通知发送统计")
    public Result<NotificationStatisticsDTO> getStatistics(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @Parameter(description = "统计类型: daily/weekly/monthly")
            @RequestParam(defaultValue = "daily") String type,
            @Parameter(description = "指定日期（Unix时间戳，可选）")
            @RequestParam(required = false) Long date,
            @Parameter(description = "开始时间（Unix时间戳，自定义范围时使用）")
            @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间（Unix时间戳，自定义范围时使用）")
            @RequestParam(required = false) Long endTime
    ) {
        log.info("获取通知统计: tenantId={}, type={}, date={}", tenantId, type, date);
        
        Instant dateInstant = date != null ? Instant.ofEpochSecond(date) : null;
        Instant startInstant = startTime != null ? Instant.ofEpochSecond(startTime) : null;
        Instant endInstant = endTime != null ? Instant.ofEpochSecond(endTime) : null;
        
        NotificationStatisticsDTO statistics;
        
        switch (type.toLowerCase()) {
            case "daily":
                statistics = statisticsService.getDailyStatistics(tenantId, dateInstant);
                break;
            case "weekly":
                statistics = statisticsService.getWeeklyStatistics(tenantId, dateInstant);
                break;
            case "monthly":
                statistics = statisticsService.getMonthlyStatistics(tenantId, dateInstant);
                break;
            case "custom":
                statistics = statisticsService.getCustomRangeStatistics(tenantId, startInstant, endInstant);
                break;
            case "channel":
                statistics = statisticsService.getStatisticsByChannel(tenantId, startInstant, endInstant);
                break;
            case "template":
                statistics = statisticsService.getStatisticsByTemplate(tenantId, startInstant, endInstant);
                break;
            default:
                return Result.error(400, "不支持的统计类型: " + type);
        }
        
        return Result.success(statistics);
    }
}
