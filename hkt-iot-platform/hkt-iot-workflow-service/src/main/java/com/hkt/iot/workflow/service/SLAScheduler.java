package com.hkt.iot.workflow.service;

import com.hkt.iot.workflow.domain.model.valueobject.SLAStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * SLA 定时任务调度器
 * 负责定时执行 SLA 预警检查、违规检查和统计生成
 *
 * @author HKT IoT Team
 */
@Component
@Slf4j
public class SLAScheduler {

    private final SLAMonitoringService slaMonitoringService;

    public SLAScheduler(SLAMonitoringService slaMonitoringService) {
        this.slaMonitoringService = slaMonitoringService;
    }

    /**
     * 检查 SLA 预警
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void checkSLAWarning() {
        log.debug("Starting SLA warning check task");
        try {
            int warningCount = slaMonitoringService.checkAndSendWarnings();
            log.debug("SLA warning check completed: {} warnings sent", warningCount);
        } catch (Exception e) {
            log.error("Error during SLA warning check: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查 SLA 违规
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000)
    public void checkSLABreach() {
        log.debug("Starting SLA breach check task");
        try {
            int breachCount = slaMonitoringService.checkAndRecordBreaches();
            log.debug("SLA breach check completed: {} breaches recorded", breachCount);
        } catch (Exception e) {
            log.error("Error during SLA breach check: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成 SLA 统计
     * 每小时执行一次
     */
    @Scheduled(fixedRate = 3600000)
    public void generateSLAStatistics() {
        log.info("Starting SLA statistics generation task");
        try {
            SLAStatistics statistics = slaMonitoringService.calculateStatistics((String) null);
            log.info("SLA Statistics - Total: {}, Completed: {}, Breached: {}, Compliance Rate: {:.2f}%",
                    statistics.getTotalTasks(),
                    statistics.getCompletedTasks(),
                    statistics.getBreachedTasks(),
                    statistics.getSlaComplianceRate());

            if (statistics.needsAttention()) {
                log.warn("SLA compliance rate is below 80%, attention required!");
            }
        } catch (Exception e) {
            log.error("Error during SLA statistics generation: {}", e.getMessage(), e);
        }
    }
}
