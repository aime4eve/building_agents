package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.command.CreateWorkflowDefinitionCommand;
import com.hkt.iot.workflow.application.dto.WorkflowDefinitionDTO;
import com.hkt.iot.workflow.domain.model.aggregate.WorkflowDefinition;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.WorkflowDefinitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 流程定义应用服务测试
 */
@DisplayName("流程定义应用服务测试")
@ExtendWith(MockitoExtension.class)
class WorkflowDefinitionApplicationServiceTest {

    @Mock
    private WorkflowDefinitionRepository workflowDefinitionRepository;

    private WorkflowDefinitionApplicationService service;

    @BeforeEach
    void setUp() {
        service = new WorkflowDefinitionApplicationService(workflowDefinitionRepository);
    }

    @Test
    @DisplayName("测试创建流程定义")
    void testCreateDefinition() {
        CreateWorkflowDefinitionCommand command = new CreateWorkflowDefinitionCommand(
                "tenant-001",
                "property-repair-workorder",
                "物业维修工单流程",
                "1.0.0",
                "用于物业维修工单的审批流程",
                "user-001"
        );

        when(workflowDefinitionRepository.existsByKey(any(WorkflowDefinitionKey.class))).thenReturn(false);
        when(workflowDefinitionRepository.save(any(WorkflowDefinition.class))).thenAnswer(invocation -> {
            WorkflowDefinition def = invocation.getArgument(0);
            return def;
        });

        WorkflowDefinitionDTO result = service.createDefinition(command);

        assertNotNull(result);
        assertEquals("property-repair-workorder", result.getDefinitionKey());
        assertEquals("物业维修工单流程", result.getName());
        assertEquals("DRAFT", result.getStatus());

        verify(workflowDefinitionRepository).save(any(WorkflowDefinition.class));
    }

    @Test
    @DisplayName("测试创建已存在的流程定义（应抛出异常）")
    void testCreateDuplicateDefinition() {
        CreateWorkflowDefinitionCommand command = new CreateWorkflowDefinitionCommand(
                "tenant-001",
                "property-repair-workorder",
                "物业维修工单流程",
                "1.0.0",
                "描述",
                "user-001"
        );

        when(workflowDefinitionRepository.existsByKey(any(WorkflowDefinitionKey.class))).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> {
            service.createDefinition(command);
        });
    }

    @Test
    @DisplayName("测试发布流程定义")
    void testPublishDefinition() {
        WorkflowDefinition definition = WorkflowDefinition.create(
                WorkflowDefinitionKey.of("test-process"),
                "测试流程",
                "1.0.0",
                "描述",
                TenantId.of("tenant-001"),
                UserId.of("user-001")
        );

        when(workflowDefinitionRepository.findById(any(WorkflowDefinitionId.class)))
                .thenReturn(Optional.of(definition));
        when(workflowDefinitionRepository.save(any(WorkflowDefinition.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var command = new com.hkt.iot.workflow.application.command.PublishWorkflowDefinitionCommand(
                "def-001", "tenant-001", "user-002"
        );

        WorkflowDefinitionDTO result = service.publishDefinition(command);

        assertEquals("PUBLISHED", result.getStatus());
        assertNotNull(result.getPublishedAt());
    }
}
