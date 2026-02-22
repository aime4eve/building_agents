package com.hkt.iot.smartapps.smartlivestock.interfaces.rest;

import com.hkt.iot.common.result.PageResult;
import com.hkt.iot.common.result.Result;
import com.hkt.iot.smartapps.smartlivestock.application.dto.*;
import com.hkt.iot.smartapps.smartlivestock.application.service.LivestockApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 牲畜管理控制器
 */
@RestController
@RequestMapping("/api/v1/livestock")
@RequiredArgsConstructor
public class LivestockController {

    private final LivestockApplicationService livestockService;

    @PostMapping
    public Result<LivestockDTO> createLivestock(@RequestBody CreateLivestockRequest request) {
        LivestockDTO dto = livestockService.createLivestock(request);
        return Result.success(dto);
    }

    @PutMapping("/{id}")
    public Result<LivestockDTO> updateLivestock(
            @PathVariable String id,
            @RequestBody UpdateLivestockRequest request) {
        LivestockDTO dto = livestockService.updateLivestock(id, request);
        return Result.success(dto);
    }

    @GetMapping("/{id}")
    public Result<LivestockDTO> getLivestock(@PathVariable String id) {
        LivestockDTO dto = livestockService.getLivestock(id);
        return Result.success(dto);
    }

    @GetMapping
    public Result<PageResult<LivestockDTO>> listLivestock(
            @RequestHeader("X-Tenant-Id") String tenantId,
            LivestockQueryRequest request) {
        PageResult<LivestockDTO> result = livestockService.listLivestock(tenantId, request);
        return Result.success(result);
    }

    @PutMapping("/{id}/location")
    public Result<Void> updateLocation(
            @PathVariable String id,
            @RequestBody LocationRequest request) {
        livestockService.updateLocation(id, request);
        return Result.success();
    }

    @PutMapping("/{id}/health")
    public Result<HealthScoreDTO> updateHealthRecord(
            @PathVariable String id,
            @RequestBody HealthRecordRequest request) {
        HealthScoreDTO dto = livestockService.updateHealthRecord(id, request);
        return Result.success(dto);
    }

    @PostMapping("/{id}/sick")
    public Result<Void> markSick(
            @PathVariable String id,
            @RequestParam String diagnosis) {
        livestockService.markSick(id, diagnosis);
        return Result.success();
    }

    @PostMapping("/{id}/recover")
    public Result<Void> markRecovered(@PathVariable String id) {
        livestockService.markRecovered(id);
        return Result.success();
    }

    @PutMapping("/{id}/geofence")
    public Result<Void> assignGeofence(
            @PathVariable String id,
            @RequestParam String geofenceId) {
        livestockService.assignGeofence(id, geofenceId);
        return Result.success();
    }

    @GetMapping("/{id}/report")
    public Result<LivestockHealthReportDTO> generateHealthReport(
            @PathVariable String id,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        LivestockHealthReportDTO dto = livestockService.generateHealthReport(id, period);
        return Result.success(dto);
    }
}
