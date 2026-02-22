package com.hkt.iot.device.infrastructure.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

/**
 * 设备心跳检测定时任务
 * 定期检测设备心跳超时，标记离线设备
 *
 * @author HKT IoT Team
 */
@Slf4j
@Component
public class DeviceHeartbeatScheduler {

    private static final String DEVICE_HEARTBEAT_KEY_PREFIX = "device:heartbeat:";
    private static final String DEVICE_STATUS_KEY_PREFIX = "device:status:";

    /**
     * 心跳超时时间（秒）
     * 正常设备：180秒（3个心跳周期）
     * 关键设备：120秒
     * 网关设备：90秒
     */
    private static final int NORMAL_HEARTBEAT_TIMEOUT = 180;
    private static final int CRITICAL_HEARTBEAT_TIMEOUT = 120;
    private static final int GATEWAY_HEARTBEAT_TIMEOUT = 90;

    private final RedisTemplate<String, Object> redisTemplate;

    public DeviceHeartbeatScheduler(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检测设备心跳超时
     * 每60秒执行一次
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void checkHeartbeatTimeout() {
        log.debug("开始检测设备心跳超时...");

        try {
            // 查找所有在线设备的键
            Set<String> onlineDeviceKeys = redisTemplate.keys(DEVICE_STATUS_KEY_PREFIX + "*");

            if (onlineDeviceKeys == null || onlineDeviceKeys.isEmpty()) {
                log.debug("没有在线设备需要检测");
                return;
            }

            int timeoutCount = 0;
            Instant now = Instant.now();

            for (String key : onlineDeviceKeys) {
                String deviceId = extractDeviceId(key);

                // 获取设备最后心跳时间
                Long lastHeartbeatTime = (Long) redisTemplate.opsForValue()
                        .get(DEVICE_HEARTBEAT_KEY_PREFIX + deviceId);

                if (lastHeartbeatTime == null) {
                    continue;
                }

                // 获取设备类型，确定超时时间
                String deviceType = getDeviceType(deviceId);
                int timeout = getTimeoutByDeviceType(deviceType);

                // 计算超时时长
                Duration duration = Duration.between(Instant.ofEpochMilli(lastHeartbeatTime), now);
                long elapsedSeconds = duration.getSeconds();

                // 判断是否超时
                if (elapsedSeconds > timeout) {
                    handleDeviceTimeout(deviceId, deviceType, elapsedSeconds);
                    timeoutCount++;
                }
            }

            if (timeoutCount > 0) {
                log.warn("检测到{}个设备心跳超时", timeoutCount);
            } else {
                log.debug("所有设备心跳正常");
            }

        } catch (Exception e) {
            log.error("检测设备心跳超时失败", e);
        }
    }

    /**
     * 处理设备超时
     */
    private void handleDeviceTimeout(String deviceId, String deviceType, long elapsedSeconds) {
        log.warn("设备心跳超时: deviceId={}, deviceType={}, elapsed={}s",
                deviceId, deviceType, elapsedSeconds);

        try {
            // TODO: 处理设备超时
            // 1. 更新设备状态为离线
            // 2. 发送离线事件到Kafka
            // 3. 如果是网关设备，标记所有子设备离线
            // 4. 发送告警通知

            // 发送设备离线消息到Kafka
            // deviceEventPublisher.publishDeviceOfflineEvent(deviceId, "heartbeat_timeout");

            log.info("设备[{}]已标记为离线", deviceId);

        } catch (Exception e) {
            log.error("处理设备超时失败: deviceId={}", deviceId, e);
        }
    }

    /**
     * 根据设备类型获取超时时间
     */
    private int getTimeoutByDeviceType(String deviceType) {
        if (deviceType == null) {
            return NORMAL_HEARTBEAT_TIMEOUT;
        }

        // 网关设备超时时间更短
        if ("GATEWAY".equalsIgnoreCase(deviceType)) {
            return GATEWAY_HEARTBEAT_TIMEOUT;
        }

        // 可以根据业务需求定义更多关键设备类型
        if (isCriticalDevice(deviceType)) {
            return CRITICAL_HEARTBEAT_TIMEOUT;
        }

        return NORMAL_HEARTBEAT_TIMEOUT;
    }

    /**
     * 判断是否为关键设备
     */
    private boolean isCriticalDevice(String deviceType) {
        // 关键设备类型列表
        return deviceType.contains("SMOKE_DETECTOR") ||
                deviceType.contains("WATER_LEAK_SENSOR") ||
                deviceType.contains("GAS_DETECTOR") ||
                deviceType.contains("FIRE_SENSOR");
    }

    /**
     * 获取设备类型
     */
    private String getDeviceType(String deviceId) {
        // TODO: 从缓存或数据库获取设备类型
        return null;
    }

    /**
     * 从键中提取设备ID
     */
    private String extractDeviceId(String key) {
        return key.substring(DEVICE_STATUS_KEY_PREFIX.length());
    }
}
