package com.hkt.iot.notification.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 通知应用服务
 * 负责通知发送的核心业务逻辑
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService {

    private final NotificationRequestRepository requestRepository;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationLogRepository logRepository;
    private final MessageChannelFactory channelFactory;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${notification.max-retry:3}")
    private Integer maxRetry;

    @Value("${notification.default-priority:NORMAL}")
    private String defaultPriority;

    /**
     * 发送通知
     */
    @Transactional
    public Result<Long> sendNotification(NotificationSendDTO dto) {
        try {
            // 1. 幂等性检查
            if (dto.getDedupeKey() != null) {
                Optional<NotificationRequest> existingRequest = requestRepository.findByDedupeKey(dto.getDedupeKey());
                if (existingRequest.isPresent()) {
                    NotificationRequest request = existingRequest.get();
                    if (request.getStatus() == NotificationRequest.NotificationStatus.SUCCESS) {
                        log.info("通知已发送，幂等键: {}", dto.getDedupeKey());
                        return Result.success(request.getId());
                    }
                }
            }

            // 2. 获取模板
            Optional<NotificationTemplate> templateOpt = templateRepository.findByTenantIdAndTemplateCode(
                    dto.getTenantId(), dto.getTemplateCode());
            if (templateOpt.isEmpty()) {
                templateOpt = templateRepository.findByTemplateCode(dto.getTemplateCode());
            }
            if (templateOpt.isEmpty()) {
                return Result.error(404, "模板不存在: " + dto.getTemplateCode());
            }

            NotificationTemplate template = templateOpt.get();
            if (!template.getEnabled()) {
                return Result.error(400, "模板已禁用");
            }

            // 3. 渲染模板
            String title = renderTemplate(template.getTitleTemplate(), dto.getVariables());
            String content = renderTemplate(template.getContentTemplate(), dto.getVariables());

            // 4. 创建通知请求
            NotificationRequest request = buildRequest(dto, title, content);
            requestRepository.save(request);

            // 5. 异步发送通知
            rabbitTemplate.convertAndSend("notification.exchange", "notification.send", request.getId());

            return Result.success(request.getId());
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return Result.error(500, "发送通知失败: " + e.getMessage());
        }
    }

    /**
     * 处理通知发送
     */
    @Transactional
    public void processNotification(Long requestId) {
        try {
            Optional<NotificationRequest> requestOpt = requestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                log.warn("通知请求不存在: {}", requestId);
                return;
            }

            NotificationRequest request = requestOpt.get();

            // 检查是否已取消或已发送
            if (request.getStatus() == NotificationRequest.NotificationStatus.CANCELLED ||
                    request.getStatus() == NotificationRequest.NotificationStatus.SUCCESS) {
                return;
            }

            request.startSending();
            requestRepository.save(request);

            // 获取消息渠道
            MessageChannel channel = channelFactory.getChannel(request.getChannelType());

            // 发送消息
            Map<String, Object> payload = buildPayload(request);
            String response = channel.send(payload);

            // 记录日志
            NotificationLog log = NotificationLog.create(request);
            logRepository.save(log);

            // 更新状态
            request.markAsSuccess();
            requestRepository.save(request);

            // 发布成功事件
            NotificationSentEvent event = buildSentEvent(request);
            rabbitTemplate.convertAndSend("domain.event.exchange", "notification.sent", event);

            log.info("通知发送成功: {}", requestId);
        } catch (Exception e) {
            log.error("通知发送失败: {}", requestId, e);
            handleSendFailure(requestId, e);
        }
    }

    /**
     * 处理发送失败
     */
    @Transactional
    public void handleSendFailure(Long requestId, Exception exception) {
        try {
            Optional<NotificationRequest> requestOpt = requestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return;
            }

            NotificationRequest request = requestOpt.get();
            request.markAsFailed(exception.getMessage());
            requestRepository.save(request);

            // 记录失败日志
            NotificationLog log = NotificationLog.create(request);
            log.updateResult("ERROR", exception.getMessage(), null);
            logRepository.save(log);

            // 发布失败事件
            NotificationFailedEvent event = buildFailedEvent(request, exception);
            rabbitTemplate.convertAndSend("domain.event.exchange", "notification.failed", event);

            // 如果还需要重试，重新加入队列
            if (request.canRetry()) {
                rabbitTemplate.convertAndSend("notification.retry.exchange", "notification.retry", requestId);
            }

            log.info("通知失败处理完成: {}", requestId);
        } catch (Exception e) {
            log.error("处理发送失败异常: {}", requestId, e);
        }
    }

    /**
     * 重试失败的通知
     */
    @Transactional
    public void retryFailedNotifications() {
        try {
            var retryableRequests = requestRepository.findRetryableRequests(100);
            for (NotificationRequest request : retryableRequests) {
                rabbitTemplate.convertAndSend("notification.exchange", "notification.send", request.getId());
            }
            log.info("已加入重试队列: {} 条", retryableRequests.size());
        } catch (Exception e) {
            log.error("重试失败通知异常", e);
        }
    }

    /**
     * 渲染模板
     */
    private String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }

    /**
     * 构建通知请求
     */
    private NotificationRequest buildRequest(NotificationSendDTO dto, String title, String content) {
        try {
            return NotificationRequest.builder()
                    .dedupeKey(dto.getDedupeKey() != null ? dto.getDedupeKey() : generateDedupeKey(dto))
                    .tenantId(dto.getTenantId())
                    .channelType(NotificationTemplate.ChannelType.valueOf(dto.getChannelType()))
                    .receiverType(NotificationRequest.ReceiverType.valueOf(dto.getReceiverType()))
                    .receiverId(dto.getReceiverId())
                    .receiverAddress(dto.getReceiverAddress())
                    .templateCode(dto.getTemplateCode())
                    .title(title)
                    .content(content)
                    .variables(dto.getVariables() != null ? objectMapper.writeValueAsString(dto.getVariables()) : "{}")
                    .priority(NotificationRequest.Priority.valueOf(dto.getPriority()))
                    .status(NotificationRequest.NotificationStatus.PENDING)
                    .retryCount(0)
                    .maxRetry(maxRetry)
                    .businessType(dto.getBusinessType())
                    .businessId(dto.getBusinessId())
                    .correlationId(dto.getCorrelationId())
                    .scheduledAt(dto.getScheduledAt() != null ? Instant.ofEpochSecond(dto.getScheduledAt()) : null)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化变量失败", e);
        }
    }

    /**
     * 构建消息负载
     */
    private Map<String, Object> buildPayload(NotificationRequest request) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("requestId", request.getId());
        payload.put("title", request.getTitle());
        payload.put("content", request.getContent());
        payload.put("receiverId", request.getReceiverId());
        payload.put("receiverAddress", request.getReceiverAddress());
        payload.put("tenantId", request.getTenantId());
        payload.put("correlationId", request.getCorrelationId());
        return payload;
    }

    /**
     * 构建发送成功事件
     */
    private NotificationSentEvent buildSentEvent(NotificationRequest request) {
        try {
            Map<String, Object> variables = objectMapper.readValue(request.getVariables(), Map.class);
            return NotificationSentEvent.create(
                    request.getId(),
                    request.getDedupeKey(),
                    request.getTenantId(),
                    request.getChannelType().name(),
                    request.getReceiverId(),
                    request.getReceiverType().name(),
                    request.getTitle(),
                    request.getTemplateCode(),
                    variables,
                    request.getBusinessType(),
                    request.getBusinessId(),
                    request.getCorrelationId()
            );
        } catch (JsonProcessingException e) {
            log.error("构建事件失败", e);
            return null;
        }
    }

    /**
     * 构建发送失败事件
     */
    private NotificationFailedEvent buildFailedEvent(NotificationRequest request, Exception exception) {
        return NotificationFailedEvent.create(
                request.getId(),
                request.getDedupeKey(),
                request.getTenantId(),
                request.getChannelType().name(),
                request.getReceiverId(),
                request.getReceiverType().name(),
                request.getTitle(),
                exception.getMessage(),
                "SEND_ERROR",
                request.getRetryCount(),
                request.getMaxRetry(),
                request.getStatus() == NotificationRequest.NotificationStatus.PENDING,
                request.getNextRetryAt(),
                request.getBusinessType(),
                request.getBusinessId(),
                request.getCorrelationId()
        );
    }

    /**
     * 生成幂等键
     */
    private String generateDedupeKey(NotificationSendDTO dto) {
        return String.format("%s:%s:%s:%s:%s",
                dto.getTenantId(),
                dto.getChannelType(),
                dto.getReceiverType(),
                dto.getReceiverId(),
                dto.getTemplateCode()
        );
    }
}
