package com.hkt.iot.notification.infrastructure.channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;

/**
 * 站内信消息渠道
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InAppMessageChannel implements MessageChannel {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String IN_APP_KEY_PREFIX = "notification:inapp:";
    private static final Duration EXPIRATION = Duration.ofDays(30);

    @Override
    public String send(Map<String, Object> payload) throws Exception {
        String receiverId = (String) payload.get("receiverId");
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");
        String tenantId = (String) payload.get("tenantId");
        String correlationId = (String) payload.get("correlationId");

        log.info("发送站内信: to={}, title={}", receiverId, title);

        // 构建站内信消息
        Map<String, Object> message = Map.of(
                "id", java.util.UUID.randomUUID().toString(),
                "title", title,
                "content", content,
                "tenantId", tenantId,
                "correlationId", correlationId != null ? correlationId : "",
                "createdAt", System.currentTimeMillis()
        );

        // 存储到Redis Sorted Set，按时间排序
        String key = IN_APP_KEY_PREFIX + tenantId + ":" + receiverId;
        redisTemplate.opsForZSet().add(key, message, System.currentTimeMillis());
        redisTemplate.expire(key, EXPIRATION);

        log.info("站内信发送成功: {}", receiverId);
        return message.get("id").toString();
    }

    @Override
    public String getChannelType() {
        return "IN_APP";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
