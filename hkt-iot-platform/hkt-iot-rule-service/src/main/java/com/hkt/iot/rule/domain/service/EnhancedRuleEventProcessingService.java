package com.hkt.iot.rule.domain.service;

import com.hkt.iot.rule.domain.event.DeviceStatusChangedEvent;
import com.hkt.iot.rule.domain.event.TelemetryReceivedEvent;
import com.hkt.iot.rule.domain.model.*;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import com.hkt.iot.rule.domain.repository.RuleExecutionLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 增强型规则事件处理服务
 * 处理设备遥测和状态变化事件，触发匹配的规则
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
public class EnhancedRuleEventProcessingService {

    private final RuleRepository ruleRepository;
    private final RuleExecutionService executionService;
    private final RuleExecutionLogRepository executionLogRepository;

    // 设备规则缓存（设备ID -> 规则列表）
    private final ConcurrentHashMap<Long, List<RuleSummary>> deviceRuleCache = new ConcurrentHashMap<>();

    public EnhancedRuleEventProcessingService(
            RuleRepository ruleRepository,
            RuleExecutionService executionService,
            RuleExecutionLogRepository executionLogRepository) {
        this.ruleRepository = ruleRepository;
        this.executionService = executionService;
        this.executionLogRepository = executionLogRepository;
    }

    /**
     * 处理遥测数据接收事件
     * 查找并执行匹配的规则
     *
     * @param event 遥测数据接收事件
     * @return 规则执行结果列表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RuleExecutionResult> processTelemetryEvent(TelemetryReceivedEvent event) {
        log.debug("处理遥测事件: deviceId={}, tenantId={}",
                event.getDeviceId(), event.getTenantId());

        // 构建规则上下文
        RuleContext context = buildTelemetryContext(event);

        // 执行租户的所有激活规则
        List<RuleExecutionResult> results = executeActiveRules(event.getTenantId(), context);

        // 记录执行结果
        long triggeredCount = results.stream()
                .filter(RuleExecutionResult::isMatched)
                .count();
        long failedCount = results.stream()
                .filter(r -> !r.isSuccess())
                .count();

        log.info("遥测事件规则执行完成: deviceId={}, total={}, triggered={}, failed={}",
                event.getDeviceId(), results.size(), triggeredCount, failedCount);

        return results;
    }

    /**
     * 处理设备状态变化事件
     * 查找并执行匹配的规则
     *
     * @param event 设备状态变化事件
     * @return 规则执行结果列表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RuleExecutionResult> processDeviceStatusChangedEvent(DeviceStatusChangedEvent event) {
        log.debug("处理设备状态变化事件: deviceId={}, oldStatus={}, newStatus={}",
                event.getDeviceId(), event.getOldStatus(), event.getNewStatus());

        // 构建规则上下文
        RuleContext context = buildDeviceStatusContext(event);

        // 执行租户的所有激活规则
        List<RuleExecutionResult> results = executeActiveRules(event.getTenantId(), context);

        // 记录执行结果
        long triggeredCount = results.stream()
                .filter(RuleExecutionResult::isMatched)
                .count();

        log.info("设备状态变化事件规则执行完成: deviceId={}, total={}, triggered={}",
                event.getDeviceId(), results.size(), triggeredCount);

        return results;
    }

    /**
     * 执行租户的所有激活规则
     *
     * @param tenantId 租户ID
     * @param context  规则上下文
     * @return 执行结果列表
     */
    public List<RuleExecutionResult> executeAllActiveRules(Long tenantId, Map<String, Object> context) {
        RuleContext ruleContext = RuleContext.of(context);
        return executeActiveRules(tenantId, ruleContext);
    }

    /**
     * 执行租户的所有激活规则
     *
     * @param tenantId 租户ID
     * @param context  规则上下文
     * @return 执行结果列表
     */
    private List<RuleExecutionResult> executeActiveRules(Long tenantId, RuleContext context) {
        List<Rule> activeRules = ruleRepository.findEnabledByTenantId(tenantId);

        return activeRules.stream()
                .map(rule -> executionService.execute(rule, context))
                .toList();
    }

    /**
     * 构建遥测数据规则上下文
     */
    private RuleContext buildTelemetryContext(TelemetryReceivedEvent event) {
        Map<String, Object> contextData = new HashMap<>();

        // 设备基本信息
        contextData.put("deviceId", event.getDeviceId());
        contextData.put("deviceSn", event.getDeviceSn());
        contextData.put("deviceType", event.getDeviceType());
        contextData.put("tenantId", event.getTenantId());
        contextData.put("spaceId", event.getSpaceId());
        contextData.put("timestamp", event.getTimestamp());

        // 遥测数据
        if (event.getTelemetryData() != null) {
            contextData.putAll(event.getTelemetryData());
        }

        // 元数据
        contextData.put("metadata", event.getMetadata());

        return RuleContext.of(contextData);
    }

    /**
     * 构建设备状态变化规则上下文
     */
    private RuleContext buildDeviceStatusContext(DeviceStatusChangedEvent event) {
        Map<String, Object> contextData = new HashMap<>();

        // 设备基本信息
        contextData.put("deviceId", event.getDeviceId().toString());
        contextData.put("deviceSn", event.getDeviceSn());
        contextData.put("tenantId", event.getTenantId());
        contextData.put("timestamp", event.getChangedAt());

        // 状态信息
        contextData.put("oldStatus", event.getOldStatus());
        contextData.put("newStatus", event.getNewStatus());
        contextData.put("isOnline", event.isOnline());
        contextData.put("isOffline", event.isOffline());

        return RuleContext.of(contextData);
    }

    /**
     * 执行单个规则（用于测试和调试）
     */
    public RuleExecutionResult executeRuleForEvent(Long ruleId, TelemetryReceivedEvent event) {
        RuleContext context = buildTelemetryContext(event);
        return executionService.execute(ruleId, context);
    }

    /**
     * 执行单个规则（用于测试和调试）
     */
    public RuleExecutionResult executeRuleForEvent(Long ruleId, DeviceStatusChangedEvent event) {
        RuleContext context = buildDeviceStatusContext(event);
        return executionService.execute(ruleId, context);
    }

    /**
     * 规则摘要
     */
    private static class RuleSummary {
        private Long ruleId;
        private String ruleCode;
        private Rule.RuleType ruleType;
        private Rule.RuleStatus ruleStatus;
        private Boolean isEnabled;
        private Integer rulePriority;

        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
        public Rule.RuleType getRuleType() { return ruleType; }
        public void setRuleType(Rule.RuleType ruleType) { this.ruleType = ruleType; }
        public Rule.RuleStatus getRuleStatus() { return ruleStatus; }
        public void setRuleStatus(Rule.RuleStatus ruleStatus) { this.ruleStatus = ruleStatus; }
        public Boolean getIsEnabled() { return isEnabled; }
        public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
        public Integer getRulePriority() { return rulePriority; }
        public void setRulePriority(Integer rulePriority) { this.rulePriority = rulePriority; }
    }
}
