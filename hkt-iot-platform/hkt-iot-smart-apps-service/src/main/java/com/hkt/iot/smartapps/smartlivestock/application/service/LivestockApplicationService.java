package com.hkt.iot.smartapps.smartlivestock.application.service;

import com.hkt.iot.common.result.PageResult;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.smartlivestock.application.dto.*;
import com.hkt.iot.smartapps.smartlivestock.domain.model.*;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.GeofenceRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.repository.LivestockRepository;
import com.hkt.iot.smartapps.smartlivestock.domain.service.GeofenceService;
import com.hkt.iot.smartapps.smartlivestock.domain.service.LivestockHealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 牲畜应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LivestockApplicationService {

    private final LivestockRepository livestockRepository;
    private final GeofenceRepository geofenceRepository;
    private final LivestockHealthService healthService;
    private final GeofenceService geofenceService;

    @Transactional
    public LivestockDTO createLivestock(CreateLivestockRequest request) {
        log.info("创建牲畜记录: tag={}", request.getTag());
        
        Livestock livestock = Livestock.create(
                LivestockTag.of(request.getTag()),
                LivestockType.valueOf(request.getType()),
                Gender.valueOf(request.getGender()),
                request.getBirthDate(),
                Weight.kg(request.getWeight()),
                request.getBreed(),
                TenantId.of(request.getTenantId())
        );
        
        livestockRepository.save(livestock);
        
        return LivestockDTO.fromDomain(livestock);
    }

    @Transactional
    public LivestockDTO updateLivestock(String id, UpdateLivestockRequest request) {
        log.info("更新牲畜信息: id={}", id);
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        if (request.getBreed() != null) {
            livestock.setBreed(request.getBreed());
        }
        if (request.getNotes() != null) {
            livestock.setNotes(request.getNotes());
        }
        
        livestockRepository.save(livestock);
        
        return LivestockDTO.fromDomain(livestock);
    }

    public LivestockDTO getLivestock(String id) {
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        return LivestockDTO.fromDomain(livestock);
    }

    public PageResult<LivestockDTO> listLivestock(String tenantId, LivestockQueryRequest request) {
        List<Livestock> livestockList = livestockRepository.findByTenantId(TenantId.of(tenantId));
        
        List<LivestockDTO> dtoList = livestockList.stream()
                .map(LivestockDTO::fromDomain)
                .collect(Collectors.toList());
        
        return PageResult.of(dtoList, dtoList.size());
    }

    @Transactional
    public void updateLocation(String id, LocationRequest request) {
        log.info("更新牲畜位置: id={}, lat={}, lng={}", id, request.getLatitude(), request.getLongitude());
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        Coordinate coordinate = new Coordinate(request.getLatitude(), request.getLongitude());
        Location location = new Location(coordinate, LocalDateTime.now());
        
        livestock.updateLocation(location);
        
        if (livestock.getGeofenceId() != null) {
            GeofenceViolation violation = geofenceService.checkViolation(
                    livestock.getGeofenceId(), 
                    livestock.getId(), 
                    location
            );
            if (violation != null) {
                log.warn("检测到围栏违规: livestockId={}, geofenceId={}", id, livestock.getGeofenceId().getValue());
            }
        }
        
        livestockRepository.save(livestock);
    }

    @Transactional
    public HealthScoreDTO updateHealthRecord(String id, HealthRecordRequest request) {
        log.info("更新健康记录: id={}", id);
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        HealthRecord record = HealthRecord.builder()
                .temperature(request.getTemperature())
                .heartRate(request.getHeartRate())
                .respiratoryRate(request.getRespiratoryRate())
                .steps(request.getSteps())
                .feedIntake(request.getFeedIntake() != null 
                        ? FeedIntake.kg(request.getFeedIntake()) : null)
                .recordTime(LocalDateTime.now())
                .build();
        
        HealthScore score = healthService.calculateHealthScore(livestock.getId(), record);
        livestock.updateHealthScore(score);
        
        livestockRepository.save(livestock);
        
        return HealthScoreDTO.fromDomain(score);
    }

    @Transactional
    public void markSick(String id, String diagnosis) {
        log.info("标记牲畜生病: id={}, diagnosis={}", id, diagnosis);
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        livestock.markSick(diagnosis);
        livestockRepository.save(livestock);
    }

    @Transactional
    public void markRecovered(String id) {
        log.info("标记牲畜康复: id={}", id);
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        livestock.markRecovered();
        livestockRepository.save(livestock);
    }

    @Transactional
    public void assignGeofence(String id, String geofenceId) {
        log.info("分配电子围栏: livestockId={}, geofenceId={}", id, geofenceId);
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        livestock.assignGeofence(GeofenceId.of(geofenceId));
        livestockRepository.save(livestock);
    }

    public LivestockHealthReportDTO generateHealthReport(String id, String period) {
        log.info("生成健康报告: id={}, period={}", id, period);
        
        Livestock livestock = livestockRepository.findById(LivestockId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("牲畜不存在: " + id));
        
        LivestockHealthReport report = healthService.generateHealthReport(
                livestock.getId(), 
                ReportPeriod.valueOf(period)
        );
        
        return LivestockHealthReportDTO.builder()
                .reportId(report.getId().getValue())
                .livestockId(id)
                .livestockTag(livestock.getTag().getValue())
                .period(period)
                .averageHealthScore(report.getAverageHealthScore())
                .generatedAt(LocalDateTime.now())
                .build();
    }
}
