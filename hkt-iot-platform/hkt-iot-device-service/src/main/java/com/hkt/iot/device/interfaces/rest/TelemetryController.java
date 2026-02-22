package com.hkt.iot.device.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.device.application.service.TelemetryService;
import com.hkt.iot.device.interfaces.rest.dto.TelemetryQueryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 遥测数据REST控制器
 * 提供遥测数据的查询接口
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "遥测数据", description = "设备遥测数据查询接口")
public class TelemetryController {

    private final TelemetryService telemetryService;

    @GetMapping("/device/{deviceId}/latest")
    @Operation(summary = "获取最新遥测数据", description = "查询设备的最新遥测数据快照")
    public Result<Map<String, Object>> getLatestTelemetry(
            @Parameter(description = "设备ID") @PathVariable Long deviceId) {
        try {
            Map<String, Object> data = telemetryService.getLatestTelemetry(deviceId);
            return Result.success(data);
        } catch (Exception e) {
            log.error("获取最新遥测数据失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/query")
    @Operation(summary = "查询历史遥测数据", description = "查询指定时间范围内的历史遥测数据")
    public Result<List<Map<String, Object>>> queryHistoricalTelemetry(
            @Valid @RequestBody TelemetryQueryRequest request) {
        try {
            List<Map<String, Object>> data = telemetryService.getHistoricalTelemetry(
                    request.getDeviceSn(),
                    request.getDataKeys() != null && !request.getDataKeys().isEmpty()
                            ? request.getDataKeys().get(0) : null,
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getLimit() != null ? request.getLimit() : 1000
            );
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询历史遥测数据失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/multi-query")
    @Operation(summary = "查询多点位历史数据", description = "查询多个数据点的历史遥测数据")
    public Result<Map<String, List<Map<String, Object>>>> queryMultiTelemetry(
            @Valid @RequestBody TelemetryQueryRequest request) {
        try {
            if (request.getDataKeys() == null || request.getDataKeys().isEmpty()) {
                return Result.error(400, "dataKeys不能为空");
            }

            Map<String, List<Map<String, Object>>> data = telemetryService.getMultiHistoricalTelemetry(
                    request.getDeviceSn(),
                    request.getDataKeys(),
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getLimit() != null ? request.getLimit() : 1000
            );
            return Result.success(data);
        } catch (Exception e) {
            log.error("查询多点位历史数据失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/aggregate")
    @Operation(summary = "聚合遥测数据", description = "按时间窗口聚合遥测数据")
    public Result<Map<String, Object>> aggregateTelemetry(
            @Valid @RequestBody TelemetryQueryRequest request) {
        try {
            if (request.getAggregation() == null) {
                return Result.error(400, "aggregation不能为空");
            }

            Map<String, Object> result = telemetryService.aggregateTelemetry(
                    request.getDeviceSn(),
                    request.getDataKeys() != null && !request.getDataKeys().isEmpty()
                            ? request.getDataKeys().get(0) : null,
                    request.getStartTime(),
                    request.getEndTime(),
                    request.getAggregation()
            );
            return Result.success(result);
        } catch (Exception e) {
            log.error("聚合遥测数据失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/receive")
    @Operation(summary = "接收遥测数据", description = "设备上报遥测数据的接口")
    public Result<Void> receiveTelemetry(
            @RequestBody Map<String, Object> payload) {
        try {
            Long tenantId = getLongValue(payload, "tenantId");
            Long deviceId = getLongValue(payload, "deviceId");
            String deviceSn = (String) payload.get("deviceSn");

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) payload.get("data");

            telemetryService.receiveTelemetry(
                    tenantId,
                    deviceId,
                    deviceSn,
                    data,
                    LocalDateTime.now(),
                    null
            );
            return Result.success();
        } catch (Exception e) {
            log.error("接收遥测数据失败: error={}", e.getMessage(), e);
            return Result.error(500, e.getMessage());
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }
}
