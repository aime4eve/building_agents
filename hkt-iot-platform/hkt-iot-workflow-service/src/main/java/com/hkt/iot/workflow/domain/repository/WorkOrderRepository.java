package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.aggregate.WorkOrder;
import com.hkt.iot.workflow.domain.model.valueobject.*;

import java.util.List;
import java.util.Optional;

/**
 * 工单仓储接口
 *
 * @author HKT IoT Team
 */
public interface WorkOrderRepository {

    WorkOrder save(WorkOrder workOrder);

    Optional<WorkOrder> findById(WorkOrderId id);

    Optional<WorkOrder> findByWorkOrderNo(WorkOrderNo workOrderNo);

    List<WorkOrder> findByTenantId(TenantId tenantId);

    List<WorkOrder> findByStatus(WorkOrderStatus status);

    List<WorkOrder> findByAssigneeId(UserId assigneeId);

    List<WorkOrder> findByReporterId(UserId reporterId);

    List<WorkOrder> findOverdue();

    List<WorkOrder> findByTenantIdAndStatus(TenantId tenantId, WorkOrderStatus status);

    List<WorkOrder> findBySpaceId(String spaceId);

    void delete(WorkOrder workOrder);
}
