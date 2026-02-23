package com.hkt.iot.workflow.infrastructure.persistence.mapper;

import com.hkt.iot.workflow.domain.model.aggregate.WorkOrder;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.infrastructure.persistence.po.WorkOrderPO;
import org.springframework.stereotype.Component;

/**
 * 工单领域对象与 PO 对象映射器
 *
 * @author HKT IoT Team
 */
@Component
public class WorkOrderMapper {

    public WorkOrderPO toPO(WorkOrder domain) {
        return WorkOrderPO.builder()
                .id(domain.getId())
                .workOrderNo(domain.getWorkOrderNo().getValue())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .type(domain.getType().name())
                .status(domain.getStatus().name())
                .priority(domain.getPriority().name())
                .processInstanceId(domain.getProcessInstanceId())
                .templateId(domain.getTemplateId())
                .spaceId(domain.getSpaceId())
                .reporterId(domain.getReporterId() != null ? domain.getReporterId().getValue() : null)
                .assigneeId(domain.getAssigneeId() != null ? domain.getAssigneeId().getValue() : null)
                .handlerId(domain.getHandlerId() != null ? domain.getHandlerId().getValue() : null)
                .dueTime(domain.getDueTime())
                .completedAt(domain.getCompletedAt())
                .tenantId(domain.getTenantId().getValue())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .version(domain.getVersion())
                .deleted(false)
                .build();
    }

    public WorkOrder toDomain(WorkOrderPO po) {
        WorkOrder workOrder = new WorkOrder();
        workOrder.id = WorkOrderId.of(po.getId());
        workOrder.workOrderNo = WorkOrderNo.of(po.getWorkOrderNo());
        workOrder.title = po.getTitle();
        workOrder.description = po.getDescription();
        workOrder.type = WorkOrderType.valueOf(po.getType());
        workOrder.status = WorkOrderStatus.valueOf(po.getStatus());
        workOrder.priority = WorkOrderPriority.valueOf(po.getPriority());
        workOrder.processInstanceId = po.getProcessInstanceId();
        workOrder.templateId = po.getTemplateId();
        workOrder.spaceId = po.getSpaceId();
        workOrder.reporterId = po.getReporterId() != null ? UserId.of(po.getReporterId()) : null;
        workOrder.assigneeId = po.getAssigneeId() != null ? UserId.of(po.getAssigneeId()) : null;
        workOrder.handlerId = po.getHandlerId() != null ? UserId.of(po.getHandlerId()) : null;
        workOrder.dueTime = po.getDueTime();
        workOrder.completedAt = po.getCompletedAt();
        workOrder.tenantId = TenantId.of(po.getTenantId());
        workOrder.createdAt = po.getCreatedAt();
        workOrder.updatedAt = po.getUpdatedAt();
        workOrder.version = po.getVersion();
        return workOrder;
    }
}
