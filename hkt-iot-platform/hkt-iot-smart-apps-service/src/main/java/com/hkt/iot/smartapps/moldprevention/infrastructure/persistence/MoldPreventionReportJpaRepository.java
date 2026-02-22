package com.hkt.iot.smartapps.moldprevention.infrastructure.persistence;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.moldprevention.domain.model.ReportPeriod;
import com.hkt.iot.smartapps.moldprevention.domain.model.ZoneId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 防霉效果报告 Spring Data JPA Repository
 */
@Repository
public interface MoldPreventionReportJpaRepository extends JpaRepository<MoldPreventionReportEntity, Long> {

    /**
     * 根据区域 ID 查询报告列表
     */
    @Query("SELECT r FROM MoldPreventionReportEntity r WHERE r.zoneId = :zoneId AND r.deleted = 0 ORDER BY r.createdAt DESC")
    List<MoldPreventionReportEntity> findByZoneId(@Param("zoneId") Long zoneId);

    /**
     * 根据租户 ID 查询报告列表
     */
    @Query("SELECT r FROM MoldPreventionReportEntity r WHERE r.tenantId = :tenantId AND r.deleted = 0 ORDER BY r.createdAt DESC")
    List<MoldPreventionReportEntity> findByTenantId(@Param("tenantId") Long tenantId);

    /**
     * 根据区域 ID 和时间范围查询
     */
    @Query("SELECT r FROM MoldPreventionReportEntity r WHERE r.zoneId = :zoneId AND r.deleted = 0 AND r.collectedFrom >= :from AND r.collectedTo <= :to ORDER BY r.createdAt DESC")
    List<MoldPreventionReportEntity> findByZoneIdAndPeriod(
        @Param("zoneId") Long zoneId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    /**
     * 根据报告周期查询
     */
    @Query("SELECT r FROM MoldPreventionReportEntity r WHERE r.reportPeriod = :period AND r.periodYear = :year AND r.periodMonth = :month AND r.deleted = 0")
    List<MoldPreventionReportEntity> findByPeriod(
        @Param("period") MoldPreventionReportEntity.ReportPeriod period,
        @Param("year") int year,
        @Param("month") int month
    );

    /**
     * 查询未完成报告
     */
    @Query("SELECT r FROM MoldPreventionReportEntity r WHERE r.status != 'COMPLETED' AND r.deleted = 0 ORDER BY r.startedAt ASC")
    List<MoldPreventionReportEntity> findUncompletedReports();

    /**
     * 根据 ID 查询 (包含软删除检查)
     */
    @Query("SELECT r FROM MoldPreventionReportEntity r WHERE r.id = :id AND r.deleted = 0")
    Optional<MoldPreventionReportEntity> findByIdWithDeletedCheck(@Param("id") Long id);
}
