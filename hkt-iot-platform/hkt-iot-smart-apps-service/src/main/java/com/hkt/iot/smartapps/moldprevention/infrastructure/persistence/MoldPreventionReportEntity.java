package com.hkt.iot.smartapps.moldprevention.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 防霉效果报告 JPA 实体
 * 对应数据库表：mold_prevention_report
 */
@Entity
@Table(name = "mold_prevention_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted = 0")
public class MoldPreventionReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    @Column(name = "report_period", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReportPeriod reportPeriod;

    @Column(name = "period_year", nullable = false)
    private Integer periodYear;

    @Column(name = "period_month", nullable = false)
    private Integer periodMonth;

    @Column(name = "period_day")
    private Integer periodDay;

    @Column(name = "collected_from", nullable = false)
    private LocalDateTime collectedFrom;

    @Column(name = "collected_to", nullable = false)
    private LocalDateTime collectedTo;

    @Column(name = "prediction_accuracy", precision = 5, scale = 4)
    private Double predictionAccuracy;

    @Column(name = "total_predictions")
    private Integer totalPredictions;

    @Column(name = "correct_predictions")
    private Integer correctPredictions;

    @Column(name = "risk_statistics", columnDefinition = "JSON")
    private String riskStatistics;

    @Column(name = "humidity_statistics", columnDefinition = "JSON")
    private String humidityStatistics;

    @Column(name = "control_effectiveness", columnDefinition = "JSON")
    private String controlEffectiveness;

    @Column(name = "incident_count")
    private Integer incidentCount;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "generation_time_ms")
    private Long generationTimeMs;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "export_format", length = 20)
    private String exportFormat;

    @Column(name = "export_url", length = 500)
    private String exportUrl;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 报告周期枚举
     */
    public enum ReportPeriod {
        DAILY, WEEKLY, MONTHLY
    }

    /**
     * 报告状态枚举
     */
    public enum ReportStatus {
        GENERATING, COMPLETED, FAILED
    }

    // Setters for builder pattern
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }
    public void setReportPeriod(ReportPeriod reportPeriod) { this.reportPeriod = reportPeriod; }
    public void setPeriodYear(Integer periodYear) { this.periodYear = periodYear; }
    public void setPeriodMonth(Integer periodMonth) { this.periodMonth = periodMonth; }
    public void setPeriodDay(Integer periodDay) { this.periodDay = periodDay; }
    public void setCollectedFrom(LocalDateTime collectedFrom) { this.collectedFrom = collectedFrom; }
    public void setCollectedTo(LocalDateTime collectedTo) { this.collectedTo = collectedTo; }
    public void setPredictionAccuracy(Double predictionAccuracy) { this.predictionAccuracy = predictionAccuracy; }
    public void setTotalPredictions(Integer totalPredictions) { this.totalPredictions = totalPredictions; }
    public void setCorrectPredictions(Integer correctPredictions) { this.correctPredictions = correctPredictions; }
    public void setRiskStatistics(String riskStatistics) { this.riskStatistics = riskStatistics; }
    public void setHumidityStatistics(String humidityStatistics) { this.humidityStatistics = humidityStatistics; }
    public void setControlEffectiveness(String controlEffectiveness) { this.controlEffectiveness = controlEffectiveness; }
    public void setIncidentCount(Integer incidentCount) { this.incidentCount = incidentCount; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public void setGenerationTimeMs(Long generationTimeMs) { this.generationTimeMs = generationTimeMs; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setExportFormat(String exportFormat) { this.exportFormat = exportFormat; }
    public void setExportUrl(String exportUrl) { this.exportUrl = exportUrl; }
    public void setVersion(Long version) { this.version = version; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
    public void setDeletedBy(Long deletedBy) { this.deletedBy = deletedBy; }
}
