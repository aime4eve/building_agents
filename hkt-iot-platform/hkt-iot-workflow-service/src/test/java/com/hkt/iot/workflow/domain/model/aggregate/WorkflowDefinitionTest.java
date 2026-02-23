package com.hkt.iot.workflow.domain.model.aggregate;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程定义聚合根测试
 */
@DisplayName("流程定义聚合根测试")
class WorkflowDefinitionTest {

    @Test
    @DisplayName("测试创建流程定义")
    void testCreateWorkflowDefinition() {
        WorkflowDefinitionKey key = WorkflowDefinitionKey.of("property-repair-workorder");
        String name = "物业维修工单流程";
        String version = "1.0.0";
        String description = "用于物业维修工单的审批流程";
        TenantId tenantId = TenantId.of("tenant-001");
        UserId createdBy = UserId.of("user-001");

        WorkflowDefinition definition = WorkflowDefinition.create(
                key, name, version, description, tenantId, createdBy
        );

        assertNotNull(definition);
        assertNotNull(definition.getId());
        assertEquals("property-repair-workorder", definition.getKey().getValue());
        assertEquals("物业维修工单流程", definition.getName());
        assertEquals("1.0.0", definition.getVersion());
        assertEquals(WorkflowDefinitionStatus.DRAFT, definition.getStatus());
        assertEquals("tenant-001", definition.getTenantId().getValue());
        assertNotNull(definition.getCreatedAt());
        assertFalse(definition.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("测试发布流程定义")
    void testPublishWorkflowDefinition() {
        WorkflowDefinition definition = createTestDefinition();

        definition.publish(UserId.of("user-002"));

        assertEquals(WorkflowDefinitionStatus.PUBLISHED, definition.getStatus());
        assertNotNull(definition.getPublishedAt());
    }

    @Test
    @DisplayName("测试发布非草稿状态的流程定义（应抛出异常）")
    void testPublishNonDraftDefinition() {
        WorkflowDefinition definition = createTestDefinition();
        definition.publish(UserId.of("user-002"));

        assertThrows(IllegalStateException.class, () -> {
            definition.publish(UserId.of("user-003"));
        });
    }

    @Test
    @DisplayName("测试归档流程定义")
    void testArchiveWorkflowDefinition() {
        WorkflowDefinition definition = createTestDefinition();
        definition.publish(UserId.of("user-002"));

        definition.archive(UserId.of("user-003"));

        assertEquals(WorkflowDefinitionStatus.ARCHIVED, definition.getStatus());
    }

    @Test
    @DisplayName("测试归档非发布状态的流程定义（应抛出异常）")
    void testArchiveNonPublishedDefinition() {
        WorkflowDefinition definition = createTestDefinition();

        assertThrows(IllegalStateException.class, () -> {
            definition.archive(UserId.of("user-002"));
        });
    }

    @Test
    @DisplayName("测试更新版本")
    void testUpdateVersion() {
        WorkflowDefinition definition = createTestDefinition();

        definition.updateVersion("1.0.1");

        assertEquals("1.0.1", definition.getVersion());
        assertNotNull(definition.getUpdatedAt());
    }

    private WorkflowDefinition createTestDefinition() {
        return WorkflowDefinition.create(
                WorkflowDefinitionKey.of("test-process"),
                "测试流程",
                "1.0.0",
                "测试描述",
                TenantId.of("tenant-001"),
                UserId.of("user-001")
        );
    }
}
