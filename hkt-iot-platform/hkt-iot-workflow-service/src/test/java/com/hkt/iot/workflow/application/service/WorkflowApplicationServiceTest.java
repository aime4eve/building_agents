package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.command.StartProcessCommand;
import com.hkt.iot.workflow.application.dto.ProcessInstanceDTO;
import com.hkt.iot.workflow.domain.model.aggregate.ProcessInstance;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.ProcessInstanceRepository;
import com.hkt.iot.workflow.infrastructure.camunda.CamundaProcessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.camunda.bpm.engine.runtime.ProcessInstance as CamundaProcessInstance;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 流程应用服务测试
 *
 * @author HKT IoT Team
 */
@DisplayName("流程应用服务测试")
@ExtendWith(MockitoExtension.class)
class WorkflowApplicationServiceTest {

    @Mock
    private ProcessInstanceRepository processInstanceRepository;

    @Mock
    private CamundaProcessService camundaProcessService;

    private WorkflowApplicationService workflowApplicationService;

    @BeforeEach
    void setUp() {
        workflowApplicationService = new WorkflowApplicationService(
                processInstanceRepository,
                camundaProcessService
        );
    }

    @Test
    @DisplayName("测试启动流程实例")
    void testStartProcess() {
        // Given
        StartProcessCommand command = new StartProcessCommand(
                "tenant-001",
                "property-repair-workorder",
                "WO-2026-000001",
                "user-001",
                Map.of("workOrderType", "REPAIR")
        );

        CamundaProcessInstance camundaInstance = mock(CamundaProcessInstance.class);
        when(camundaInstance.getId()).thenReturn("pi-001");
        when(camundaInstance.getActivityId()).thenReturn("auto-assign");

        when(processInstanceRepository.existsByBusinessKey(any(BusinessKey.class))).thenReturn(false);
        when(processInstanceRepository.save(any(ProcessInstance.class))).thenAnswer(invocation -> {
            ProcessInstance instance = invocation.getArgument(0);
            // 设置 ID 以便后续使用
            return instance;
        });
        when(camundaProcessService.startProcess(anyString(), anyString(), anyMap())).thenReturn(camundaInstance);

        // When
        ProcessInstanceDTO result = workflowApplicationService.startProcess(command);

        // Then
        assertNotNull(result);
        assertEquals("WO-2026-000001", result.getBusinessKey());
        assertEquals("property-repair-workorder", result.getProcessDefinitionKey());
        assertEquals("tenant-001", result.getTenantId());

        verify(processInstanceRepository).save(any(ProcessInstance.class));
        verify(camundaProcessService).startProcess(eq("property-repair-workorder"), eq("WO-2026-000001"), anyMap());
    }

    @Test
    @DisplayName("测试启动已存在的流程（应抛出异常）")
    void testStartProcessAlreadyExists() {
        // Given
        StartProcessCommand command = new StartProcessCommand(
                "tenant-001",
                "property-repair-workorder",
                "WO-2026-000001",
                "user-001",
                Map.of()
        );

        when(processInstanceRepository.existsByBusinessKey(any(BusinessKey.class))).thenReturn(true);

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            workflowApplicationService.startProcess(command);
        });
    }

    @Test
    @DisplayName("测试挂起流程")
    void testSuspendProcess() {
        // Given
        ProcessInstance instance = ProcessInstance.start(
                ProcessDefinitionKey.of("test-process"),
                BusinessKey.of("TEST-001"),
                TenantId.of("tenant-001"),
                UserId.of("user-001"),
                Map.of()
        );
        instance.updateCurrentActivity(ActivityId.of("task-001"));

        when(processInstanceRepository.findById(any(ProcessInstanceId.class))).thenReturn(Optional.of(instance));
        when(processInstanceRepository.save(any(ProcessInstance.class))).thenReturn(instance);

        // When
        workflowApplicationService.suspendProcess(new com.hkt.iot.workflow.application.command.SuspendProcessCommand(
                "pi-001", "tenant-001"
        ));

        // Then
        assertEquals(ProcessInstanceState.SUSPENDED, instance.getState());
        verify(processInstanceRepository).save(instance);
        verify(camundaProcessService).suspendProcess("pi-001");
    }
}
