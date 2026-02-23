package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * SLA 配置实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class SLAConfig extends Entity<String> {

    private String id;
    private ProcessDefinitionKey processDefinitionKey;
    private ActivityId taskDefinitionKey;
    private TenantId tenantId;
    private Duration responseTimeLimit;
    private Duration resolutionTimeLimit;
    private String priority;
    private Duration warningThreshold;
    private String escalationRules;
    private String notificationChannels;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 工厂方法：创建 SLA 配置
     */
    public static SLAConfig create(
            ProcessDefinitionKey processDefinitionKey,
            ActivityId taskDefinitionKey,
            TenantId tenantId,
            Duration responseTimeLimit,
            Duration resolutionTimeLimit,
            String priority) {
        SLAConfig config = new SLAConfig();
        config.id = java.util.UUID.randomUUID().toString();
        config.processDefinitionKey = processDefinitionKey;
        config.taskDefinitionKey = taskDefinitionKey;
        config.tenantId = tenantId;
        config.responseTimeLimit = responseTimeLimit;
        config.resolutionTimeLimit = resolutionTimeLimit;
        config.priority = priority;
        config.warningThreshold = Duration.ofMinutes(30);
        config.createdAt = LocalDateTime.now();
        config.updatedAt = LocalDateTime.now();
        return config;
    }

    /**
     * 工厂方法：创建完整的 SLA 配置
     */
    public static SLAConfig create(
            ProcessDefinitionKey processDefinitionKey,
            ActivityId taskDefinitionKey,
            TenantId tenantId,
            Duration responseTimeLimit,
            Duration resolutionTimeLimit,
            String priority,
            Duration warningThreshold,
            String escalationRules,
            String notificationChannels) {
        SLAConfig config = new SLAConfig();
        config.id = java.util.UUID.randomUUID().toString();
        config.processDefinitionKey = processDefinitionKey;
        config.taskDefinitionKey = taskDefinitionKey;
        config.tenantId = tenantId;
        config.responseTimeLimit = responseTimeLimit;
        config.resolutionTimeLimit = resolutionTimeLimit;
        config.priority = priority;
        config.warningThreshold = warningThreshold != null ? warningThreshold : Duration.ofMinutes(30);
        config.escalationRules = escalationRules;
        config.notificationChannels = notificationChannels;
        config.createdAt = LocalDateTime.now();
        config.updatedAt = LocalDateTime.now();
        return config;
    }

    /**
     * 更新配置
     */
    public void update(
            Duration responseTimeLimit,
            Duration resolutionTimeLimit,
            String priority) {
        this.responseTimeLimit = responseTimeLimit;
        this.resolutionTimeLimit = resolutionTimeLimit;
        this.priority = priority;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新完整配置
     */
    public void update(
            Duration responseTimeLimit,
            Duration resolutionTimeLimit,
            String priority,
            Duration warningThreshold,
            String escalationRules,
            String notificationChannels) {
        this.responseTimeLimit = responseTimeLimit;
        this.resolutionTimeLimit = resolutionTimeLimit;
        this.priority = priority;
        this.warningThreshold = warningThreshold;
        this.escalationRules = escalationRules;
        this.notificationChannels = notificationChannels;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取预警阈值，默认30分钟
     */
    public Duration getWarningThreshold() {
        return warningThreshold != null ? warningThreshold : Duration.ofMinutes(30);
    }

    @Override
    public String getId() {
        return id;
    }
}
