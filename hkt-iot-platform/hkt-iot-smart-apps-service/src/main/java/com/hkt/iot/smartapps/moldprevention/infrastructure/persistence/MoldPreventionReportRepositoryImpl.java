package com.hkt.iot.smartapps.moldprevention.infrastructure.persistence;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.moldprevention.domain.model.*;
import com.hkt.iot.smartapps.moldprevention.domain.repository.MoldPreventionReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 防霉效果报告仓储实现
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class MoldPreventionReportRepositoryImpl implements MoldPreventionReportRepository {

    private final MoldPreventionReportJpaRepository jpaRepository;

    @Override
    @Transactional
    public void save(MoldPreventionReport report) {
        log.debug("保存防霉效果报告：reportId={}, zoneId={}, status={}", 
            report.getId() != null ? report.getId().getValue() : "new",
            report.getZoneId() != null ? report.getZoneId().getValue() : "N/A",
            report.getStatus());
        
        MoldPreventionReportEntity entity = toEntity(report);
        jpaRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MoldPreventionReport> findById(ReportId id) {
        log.debug("查询防霉效果报告：id={}", id.getValue());
        
        return jpaRepository.findByIdWithDeletedCheck(id.getValue())
            .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionReport> findByZoneId(ZoneId zoneId) {
        log.debug("查询区域下的防霉效果报告：zoneId={}", zoneId.getValue());
        
        List<MoldPreventionReportEntity> entities = jpaRepository.findByZoneId(zoneId.getValue());
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionReport> findByTenantId(TenantId tenantId) {
        log.debug("查询租户下的防霉效果报告：tenantId={}", tenantId.getValue());
        
        List<MoldPreventionReportEntity> entities = jpaRepository.findByTenantId(tenantId.getValue());
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionReport> findByZoneIdAndPeriod(ZoneId zoneId, LocalDateTime from, LocalDateTime to) {
        log.debug("查询区域指定时间段的报告：zoneId={}, from={}, to={}", zoneId.getValue(), from, to);
        
        List<MoldPreventionReportEntity> entities = jpaRepository.findByZoneIdAndPeriod(zoneId.getValue(), from, to);
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionReport> findByPeriod(ReportPeriod period, int year, int month) {
        log.debug("查询指定周期的报告：period={}, year={}, month={}", period, year, month);
        
        MoldPreventionReportEntity.ReportPeriod entityPeriod = 
            MoldPreventionReportEntity.ReportPeriod.valueOf(period.name());
        
        List<MoldPreventionReportEntity> entities = jpaRepository.findByPeriod(entityPeriod, year, month);
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoldPreventionReport> findUncompletedReports() {
        log.debug("查询未完成报告");
        
        List<MoldPreventionReportEntity> entities = jpaRepository.findUncompletedReports();
        return entities.stream()
            .map(this::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(ReportId id) {
        log.debug("删除防霉效果报告：id={}", id.getValue());
        
        jpaRepository.findByIdWithDeletedCheck(id.getValue())
            .ifPresent(entity -> {
                entity.setDeleted(true);
                entity.setDeletedAt(LocalDateTime.now());
                jpaRepository.save(entity);
            });
    }

    /**
     * 将领域模型转换为 JPA 实体
     */
    private MoldPreventionReportEntity toEntity(MoldPreventionReport report) {
        MoldPreventionReportEntity entity = new MoldPreventionReportEntity();
        if (report.getId() != null) {
            entity.setId(Long.parseLong(report.getId().getValue()));
        }
        entity.setTenantId(report.getTenantId().getValue());
        entity.setZoneId(report.getZoneId().getValue());
        entity.setReportPeriod(MoldPreventionReportEntity.ReportPeriod.valueOf(report.getPeriod().name()));
        entity.setPeriodYear(report.getPeriod().getYear());
        entity.setPeriodMonth(report.getPeriod().getMonth());
        entity.setCollectedFrom(report.getCollectedFrom());
        entity.setCollectedTo(report.getCollectedTo());
        
        if (report.getPredictionAccuracy() != null) {
            entity.setPredictionAccuracy(report.getPredictionAccuracy().getAccuracy());
            entity.setTotalPredictions(report.getPredictionAccuracy().getTotalPredictions());
            entity.setCorrectPredictions(report.getPredictionAccuracy().getCorrectPredictions());
        }
        
        entity.setStatus(MoldPreventionReportEntity.ReportStatus.valueOf(report.getStatus().name()));
        entity.setStartedAt(report.getStartedAt());
        entity.setCompletedAt(report.getCompletedAt());
        
        if (report.getGenerationTime() != null) {
            entity.setGenerationTimeMs(report.getGenerationTime().toMillis());
        }
        
        entity.setErrorMessage(report.getErrorMessage());
        entity.setExportFormat(report.getExportFormat());
        entity.setExportUrl(report.getExportUrl());
        entity.setVersion(report.getVersion());
        
        return entity;
    }

    /**
     * 将 JPA 实体转换为领域模型
     */
    private MoldPreventionReport toDomain(MoldPreventionReportEntity entity) {
        ReportPeriod period = ReportPeriod.of(entity.getPeriodYear(), entity.getPeriodMonth());
        
        PredictionAccuracy accuracy = null;
        if (entity.getPredictionAccuracy() != null) {
            accuracy = PredictionAccuracy.builder()
                .accuracy(entity.getPredictionAccuracy())
                .totalPredictions(entity.getTotalPredictions() != null ? entity.getTotalPredictions() : 0)
                .correctPredictions(entity.getCorrectPredictions() != null ? entity.getCorrectPredictions() : 0)
                .calculatedAt(entity.getCompletedAt() != null ? entity.getCompletedAt() : LocalDateTime.now())
                .build();
        }
        
        Duration generationTime = null;
        if (entity.getGenerationTimeMs() != null) {
            generationTime = Duration.ofMillis(entity.getGenerationTimeMs());
        }
        
        return MoldPreventionReport.builder()
            .id(ReportId.of(entity.getId().toString()))
            .zoneId(ZoneId.of(entity.getZoneId().toString()))
            .tenantId(TenantId.of(entity.getTenantId()))
            .period(period)
            .collectedFrom(entity.getCollectedFrom())
            .collectedTo(entity.getCollectedTo())
            .predictionAccuracy(accuracy)
            .status(com.hkt.iot.smartapps.moldprevention.domain.model.ReportStatus.valueOf(entity.getStatus().name()))
            .startedAt(entity.getStartedAt())
            .completedAt(entity.getCompletedAt())
            .generationTime(generationTime)
            .errorMessage(entity.getErrorMessage())
            .exportFormat(entity.getExportFormat())
            .exportUrl(entity.getExportUrl())
            .sensorDataTraces(new ArrayList<>())
            .incidents(new ArrayList<>())
            .version(entity.getVersion())
            .build();
    }
}
