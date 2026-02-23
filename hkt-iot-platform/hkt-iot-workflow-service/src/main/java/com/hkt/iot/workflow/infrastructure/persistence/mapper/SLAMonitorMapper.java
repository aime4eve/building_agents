package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.SLAMonitor;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.SLAMonitorPO;
import org.springframework.stereotype.Component;

/**
 * SLA 监控记录领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class SLAMonitorMapper {

    public SLAMonitorPO toPO(SLAMonitor domain) {
        return SLAMonitorPO.builder()
                .id(domain.getId().getValue())
                .processInstanceId(domain.getProcessInstanceId().getValue())
                .taskId(domain.getTaskId() != null ? domain.getTaskId().getValue() : null)
                .slaConfigId(domain.getSlaConfigId())
                .slaDeadline(domain.getSlaDeadline())
                .responseStatus(domain.getResponseStatus().name())
                .resolutionStatus(domain.getResolutionStatus() != null ?
                        domain.getResolutionStatus().name() : null)
                .actualResponseTime(domain.getActualResponseTime())
                .actualResolutionTime(domain.getActualResolutionTime())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .tenantId(domain.getTenantId().getValue())
                .deleted(false)
                .build();
    }

    public SLAMonitor toDomain(SLAMonitorPO po) {
        SLAMonitor monitor = new SLAMonitor();
        monitor.id = SLAMonitorId.of(po.getId());
        monitor.processInstanceId = ProcessInstanceId.of(po.getProcessInstanceId());
        monitor.taskId = po.getTaskId() != null ? TaskId.of(po.getTaskId()) : null;
        monitor.slaConfigId = po.getSlaConfigId();
        monitor.slaDeadline = po.getSlaDeadline();
        monitor.responseStatus = SLAStatus.valueOf(po.getResponseStatus());
        monitor.resolutionStatus = po.getResolutionStatus() != null ?
                SLAStatus.valueOf(po.getResolutionStatus()) : null;
        monitor.actualResponseTime = po.getActualResponseTime();
        monitor.actualResolutionTime = po.getActualResolutionTime();
        monitor.createdAt = po.getCreatedAt();
        monitor.updatedAt = po.getUpdatedAt();
        monitor.tenantId = TenantId.of(po.getTenantId());
        return monitor;
    }
}
