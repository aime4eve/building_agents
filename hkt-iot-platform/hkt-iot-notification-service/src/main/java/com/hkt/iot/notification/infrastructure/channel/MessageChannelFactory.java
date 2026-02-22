package com.hkt.iot.notification.infrastructure.channel;

import com.hkt.iot.notification.domain.model.NotificationTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息渠道工厂
 * 根据渠道类型获取对应的渠道实现
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageChannelFactory {

    private final List<MessageChannel> channels;

    /**
     * 获取消息渠道
     *
     * @param channelType 渠道类型
     * @return 消息渠道
     */
    public MessageChannel getChannel(NotificationTemplate.ChannelType channelType) {
        return channels.stream()
                .filter(channel -> channel.getChannelType().equals(channelType.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的通知渠道: " + channelType));
    }
}
