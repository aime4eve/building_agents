package com.hkt.iot.workflow.domain.model.aggregate;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程实例聚合根测试
 *
 * @author HKT IoT Team
 */
@DisplayName("流程实例聚合根测试")
class ProcessInstanceTest {

    @Test
    @DisplayName("测试启动流程实例")
    void testStartProcessInstance() {
        // Given
        ProcessDefinitionKey processDefinitionKey = ProcessDefinitionKey.of("property-repair-workorder");
        BusinessKey businessKey = BusinessKey.of("WO-2026-000001");
        TenantId tenantId = TenantId.of("tenant-001");
        UserId startedBy = UserId.of("user-001");
        Map<String, Object> variables = Map.of(
                "workOrderType", "REPAIR",
                "priority", "HIGH"
        );

        // When
        ProcessInstance instance = ProcessInstance.start(
                processDefinitionKey,
                businessKey,
                tenantId,
                startedBy,
                variables
        );

        // Then
        assertNotNull(instance);
        assertNotNull(instance.getId());
        assertEquals(ProcessInstanceState.STARTED, instance.getState());
        assertEquals("property-repair-workorder", instance.getProcessDefinitionKey().getValue());
        assertEquals("WO-2026-000001", instance.getBusinessKey().getValue());
        assertEquals("tenant-001", instance.getTenantId().getValue());
        assertFalse(instance.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("测试完成流程")
    void testCompleteProcess() {
        // Given
        ProcessInstance instance = createTestInstance();

        // When
        instance.complete();

        // Then
        assertEquals(ProcessInstanceState.COMPLETED, instance.getState());
        assertNotNull(instance.getEndedAt());
        assertEquals(2, instance.getDomainEvents().size());
    }

    @Test
    @DisplayName("测试挂起流程")
    void testSuspendProcess() {
        // Given
        ProcessInstance instance = createTestInstance();
        instance.updateCurrentActivity(ActivityId.of("task-001"));

        // When
        instance.suspend();

        // Then
        assertEquals(ProcessInstanceState.SUSPENDED, instance.getState());
    }

    @Test
    @DisplayName("测试恢复流程")
    void testResumeProcess() {
        // Given
        ProcessInstance instance = createTestInstance();
        instance.updateCurrentActivity(ActivityId.of("task-001"));
        instance.suspend();

        // When
        instance.resume();

        // Then
        assertEquals(ProcessInstanceState.RUNNING, instance.getState());
    }

    @Test
    @DisplayName("测试取消流程")
    void testCancelProcess() {
        // Given
        ProcessInstance instance = createTestInstance();

        // When
        instance.cancel("用户主动取消");

        // Then
        assertEquals(ProcessInstanceState.CANCELLED, instance.getState());
        assertNotNull(instance.getEndedAt());
    }

    @Test
    @DisplayName("测试更新当前活动")
    void testUpdateCurrentActivity() {
        // Given
        ProcessInstance instance = createTestInstance();

        // When
        instance.updateCurrentActivity(ActivityId.of("repair-processing"));

        // Then
        assertEquals("repair-processing", instance.getCurrentActivityId().getValue());
        assertEquals(ProcessInstanceState.RUNNING, instance.getState());
    }

    @Test
    @DisplayName("测试完成已完成的流程（幂等性）")
    void testCompleteAlreadyCompletedProcess() {
        // Given
        ProcessInstance instance = createTestInstance();
        instance.complete();

        // When
        instance.complete();

        // Then
        assertEquals(ProcessInstanceState.COMPLETED, instance.getState());
    }

    @Test
    @DisplayName("测试挂起已完成的流程（应抛出异常）")
    void testSuspendCompletedProcess() {
        // Given
        ProcessInstance instance = createTestInstance();
        instance.complete();

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            instance.suspend();
        });
    }

    /**
     * 创建测试用的流程实例
     */
    private ProcessInstance createTestInstance() {
        return ProcessInstance.start(
                ProcessDefinitionKey.of("test-process"),
                BusinessKey.of("TEST-001"),
                TenantId.of("tenant-001"),
                UserId.of("user-001"),
                Map.of()
        );
    }
}
