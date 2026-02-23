package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.SLAConfig;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.SLAConfigPO;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * SLA 配置领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class SLAConfigMapper {

    public SLAConfigPO toPO(SLAConfig domain) {
        return SLAConfigPO.builder()
                .id(domain.getId())
                .processDefinitionKey(domain.getProcessDefinitionKey().getValue())
                .taskDefinitionKey(domain.getTaskDefinitionKey() != null ?
                        domain.getTaskDefinitionKey().getValue() : null)
                .tenantId(domain.getTenantId().getValue())
                .responseTimeLimit(domain.getResponseTimeLimit() != null ?
                        domain.getResponseTimeLimit().getSeconds() : null)
                .resolutionTimeLimit(domain.getResolutionTimeLimit() != null ?
                        domain.getResolutionTimeLimit().getSeconds() : null)
                .priority(domain.getPriority())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deleted(false)
                .build();
    }

    public SLAConfig toDomain(SLAConfigPO po) {
        SLAConfig config = new SLAConfig();
        config.id = po.getId();
        config.processDefinitionKey = ProcessDefinitionKey.of(po.getProcessDefinitionKey());
        config.taskDefinitionKey = po.getTaskDefinitionKey() != null ?
                ActivityId.of(po.getTaskDefinitionKey()) : null;
        config.tenantId = TenantId.of(po.getTenantId());
        config.responseTimeLimit = po.getResponseTimeLimit() != null ?
                Duration.ofSeconds(po.getResponseTimeLimit()) : null;
        config.resolutionTimeLimit = po.getResolutionTimeLimit() != null ?
                Duration.ofSeconds(po.getResolutionTimeLimit()) : null;
        config.priority = po.getPriority();
        config.createdAt = po.getCreatedAt();
        config.updatedAt = po.getUpdatedAt();
        return config;
    }
}
