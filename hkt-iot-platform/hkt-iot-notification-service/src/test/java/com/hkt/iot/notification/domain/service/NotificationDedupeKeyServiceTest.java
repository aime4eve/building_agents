package com.hkt.iot.notification.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationDedupeKeyService 测试")
class NotificationDedupeKeyServiceTest {

    private NotificationDedupeKeyService service;

    @BeforeEach
    void setUp() {
        service = new NotificationDedupeKeyService();
    }

    @Test
    @DisplayName("测试告警幂等键生成")
    void testGenerateAlarmDedupeKey() {
        String key = service.generateAlarmDedupeKey(
                "tenant1",
                "alarm123",
                "EMAIL",
                "user1",
                "template1"
        );

        assertEquals("tenant1:alarm123:EMAIL:user1:template1", key);
    }

    @Test
    @DisplayName("测试告警幂等键生成 - 空值处理")
    void testGenerateAlarmDedupeKey_NullValues() {
        String key = service.generateAlarmDedupeKey(
                null,
                "alarm123",
                null,
                "user1",
                "template1"
        );

        assertEquals(":alarm123::user1:template1", key);
    }

    @Test
    @DisplayName("测试业务幂等键生成")
    void testGenerateBusinessDedupeKey() {
        String key = service.generateBusinessDedupeKey(
                "tenant1",
                "ORDER",
                "order123",
                "EMAIL",
                "user1",
                "template1"
        );

        assertEquals("tenant1:ORDER:order123:EMAIL:user1:template1", key);
    }

    @Test
    @DisplayName("测试业务幂等键生成 - 空值处理")
    void testGenerateBusinessDedupeKey_NullValues() {
        String key = service.generateBusinessDedupeKey(
                "tenant1",
                null,
                "order123",
                "EMAIL",
                null,
                "template1"
        );

        assertEquals("tenant1::order123:EMAIL::template1", key);
    }

    @Test
    @DisplayName("测试带变量的幂等键生成")
    void testGenerateDedupeKeyWithVariables() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("name", "张三");
        variables.put("orderId", "12345");

        String key = service.generateDedupeKeyWithVariables(
                "tenant1",
                "EMAIL",
                "user1",
                "template1",
                variables
        );

        assertNotNull(key);
        assertTrue(key.startsWith("tenant1:EMAIL:user1:template1:"));
        assertTrue(key.length() > "tenant1:EMAIL:user1:template1:".length());
    }

    @Test
    @DisplayName("测试带变量的幂等键生成 - 空变量")
    void testGenerateDedupeKeyWithVariables_EmptyVariables() {
        String key = service.generateDedupeKeyWithVariables(
                "tenant1",
                "EMAIL",
                "user1",
                "template1",
                new HashMap<>()
        );

        assertEquals("tenant1:EMAIL:user1:template1", key);
    }

    @Test
    @DisplayName("测试带变量的幂等键生成 - null变量")
    void testGenerateDedupeKeyWithVariables_NullVariables() {
        String key = service.generateDedupeKeyWithVariables(
                "tenant1",
                "EMAIL",
                "user1",
                "template1",
                null
        );

        assertEquals("tenant1:EMAIL:user1:template1", key);
    }

    @Test
    @DisplayName("测试带变量的幂等键生成 - 相同变量生成相同键")
    void testGenerateDedupeKeyWithVariables_SameVariablesSameKey() {
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("name", "张三");
        variables1.put("orderId", "12345");

        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("name", "张三");
        variables2.put("orderId", "12345");

        String key1 = service.generateDedupeKeyWithVariables(
                "tenant1", "EMAIL", "user1", "template1", variables1
        );
        String key2 = service.generateDedupeKeyWithVariables(
                "tenant1", "EMAIL", "user1", "template1", variables2
        );

        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("测试带变量的幂等键生成 - 不同变量顺序生成相同键")
    void testGenerateDedupeKeyWithVariables_DifferentOrderSameKey() {
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("name", "张三");
        variables1.put("orderId", "12345");

        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("orderId", "12345");
        variables2.put("name", "张三");

        String key1 = service.generateDedupeKeyWithVariables(
                "tenant1", "EMAIL", "user1", "template1", variables1
        );
        String key2 = service.generateDedupeKeyWithVariables(
                "tenant1", "EMAIL", "user1", "template1", variables2
        );

        assertEquals(key1, key2);
    }

    @Test
    @DisplayName("测试定时通知幂等键生成")
    void testGenerateScheduledDedupeKey() {
        String key = service.generateScheduledDedupeKey(
                "tenant1",
                "schedule1",
                "2024-01-15",
                "EMAIL",
                "user1"
        );

        assertEquals("tenant1:schedule1:2024-01-15:EMAIL:user1", key);
    }

    @Test
    @DisplayName("测试解析幂等键")
    void testParseDedupeKey() {
        String dedupeKey = "tenant1:alarm123:EMAIL:user1:template1";

        NotificationDedupeKeyService.DedupeKeyComponents components = service.parseDedupeKey(dedupeKey);

        assertNotNull(components);
        assertEquals("tenant1", components.getTenantId());
        assertEquals("alarm123", components.getAlarmId());
        assertEquals("EMAIL", components.getChannel());
        assertEquals("user1", components.getReceiverId());
        assertEquals("template1", components.getTemplateCode());
    }

    @Test
    @DisplayName("测试解析幂等键 - 带变量哈希")
    void testParseDedupeKey_WithVariablesHash() {
        String dedupeKey = "tenant1:alarm123:EMAIL:user1:template1:abc12345";

        NotificationDedupeKeyService.DedupeKeyComponents components = service.parseDedupeKey(dedupeKey);

        assertNotNull(components);
        assertEquals("tenant1", components.getTenantId());
        assertEquals("alarm123", components.getAlarmId());
        assertEquals("EMAIL", components.getChannel());
        assertEquals("user1", components.getReceiverId());
        assertEquals("template1", components.getTemplateCode());
        assertEquals("abc12345", components.getVariablesHash());
    }

    @Test
    @DisplayName("测试解析幂等键 - null值")
    void testParseDedupeKey_Null() {
        NotificationDedupeKeyService.DedupeKeyComponents components = service.parseDedupeKey(null);

        assertNull(components);
    }

    @Test
    @DisplayName("测试解析幂等键 - 空字符串")
    void testParseDedupeKey_Empty() {
        NotificationDedupeKeyService.DedupeKeyComponents components = service.parseDedupeKey("");

        assertNull(components);
    }

    @Test
    @DisplayName("测试解析幂等键 - 部分字段")
    void testParseDedupeKey_PartialFields() {
        String dedupeKey = "tenant1:alarm123";

        NotificationDedupeKeyService.DedupeKeyComponents components = service.parseDedupeKey(dedupeKey);

        assertNotNull(components);
        assertEquals("tenant1", components.getTenantId());
        assertEquals("alarm123", components.getAlarmId());
        assertNull(components.getChannel());
        assertNull(components.getReceiverId());
        assertNull(components.getTemplateCode());
    }

    @Test
    @DisplayName("测试验证幂等键格式 - 有效键")
    void testIsValidDedupeKey_Valid() {
        assertTrue(service.isValidDedupeKey("tenant1:alarm123:EMAIL:user1:template1"));
        assertTrue(service.isValidDedupeKey("tenant1:alarm123:EMAIL:user1:template1:hash123"));
    }

    @Test
    @DisplayName("测试验证幂等键格式 - 无效键 - 字段不足")
    void testIsValidDedupeKey_Invalid_TooFewFields() {
        assertFalse(service.isValidDedupeKey("tenant1:alarm123:EMAIL"));
        assertFalse(service.isValidDedupeKey("tenant1:alarm123"));
        assertFalse(service.isValidDedupeKey("tenant1"));
    }

    @Test
    @DisplayName("测试验证幂等键格式 - 无效键 - null")
    void testIsValidDedupeKey_Invalid_Null() {
        assertFalse(service.isValidDedupeKey(null));
    }

    @Test
    @DisplayName("测试验证幂等键格式 - 无效键 - 空字符串")
    void testIsValidDedupeKey_Invalid_Empty() {
        assertFalse(service.isValidDedupeKey(""));
    }

    @Test
    @DisplayName("测试业务幂等键格式")
    void testGenerateBusinessDedupeKey_Format() {
        String key = service.generateBusinessDedupeKey(
                "tenant-001",
                "ORDER_CREATED",
                "ORD-2024-001",
                "SMS",
                "user-123",
                "order-notification"
        );

        String[] parts = key.split(":");
        assertEquals(6, parts.length);
        assertEquals("tenant-001", parts[0]);
        assertEquals("ORDER_CREATED", parts[1]);
        assertEquals("ORD-2024-001", parts[2]);
        assertEquals("SMS", parts[3]);
        assertEquals("user-123", parts[4]);
        assertEquals("order-notification", parts[5]);
    }

    @Test
    @DisplayName("测试告警幂等键格式")
    void testGenerateAlarmDedupeKey_Format() {
        String key = service.generateAlarmDedupeKey(
                "tenant-001",
                "ALM-2024-001",
                "PUSH",
                "device-123",
                "alarm-alert"
        );

        String[] parts = key.split(":");
        assertEquals(5, parts.length);
        assertEquals("tenant-001", parts[0]);
        assertEquals("ALM-2024-001", parts[1]);
        assertEquals("PUSH", parts[2]);
        assertEquals("device-123", parts[3]);
        assertEquals("alarm-alert", parts[4]);
    }
}
