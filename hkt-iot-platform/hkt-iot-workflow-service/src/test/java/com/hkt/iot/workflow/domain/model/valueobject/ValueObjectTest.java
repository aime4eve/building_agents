package com.hkt.iot.workflow.domain.model.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 值对象测试
 *
 * @author HKT IoT Team
 */
@DisplayName("值对象测试")
class ValueObjectTest {

    @Test
    @DisplayName("测试 ProcessInstanceId 生成")
    void testProcessInstanceId() {
        // When
        ProcessInstanceId id = ProcessInstanceId.generate();

        // Then
        assertNotNull(id);
        assertNotNull(id.getValue());
        assertFalse(id.getValue().isEmpty());
    }

    @Test
    @DisplayName("测试 ProcessInstanceId 相等性")
    void testProcessInstanceIdEquality() {
        // Given
        ProcessInstanceId id1 = ProcessInstanceId.of("test-id");
        ProcessInstanceId id2 = ProcessInstanceId.of("test-id");
        ProcessInstanceId id3 = ProcessInstanceId.of("different-id");

        // Then
        assertEquals(id1, id2);
        assertNotEquals(id1, id3);
    }

    @Test
    @DisplayName("测试 BusinessKey 有效性")
    void testBusinessKey() {
        // Given & When
        BusinessKey key = BusinessKey.of("WO-2026-000001");

        // Then
        assertEquals("WO-2026-000001", key.getValue());
    }

    @Test
    @DisplayName("测试 BusinessKey 空值检查")
    void testBusinessKeyNull() {
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            BusinessKey.of(null);
        });
    }

    @Test
    @DisplayName("测试 TenantId 相等性")
    void testTenantIdEquality() {
        // Given
        TenantId tenant1 = TenantId.of("tenant-001");
        TenantId tenant2 = TenantId.of("tenant-001");
        TenantId tenant3 = TenantId.of("tenant-002");

        // Then
        assertEquals(tenant1, tenant2);
        assertNotEquals(tenant1, tenant3);
    }

    @Test
    @DisplayName("测试 ProcessInstanceState 枚举")
    void testProcessInstanceState() {
        // Then
        assertEquals(ProcessInstanceState.STARTED, ProcessInstanceState.STARTED);
        assertEquals(ProcessInstanceState.RUNNING, ProcessInstanceState.RUNNING);
        assertEquals(ProcessInstanceState.COMPLETED, ProcessInstanceState.COMPLETED);
        assertEquals(ProcessInstanceState.SUSPENDED, ProcessInstanceState.SUSPENDED);
        assertEquals(ProcessInstanceState.CANCELLED, ProcessInstanceState.CANCELLED);
        assertEquals(ProcessInstanceState.FAILED, ProcessInstanceState.FAILED);
    }
}
