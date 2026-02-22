package com.hkt.iot.rule.application.service;

import com.hkt.iot.rule.domain.model.*;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import com.hkt.iot.rule.domain.service.RuleExecutionService;
import com.hkt.iot.rule.domain.service.RuleValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 规则应用服务
 * 提供规则管理的用例编排
 *
 * @author AI Engineer
 * @since 1.0.0
 */
@Service
@Transactional
public class RuleApplicationService {

    private final RuleRepository ruleRepository;
    private final RuleExecutionService executionService;
    private final RuleValidationService validationService;

    public RuleApplicationService(RuleRepository ruleRepository,
                                  RuleExecutionService executionService,
                                  RuleValidationService validationService) {
        this.ruleRepository = ruleRepository;
        this.executionService = executionService;
        this.validationService = validationService;
    }

    /**
     * 创建规则
     */
    public Long createRule(CreateRuleCommand command) {
        // 验证规则编码唯一性
        if (ruleRepository.existsByTenantIdAndRuleCode(command.getTenantId(), command.getRuleCode())) {
            throw new IllegalArgumentException("Rule code already exists: " + command.getRuleCode());
        }

        // 验证表达式
        RuleValidationService.ValidationResult validationResult =
                validationService.validateExpression(command.getTriggerExpression());
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Invalid expression: " + validationResult.getErrorMessage());
        }

        // 创建规则
        Rule rule = Rule.create(
                command.getTenantId(),
                command.getRuleCode(),
                command.getRuleName(),
                Rule.RuleType.valueOf(command.getRuleType()),
                command.getRuleCategory(),
                command.getDescription(),
                Rule.TriggerType.valueOf(command.getTriggerType()),
                command.getRuleConfig(),
                command.getDeviceIds(),
                command.getCreatedBy()
        );

        rule.setTriggerExpression(command.getTriggerExpression());

        if (command.getEffectiveTime() != null) {
            rule.setEffectiveTime(command.getEffectiveTime());
        }

        if (command.getExpireTime() != null) {
            rule.setExpireTime(command.getExpireTime());
        }

        if (command.getCronExpression() != null) {
            rule.setCronExpression(command.getCronExpression());
        }

        return ruleRepository.save(rule).getId();
    }

    /**
     * 更新规则
     */
    public void updateRule(Long ruleId, UpdateRuleCommand command) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        // 验证表达式（如果提供）
        if (command.getTriggerExpression() != null) {
            RuleValidationService.ValidationResult validationResult =
                    validationService.validateExpression(command.getTriggerExpression());
            if (!validationResult.isValid()) {
                throw new IllegalArgumentException("Invalid expression: " + validationResult.getErrorMessage());
            }
            rule.setTriggerExpression(command.getTriggerExpression());
        }

        // 更新基本属性
        if (command.getRuleName() != null) {
            rule.setRuleName(command.getRuleName());
        }
        if (command.getDescription() != null) {
            rule.setDescription(command.getDescription());
        }
        if (command.getRuleConfig() != null) {
            rule.setRuleConfig(command.getRuleConfig());
        }
        if (command.getDeviceIds() != null) {
            rule.setDeviceIds(command.getDeviceIds());
        }

        rule.setUpdatedAt(LocalDateTime.now());
        rule.setUpdatedBy(command.getUpdatedBy());

        ruleRepository.save(rule);
    }

    /**
     * 启用规则
     */
    public void enableRule(Long ruleId, Long operatorId) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        rule.enable();
        rule.setUpdatedBy(operatorId);

        ruleRepository.save(rule);
    }

    /**
     * 禁用规则
     */
    public void disableRule(Long ruleId, Long operatorId) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        rule.disable();
        rule.setUpdatedBy(operatorId);

        ruleRepository.save(rule);
    }

    /**
     * 归档规则
     */
    public void archiveRule(Long ruleId, Long operatorId) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        rule.archive();
        rule.setUpdatedBy(operatorId);

        ruleRepository.save(rule);
    }

    /**
     * 删除规则
     */
    public void deleteRule(Long ruleId, Long deletedBy) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        rule.softDelete(deletedBy);
        ruleRepository.save(rule);
    }

    /**
     * 执行规则
     */
    @Transactional
    public RuleExecutionResult executeRule(Long ruleId, Map<String, Object> context) {
        RuleContext ruleContext = RuleContext.of(context);
        RuleExecutionResult result = executionService.execute(ruleId, ruleContext);

        // 更新执行统计
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));
        rule.updateExecutionStats(result.isSuccess());
        ruleRepository.save(rule);

        return result;
    }

    /**
     * 测试规则（不更新统计）
     */
    public RuleExecutionResult testRule(Long ruleId, Map<String, Object> context) {
        RuleContext ruleContext = RuleContext.of(context);
        return executionService.test(ruleId, ruleContext);
    }

    /**
     * 验证规则表达式
     */
    public RuleValidationService.ValidationResult validateExpression(String expression) {
        return validationService.validateExpression(expression);
    }

    /**
     * 提取表达式中的变量
     */
    public Set<String> extractVariables(String expression) {
        return validationService.extractVariables(expression);
    }

    /**
     * 查询规则详情
     */
    @Transactional(readOnly = true)
    public RuleDetailDTO getRuleDetail(Long ruleId) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Rule not found: " + ruleId));

        return RuleDetailDTO.from(rule);
    }

    /**
     * 查询租户的规则列表
     */
    @Transactional(readOnly = true)
    public List<RuleSummaryDTO> listRules(Long tenantId) {
        List<Rule> rules = ruleRepository.findByTenantIdOrderByPriorityDesc(tenantId);
        return rules.stream()
                .map(RuleSummaryDTO::from)
                .toList();
    }

    /**
     * 查询租户的激活规则
     */
    @Transactional(readOnly = true)
    public List<RuleSummaryDTO> listActiveRules(Long tenantId) {
        List<Rule> rules = ruleRepository.findEnabledByTenantId(tenantId);
        return rules.stream()
                .map(RuleSummaryDTO::from)
                .toList();
    }

    /**
     * 查询指定类型的规则
     */
    @Transactional(readOnly = true)
    public List<RuleSummaryDTO> listRulesByType(Long tenantId, String ruleType) {
        List<Rule> rules = ruleRepository.findByTenantIdAndRuleType(tenantId, Rule.RuleType.valueOf(ruleType));
        return rules.stream()
                .map(RuleSummaryDTO::from)
                .toList();
    }

    // ==================== DTO类 ====================

    /**
     * 创建规则命令
     */
    public static class CreateRuleCommand {
        private Long tenantId;
        private String ruleCode;
        private String ruleName;
        private String ruleType;
        private String ruleCategory;
        private String description;
        private String triggerType;
        private String triggerExpression;
        private Map<String, Object> ruleConfig;
        private List<Long> deviceIds;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private String cronExpression;
        private Long createdBy;

        // Getters and Setters
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public String getRuleCode() { return ruleCode; }
        public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }
        public String getRuleCategory() { return ruleCategory; }
        public void setRuleCategory(String ruleCategory) { this.ruleCategory = ruleCategory; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTriggerType() { return triggerType; }
        public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
        public String getTriggerExpression() { return triggerExpression; }
        public void setTriggerExpression(String triggerExpression) { this.triggerExpression = triggerExpression; }
        public Map<String, Object> getRuleConfig() { return ruleConfig; }
        public void setRuleConfig(Map<String, Object> ruleConfig) { this.ruleConfig = ruleConfig; }
        public List<Long> getDeviceIds() { return deviceIds; }
        public void setDeviceIds(List<Long> deviceIds) { this.deviceIds = deviceIds; }
        public LocalDateTime getEffectiveTime() { return effectiveTime; }
        public void setEffectiveTime(LocalDateTime effectiveTime) { this.effectiveTime = effectiveTime; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
        public String getCronExpression() { return cronExpression; }
        public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    }

    /**
     * 更新规则命令
     */
    public static class UpdateRuleCommand {
        private String ruleName;
        private String description;
        private String triggerExpression;
        private Map<String, Object> ruleConfig;
        private List<Long> deviceIds;
        private Long updatedBy;

        // Getters and Setters
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTriggerExpression() { return triggerExpression; }
        public void setTriggerExpression(String triggerExpression) { this.triggerExpression = triggerExpression; }
        public Map<String, Object> getRuleConfig() { return ruleConfig; }
        public void setRuleConfig(Map<String, Object> ruleConfig) { this.ruleConfig = ruleConfig; }
        public List<Long> getDeviceIds() { return deviceIds; }
        public void setDeviceIds(List<Long> deviceIds) { this.deviceIds = deviceIds; }
        public Long getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    }

    /**
     * 规则摘要DTO
     */
    public static class RuleSummaryDTO {
        private Long id;
        private String ruleCode;
        private String ruleName;
        private String ruleType;
        private String ruleStatus;
        private Boolean isEnabled;
        private Integer rulePriority;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static RuleSummaryDTO from(Rule rule) {
            RuleSummaryDTO dto = new RuleSummaryDTO();
            dto.id = rule.getId();
            dto.ruleCode = rule.getRuleCode();
            dto.ruleName = rule.getRuleName();
            dto.ruleType = rule.getRuleType().name();
            dto.ruleStatus = rule.getRuleStatus().name();
            dto.isEnabled = rule.getIsEnabled();
            dto.rulePriority = rule.getRulePriority();
            dto.createdAt = rule.getCreatedAt();
            dto.updatedAt = rule.getUpdatedAt();
            return dto;
        }

        // Getters
        public Long getId() { return id; }
        public String getRuleCode() { return ruleCode; }
        public String getRuleName() { return ruleName; }
        public String getRuleType() { return ruleType; }
        public String getRuleStatus() { return ruleStatus; }
        public Boolean getIsEnabled() { return isEnabled; }
        public Integer getRulePriority() { return rulePriority; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    /**
     * 规则详情DTO
     */
    public static class RuleDetailDTO extends RuleSummaryDTO {
        private String ruleCategory;
        private String description;
        private String triggerType;
        private String triggerExpression;
        private Map<String, Object> ruleConfig;
        private List<Long> deviceIds;
        private Long spaceId;
        private LocalDateTime effectiveTime;
        private LocalDateTime expireTime;
        private String cronExpression;
        private Long totalExecutions;
        private Long successExecutions;
        private Long failedExecutions;
        private LocalDateTime lastExecutionTime;
        private String lastExecutionStatus;

        public static RuleDetailDTO from(Rule rule) {
            RuleDetailDTO dto = new RuleDetailDTO();
            dto.id = rule.getId();
            dto.ruleCode = rule.getRuleCode();
            dto.ruleName = rule.getRuleName();
            dto.ruleType = rule.getRuleType().name();
            dto.ruleStatus = rule.getRuleStatus().name();
            dto.isEnabled = rule.getIsEnabled();
            dto.rulePriority = rule.getRulePriority();
            dto.createdAt = rule.getCreatedAt();
            dto.updatedAt = rule.getUpdatedAt();
            dto.ruleCategory = rule.getRuleCategory();
            dto.description = rule.getDescription();
            dto.triggerType = rule.getTriggerType().name();
            dto.triggerExpression = rule.getTriggerExpression();
            dto.ruleConfig = rule.getRuleConfig();
            dto.deviceIds = rule.getDeviceIds();
            dto.spaceId = rule.getSpaceId();
            dto.effectiveTime = rule.getEffectiveTime();
            dto.expireTime = rule.getExpireTime();
            dto.cronExpression = rule.getCronExpression();
            dto.totalExecutions = rule.getTotalExecutions();
            dto.successExecutions = rule.getSuccessExecutions();
            dto.failedExecutions = rule.getFailedExecutions();
            dto.lastExecutionTime = rule.getLastExecutionTime();
            dto.lastExecutionStatus = rule.getLastExecutionStatus();
            return dto;
        }

        // Getters
        public String getRuleCategory() { return ruleCategory; }
        public String getDescription() { return description; }
        public String getTriggerType() { return triggerType; }
        public String getTriggerExpression() { return triggerExpression; }
        public Map<String, Object> getRuleConfig() { return ruleConfig; }
        public List<Long> getDeviceIds() { return deviceIds; }
        public Long getSpaceId() { return spaceId; }
        public LocalDateTime getEffectiveTime() { return effectiveTime; }
        public LocalDateTime getExpireTime() { return expireTime; }
        public String getCronExpression() { return cronExpression; }
        public Long getTotalExecutions() { return totalExecutions; }
        public Long getSuccessExecutions() { return successExecutions; }
        public Long getFailedExecutions() { return failedExecutions; }
        public LocalDateTime getLastExecutionTime() { return lastExecutionTime; }
        public String getLastExecutionStatus() { return lastExecutionStatus; }
    }
}
