package com.hkt.iot.notification.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NotificationRequest 测试")
class NotificationRequestTest {

    private NotificationRequest request;

    @BeforeEach
    void setUp() {
        request = NotificationRequest.builder()
                .id(1L)
                .dedupeKey("tenant1:alarm1:EMAIL:user1:template1")
                .tenantId("tenant1")
                .channelType(NotificationTemplate.ChannelType.EMAIL)
                .receiverType(NotificationRequest.ReceiverType.USER)
                .receiverId("user1")
                .receiverAddress("user1@example.com")
                .templateCode("template1")
                .title("测试标题")
                .content("测试内容")
                .priority(NotificationRequest.Priority.NORMAL)
                .status(NotificationRequest.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetry(3)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("测试开始发送状态转换")
    void testStartSending() {
        assertEquals(NotificationRequest.NotificationStatus.PENDING, request.getStatus());

        request.startSending();

        assertEquals(NotificationRequest.NotificationStatus.SENDING, request.getStatus());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    @DisplayName("测试成功状态转换")
    void testMarkAsSuccess() {
        request.startSending();
        assertEquals(NotificationRequest.NotificationStatus.SENDING, request.getStatus());

        request.markAsSuccess();

        assertEquals(NotificationRequest.NotificationStatus.SUCCESS, request.getStatus());
        assertNotNull(request.getSentAt());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    @DisplayName("测试失败状态转换 - 可以重试")
    void testMarkAsFailed_CanRetry() {
        request.startSending();
        assertEquals(NotificationRequest.NotificationStatus.SENDING, request.getStatus());

        request.markAsFailed("发送失败");

        assertEquals(NotificationRequest.NotificationStatus.PENDING, request.getStatus());
        assertEquals("发送失败", request.getErrorMessage());
        assertEquals(1, request.getRetryCount());
        assertNotNull(request.getNextRetryAt());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    @DisplayName("测试失败状态转换 - 达到最大重试次数")
    void testMarkAsFailed_MaxRetryReached() {
        request.setRetryCount(3);
        request.startSending();

        request.markAsFailed("发送失败");

        assertEquals(NotificationRequest.NotificationStatus.FAILED, request.getStatus());
        assertEquals("发送失败", request.getErrorMessage());
        assertNull(request.getNextRetryAt());
    }

    @Test
    @DisplayName("测试指数退避重试时间计算 - 第一次重试")
    void testCalculateNextRetryTime_FirstRetry() {
        request.setRetryCount(0);
        Instant beforeMark = Instant.now();

        request.markAsFailed("发送失败");

        assertNotNull(request.getNextRetryAt());
        long expectedDelaySeconds = (long) Math.pow(2, 0) * 60;
        Instant expectedRetryTime = beforeMark.plusSeconds(expectedDelaySeconds);

        long diffSeconds = Math.abs(
                ChronoUnit.SECONDS.between(request.getNextRetryAt(), expectedRetryTime)
        );
        assertTrue(diffSeconds < 2, "重试时间应该在预期时间的2秒误差范围内");
    }

    @Test
    @DisplayName("测试指数退避重试时间计算 - 第二次重试")
    void testCalculateNextRetryTime_SecondRetry() {
        request.setRetryCount(1);
        Instant beforeMark = Instant.now();

        request.markAsFailed("发送失败");

        assertNotNull(request.getNextRetryAt());
        long expectedDelaySeconds = (long) Math.pow(2, 1) * 60;
        Instant expectedRetryTime = beforeMark.plusSeconds(expectedDelaySeconds);

        long diffSeconds = Math.abs(
                ChronoUnit.SECONDS.between(request.getNextRetryAt(), expectedRetryTime)
        );
        assertTrue(diffSeconds < 2, "重试时间应该在预期时间的2秒误差范围内");
    }

    @Test
    @DisplayName("测试指数退避重试时间计算 - 第三次重试")
    void testCalculateNextRetryTime_ThirdRetry() {
        request.setRetryCount(2);
        Instant beforeMark = Instant.now();

        request.markAsFailed("发送失败");

        assertNotNull(request.getNextRetryAt());
        long expectedDelaySeconds = (long) Math.pow(2, 2) * 60;
        Instant expectedRetryTime = beforeMark.plusSeconds(expectedDelaySeconds);

        long diffSeconds = Math.abs(
                ChronoUnit.SECONDS.between(request.getNextRetryAt(), expectedRetryTime)
        );
        assertTrue(diffSeconds < 2, "重试时间应该在预期时间的2秒误差范围内");
    }

    @Test
    @DisplayName("测试取消功能")
    void testCancel() {
        assertEquals(NotificationRequest.NotificationStatus.PENDING, request.getStatus());

        request.cancel();

        assertEquals(NotificationRequest.NotificationStatus.CANCELLED, request.getStatus());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    @DisplayName("测试重试条件判断 - 可以重试")
    void testCanRetry_CanRetry() {
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);
        request.setRetryCount(1);
        request.setMaxRetry(3);
        request.setNextRetryAt(Instant.now().minusSeconds(10));

        assertTrue(request.canRetry());
    }

    @Test
    @DisplayName("测试重试条件判断 - 状态不是PENDING")
    void testCanRetry_WrongStatus() {
        request.setStatus(NotificationRequest.NotificationStatus.SENDING);
        request.setRetryCount(1);
        request.setMaxRetry(3);

        assertFalse(request.canRetry());
    }

    @Test
    @DisplayName("测试重试条件判断 - 达到最大重试次数")
    void testCanRetry_MaxRetryReached() {
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);
        request.setRetryCount(3);
        request.setMaxRetry(3);

        assertFalse(request.canRetry());
    }

    @Test
    @DisplayName("测试重试条件判断 - 重试时间未到")
    void testCanRetry_RetryTimeNotReached() {
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);
        request.setRetryCount(1);
        request.setMaxRetry(3);
        request.setNextRetryAt(Instant.now().plusSeconds(60));

        assertFalse(request.canRetry());
    }

    @Test
    @DisplayName("测试重试条件判断 - 重试时间为空")
    void testCanRetry_NullRetryTime() {
        request.setStatus(NotificationRequest.NotificationStatus.PENDING);
        request.setRetryCount(1);
        request.setMaxRetry(3);
        request.setNextRetryAt(null);

        assertTrue(request.canRetry());
    }

    @Test
    @DisplayName("测试死信状态转换")
    void testMarkAsDeadLetter() {
        request.setStatus(NotificationRequest.NotificationStatus.FAILED);

        request.markAsDeadLetter();

        assertEquals(NotificationRequest.NotificationStatus.DEAD_LETTER, request.getStatus());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    @DisplayName("测试重置以允许重试")
    void testResetForRetry() {
        request.setStatus(NotificationRequest.NotificationStatus.DEAD_LETTER);
        request.setRetryCount(3);
        request.setNextRetryAt(Instant.now().plusSeconds(60));
        request.setErrorMessage("之前的错误");

        request.resetForRetry();

        assertEquals(NotificationRequest.NotificationStatus.PENDING, request.getStatus());
        assertEquals(0, request.getRetryCount());
        assertNull(request.getNextRetryAt());
        assertNull(request.getErrorMessage());
        assertNotNull(request.getUpdatedAt());
    }

    @Test
    @DisplayName("测试获取最后一次错误信息")
    void testGetLastError() {
        assertNull(request.getLastError());

        request.setErrorMessage("测试错误信息");

        assertEquals("测试错误信息", request.getLastError());
    }

    @Test
    @DisplayName("测试枚举值 - ReceiverType")
    void testReceiverTypeEnum() {
        assertEquals("USER", NotificationRequest.ReceiverType.USER.getCode());
        assertEquals("用户", NotificationRequest.ReceiverType.USER.getDescription());
        assertEquals("ROLE", NotificationRequest.ReceiverType.ROLE.getCode());
        assertEquals("GROUP", NotificationRequest.ReceiverType.GROUP.getCode());
        assertEquals("DEVICE", NotificationRequest.ReceiverType.DEVICE.getCode());
    }

    @Test
    @DisplayName("测试枚举值 - Priority")
    void testPriorityEnum() {
        assertEquals(1, NotificationRequest.Priority.LOW.getLevel());
        assertEquals(2, NotificationRequest.Priority.NORMAL.getLevel());
        assertEquals(3, NotificationRequest.Priority.HIGH.getLevel());
        assertEquals(4, NotificationRequest.Priority.URGENT.getLevel());
    }

    @Test
    @DisplayName("测试枚举值 - NotificationStatus")
    void testNotificationStatusEnum() {
        assertEquals("PENDING", NotificationRequest.NotificationStatus.PENDING.getCode());
        assertEquals("待发送", NotificationRequest.NotificationStatus.PENDING.getDescription());
        assertEquals("SENDING", NotificationRequest.NotificationStatus.SENDING.getCode());
        assertEquals("SUCCESS", NotificationRequest.NotificationStatus.SUCCESS.getCode());
        assertEquals("FAILED", NotificationRequest.NotificationStatus.FAILED.getCode());
        assertEquals("CANCELLED", NotificationRequest.NotificationStatus.CANCELLED.getCode());
        assertEquals("DEAD_LETTER", NotificationRequest.NotificationStatus.DEAD_LETTER.getCode());
    }
}
