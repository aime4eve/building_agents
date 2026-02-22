package com.hkt.iot.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.common.web.Result;
import com.hkt.iot.notification.application.dto.NotificationSendDTO;
import com.hkt.iot.notification.domain.event.NotificationFailedEvent;
import com.hkt.iot.notification.domain.event.NotificationSentEvent;
import com.hkt.iot.notification.domain.model.NotificationLog;
import com.hkt.iot.notification.domain.model.NotificationRequest;
import com.hkt.iot.notification.domain.model.NotificationTemplate;
import com.hkt.iot.notification.domain.repository.NotificationLogRepository;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import com.hkt.iot.notification.domain.repository.NotificationTemplateRepository;
import com.hkt.iot.notification.infrastructure.channel.MessageChannel;
import com.hkt.iot.notification.infrastructure.channel.MessageChannelFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationApplicationService 测试")
class NotificationApplicationServiceTest {

    @Mock
    private NotificationRequestRepository requestRepository;

    @Mock
    private NotificationTemplateRepository templateRepository;

    @Mock
    private NotificationLogRepository logRepository;

    @Mock
    private MessageChannelFactory channelFactory;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private NotificationApplicationService service;

    private NotificationSendDTO sendDTO;
    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "maxRetry", 3);
        ReflectionTestUtils.setField(service, "defaultPriority", "NORMAL");

        sendDTO = NotificationSendDTO.builder()
                .tenantId("tenant1")
                .channelType("EMAIL")
                .receiverType("USER")
                .receiverId("user1")
                .receiverAddress("user1@example.com")
                .templateCode("template1")
                .priority("NORMAL")
                .variables(new HashMap<>(Map.of("name", "张三")))
                .build();

        template = NotificationTemplate.builder()
                .id(1L)
                .templateCode("template1")
                .templateName("测试模板")
                .templateType(NotificationTemplate.TemplateType.BUSINESS)
                .channelType(NotificationTemplate.ChannelType.EMAIL)
                .titleTemplate("通知: ${name}")
                .contentTemplate("您好 ${name}, 这是一条测试通知")
                .enabled(true)
                .tenantId("tenant1")
                .build();
    }

    @Test
    @DisplayName("测试成功发送通知")
    void testSendNotification_Success() {
        when(requestRepository.findByDedupeKey(anyString())).thenReturn(Optional.empty());
        when(templateRepository.findByTenantIdAndTemplateCode("tenant1", "template1"))
                .thenReturn(Optional.of(template));
        when(requestRepository.save(any(NotificationRequest.class))).thenAnswer(invocation -> {
            NotificationRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        Result<Long> result = service.sendNotification(sendDTO);

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(requestRepository).save(any(NotificationRequest.class));
        verify(rabbitTemplate).convertAndSend(eq("notification.exchange"), eq("notification.send"), anyLong());
    }

    @Test
    @DisplayName("测试幂等性检查 - 已发送成功")
    void testSendNotification_DedupeCheck_AlreadySent() {
        sendDTO.setDedupeKey("existing-dedupe-key");

        NotificationRequest existingRequest = NotificationRequest.builder()
                .id(100L)
                .dedupeKey("existing-dedupe-key")
                .status(NotificationRequest.NotificationStatus.SUCCESS)
                .build();

        when(requestRepository.findByDedupeKey("existing-dedupe-key"))
                .thenReturn(Optional.of(existingRequest));

        Result<Long> result = service.sendNotification(sendDTO);

        assertTrue(result.isSuccess());
        assertEquals(100L, result.getData());
        verify(requestRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("测试幂等性检查 - 已存在但未成功")
    void testSendNotification_DedupeCheck_NotSuccess() {
        sendDTO.setDedupeKey("existing-dedupe-key");

        NotificationRequest existingRequest = NotificationRequest.builder()
                .id(100L)
                .dedupeKey("existing-dedupe-key")
                .status(NotificationRequest.NotificationStatus.PENDING)
                .build();

        when(requestRepository.findByDedupeKey("existing-dedupe-key"))
                .thenReturn(Optional.of(existingRequest));
        when(templateRepository.findByTenantIdAndTemplateCode("tenant1", "template1"))
                .thenReturn(Optional.of(template));
        when(requestRepository.save(any(NotificationRequest.class))).thenAnswer(invocation -> {
            NotificationRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        Result<Long> result = service.sendNotification(sendDTO);

        assertTrue(result.isSuccess());
        verify(requestRepository).save(any(NotificationRequest.class));
    }

    @Test
    @DisplayName("测试模板不存在")
    void testSendNotification_TemplateNotFound() {
        when(requestRepository.findByDedupeKey(anyString())).thenReturn(Optional.empty());
        when(templateRepository.findByTenantIdAndTemplateCode("tenant1", "template1"))
                .thenReturn(Optional.empty());
        when(templateRepository.findByTemplateCode("template1"))
                .thenReturn(Optional.empty());

        Result<Long> result = service.sendNotification(sendDTO);

        assertFalse(result.isSuccess());
        assertEquals(404, result.getCode());
        assertTrue(result.getMessage().contains("模板不存在"));
        verify(requestRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试模板已禁用")
    void testSendNotification_TemplateDisabled() {
        template.setEnabled(false);
        when(requestRepository.findByDedupeKey(anyString())).thenReturn(Optional.empty());
        when(templateRepository.findByTenantIdAndTemplateCode("tenant1", "template1"))
                .thenReturn(Optional.of(template));

        Result<Long> result = service.sendNotification(sendDTO);

        assertFalse(result.isSuccess());
        assertEquals(400, result.getCode());
        assertTrue(result.getMessage().contains("模板已禁用"));
    }

    @Test
    @DisplayName("测试处理通知成功")
    void testProcessNotification_Success() {
        NotificationRequest request = NotificationRequest.builder()
                .id(1L)
                .dedupeKey("test-key")
                .tenantId("tenant1")
                .channelType(NotificationTemplate.ChannelType.EMAIL)
                .receiverType(NotificationRequest.ReceiverType.USER)
                .receiverId("user1")
                .receiverAddress("user1@example.com")
                .templateCode("template1")
                .title("测试标题")
                .content("测试内容")
                .status(NotificationRequest.NotificationStatus.PENDING)
                .retryCount(0)
                .maxRetry(3)
                .build();

        MessageChannel mockChannel = mock(MessageChannel.class);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(channelFactory.getChannel(NotificationTemplate.ChannelType.EMAIL)).thenReturn(mockChannel);
        when(mockChannel.send(anyMap())).thenReturn("success-response");
        when(requestRepository.save(any(NotificationRequest.class))).thenReturn(request);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        service.processNotification(1L);

        verify(requestRepository, times(2)).save(any(NotificationRequest.class));
        verify(logRepository).save(any(NotificationLog.class));
        verify(rabbitTemplate).convertAndSend(eq("domain.event.exchange"), eq("notification.sent"), any(NotificationSentEvent.class));

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestRepository, atLeastOnce()).save(captor.capture());
        NotificationRequest savedRequest = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(NotificationRequest.NotificationStatus.SUCCESS, savedRequest.getStatus());
    }

    @Test
    @DisplayName("测试处理通知 - 请求不存在")
    void testProcessNotification_RequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        service.processNotification(999L);

        verify(requestRepository, never()).save(any());
        verify(channelFactory, never()).getChannel(any());
    }

    @Test
    @DisplayName("测试处理通知 - 已取消")
    void testProcessNotification_AlreadyCancelled() {
        NotificationRequest request = NotificationRequest.builder()
                .id(1L)
                .status(NotificationRequest.NotificationStatus.CANCELLED)
                .build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        service.processNotification(1L);

        verify(requestRepository, never()).save(any());
        verify(channelFactory, never()).getChannel(any());
    }

    @Test
    @DisplayName("测试处理通知 - 已成功")
    void testProcessNotification_AlreadySuccess() {
        NotificationRequest request = NotificationRequest.builder()
                .id(1L)
                .status(NotificationRequest.NotificationStatus.SUCCESS)
                .build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));

        service.processNotification(1L);

        verify(requestRepository, never()).save(any());
        verify(channelFactory, never()).getChannel(any());
    }

    @Test
    @DisplayName("测试失败处理")
    void testHandleSendFailure() {
        NotificationRequest request = NotificationRequest.builder()
                .id(1L)
                .dedupeKey("test-key")
                .tenantId("tenant1")
                .channelType(NotificationTemplate.ChannelType.EMAIL)
                .receiverType(NotificationRequest.ReceiverType.USER)
                .receiverId("user1")
                .receiverAddress("user1@example.com")
                .templateCode("template1")
                .title("测试标题")
                .content("测试内容")
                .status(NotificationRequest.NotificationStatus.SENDING)
                .retryCount(0)
                .maxRetry(3)
                .build();

        Exception exception = new RuntimeException("网络错误");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(NotificationRequest.class))).thenReturn(request);
        when(logRepository.save(any(NotificationLog.class))).thenAnswer(invocation -> {
            NotificationLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        service.handleSendFailure(1L, exception);

        verify(requestRepository).save(any(NotificationRequest.class));
        verify(logRepository).save(any(NotificationLog.class));
        verify(rabbitTemplate).convertAndSend(eq("domain.event.exchange"), eq("notification.failed"), any(NotificationFailedEvent.class));
        verify(rabbitTemplate).convertAndSend(eq("notification.retry.exchange"), eq("notification.retry"), eq(1L));
    }

    @Test
    @DisplayName("测试失败处理 - 达到最大重试次数")
    void testHandleSendFailure_MaxRetryReached() {
        NotificationRequest request = NotificationRequest.builder()
                .id(1L)
                .dedupeKey("test-key")
                .tenantId("tenant1")
                .channelType(NotificationTemplate.ChannelType.EMAIL)
                .receiverType(NotificationRequest.ReceiverType.USER)
                .receiverId("user1")
                .status(NotificationRequest.NotificationStatus.SENDING)
                .retryCount(3)
                .maxRetry(3)
                .build();

        Exception exception = new RuntimeException("网络错误");

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestRepository.save(any(NotificationRequest.class))).thenReturn(request);
        when(logRepository.save(any(NotificationLog.class))).thenReturn(NotificationLog.builder().build());

        service.handleSendFailure(1L, exception);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestRepository).save(captor.capture());
        assertEquals(NotificationRequest.NotificationStatus.FAILED, captor.getValue().getStatus());

        verify(rabbitTemplate, never()).convertAndSend(eq("notification.retry.exchange"), anyString(), anyLong());
    }

    @Test
    @DisplayName("测试失败处理 - 请求不存在")
    void testHandleSendFailure_RequestNotFound() {
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        service.handleSendFailure(999L, new RuntimeException("错误"));

        verify(requestRepository, never()).save(any());
        verify(logRepository, never()).save(any());
    }

    @Test
    @DisplayName("测试模板渲染")
    void testRenderTemplate() {
        when(requestRepository.findByDedupeKey(anyString())).thenReturn(Optional.empty());
        when(templateRepository.findByTenantIdAndTemplateCode("tenant1", "template1"))
                .thenReturn(Optional.of(template));
        when(requestRepository.save(any(NotificationRequest.class))).thenAnswer(invocation -> {
            NotificationRequest req = invocation.getArgument(0);
            req.setId(1L);
            return req;
        });

        Result<Long> result = service.sendNotification(sendDTO);

        assertTrue(result.isSuccess());
        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(requestRepository).save(captor.capture());
        NotificationRequest savedRequest = captor.getValue();

        assertEquals("通知: 张三", savedRequest.getTitle());
        assertTrue(savedRequest.getContent().contains("张三"));
    }

    @Test
    @DisplayName("测试重试失败通知")
    void testRetryFailedNotifications() {
        NotificationRequest request1 = NotificationRequest.builder()
                .id(1L)
                .status(NotificationRequest.NotificationStatus.PENDING)
                .retryCount(1)
                .maxRetry(3)
                .nextRetryAt(Instant.now().minusSeconds(10))
                .build();

        NotificationRequest request2 = NotificationRequest.builder()
                .id(2L)
                .status(NotificationRequest.NotificationStatus.PENDING)
                .retryCount(1)
                .maxRetry(3)
                .nextRetryAt(Instant.now().minusSeconds(10))
                .build();

        when(requestRepository.findRetryableRequests(100))
                .thenReturn(java.util.List.of(request1, request2));

        service.retryFailedNotifications();

        verify(rabbitTemplate, times(2)).convertAndSend(eq("notification.exchange"), eq("notification.send"), anyLong());
    }
}
