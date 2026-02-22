package com.hkt.iot.rule.interfaces.rest;

import com.hkt.iot.rule.application.service.RuleApplicationService;
import com.hkt.iot.rule.domain.service.RuleValidationService;
import com.hkt.iot.rule.domain.model.RuleContext;
import com.hkt.iot.rule.domain.model.RuleExecutionResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 规则管理REST API控制器
 *
 * @author AI Engineer
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/rules")
public class RuleController {

    private final RuleApplicationService ruleApplicationService;

    public RuleController(RuleApplicationService ruleApplicationService) {
        this.ruleApplicationService = ruleApplicationService;
    }

    /**
     * 创建规则
     */
    @PostMapping
    public Long createRule(@RequestBody CreateRuleRequest request) {
        RuleApplicationService.CreateCommand command = new RuleApplicationService.CreateCommand();
        command.setTenantId(request.getTenantId());
        command.setRuleCode(request.getRuleCode());
        command.setRuleName(request.getRuleName());
        command.setRuleType(request.getRuleType());
        command.setRuleCategory(request.getRuleCategory());
        command.setDescription(request.getDescription());
        command.setTriggerType(request.getTriggerType());
        command.setTriggerExpression(request.getTriggerExpression());
        command.setRuleConfig(request.getRuleConfig());
        command.setDeviceIds(request.getDeviceIds());
        command.setEffectiveTime(request.getEffectiveTime());
        command.setExpireTime(request.getExpireTime());
        command.setCronExpression(request.getCronExpression());
        command.setCreatedBy(request.getCreatedBy());

        return ruleApplicationService.createRule(command);
    }

    /**
     * 更新规则
     */
    @PutMapping("/{ruleId}")
    public void updateRule(@PathVariable Long ruleId,
                          @RequestBody UpdateRuleRequest request) {
        RuleApplicationService.UpdateRuleCommand command = new RuleApplicationService.UpdateRuleCommand();
        command.setRuleName(request.getRuleName());
        command.setDescription(request.getDescription());
        command.setTriggerExpression(request.getTriggerExpression());
        command.setRuleConfig(request.getRuleConfig());
        command.setDeviceIds(request.getDeviceIds());
        command.setUpdatedBy(request.getUpdatedBy());

        ruleApplicationService.updateRule(ruleId, command);
    }

    /**
     * 启用规则
     */
    @PostMapping("/{ruleId}/enable")
    public void enableRule(@PathVariable Long ruleId,
                          @RequestParam Long operatorId) {
        ruleApplicationService.enableRule(ruleId, operatorId);
    }

    /**
     * 禁用规则
     */
    @PostMapping("/{ruleId}/disable")
    public void disableRule(@PathVariable Long ruleId,
                           @RequestParam Long operatorId) {
        ruleApplicationService.disableRule(ruleId, operatorId);
    }

    /**
     * 归档规则
     */
    @PostMapping("/{ruleId}/archive")
    public void archiveRule(@PathVariable Long ruleId,
                           @RequestParam Long operatorId) {
        ruleApplicationService.archiveRule(ruleId, operatorId);
    }

    /**
     * 删除规则
     */
    @DeleteMapping("/{ruleId}")
    public void deleteRule(@PathVariable Long ruleId,
                          @RequestParam Long deletedBy) {
        ruleApplicationService.deleteRule(ruleId, deletedBy);
    }

    /**
     * 查询规则详情
     */
    @GetMapping("/{ruleId}")
    public RuleApplicationService.RuleDetailDTO getRule(@PathVariable Long ruleId) {
        return ruleApplicationService.getRuleDetail(ruleId);
    }

    /**
     * 查询规则列表
     */
    @GetMapping
    public List<RuleApplicationService.RuleSummaryDTO> listRules(
            @RequestParam Long tenantId,
            @RequestParam(required = false) String ruleType) {
        if (ruleType != null) {
            return ruleApplicationService.listRulesByType(tenantId, ruleType);
        }
        return ruleApplicationService.listRules(tenantId);
    }

    /**
     * 查询激活规则列表
     */
    @GetMapping("/active")
    public List<RuleApplicationService.RuleSummaryDTO> listActiveRules(
            @RequestParam Long tenantId) {
        return ruleApplicationService.listActiveRules(tenantId);
    }

    /**
     * 执行规则
     */
    @PostMapping("/{ruleId}/execute")
    public RuleExecutionResult executeRule(@PathVariable Long ruleId,
                                          @RequestBody Map<String, Object> context) {
        return ruleApplicationService.executeRule(ruleId, context);
    }

    /**
     * 测试规则
     */
    @PostMapping("/{ruleId}/test")
    public RuleExecutionResult testRule(@PathVariable Long ruleId,
                                       @RequestBody Map<String, Object> context) {
        return ruleApplicationService.testRule(ruleId, context);
    }

    /**
     * 验证规则表达式
     */
    @PostMapping("/validate")
    public RuleValidationService.ValidationResult validateExpression(
            @RequestBody ValidateExpressionRequest request) {
        return ruleApplicationService.validateExpression(request.getExpression());
    }

    /**
     * 提取表达式变量
     */
    @PostMapping("/extract-variables")
    public Set<String> extractVariables(@RequestBody ExtractVariablesRequest request) {
        return ruleApplicationService.extractVariables(request.getExpression());
    }

    // ==================== Request DTO类 ====================

    /**
     * 创建规则请求
     */
    public static class CreateRuleRequest {
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
     * 更新规则请求
     */
    public static class UpdateRuleRequest {
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
     * 验证表达式请求
     */
    public static class ValidateExpressionRequest {
        private String expression;

        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
    }

    /**
     * 提取变量请求
     */
    public static class ExtractVariablesRequest {
        private String expression;

        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
    }
}
