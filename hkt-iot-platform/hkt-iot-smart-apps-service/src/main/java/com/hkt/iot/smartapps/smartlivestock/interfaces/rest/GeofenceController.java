package com.hkt.iot.smartapps.smartlivestock.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.smartapps.smartlivestock.application.dto.*;
import com.hkt.iot.smartapps.smartlivestock.application.service.GeofenceApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电子围栏管理控制器
 */
@RestController
@RequestMapping("/api/v1/geofences")
@RequiredArgsConstructor
public class GeofenceController {

    private final GeofenceApplicationService geofenceService;

    @PostMapping
    public Result<GeofenceDTO> createGeofence(@RequestBody CreateGeofenceRequest request) {
        GeofenceDTO dto = geofenceService.createGeofence(request);
        return Result.success(dto);
    }

    @PutMapping("/{id}")
    public Result<GeofenceDTO> updateGeofence(
            @PathVariable String id,
            @RequestBody UpdateGeofenceRequest request) {
        GeofenceDTO dto = geofenceService.updateGeofence(id, request);
        return Result.success(dto);
    }

    @GetMapping("/{id}")
    public Result<GeofenceDTO> getGeofence(@PathVariable String id) {
        GeofenceDTO dto = geofenceService.getGeofence(id);
        return Result.success(dto);
    }

    @GetMapping
    public Result<List<GeofenceDTO>> listGeofences(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        List<GeofenceDTO> list = geofenceService.listGeofences(tenantId);
        return Result.success(list);
    }

    @PostMapping("/{id}/activate")
    public Result<Void> activateGeofence(@PathVariable String id) {
        geofenceService.activateGeofence(id);
        return Result.success();
    }

    @PostMapping("/{id}/deactivate")
    public Result<Void> deactivateGeofence(@PathVariable String id) {
        geofenceService.deactivateGeofence(id);
        return Result.success();
    }

    @GetMapping("/{id}/violations")
    public Result<List<GeofenceViolationDTO>> getActiveViolations(@PathVariable String id) {
        List<GeofenceViolationDTO> list = geofenceService.getActiveViolations(id);
        return Result.success(list);
    }

    @PutMapping("/violations/{violationId}/resolve")
    public Result<Void> resolveViolation(@PathVariable String violationId) {
        geofenceService.resolveViolation(violationId);
        return Result.success();
    }
}
