package com.hkt.iot.smartapps.smartlivestock.domain.service.impl;

import com.hkt.iot.smartapps.smartlivestock.domain.model.Geofence;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.GeofenceViolation;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockId;
import com.hkt.iot.smartapps.smartlivestock.domain.model.LivestockLocation;
import com.hkt.iot.smartapps.smartlivestock.domain.model.Location;
import com.hkt.iot.smartapps.smartlivestock.domain.model.ViolationId;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.GeofenceRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.GeofenceViolationRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.service.GeofenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 电子围栏领域服务实现类
 *
 * 职责：实现电子围栏相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeofenceServiceImpl implements GeofenceService {

    private final GeofenceRepository geofenceRepository;
    private final GeofenceViolationRepository violationRepository;

    @Override
    public GeofenceViolation checkViolation(GeofenceId geofenceId, LivestockId livestockId, Location location) {
        log.debug("检查围栏越界, geofenceId={}, livestockId={}, location={}", geofenceId, livestockId, location);

        Optional<Geofence> geofenceOpt = geofenceRepository.findById(geofenceId);
        if (!geofenceOpt.isPresent()) {
            log.warn("围栏不存在, geofenceId={}", geofenceId);
            return null;
        }

        Geofence geofence = geofenceOpt.get();
        GeofenceViolation violation = geofence.checkViolation(livestockId, location);

        if (violation != null) {
            log.info("检测到围栏越界, geofenceId={}, livestockId={}, violationType={}",
                    geofenceId, livestockId, violation.getViolationType());
            violationRepository.save(violation);
        }

        return violation;
    }

    @Override
    public Map<LivestockId, GeofenceViolation> batchCheckViolation(GeofenceId geofenceId, List<LivestockLocation> locations) {
        log.debug("批量检查围栏越界, geofenceId={}, locationCount={}", geofenceId, locations.size());

        Map<LivestockId, GeofenceViolation> violations = new HashMap<>();

        Optional<Geofence> geofenceOpt = geofenceRepository.findById(geofenceId);
        if (!geofenceOpt.isPresent()) {
            log.warn("围栏不存在, geofenceId={}", geofenceId);
            return violations;
        }

        Geofence geofence = geofenceOpt.get();

        for (LivestockLocation livestockLocation : locations) {
            try {
                GeofenceViolation violation = geofence.checkViolation(
                        livestockLocation.getLivestockId(),
                        livestockLocation.getLocation()
                );

                if (violation != null) {
                    violations.put(livestockLocation.getLivestockId(), violation);
                    violationRepository.save(violation);
                }
            } catch (Exception e) {
                log.warn("检查围栏越界失败, livestockId={}", livestockLocation.getLivestockId(), e);
            }
        }

        log.info("批量检查完成, geofenceId={}, totalChecked={}, violationCount={}",
                geofenceId, locations.size(), violations.size());

        return violations;
    }

    @Override
    public List<GeofenceViolation> getActiveViolations(GeofenceId geofenceId) {
        log.debug("获取围栏活跃违规, geofenceId={}", geofenceId);

        List<GeofenceViolation> violations = violationRepository.findByGeofenceIdAndStatus(
                geofenceId,
                GeofenceViolation.ViolationStatus.PENDING
        );

        log.debug("获取活跃违规完成, geofenceId={}, count={}", geofenceId, violations.size());
        return violations;
    }

    @Override
    public boolean resolveViolation(ViolationId violationId) {
        return resolveViolation(violationId, null);
    }

    @Override
    public boolean resolveViolation(ViolationId violationId, String notes) {
        log.debug("解决违规, violationId={}, notes={}", violationId, notes);

        Optional<GeofenceViolation> violationOpt = violationRepository.findById(violationId);
        if (!violationOpt.isPresent()) {
            log.warn("违规记录不存在, violationId={}", violationId);
            return false;
        }

        GeofenceViolation violation = violationOpt.get();
        violation.markAsResolved(notes);
        violationRepository.save(violation);

        log.info("违规已解决, violationId={}, livestockId={}", violationId, violation.getLivestockId());
        return true;
    }

    @Override
    public List<GeofenceViolation> getActiveViolationsByLivestock(LivestockId livestockId) {
        log.debug("获取牲畜的活跃违规, livestockId={}", livestockId);

        List<GeofenceViolation> violations = violationRepository.findByLivestockIdAndStatus(
                livestockId,
                GeofenceViolation.ViolationStatus.PENDING
        );

        log.debug("获取牲畜活跃违规完成, livestockId={}, count={}", livestockId, violations.size());
        return violations;
    }

    @Override
    public boolean isInsideGeofence(GeofenceId geofenceId, Location location) {
        log.debug("检查位置是否在围栏内, geofenceId={}, location={}", geofenceId, location);

        Optional<Geofence> geofenceOpt = geofenceRepository.findById(geofenceId);
        if (!geofenceOpt.isPresent()) {
            log.warn("围栏不存在, geofenceId={}", geofenceId);
            return false;
        }

        Geofence geofence = geofenceOpt.get();
        boolean isInside = geofence.contains(location);

        log.debug("位置检查完成, geofenceId={}, isInside={}", geofenceId, isInside);
        return isInside;
    }
}
