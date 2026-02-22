package com.hkt.iot.notification.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.notification.domain.model.NotificationRequest;
import com.hkt.iot.notification.domain.repository.NotificationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 死信队列处理器
 * 处理超过最大重试次数的通知请求
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDlqHandler {

    private final NotificationRequestRepository requestRepository;
    private final ObjectMapper objectMapper;

    private static final int DLQ_ALERT_THRESHOLD = 10;
    private static final Map<String, Integer> failureStats = new HashMap<>();

    /**
     * 处理死信队列消息
     */
    @RabbitListener(queues = "notification.dlq.queue")
    public void handleDeadLetter(Object message) {
        log.warn("收到死信队列消息: {}", message);

        try {
            Long requestId = extractRequestId(message);
            if (requestId == null) {
                log.error("无法解析请求ID: {}", message);
                return;
            }

            Optional<NotificationRequest> requestOpt = requestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                log.warn("死信消息对应的请求不存在: {}", requestId);
                return;
            }

            NotificationRequest request = requestOpt.get();

            request.markAsDeadLetter();
            requestRepository.save(request);

            recordFailure(request);

            sendDeadLetterAlert(request);

            log.info("死信处理完成: requestId={}, channel={}, tenantId={}",
                    requestId, request.getChannelType(), request.getTenantId());

        } catch (Exception e) {
            log.error("处理死信消息异常: {}", message, e);
        }
    }

    /**
     * 从消息中提取请求ID
     */
    private Long extractRequestId(Object message) {
        if (message instanceof Long) {
            return (Long) message;
        } else if (message instanceof Integer) {
            return ((Integer) message).longValue();
        } else if (message instanceof String) {
            try {
                return Long.parseLong((String) message);
            } catch (NumberFormatException e) {
                try {
                    Map<String, Object> map = objectMapper.readValue((String) message, Map.class);
                    Object id = map.get("requestId");
                    if (id instanceof Number) {
                        return ((Number) id).longValue();
                    }
                } catch (Exception ex) {
                    log.debug("解析消息失败", ex);
                }
            }
        } else if (message instanceof Map) {
            Object id = ((Map<?, ?>) message).get("requestId");
            if (id instanceof Number) {
                return ((Number) id).longValue();
            }
        }
        return null;
    }

    /**
     * 记录失败统计
     */
    private void recordFailure(NotificationRequest request) {
        String key = request.getTenantId() + ":" + request.getChannelType().name();
        synchronized (failureStats) {
            failureStats.merge(key, 1, Integer::sum);
        }
    }

    /**
     * 发送死信告警
     */
    private void sendDeadLetterAlert(NotificationRequest request) {
        String key = request.getTenantId() + ":" + request.getChannelType().name();
        int failureCount;
        synchronized (failureStats) {
            failureCount = failureStats.getOrDefault(key, 0);
        }

        if (failureCount >= DLQ_ALERT_THRESHOLD) {
            log.error("【告警】租户 {} 渠道 {} 死信数量达到 {}，请检查通知服务状态",
                    request.getTenantId(), request.getChannelType(), failureCount);
        }

        log.warn("【死信告警】通知发送失败超过最大重试次数 - " +
                        "tenantId={}, channel={}, receiverId={}, templateCode={}, retryCount={}, lastError={}",
                request.getTenantId(),
                request.getChannelType(),
                request.getReceiverId(),
                request.getTemplateCode(),
                request.getRetryCount(),
                request.getLastError());
    }

    /**
     * 获取失败统计
     */
    public Map<String, Integer> getFailureStats() {
        synchronized (failureStats) {
            return new HashMap<>(failureStats);
        }
    }

    /**
     * 重置失败统计
     */
    public void resetFailureStats() {
        synchronized (failureStats) {
            failureStats.clear();
        }
    }

    /**
     * 手动重试死信消息
     */
    public boolean manualRetry(Long requestId) {
        try {
            Optional<NotificationRequest> requestOpt = requestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                log.warn("请求不存在: {}", requestId);
                return false;
            }

            NotificationRequest request = requestOpt.get();
            if (request.getStatus() != NotificationRequest.NotificationStatus.DEAD_LETTER) {
                log.warn("请求状态不是死信状态: {}", requestId);
                return false;
            }

            request.resetForRetry();
            requestRepository.save(request);

            log.info("手动重试死信消息: {}", requestId);
            return true;
        } catch (Exception e) {
            log.error("手动重试失败: {}", requestId, e);
            return false;
        }
    }
}
