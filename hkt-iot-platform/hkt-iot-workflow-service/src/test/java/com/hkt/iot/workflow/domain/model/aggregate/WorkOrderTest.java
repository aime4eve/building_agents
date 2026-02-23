package com.hkt.iot.workflow.domain.model.aggregate;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工单聚合根测试
 */
@DisplayName("工单聚合根测试")
class WorkOrderTest {

    @Test
    @DisplayName("测试创建工单")
    void testCreateWorkOrder() {
        WorkOrder workOrder = WorkOrder.create(
                "维修工单-空调故障",
                "空调不制冷，需要维修",
                WorkOrderType.REPAIR,
                WorkOrderPriority.HIGH,
                "template-001",
                "space-001",
                UserId.of("reporter-001"),
                TenantId.of("tenant-001")
        );

        assertNotNull(workOrder);
        assertNotNull(workOrder.getId());
        assertNotNull(workOrder.getWorkOrderNo());
        assertEquals("维修工单-空调故障", workOrder.getTitle());
        assertEquals(WorkOrderType.REPAIR, workOrder.getType());
        assertEquals(WorkOrderStatus.CREATED, workOrder.getStatus());
        assertEquals(WorkOrderPriority.HIGH, workOrder.getPriority());
        assertFalse(workOrder.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("测试分配工单")
    void testAssignWorkOrder() {
        WorkOrder workOrder = createTestWorkOrder();

        workOrder.assign(UserId.of("handler-001"));

        assertEquals(WorkOrderStatus.ASSIGNED, workOrder.getStatus());
        assertEquals("handler-001", workOrder.getAssigneeId().getValue());
    }

    @Test
    @DisplayName("测试开始处理工单")
    void testStartProcessingWorkOrder() {
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.assign(UserId.of("handler-001"));

        workOrder.startProcessing();

        assertEquals(WorkOrderStatus.PROCESSING, workOrder.getStatus());
    }

    @Test
    @DisplayName("测试完成工单")
    void testCompleteWorkOrder() {
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.assign(UserId.of("handler-001"));
        workOrder.startProcessing();

        workOrder.complete("维修完成，空调已正常工作");

        assertEquals(WorkOrderStatus.COMPLETED, workOrder.getStatus());
        assertNotNull(workOrder.getCompletedAt());
    }

    @Test
    @DisplayName("测试取消工单")
    void testCancelWorkOrder() {
        WorkOrder workOrder = createTestWorkOrder();

        workOrder.cancel("用户取消");

        assertEquals(WorkOrderStatus.CANCELLED, workOrder.getStatus());
    }

    @Test
    @DisplayName("测试驳回工单")
    void testRejectWorkOrder() {
        WorkOrder workOrder = createTestWorkOrder();
        workOrder.assign(UserId.of("handler-001"));
        workOrder.startProcessing();

        workOrder.reject("维修方案不符合要求");

        assertEquals(WorkOrderStatus.REJECTED, workOrder.getStatus());
    }

    @Test
    @DisplayName("测试工单状态转换规则")
    void testWorkOrderStatusTransition() {
        assertTrue(WorkOrderStatus.CREATED.canTransitionTo(WorkOrderStatus.PENDING_ASSIGN));
        assertTrue(WorkOrderStatus.CREATED.canTransitionTo(WorkOrderStatus.CANCELLED));
        assertFalse(WorkOrderStatus.CREATED.canTransitionTo(WorkOrderStatus.COMPLETED));

        assertTrue(WorkOrderStatus.ASSIGNED.canTransitionTo(WorkOrderStatus.PROCESSING));
        assertTrue(WorkOrderStatus.PROCESSING.canTransitionTo(WorkOrderStatus.PENDING_CONFIRM));
        assertTrue(WorkOrderStatus.PENDING_CONFIRM.canTransitionTo(WorkOrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("测试工单编号生成")
    void testWorkOrderNoGeneration() {
        WorkOrder workOrder1 = createTestWorkOrder();
        WorkOrder workOrder2 = createTestWorkOrder();

        assertNotEquals(workOrder1.getWorkOrderNo().getValue(), workOrder2.getWorkOrderNo().getValue());
        assertTrue(workOrder1.getWorkOrderNo().getValue().startsWith("WO-"));
    }

    private WorkOrder createTestWorkOrder() {
        return WorkOrder.create(
                "测试工单",
                "测试描述",
                WorkOrderType.REPAIR,
                WorkOrderPriority.NORMAL,
                "template-001",
                "space-001",
                UserId.of("reporter-001"),
                TenantId.of("tenant-001")
        );
    }
}
