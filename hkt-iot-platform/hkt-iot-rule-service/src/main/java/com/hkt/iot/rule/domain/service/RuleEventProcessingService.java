package com.hkt.iot.rule.domain.service;

import com.hkt.iot.rule.domain.event.DeviceStatusChangedEvent;
import com.hkt.iot.rule.domain.event.TelemetryReceivedEvent;
import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.model.RuleContext;
import com.hkt.iot.rule.domain.model.RuleExecutionResult;
import com.hkt.iot.rule.domain.model.RuleExecutionLog;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import com.hkt.iot.rule.domain.repository.RuleExecutionLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HashMap;

/**
 * 规则事件处理服务
 * 负责处理设备事件并触发规则执行
 *
 * @author AI Engineer
 * @since 1.0.0
 */
@Service
public class RuleEventProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(RuleEventProcessingService.class);

    private final RuleRepository ruleRepository;
    private final RuleExecutionService executionService;
    private final RuleExecutionLogRepository executionLogRepository;

    // 设备规则缓存（设备ID -> 规则列表）
    private final ConcurrentHashMap<Long, List<RuleSummary>> deviceRuleCache = new ConcurrentHashMap<>();

    public RuleEventProcessingService(RuleRepository ruleRepository,
                                     RuleExecutionService executionService,
                                     RuleExecutionLogRepository executionLogRepository) {
        this.ruleRepository = ruleRepository;
        this.executionService = executionService;
        this.executionLogRepository = executionLogRepository;
    }

    /**
     * 处理设备事件
     *
     * @param event 设备事件
     * @return 触发的规则执行结果列表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RuleExecutionResult> handleDeviceEvent(DeviceEvent event) {
        logger.info("Processing device event: deviceId={}, eventType={}",
                event.getDeviceId(), event.getEventType());

        // 获取与设备关联的规则
        List<Rule> rules = findRulesByDeviceId(event.getDeviceId());

        if (rules.isEmpty()) {
            logger.debug("No rules found for device: {}", event.getDeviceId());
            return List.of();
        }

        // 构建规则上下文
        RuleContext context = buildContext(event);

        // 执行规则并记录日志
        List<RuleExecutionResult> results = new java.util.ArrayList<>();
        for (Rule rule : rules) {
            RuleExecutionResult result = executeRuleWithLogging(rule, context, event);
            results.add(result);
        }

        return results;
    }

    /**
     * 批量处理设备事件
     *
     * @param events 设备事件列表
     * @return 触发的规则执行结果列表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RuleExecutionResult> handleDeviceEvents(List<DeviceEvent> events) {
        List<RuleExecutionResult> allResults = new java.util.ArrayList<>();

        for (DeviceEvent event : events) {
            List<RuleExecutionResult> results = handleDeviceEvent(event);
            allResults.addAll(results);
        }

        return allResults;
    }

    /**
     * 处理定时规则
     *
     * @param tenantId 租户ID
     * @return 触发的规则执行结果列表
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<RuleExecutionResult> handleScheduledRules(Long tenantId) {
        logger.info("Processing scheduled rules for tenant: {}", tenantId);

        // 获取所有定时规则
        List<Rule> scheduledRules = ruleRepository.findScheduledRulesByTenantId(tenantId);

        if (scheduledRules.isEmpty()) {
            logger.debug("No scheduled rules found for tenant: {}", tenantId);
            return List.of();
        }

        List<RuleExecutionResult> results = new java.util.ArrayList<>();

        for (Rule rule : scheduledRules) {
            if (rule.getIsEnabled() && rule.getRuleStatus() == Rule.RuleStatus.ACTIVE) {
                RuleContext context = RuleContext.of();
                context.put("now", LocalDateTime.now());
                context.put("tenantId", tenantId);

                RuleExecutionResult result = executeRuleWithLogging(rule, context, null);
                results.add(result);
            }
        }

        return results;
    }

    /**
     * 刷新设备规则缓存
     *
     * @param deviceId 设备ID
     */
    public void refreshDeviceRules(Long deviceId) {
        List<Rule> rules = ruleRepository.findByDeviceId(deviceId);

        List<RuleSummary> summaries = rules.stream()
                .map(this::toSummary)
                .toList();

        deviceRuleCache.put(deviceId, summaries);

        logger.debug("Refreshed rule cache for device: {}, rule count: {}",
                deviceId, summaries.size());
    }

    /**
     * 刷新所有规则缓存
     */
    public void refreshAllRuleCache() {
        deviceRuleCache.clear();

        // 可以异步加载所有设备的规则
        logger.info("Cleared all device rule cache");
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
        logger.debug("处理遥测事件: deviceId={}, tenantId={}",
                event.getDeviceId(), event.getTenantId());

        // 构建规则上下文
        RuleContext context = buildTelemetryContext(event);

        // 执行租户的所有激活规则
        List<Rule> activeRules = ruleRepository.findEnabledByTenantId(event.getTenantId());
        List<RuleExecutionResult> results = new java.util.ArrayList<>();

        for (Rule rule : activeRules) {
            RuleExecutionResult result = executeRuleWithLogging(rule, context, null);
            results.add(result);
        }

        // 记录执行结果
        long triggeredCount = results.stream()
                .filter(RuleExecutionResult::isMatched)
                .count();
        long failedCount = results.stream()
                .filter(r -> !r.isSuccess())
                .count();

        logger.info("遥测事件规则执行完成: deviceId={}, total={}, triggered={}, failed={}",
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
        logger.debug("处理设备状态变化事件: deviceId={}, oldStatus={}, newStatus={}",
                event.getDeviceId(), event.getOldStatus(), event.getNewStatus());

        // 构建规则上下文
        RuleContext context = buildDeviceStatusContext(event);

        // 执行租户的所有激活规则
        List<Rule> activeRules = ruleRepository.findEnabledByTenantId(event.getTenantId());
        List<RuleExecutionResult> results = new java.util.ArrayList<>();

        for (Rule rule : activeRules) {
            RuleExecutionResult result = executeRuleWithLogging(rule, context, null);
            results.add(result);
        }

        // 记录执行结果
        long triggeredCount = results.stream()
                .filter(RuleExecutionResult::isMatched)
                .count();

        logger.info("设备状态变化事件规则执行完成: deviceId={}, total={}, triggered={}",
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
        List<Rule> activeRules = ruleRepository.findEnabledByTenantId(tenantId);

        return activeRules.stream()
                .map(rule -> executionService.execute(rule, ruleContext))
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
     * 根据设备ID查找关联的规则
     */
    private List<Rule> findRulesByDeviceId(Long deviceId) {
        // 先从缓存查找
        List<RuleSummary> summaries = deviceRuleCache.get(deviceId);

        if (summaries != null) {
            // 从缓存获取规则详情
            List<Rule> rules = new java.util.ArrayList<>();
            for (RuleSummary summary : summaries) {
                ruleRepository.findById(() -> summary.getRuleId())
                        .ifPresent(rules::add);
            }
            return rules;
        }

        // 从数据库查询
        List<Rule> rules = ruleRepository.findByDeviceId(deviceId);

        // 更新缓存
        deviceRuleCache.put(deviceId, rules.stream()
                .map(this::toSummary)
                .toList());

        return rules;
    }

    /**
     * 执行规则并记录日志
     */
    private RuleExecutionResult executeRuleWithLogging(Rule rule, RuleContext context,
                                                     DeviceEvent event) {
        String executionId = generateExecutionId();

        // 创建执行日志
        RuleExecutionLog log = RuleExecutionLog.create(
                rule.getTenantId(),
                rule.getId(),
                rule.getRuleCode(),
                rule.getRuleType(),
                executionId,
                rule.getTriggerType(),
                event != null ? event.getEventType() : "MANUAL",
                event != null ? event.getSource() : null,
                null
        );

        log.setTriggerData(context.toMap());
        log.start();

        try {
            // 执行规则
            RuleExecutionResult result = executionService.execute(rule, context);

            // 更新执行日志
            int totalActions = result.getActionResults().size();
            long successActions = result.getActionResults().stream()
                    .filter(ActionResult::isSuccess)
                    .count();
            long failedActions = totalActions - successActions;

            RuleExecutionLog.ExecutionStatus status;
            if (result.isSuccess()) {
                status = RuleExecutionLog.ExecutionStatus.SUCCESS;
            } else if (result.isFailed()) {
                status = RuleExecutionLog.ExecutionStatus.FAILED;
            } else {
                status = RuleExecutionLog.ExecutionStatus.PARTIAL;
            }

            log.complete(status, totalActions, (int) successActions, (int) failedActions);
            log.setExecutionResult(Map.of(
                    "matched", result.isMatched(),
                    "status", result.getStatus().name()
            ));

            // 异步保存日志（避免影响主流程）
            saveLogAsync(log);

            return result;

        } catch (Exception e) {
            logger.error("Error executing rule: {}", rule.getRuleCode(), e);
            log.fail("EXECUTION_ERROR", e.getMessage());
            saveLogAsync(log);
            return RuleExecutionResult.failed(e.getMessage());
        }
    }

    /**
     * 异步保存执行日志
     */
    private void saveLogAsync(RuleExecutionLog log) {
        try {
            executionLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to save execution log", e);
        }
    }

    /**
     * 构建规则执行上下文
     */
    private RuleContext buildContext(DeviceEvent event) {
        RuleContext context = RuleContext.of(
                event.getEventData(),
                event.getSource(),
                event.getDeviceId()
        );

        // 添加时间信息
        context.put("eventTime", event.getEventTime());
        context.put("now", LocalDateTime.now());

        return context;
    }

    /**
     * 生成执行ID
     */
    private String generateExecutionId() {
        return "EXEC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 转换为规则摘要
     */
    private RuleSummary toSummary(Rule rule) {
        RuleSummary summary = new RuleSummary();
        summary.setRuleId(rule.getId());
        summary.setRuleCode(rule.getRuleCode());
        summary.setRuleType(rule.getRuleType());
        summary.setRuleStatus(rule.getRuleStatus());
        summary.setIsEnabled(rule.getIsEnabled());
        summary.setPriority(rule.getRulePriority());
        return summary;
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

        // Getters and Setters
        public Long getRuleId() { return ruleId; }
        public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
        public Rule.RuleType getRuleType() { return ruleType; }
        public void setRuleType(Rule.RuleType ruleType) { this.ruleType = ruleType; }
        public Rule.RuleStatus getRuleStatus() { return ruleStatus; }
        public void setRuleStatus(RuleStatus ruleStatus) { this.ruleStatus = ruleStatus; }
        public Boolean getIsEnabled() { return isEnabled; }
        public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
    }
}
