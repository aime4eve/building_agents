package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.entity.AutoAssignRule;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;
import com.hkt.iot.workflow.domain.model.valueobject.WorkOrderType;
import com.hkt.iot.workflow.infrastructure.persistence.po.AutoAssignRulePO;
import org.springframework.stereotype.Component;

/**
 * 自动派单规则领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class AutoAssignRuleMapper {

    public AutoAssignRulePO toPO(AutoAssignRule domain) {
        return AutoAssignRulePO.builder()
                .id(domain.getId())
                .name(domain.getName())
                .workOrderType(domain.getWorkOrderType().name())
                .ruleType(domain.getRuleType().name())
                .ruleConfig(domain.getRuleConfig())
                .priority(domain.getPriority())
                .enabled(domain.getEnabled())
                .tenantId(domain.getTenantId().getValue())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .deleted(false)
                .build();
    }

    public AutoAssignRule toDomain(AutoAssignRulePO po) {
        AutoAssignRule rule = new AutoAssignRule();
        rule.id = po.getId();
        rule.name = po.getName();
        rule.workOrderType = WorkOrderType.valueOf(po.getWorkOrderType());
        rule.ruleType = AutoAssignRule.RuleType.valueOf(po.getRuleType());
        rule.ruleConfig = po.getRuleConfig();
        rule.priority = po.getPriority();
        rule.enabled = po.getEnabled();
        rule.tenantId = TenantId.of(po.getTenantId());
        rule.createdAt = po.getCreatedAt();
        rule.updatedAt = po.getUpdatedAt();
        return rule;
    }
}
