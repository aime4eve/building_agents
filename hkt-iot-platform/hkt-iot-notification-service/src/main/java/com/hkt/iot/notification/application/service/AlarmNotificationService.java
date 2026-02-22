package com.hkt.iot.notification.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.iot.notification.application.dto.NotificationSendDTO;
import com.hkt.iot.notification.domain.event.AlarmTriggeredEvent;
import com.hkt.iot.notification.domain.model.NotificationTemplate;
import com.hkt.iot.notification.domain.repository.NotificationTemplateRepository;
import com.hkt.iot.notification.domain.service.NotificationDedupeKeyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 告警通知处理服务
 * 负责处理告警触发事件并发送通知
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmNotificationService {

    private final NotificationApplicationService notificationApplicationService;
    private final NotificationTemplateRepository templateRepository;
    private final NotificationDedupeKeyService dedupeKeyService;
    private final ObjectMapper objectMapper;

    private static final String DEFAULT_ALARM_TEMPLATE_EMAIL = "ALARM_TRIGGERED";
    private static final String DEFAULT_ALARM_TEMPLATE_PUSH = "ALARM_TRIGGERED_PUSH";

    private static final Map<String, String> ALARM_LEVEL_TEMPLATE_MAP = new HashMap<>();

    static {
        ALARM_LEVEL_TEMPLATE_MAP.put("CRITICAL", "ALARM_CRITICAL");
        ALARM_LEVEL_TEMPLATE_MAP.put("HIGH", "ALARM_HIGH");
        ALARM_LEVEL_TEMPLATE_MAP.put("MEDIUM", "ALARM_MEDIUM");
        ALARM_LEVEL_TEMPLATE_MAP.put("LOW", "ALARM_LOW");
    }

    /**
     * 处理告警触发事件
     *
     * @param event 告警触发事件
     * @param receiverIds 接收者ID列表
     * @param channels 通知渠道列表
     */
    public void handleAlarmTriggered(AlarmTriggeredEvent event, List<String> receiverIds, List<String> channels) {
        log.info("处理告警触发事件: alarmId={}, tenantId={}, alarmLevel={}",
                event.getAlarmId(), event.getTenantId(), event.getAlarmLevel());

        Map<String, Object> variables = buildVariables(event);

        for (String receiverId : receiverIds) {
            for (String channel : channels) {
                try {
                    sendAlarmNotification(event, receiverId, channel, variables);
                } catch (Exception e) {
                    log.error("发送告警通知失败: alarmId={}, receiverId={}, channel={}",
                            event.getAlarmId(), receiverId, channel, e);
                }
            }
        }
    }

    /**
     * 处理告警触发事件（使用默认配置）
     *
     * @param eventJson 告警事件JSON字符串
     */
    public void handleAlarmTriggered(String eventJson) {
        log.info("处理告警触发事件JSON: {}", eventJson);

        try {
            AlarmTriggeredEvent event = objectMapper.readValue(eventJson, AlarmTriggeredEvent.class);

            List<String> defaultChannels = List.of("EMAIL", "PUSH");

            List<String> receiverIds = determineReceivers(event);

            handleAlarmTriggered(event, receiverIds, defaultChannels);

        } catch (Exception e) {
            log.error("解析告警事件失败: {}", eventJson, e);
        }
    }

    /**
     * 发送单条告警通知
     */
    private void sendAlarmNotification(AlarmTriggeredEvent event, String receiverId,
                                        String channel, Map<String, Object> variables) {
        String templateCode = selectTemplate(event.getAlarmLevel(), channel);

        String dedupeKey = dedupeKeyService.generateAlarmDedupeKey(
                event.getTenantId(),
                event.getAlarmId(),
                channel,
                receiverId,
                templateCode
        );

        NotificationSendDTO dto = NotificationSendDTO.builder()
                .tenantId(event.getTenantId())
                .channelType(channel)
                .receiverType("USER")
                .receiverId(receiverId)
                .templateCode(templateCode)
                .variables(variables)
                .priority(mapAlarmLevelToPriority(event.getAlarmLevel()))
                .businessType("ALARM")
                .businessId(event.getAlarmId())
                .correlationId(event.getEventId())
                .dedupeKey(dedupeKey)
                .build();

        notificationApplicationService.sendNotification(dto);

        log.info("告警通知发送成功: alarmId={}, receiverId={}, channel={}",
                event.getAlarmId(), receiverId, channel);
    }

    /**
     * 构建模板变量
     */
    private Map<String, Object> buildVariables(AlarmTriggeredEvent event) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("alarmId", event.getAlarmId());
        variables.put("tenantId", event.getTenantId());
        variables.put("deviceId", event.getDeviceId());
        variables.put("deviceName", event.getDeviceName() != null ? event.getDeviceName() : "未知设备");
        variables.put("alarmType", event.getAlarmType());
        variables.put("alarmLevel", event.getAlarmLevel());
        variables.put("alarmTitle", event.getAlarmTitle());
        variables.put("alarmMessage", event.getAlarmMessage());
        variables.put("time", event.getTriggeredAt() != null ? event.getTriggeredAt().toString() : "");
        variables.put("ruleId", event.getRuleId());
        variables.put("ruleName", event.getRuleName());

        if (event.getAlarmData() != null) {
            variables.putAll(event.getAlarmData());
        }

        return variables;
    }

    /**
     * 根据告警级别选择模板
     */
    private String selectTemplate(String alarmLevel, String channel) {
        String levelTemplate = ALARM_LEVEL_TEMPLATE_MAP.get(alarmLevel);

        if (levelTemplate != null) {
            String templateCode = levelTemplate + "_" + channel;
            Optional<NotificationTemplate> template = templateRepository.findByTemplateCode(templateCode);
            if (template.isPresent()) {
                return templateCode;
            }
        }

        if ("PUSH".equals(channel)) {
            return DEFAULT_ALARM_TEMPLATE_PUSH;
        }
        return DEFAULT_ALARM_TEMPLATE_EMAIL;
    }

    /**
     * 确定通知接收者
     */
    private List<String> determineReceivers(AlarmTriggeredEvent event) {
        if (event.getAlarmData() != null) {
            Object receivers = event.getAlarmData().get("receivers");
            if (receivers instanceof List) {
                @SuppressWarnings("unchecked")
                List<String> receiverList = (List<String>) receivers;
                return receiverList;
            }
        }

        log.warn("告警事件未指定接收者，使用默认接收者: alarmId={}", event.getAlarmId());
        return List.of("admin");
    }

    /**
     * 映射告警级别到通知优先级
     */
    private String mapAlarmLevelToPriority(String alarmLevel) {
        if (alarmLevel == null) {
            return "NORMAL";
        }

        return switch (alarmLevel.toUpperCase()) {
            case "CRITICAL" -> "URGENT";
            case "HIGH" -> "HIGH";
            case "MEDIUM" -> "NORMAL";
            case "LOW" -> "LOW";
            default -> "NORMAL";
        };
    }
}
