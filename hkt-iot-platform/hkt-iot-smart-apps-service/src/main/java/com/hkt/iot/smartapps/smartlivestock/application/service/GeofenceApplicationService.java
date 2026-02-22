package com.hkt.iot.smartapps.smartlivestock.application.service;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.smartlivestock.application.dto.*;
import com.hkt.iot.smartapps.smartlivestock.domain.model.*;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.GeofenceRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.GeofenceViolationRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.service.GeofenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 电子围栏应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceApplicationService {

    private final GeofenceRepository geofenceRepository;
    private final GeofenceViolationRepository violationRepository;
    private final GeofenceService geofenceService;

    @Transactional
    public GeofenceDTO createGeofence(CreateGeofenceRequest request) {
        log.info("创建电子围栏: name={}", request.getName());
        
        List<Coordinate> boundary = request.getBoundary().stream()
                .map(c -> new Coordinate(c.getLatitude(), c.getLongitude()))
                .collect(Collectors.toList());
        
        Geofence geofence = Geofence.create(
                GeofenceName.of(request.getName()),
                GeofenceCode.generate(),
                GeofenceType.valueOf(request.getType()),
                boundary,
                TenantId.of(request.getTenantId()),
                null
        );
        
        geofenceRepository.save(geofence);
        
        return GeofenceDTO.fromDomain(geofence);
    }

    @Transactional
    public GeofenceDTO updateGeofence(String id, UpdateGeofenceRequest request) {
        log.info("更新电子围栏: id={}", id);
        
        Geofence geofence = geofenceRepository.findById(GeofenceId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("电子围栏不存在: " + id));
        
        if (request.getName() != null) {
            geofence.setName(GeofenceName.of(request.getName()));
        }
        if (request.getBoundary() != null) {
            List<Coordinate> boundary = request.getBoundary().stream()
                    .map(c -> new Coordinate(c.getLatitude(), c.getLongitude()))
                    .collect(Collectors.toList());
            geofence.updateBoundary(boundary);
        }
        if (request.getDescription() != null) {
            geofence.setDescription(request.getDescription());
        }
        
        geofenceRepository.save(geofence);
        
        return GeofenceDTO.fromDomain(geofence);
    }

    public GeofenceDTO getGeofence(String id) {
        Geofence geofence = geofenceRepository.findById(GeofenceId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("电子围栏不存在: " + id));
        return GeofenceDTO.fromDomain(geofence);
    }

    public List<GeofenceDTO> listGeofences(String tenantId) {
        List<Geofence> geofences = geofenceRepository.findByTenantId(TenantId.of(tenantId));
        return geofences.stream()
                .map(GeofenceDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void activateGeofence(String id) {
        log.info("激活电子围栏: id={}", id);
        
        Geofence geofence = geofenceRepository.findById(GeofenceId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("电子围栏不存在: " + id));
        
        geofence.activate();
        geofenceRepository.save(geofence);
    }

    @Transactional
    public void deactivateGeofence(String id) {
        log.info("停用电子围栏: id={}", id);
        
        Geofence geofence = geofenceRepository.findById(GeofenceId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("电子围栏不存在: " + id));
        
        geofence.deactivate();
        geofenceRepository.save(geofence);
    }

    public List<GeofenceViolationDTO> getActiveViolations(String geofenceId) {
        List<GeofenceViolation> violations = geofenceService.getActiveViolations(GeofenceId.of(geofenceId));
        return violations.stream()
                .map(GeofenceViolationDTO::fromDomain)
                .collect(Collectors.toList());
    }

    @Transactional
    public void resolveViolation(String violationId) {
        log.info("解决违规: id={}", violationId);
        geofenceService.resolveViolation(ViolationId.of(violationId));
    }
}
