package com.hkt.iot.notification.infrastructure.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Webhook消息渠道
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookMessageChannel implements MessageChannel {

    private final ObjectMapper objectMapper;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public String send(Map<String, Object> payload) throws Exception {
        // Webhook URL应该从payload中获取
        // 实际使用时需要在NotificationRequest中存储webhook配置
        log.info("发送Webhook通知: payload={}", payload);

        // 这里简化处理，实际使用时需要从payload中获取webhook配置
        String webhookUrl = (String) payload.getOrDefault("webhookUrl", "");
        String webhookMethod = (String) payload.getOrDefault("webhookMethod", "POST");

        if (webhookUrl.isEmpty()) {
            log.warn("Webhook URL为空，跳过发送");
            return "SKIPPED";
        }

        // 构建请求体
        RequestBody body = RequestBody.create(
                objectMapper.writeValueAsBytes(payload),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request.Builder requestBuilder = new Request.Builder()
                .url(webhookUrl);

        if ("GET".equalsIgnoreCase(webhookMethod)) {
            requestBuilder.get();
        } else {
            requestBuilder.post(body);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                log.info("Webhook发送成功: {}", webhookUrl);
                return "SUCCESS";
            } else {
                log.error("Webhook发送失败: {}", response.code());
                throw new IOException("Webhook发送失败: " + response.code());
            }
        }
    }

    @Override
    public String getChannelType() {
        return "WEBHOOK";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
