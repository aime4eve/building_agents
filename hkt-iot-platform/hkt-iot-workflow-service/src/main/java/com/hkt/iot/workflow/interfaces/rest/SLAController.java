package com.hkt.iot.workflow.interfaces.rest;

import com.hkt.iot.workflow.application.dto.ApiResponse;
import com.hkt.iot.workflow.application.dto.SLAConfigDTO;
import com.hkt.iot.workflow.application.dto.SLAReportDTO;
import com.hkt.iot.workflow.application.service.SLAMonitoringApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * SLA 监控控制器
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/workflow-engine")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "SLA 监控 API", description = "SLA 监控管理接口")
public class SLAController {

    private final SLAMonitoringApplicationService slaMonitoringApplicationService;

    /**
     * 创建 SLA 配置
     */
    @PostMapping("/sla/config")
    @Operation(summary = "创建 SLA 配置")
    public ApiResponse<SLAConfigDTO> createSLAConfig(
            @RequestParam String processDefinitionKey,
            @RequestParam(required = false) String taskDefinitionKey,
            @RequestParam String tenantId,
            @RequestParam Long responseTimeLimit,
            @RequestParam Long resolutionTimeLimit,
            @RequestParam(required = false) String priority) {
        SLAConfigDTO dto = slaMonitoringApplicationService.createSLAConfig(
                processDefinitionKey,
                taskDefinitionKey,
                tenantId,
                responseTimeLimit,
                resolutionTimeLimit,
                priority
        );
        return ApiResponse.success(dto);
    }

    /**
     * 查询 SLA 配置列表
     */
    @GetMapping("/sla/config/list")
    @Operation(summary = "查询 SLA 配置列表")
    public ApiResponse<List<SLAConfigDTO>> listSLAConfigs(@RequestParam String tenantId) {
        List<SLAConfigDTO> list = slaMonitoringApplicationService.listSLAConfigs(tenantId);
        return ApiResponse.success(list);
    }

    /**
     * 查询 SLA 报告
     */
    @GetMapping("/sla/report")
    @Operation(summary = "查询 SLA 报告")
    public ApiResponse<SLAReportDTO> getSLAReport(
            @RequestParam String tenantId,
            @RequestParam @DateTimeFormat(iso = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME) LocalDateTime endTime) {
        SLAReportDTO report = slaMonitoringApplicationService.getSLAReport(tenantId, startTime, endTime);
        return ApiResponse.success(report);
    }
}
