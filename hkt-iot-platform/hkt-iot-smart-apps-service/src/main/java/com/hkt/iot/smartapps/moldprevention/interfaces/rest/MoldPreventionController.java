package com.hkt.iot.smartapps.moldprevention.interfaces.rest;

import com.hkt.iot.common.result.Result;
import com.hkt.iot.smartapps.moldprevention.application.dto.*;
import com.hkt.iot.smartapps.moldprevention.application.service.MoldPreventionApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 防霉管控控制器
 */
@RestController
@RequestMapping("/api/v1/mold-prevention/zones")
@RequiredArgsConstructor
public class MoldPreventionController {

    private final MoldPreventionApplicationService zoneService;

    @PostMapping
    public Result<MoldPreventionZoneDTO> createZone(@RequestBody CreateMoldPreventionZoneRequest request) {
        MoldPreventionZoneDTO dto = zoneService.createZone(request);
        return Result.success(dto);
    }

    @GetMapping("/{id}")
    public Result<MoldPreventionZoneDTO> getZone(@PathVariable String id) {
        MoldPreventionZoneDTO dto = zoneService.getZone(id);
        return Result.success(dto);
    }

    @GetMapping
    public Result<List<MoldPreventionZoneDTO>> listZones(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        List<MoldPreventionZoneDTO> list = zoneService.listZones(tenantId);
        return Result.success(list);
    }

    @GetMapping("/active")
    public Result<List<MoldPreventionZoneDTO>> listActiveZones(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        List<MoldPreventionZoneDTO> list = zoneService.listActiveZones(tenantId);
        return Result.success(list);
    }

    @PostMapping("/{id}/activate")
    public Result<Void> activateZone(@PathVariable String id) {
        zoneService.activateZone(id);
        return Result.success();
    }

    @PostMapping("/{id}/deactivate")
    public Result<Void> deactivateZone(@PathVariable String id) {
        zoneService.deactivateZone(id);
        return Result.success();
    }

    @PostMapping("/{id}/evaluate")
    public Result<RiskEvaluationResultDTO> evaluateRisk(
            @PathVariable String id,
            @RequestBody EnvironmentDataRequest request) {
        RiskEvaluationResultDTO dto = zoneService.evaluateRisk(id, request);
        return Result.success(dto);
    }

    @PostMapping("/{id}/auto-control")
    public Result<Void> executeAutoControl(@PathVariable String id) {
        zoneService.executeAutoControl(id);
        return Result.success();
    }

    @PostMapping("/{id}/sensors")
    public Result<Void> addSensor(
            @PathVariable String id,
            @RequestParam String sensorId,
            @RequestParam String sensorType) {
        zoneService.addSensor(id, sensorId, sensorType);
        return Result.success();
    }

    @PostMapping("/{id}/controllers")
    public Result<Void> addController(
            @PathVariable String id,
            @RequestParam String controllerId,
            @RequestParam String controllerType) {
        zoneService.addController(id, controllerId, controllerType);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteZone(@PathVariable String id) {
        zoneService.deleteZone(id);
        return Result.success();
    }
}
