package com.hkt.iot.smartapps.moldprevention.application.service;

import com.hkt.iot.common.result.PageResult;
import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.moldprevention.application.dto.*;
import com.hkt.iot.smartapps.moldprevention.domain.model.*;
import com.hkt.iot.smartapps.moldprevention.domain.repository.MoldPreventionZoneRepository;
import com.hkt.iot.smartapps.moldprevention.domain.service.HumidityControlService;
import com.hkt.iot.smartapps.moldprevention.domain.service.MoldRiskEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 防霉管控应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MoldPreventionApplicationService {

    private final MoldPreventionZoneRepository zoneRepository;
    private final MoldRiskEvaluationService riskEvaluationService;
    private final HumidityControlService humidityControlService;

    @Transactional
    public MoldPreventionZoneDTO createZone(CreateMoldPreventionZoneRequest request) {
        log.info("创建防霉管控区域: name={}", request.getName());

        MoldRiskThreshold threshold = MoldRiskThreshold.builder()
                .humidityLow(request.getHumidityThresholdLow() != null ? request.getHumidityThresholdLow() : 55.0)
                .humidityHigh(request.getHumidityThresholdHigh() != null ? request.getHumidityThresholdHigh() : 65.0)
                .build();

        MoldPreventionZone zone = MoldPreventionZone.create(
                ZoneName.of(request.getName()),
                ZoneCode.generate(),
                SpaceId.of(request.getSpaceId()),
                TenantId.of(request.getTenantId()),
                request.getDescription(),
                threshold,
                HumidityControlStrategy.defaultStrategy()
        );

        zoneRepository.save(zone);

        return MoldPreventionZoneDTO.fromDomain(zone);
    }

    public MoldPreventionZoneDTO getZone(String id) {
        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + id));
        return MoldPreventionZoneDTO.fromDomain(zone);
    }

    public List<MoldPreventionZoneDTO> listZones(String tenantId) {
        List<MoldPreventionZone> zones = zoneRepository.findByTenantId(TenantId.of(tenantId));
        return zones.stream()
                .map(MoldPreventionZoneDTO::fromDomain)
                .collect(Collectors.toList());
    }

    public List<MoldPreventionZoneDTO> listActiveZones(String tenantId) {
        List<MoldPreventionZone> zones = zoneRepository.findActiveZones(TenantId.of(tenantId));
        return zones.stream()
                .map(MoldPreventionZoneDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void activateZone(String id) {
        log.info("激活防霉管控区域: id={}", id);

        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + id));

        zone.activate();
        zoneRepository.save(zone);
    }

    @Transactional
    public void deactivateZone(String id) {
        log.info("停用防霉管控区域: id={}", id);

        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + id));

        zone.deactivate();
        zoneRepository.save(zone);
    }

    @Transactional
    public RiskEvaluationResultDTO evaluateRisk(String id, EnvironmentDataRequest request) {
        log.info("评估霉菌风险: zoneId={}", id);

        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + id));

        EnvironmentData data = EnvironmentData.builder()
                .temperature(request.getTemperature())
                .humidity(request.getHumidity())
                .timestamp(request.getTimestamp() != null ? LocalDateTime.parse(request.getTimestamp()) : LocalDateTime.now())
                .build();

        MoldRiskEvaluationResult result = riskEvaluationService.evaluateRisk(zone.getId(), data);
        int riskScore = riskEvaluationService.calculateRiskScore(data);

        zoneRepository.save(zone);

        return RiskEvaluationResultDTO.fromDomain(result, riskScore);
    }

    @Transactional
    public void executeAutoControl(String id) {
        log.info("执行自动控制: zoneId={}", id);

        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + id));

        humidityControlService.autoAdjust(zone.getId());
    }

    @Transactional
    public void addSensor(String zoneId, String sensorId, String sensorType) {
        log.info("添加传感器: zoneId={}, sensorId={}", zoneId, sensorId);

        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(zoneId))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId));

        SensorDevice sensor = SensorDevice.builder()
                .id(SensorId.of(sensorId))
                .type(sensorType)
                .online(true)
                .build();

        zone.addSensor(sensor);
        zoneRepository.save(zone);
    }

    @Transactional
    public void addController(String zoneId, String controllerId, String controllerType) {
        log.info("添加控制器: zoneId={}, controllerId={}", zoneId, controllerId);

        MoldPreventionZone zone = zoneRepository.findById(ZoneId.of(zoneId))
                .orElseThrow(() -> new IllegalArgumentException("防霉管控区域不存在: " + zoneId));

        ControlDevice controller = ControlDevice.builder()
                .id(ControllerId.of(controllerId))
                .type(ControllerType.valueOf(controllerType))
                .online(true)
                .build();

        zone.addController(controller);
        zoneRepository.save(zone);
    }

    @Transactional
    public void deleteZone(String id) {
        log.info("删除防霉管控区域: id={}", id);
        zoneRepository.delete(ZoneId.of(id));
    }
}
