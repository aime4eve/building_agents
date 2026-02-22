package com.hkt.iot.notification.infrastructure.channel;

import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.PushPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

import static cn.jpush.api.push.model.PushPayload.newBuilder;

/**
 * APP推送消息渠道（极光推送）
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class PushMessageChannel implements MessageChannel {

    @Value("${notification.push.master-secret:}")
    private String masterSecret;

    @Value("${notification.push.app-key:}")
    private String appKey;

    @Value("${notification.push.enabled:true}")
    private boolean enabled;

    private JPushClient jpushClient;

    @Override
    public String send(Map<String, Object> payload) throws Exception {
        String receiverId = (String) payload.get("receiverId");
        String title = (String) payload.get("title");
        String content = (String) payload.get("content");

        log.info("发送推送: to={}, title={}", receiverId, title);

        if (jpushClient == null) {
            initClient();
        }

        PushPayload pushPayload =.newBuilder()
                .setPlatform(cn.jpush.api.push.model.Platform.all())
                .setAudience(cn.jpush.api.push.model.Audience.alias(receiverId))
                .setNotification(cn.jpush.api.push.model.Notification.alert(content))
                .build();

        PushResult result = jpushClient.sendPush(pushPayload);

        if (result.isResultOK()) {
            log.info("推送发送成功: {}", receiverId);
            return result.msg_id;
        } else {
            log.error("推送发送失败: {}", result.getErrorMessage());
            throw new RuntimeException("推送发送失败: " + result.getErrorMessage());
        }
    }

    @Override
    public String getChannelType() {
        return "PUSH";
    }

    @Override
    public boolean isAvailable() {
        return enabled && appKey != null && !appKey.isEmpty();
    }

    /**
     * 初始化极光客户端
     */
    private void initClient() {
        jpushClient = new JPushClient(masterSecret, appKey);
    }
}
