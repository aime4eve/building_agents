package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.dto.SLAConfigDTO;
import com.hkt.iot.workflow.application.dto.SLAReportDTO;
import com.hkt.iot.workflow.domain.model.entity.SLAConfig;
import com.hkt.iot.workflow.domain.model.entity.SLAMonitor;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.SLAConfigRepository;
import com.hkt.iot.workflow.domain.repository.SLAMonitorRepository;
import com.hkt.iot.workflow.infrastructure.persistence.po.SLAMonitorPO;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.SLAMonitorJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SLA 监控应用服务
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SLAMonitoringApplicationService {

    private final SLAConfigRepository slaConfigRepository;
    private final SLAMonitorRepository slaMonitorRepository;
    private final SLAMonitorJpaRepository slaMonitorJpaRepository;

    /**
     * 创建 SLA 配置
     */
    @Transactional
    public SLAConfigDTO createSLAConfig(
            String processDefinitionKey,
            String taskDefinitionKey,
            String tenantId,
            Long responseTimeLimit,
            Long resolutionTimeLimit,
            String priority) {
        SLAConfig config = SLAConfig.create(
                ProcessDefinitionKey.of(processDefinitionKey),
                taskDefinitionKey != null ? ActivityId.of(taskDefinitionKey) : null,
                TenantId.of(tenantId),
                Duration.ofSeconds(responseTimeLimit),
                Duration.ofSeconds(resolutionTimeLimit),
                priority
        );
        SLAConfig saved = slaConfigRepository.save(config);
        return toConfigDTO(saved);
    }

    /**
     * 启动 SLA 监控
     */
    @Transactional
    public void startSLAMonitor(
            String processInstanceId,
            String taskId,
            String slaConfigId,
            String tenantId,
            Long responseTimeLimitSeconds) {
        LocalDateTime deadline = LocalDateTime.now().plusSeconds(responseTimeLimitSeconds);
        SLAMonitor monitor = SLAMonitor.create(
                ProcessInstanceId.of(processInstanceId),
                taskId != null ? TaskId.of(taskId) : null,
                slaConfigId,
                deadline,
                TenantId.of(tenantId)
        );
        slaMonitorRepository.save(monitor);
        log.info("Started SLA monitor: monitorId={}, processInstanceId={}, deadline={}",
                monitor.getId().getValue(), processInstanceId, deadline);
    }

    /**
     * 标记 SLA 响应完成
     */
    @Transactional
    public void markResponseCompliant(String monitorId) {
        SLAMonitor monitor = slaMonitorRepository.findById(SLAMonitorId.of(monitorId))
                .orElseThrow(() -> new IllegalArgumentException("SLA 监控记录不存在：" + monitorId));
        monitor.markResponseCompliant();
        slaMonitorRepository.save(monitor);
        log.info("Marked SLA response compliant: monitorId={}", monitorId);
    }

    /**
     * 标记 SLA 响应超时
     */
    @Transactional
    public void markResponseBreached(String monitorId) {
        SLAMonitor monitor = slaMonitorRepository.findById(SLAMonitorId.of(monitorId))
                .orElseThrow(() -> new IllegalArgumentException("SLA 监控记录不存在：" + monitorId));
        monitor.markResponseBreached();
        slaMonitorRepository.save(monitor);
        log.warn("Marked SLA response breached: monitorId={}", monitorId);
    }

    /**
     * 查询 SLA 报告
     */
    public SLAReportDTO getSLAReport(String tenantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<SLAMonitorPO> monitors = slaMonitorJpaRepository.findByTenantIdAndCreatedAtBetween(
                tenantId, startTime, endTime);

        int total = monitors.size();
        long compliant = monitors.stream()
                .filter(m -> "COMPLIANT".equals(m.getResponseStatus()))
                .count();
        long breached = monitors.stream()
                .filter(m -> "BREACHED".equals(m.getResponseStatus()))
                .count();
        long warning = monitors.stream()
                .filter(m -> "WARNING".equals(m.getResponseStatus()))
                .count();

        double responseRate = total > 0 ? (double) compliant / total : 0;

        return SLAReportDTO.builder()
                .tenantId(tenantId)
                .startTime(startTime)
                .endTime(endTime)
                .totalCount(total)
                .compliantCount((int) compliant)
                .breachedCount((int) breached)
                .warningCount((int) warning)
                .responseSLARate(responseRate)
                .resolutionSLARate(responseRate)
                .build();
    }

    /**
     * 查询 SLA 配置列表
     */
    public List<SLAConfigDTO> listSLAConfigs(String tenantId) {
        List<SLAConfig> configs = slaConfigRepository.findByTenantId(TenantId.of(tenantId));
        return configs.stream().map(this::toConfigDTO).collect(Collectors.toList());
    }

    private SLAConfigDTO toConfigDTO(SLAConfig config) {
        return SLAConfigDTO.builder()
                .id(config.getId())
                .processDefinitionKey(config.getProcessDefinitionKey().getValue())
                .taskDefinitionKey(config.getTaskDefinitionKey() != null ?
                        config.getTaskDefinitionKey().getValue() : null)
                .tenantId(config.getTenantId().getValue())
                .responseTimeLimit(config.getResponseTimeLimit() != null ?
                        config.getResponseTimeLimit().getSeconds() : null)
                .resolutionTimeLimit(config.getResolutionTimeLimit() != null ?
                        config.getResolutionTimeLimit().getSeconds() : null)
                .priority(config.getPriority())
                .build();
    }
}
