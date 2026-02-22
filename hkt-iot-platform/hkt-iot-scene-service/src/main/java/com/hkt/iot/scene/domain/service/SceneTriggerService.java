package com.hkt.iot.scene.domain.service;

import com.hkt.iot.domain.shared.DeviceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 场景触发器服务
 * 负责管理和评估场景触发条件
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SceneTriggerService {

    private final SceneRepository sceneRepository;
    private final SceneExecutionService sceneExecutionService;

    private final Map<String, TriggerState> triggerStates = new ConcurrentHashMap<>();

    /**
     * 处理设备事件触发
     *
     * @param tenantId 租户ID
     * @param deviceId 设备ID
     * @param eventIdentifier 事件标识
     * @param eventData 事件数据
     */
    public List<SceneExecutionResult> handleDeviceEvent(TenantId tenantId, DeviceId deviceId,
                                                         String eventIdentifier, Map<String, Object> eventData) {
        log.info("处理设备事件触发: tenantId={}, deviceId={}, event={}", tenantId, deviceId, eventIdentifier);

        List<Scene> scenes = sceneRepository.findByTenantIdAndEnabled(tenantId, true);
        List<SceneExecutionResult> results = new ArrayList<>();

        for (Scene scene : scenes) {
            if (!scene.hasTriggers()) {
                continue;
            }

            SceneContext context = SceneContext.builder()
                    .tenantId(tenantId)
                    .deviceId(deviceId)
                    .eventIdentifier(eventIdentifier)
                    .eventData(eventData)
                    .triggeredAt(LocalDateTime.now())
                    .triggerType(SceneTrigger.TriggerType.DEVICE_EVENT)
                    .build();

            for (SceneTrigger trigger : scene.getTriggers()) {
                if (trigger.matches(context)) {
                    log.info("场景触发匹配: sceneId={}, sceneName={}", scene.getId(), scene.getName());
                    SceneExecutionResult result = sceneExecutionService.execute(scene.getId(), context);
                    results.add(result);
                    break;
                }
            }
        }

        return results;
    }

    /**
     * 处理遥测数据触发
     *
     * @param tenantId 租户ID
     * @param deviceId 设备ID
     * @param telemetryKey 遥测键
     * @param value 遥测值
     */
    public List<SceneExecutionResult> handleTelemetryTrigger(TenantId tenantId, DeviceId deviceId,
                                                              String telemetryKey, Object value) {
        log.debug("处理遥测触发: tenantId={}, deviceId={}, key={}, value={}", 
                tenantId, deviceId, telemetryKey, value);

        List<Scene> scenes = sceneRepository.findByTenantIdAndEnabled(tenantId, true);
        List<SceneExecutionResult> results = new ArrayList<>();

        for (Scene scene : scenes) {
            if (!scene.hasTriggers()) {
                continue;
            }

            Map<String, Object> eventData = new HashMap<>();
            eventData.put(telemetryKey, value);

            SceneContext context = SceneContext.builder()
                    .tenantId(tenantId)
                    .deviceId(deviceId)
                    .eventIdentifier("telemetry." + telemetryKey)
                    .eventData(eventData)
                    .triggeredAt(LocalDateTime.now())
                    .triggerType(SceneTrigger.TriggerType.CONDITION)
                    .build();

            for (SceneTrigger trigger : scene.getTriggers()) {
                if (trigger.getType() == SceneTrigger.TriggerType.CONDITION && 
                        evaluateTelemetryCondition(trigger, telemetryKey, value)) {
                    log.info("遥测条件触发场景: sceneId={}, key={}, value={}", scene.getId(), telemetryKey, value);
                    SceneExecutionResult result = sceneExecutionService.execute(scene.getId(), context);
                    results.add(result);
                    break;
                }
            }
        }

        return results;
    }

    /**
     * 处理定时触发
     * 每分钟执行一次，检查需要触发的场景
     */
    @Scheduled(cron = "0 * * * * ?")
    public void handleScheduledTriggers() {
        log.debug("检查定时触发场景");

        LocalTime now = LocalTime.now();
        LocalDateTime nowDateTime = LocalDateTime.now();

        List<Scene> allScenes = sceneRepository.findAllEnabled();

        for (Scene scene : allScenes) {
            if (!scene.hasTriggers()) {
                continue;
            }

            for (SceneTrigger trigger : scene.getTriggers()) {
                if (trigger.getType() == SceneTrigger.TriggerType.TIME) {
                    if (shouldTriggerNow(trigger, nowDateTime)) {
                        log.info("定时触发场景: sceneId={}, sceneName={}", scene.getId(), scene.getName());

                        SceneContext context = SceneContext.builder()
                                .tenantId(scene.getTenantId())
                                .triggeredAt(nowDateTime)
                                .triggerType(SceneTrigger.TriggerType.TIME)
                                .build();

                        sceneExecutionService.execute(scene.getId(), context);
                    }
                }
            }
        }
    }

    /**
     * 手动触发场景
     *
     * @param sceneId 场景ID
     * @param triggeredBy 触发用户
     */
    public SceneExecutionResult manualTrigger(SceneId sceneId, com.hkt.iot.domain.shared.UserId triggeredBy) {
        log.info("手动触发场景: sceneId={}, triggeredBy={}", sceneId, triggeredBy);

        Scene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("场景不存在: " + sceneId));

        if (!scene.isEnabled()) {
            return SceneExecutionResult.failed(sceneId, "场景未启用");
        }

        SceneContext context = SceneContext.builder()
                .tenantId(scene.getTenantId())
                .triggeredAt(LocalDateTime.now())
                .triggerType(SceneTrigger.TriggerType.MANUAL)
                .triggeredBy(triggeredBy)
                .build();

        return sceneExecutionService.execute(sceneId, context);
    }

    /**
     * 评估遥测条件
     */
    private boolean evaluateTelemetryCondition(SceneTrigger trigger, String telemetryKey, Object value) {
        Map<String, Object> parameters = trigger.getParameters();
        if (parameters == null) {
            return false;
        }

        String triggerKey = (String) parameters.get("telemetryKey");
        if (!telemetryKey.equals(triggerKey)) {
            return false;
        }

        String operator = (String) parameters.get("operator");
        Object threshold = parameters.get("value");

        if (operator == null || threshold == null) {
            return false;
        }

        return compareValues(value, threshold, operator);
    }

    /**
     * 比较值
     */
    private boolean compareValues(Object actual, Object expected, String operator) {
        if (actual == null || expected == null) {
            return false;
        }

        try {
            double actualNum = convertToDouble(actual);
            double expectedNum = convertToDouble(expected);

            return switch (operator) {
                case ">" -> actualNum > expectedNum;
                case ">=" -> actualNum >= expectedNum;
                case "<" -> actualNum < expectedNum;
                case "<=" -> actualNum <= expectedNum;
                case "==" -> Math.abs(actualNum - expectedNum) < 0.0001;
                case "!=" -> Math.abs(actualNum - expectedNum) >= 0.0001;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return operator.equals("==") && actual.toString().equals(expected.toString());
        }
    }

    private double convertToDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    /**
     * 判断是否应该在当前时间触发
     */
    private boolean shouldTriggerNow(SceneTrigger trigger, LocalDateTime now) {
        Map<String, Object> parameters = trigger.getParameters();
        if (parameters == null) {
            return false;
        }

        String cronExpression = (String) parameters.get("cronExpression");
        if (cronExpression != null) {
            return evaluateCronExpression(cronExpression, now);
        }

        String timeStr = (String) parameters.get("time");
        if (timeStr != null) {
            LocalTime triggerTime = LocalTime.parse(timeStr);
            LocalTime nowTime = now.toLocalTime();
            return Math.abs(nowTime.toSecondOfDay() - triggerTime.toSecondOfDay()) < 60;
        }

        return false;
    }

    /**
     * 评估Cron表达式
     */
    private boolean evaluateCronExpression(String cronExpression, LocalDateTime now) {
        // TODO: 使用Quartz或其他Cron解析器
        return false;
    }

    /**
     * 触发器状态
     */
    @lombok.Data
    private static class TriggerState {
        private String triggerId;
        private LocalDateTime lastTriggeredAt;
        private int triggerCount;
    }
}
