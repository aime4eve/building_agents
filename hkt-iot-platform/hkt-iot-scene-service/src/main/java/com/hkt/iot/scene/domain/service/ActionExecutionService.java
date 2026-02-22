package com.hkt.iot.scene.domain.service;

import com.hkt.iot.domain.shared.DeviceId;
import com.hkt.iot.scene.domain.model.SceneAction;
import com.hkt.iot.scene.domain.model.SceneContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 场景动作执行服务
 * 负责执行场景中定义的各种动作
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutionService {

    private final RestTemplate restTemplate;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private static final String DEVICE_SERVICE_URL = "http://hkt-iot-device-service";
    private static final String NOTIFICATION_SERVICE_URL = "http://hkt-iot-notification-service";

    /**
     * 执行动作
     *
     * @param action 动作
     * @param context 执行上下文
     * @return 执行结果
     */
    public ActionExecutionResult execute(SceneAction action, SceneContext context) {
        log.info("执行场景动作: type={}, deviceId={}", action.getType(), action.getDeviceId());

        try {
            if (action.getDelaySeconds() > 0) {
                return executeWithDelay(action, context);
            }

            return switch (action.getType()) {
                case DEVICE_CONTROL -> executeDeviceControl(action, context);
                case SCENE_SWITCH -> executeSceneSwitch(action, context);
                case NOTIFY -> executeNotify(action, context);
                case DELAY -> executeDelay(action, context);
                case WEBHOOK -> executeWebhook(action, context);
                case HTTP_CALL -> executeHttpCall(action, context);
            };
        } catch (Exception e) {
            log.error("动作执行失败: type={}", action.getType(), e);
            return ActionExecutionResult.failed(action.getId(), e.getMessage());
        }
    }

    /**
     * 异步执行动作
     */
    public CompletableFuture<ActionExecutionResult> executeAsync(SceneAction action, SceneContext context) {
        return CompletableFuture.supplyAsync(() -> execute(action, context), executorService);
    }

    /**
     * 延迟执行
     */
    private ActionExecutionResult executeWithDelay(SceneAction action, SceneContext context) {
        try {
            Thread.sleep(action.getDelaySeconds() * 1000L);
            return execute(action.toBuilder().delaySeconds(0).build(), context);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ActionExecutionResult.failed(action.getId(), "延迟执行被中断");
        }
    }

    /**
     * 执行设备控制
     */
    private ActionExecutionResult executeDeviceControl(SceneAction action, SceneContext context) {
        DeviceId deviceId = action.getDeviceId();
        String serviceIdentifier = action.getServiceIdentifier();
        Map<String, Object> params = action.getParams();

        if (deviceId == null) {
            return ActionExecutionResult.failed(action.getId(), "设备ID不能为空");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("deviceId", deviceId.getValue());
            requestBody.put("commandCode", serviceIdentifier);
            requestBody.put("params", params);
            requestBody.put("tenantId", context.getTenantId().getValue());

            String url = DEVICE_SERVICE_URL + "/api/v1/commands/send";
            restTemplate.postForEntity(url, requestBody, Map.class);

            log.info("设备控制命令已发送: deviceId={}, command={}", deviceId, serviceIdentifier);
            return ActionExecutionResult.success(action.getId(), "设备控制命令已发送");
        } catch (Exception e) {
            log.error("设备控制失败: deviceId={}", deviceId, e);
            return ActionExecutionResult.failed(action.getId(), "设备控制失败: " + e.getMessage());
        }
    }

    /**
     * 执行场景联动
     */
    private ActionExecutionResult executeSceneSwitch(SceneAction action, SceneContext context) {
        Map<String, Object> params = action.getParams();
        if (params == null || !params.containsKey("targetSceneId")) {
            return ActionExecutionResult.failed(action.getId(), "目标场景ID不能为空");
        }

        try {
            String targetSceneId = params.get("targetSceneId").toString();
            log.info("触发联动场景: targetSceneId={}", targetSceneId);

            return ActionExecutionResult.success(action.getId(), "联动场景已触发: " + targetSceneId);
        } catch (Exception e) {
            log.error("场景联动失败", e);
            return ActionExecutionResult.failed(action.getId(), "场景联动失败: " + e.getMessage());
        }
    }

    /**
     * 执行通知发送
     */
    private ActionExecutionResult executeNotify(SceneAction action, SceneContext context) {
        Map<String, Object> params = action.getParams();
        if (params == null) {
            return ActionExecutionResult.failed(action.getId(), "通知参数不能为空");
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("tenantId", context.getTenantId().getValue());
            requestBody.put("channelType", params.getOrDefault("channelType", "IN_APP"));
            requestBody.put("receiverType", params.getOrDefault("receiverType", "USER"));
            requestBody.put("receiverId", params.get("receiverId"));
            requestBody.put("templateCode", params.get("templateCode"));
            requestBody.put("variables", params.get("variables"));
            requestBody.put("businessType", "SCENE");
            requestBody.put("businessId", context.getSceneId().getValue());

            String url = NOTIFICATION_SERVICE_URL + "/api/v1/notifications/send";
            restTemplate.postForEntity(url, requestBody, Map.class);

            log.info("通知已发送: receiverId={}", params.get("receiverId"));
            return ActionExecutionResult.success(action.getId(), "通知已发送");
        } catch (Exception e) {
            log.error("通知发送失败", e);
            return ActionExecutionResult.failed(action.getId(), "通知发送失败: " + e.getMessage());
        }
    }

    /**
     * 执行延迟
     */
    private ActionExecutionResult executeDelay(SceneAction action, SceneContext context) {
        Map<String, Object> params = action.getParams();
        int delaySeconds = params != null ? (int) params.getOrDefault("seconds", 0) : 0;

        if (delaySeconds <= 0) {
            return ActionExecutionResult.success(action.getId(), "延迟时间为0，跳过");
        }

        try {
            Thread.sleep(delaySeconds * 1000L);
            return ActionExecutionResult.success(action.getId(), "延迟完成: " + delaySeconds + "秒");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ActionExecutionResult.failed(action.getId(), "延迟被中断");
        }
    }

    /**
     * 执行Webhook调用
     */
    private ActionExecutionResult executeWebhook(SceneAction action, SceneContext context) {
        Map<String, Object> params = action.getParams();
        if (params == null || !params.containsKey("url")) {
            return ActionExecutionResult.failed(action.getId(), "Webhook URL不能为空");
        }

        try {
            String url = (String) params.get("url");
            String method = (String) params.getOrDefault("method", "POST");
            Map<String, Object> body = (Map<String, Object>) params.get("body");

            if ("GET".equalsIgnoreCase(method)) {
                restTemplate.getForEntity(url, String.class);
            } else {
                restTemplate.postForEntity(url, body, String.class);
            }

            log.info("Webhook调用成功: url={}", url);
            return ActionExecutionResult.success(action.getId(), "Webhook调用成功");
        } catch (Exception e) {
            log.error("Webhook调用失败", e);
            return ActionExecutionResult.failed(action.getId(), "Webhook调用失败: " + e.getMessage());
        }
    }

    /**
     * 执行HTTP调用
     */
    private ActionExecutionResult executeHttpCall(SceneAction action, SceneContext context) {
        return executeWebhook(action, context);
    }

    /**
     * 动作执行结果
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ActionExecutionResult {
        private SceneAction.ActionId actionId;
        private boolean success;
        private String message;
        private Map<String, Object> data;

        public static ActionExecutionResult success(SceneAction.ActionId actionId, String message) {
            return new ActionExecutionResult(actionId, true, message, null);
        }

        public static ActionExecutionResult success(SceneAction.ActionId actionId, String message, 
                                                     Map<String, Object> data) {
            return new ActionExecutionResult(actionId, true, message, data);
        }

        public static ActionExecutionResult failed(SceneAction.ActionId actionId, String message) {
            return new ActionExecutionResult(actionId, false, message, null);
        }
    }
}
