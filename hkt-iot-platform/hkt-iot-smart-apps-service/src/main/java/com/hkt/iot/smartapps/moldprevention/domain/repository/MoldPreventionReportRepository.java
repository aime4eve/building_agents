package com.hkt.iot.smartapps.moldprevention.domain.repository;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.smartapps.moldprevention.domain.model.MoldPreventionReport;
import com.hkt.iot.smartapps.moldprevention.domain.model.ReportId;
import com.hkt.iot.smartapps.moldprevention.domain.model.ReportPeriod;
import com.hkt.iot.smartapps.moldprevention.domain.model.ZoneId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 防霉效果报告仓储接口
 */
public interface MoldPreventionReportRepository {

    /**
     * 保存报告
     */
    void save(MoldPreventionReport report);

    /**
     * 根据 ID 查询
     */
    Optional<MoldPreventionReport> findById(ReportId id);

    /**
     * 根据区域 ID 查询报告列表
     */
    List<MoldPreventionReport> findByZoneId(ZoneId zoneId);

    /**
     * 根据租户 ID 查询报告列表
     */
    List<MoldPreventionReport> findByTenantId(TenantId tenantId);

    /**
     * 根据区域 ID 和时间范围查询
     */
    List<MoldPreventionReport> findByZoneIdAndPeriod(ZoneId zoneId, LocalDateTime from, LocalDateTime to);

    /**
     * 根据报告周期查询
     */
    List<MoldPreventionReport> findByPeriod(ReportPeriod period, int year, int month);

    /**
     * 查询未完成报告
     */
    List<MoldPreventionReport> findUncompletedReports();

    /**
     * 删除报告
     */
    void delete(ReportId id);
}
