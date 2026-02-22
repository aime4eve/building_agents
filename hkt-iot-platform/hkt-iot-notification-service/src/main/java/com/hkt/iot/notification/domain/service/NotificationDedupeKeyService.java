package com.hkt.iot.notification.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;

/**
 * 通知幂等键服务
 * 负责生成和验证通知幂等键，确保通知不重复发送
 *
 * 幂等键格式: tenantId:alarmId:channel:receiver:templateCode[:hash]
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
public class NotificationDedupeKeyService {

    private static final String DELIMITER = ":";

    /**
     * 生成告警通知幂等键
     * 格式: tenantId:alarmId:channel:receiver:templateCode
     */
    public String generateAlarmDedupeKey(String tenantId, String alarmId, String channel,
                                         String receiverId, String templateCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(nullToEmpty(tenantId)).append(DELIMITER);
        sb.append(nullToEmpty(alarmId)).append(DELIMITER);
        sb.append(nullToEmpty(channel)).append(DELIMITER);
        sb.append(nullToEmpty(receiverId)).append(DELIMITER);
        sb.append(nullToEmpty(templateCode));
        return sb.toString();
    }

    /**
     * 生成通用通知幂等键
     * 格式: tenantId:businessType:businessId:channel:receiver:templateCode
     */
    public String generateBusinessDedupeKey(String tenantId, String businessType, String businessId,
                                            String channel, String receiverId, String templateCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(nullToEmpty(tenantId)).append(DELIMITER);
        sb.append(nullToEmpty(businessType)).append(DELIMITER);
        sb.append(nullToEmpty(businessId)).append(DELIMITER);
        sb.append(nullToEmpty(channel)).append(DELIMITER);
        sb.append(nullToEmpty(receiverId)).append(DELIMITER);
        sb.append(nullToEmpty(templateCode));
        return sb.toString();
    }

    /**
     * 生成带变量的幂等键
     * 当变量不同时需要区分发送时使用
     */
    public String generateDedupeKeyWithVariables(String tenantId, String channel, String receiverId,
                                                  String templateCode, Map<String, Object> variables) {
        StringBuilder sb = new StringBuilder();
        sb.append(nullToEmpty(tenantId)).append(DELIMITER);
        sb.append(nullToEmpty(channel)).append(DELIMITER);
        sb.append(nullToEmpty(receiverId)).append(DELIMITER);
        sb.append(nullToEmpty(templateCode));

        if (variables != null && !variables.isEmpty()) {
            String variablesHash = hashVariables(variables);
            sb.append(DELIMITER).append(variablesHash);
        }

        return sb.toString();
    }

    /**
     * 生成定时通知幂等键
     * 格式: tenantId:scheduleId:executionDate:channel:receiver
     */
    public String generateScheduledDedupeKey(String tenantId, String scheduleId, String executionDate,
                                             String channel, String receiverId) {
        StringBuilder sb = new StringBuilder();
        sb.append(nullToEmpty(tenantId)).append(DELIMITER);
        sb.append(nullToEmpty(scheduleId)).append(DELIMITER);
        sb.append(nullToEmpty(executionDate)).append(DELIMITER);
        sb.append(nullToEmpty(channel)).append(DELIMITER);
        sb.append(nullToEmpty(receiverId));
        return sb.toString();
    }

    /**
     * 解析幂等键
     */
    public DedupeKeyComponents parseDedupeKey(String dedupeKey) {
        if (dedupeKey == null || dedupeKey.isEmpty()) {
            return null;
        }

        String[] parts = dedupeKey.split(DELIMITER);
        DedupeKeyComponents components = new DedupeKeyComponents();
        
        if (parts.length >= 1) components.setTenantId(parts[0]);
        if (parts.length >= 2) components.setAlarmId(parts[1]);
        if (parts.length >= 3) components.setChannel(parts[2]);
        if (parts.length >= 4) components.setReceiverId(parts[3]);
        if (parts.length >= 5) components.setTemplateCode(parts[4]);
        if (parts.length >= 6) components.setVariablesHash(parts[5]);

        return components;
    }

    /**
     * 验证幂等键格式
     */
    public boolean isValidDedupeKey(String dedupeKey) {
        if (dedupeKey == null || dedupeKey.isEmpty()) {
            return false;
        }

        String[] parts = dedupeKey.split(DELIMITER);
        return parts.length >= 5;
    }

    /**
     * 对变量进行哈希
     */
    private String hashVariables(Map<String, Object> variables) {
        try {
            TreeMap<String, Object> sortedMap = new TreeMap<>(variables);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
            }

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            log.warn("MD5算法不可用，使用简单哈希", e);
            return String.valueOf(sb.toString().hashCode());
        }
    }

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }

    /**
     * 幂等键组件
     */
    @lombok.Data
    public static class DedupeKeyComponents {
        private String tenantId;
        private String alarmId;
        private String channel;
        private String receiverId;
        private String templateCode;
        private String variablesHash;
    }
}
