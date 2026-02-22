package com.hkt.iot.rule.interfaces.rest;

import com.hkt.iot.rule.application.service.RuleSetApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 规则集管理REST API控制器
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/rule-sets")
public class RuleSetController {

    private final RuleSetApplicationService ruleSetApplicationService;

    public RuleSetController(RuleSetApplicationService ruleSetApplicationService) {
        this.ruleSetApplicationService = ruleSetApplicationService;
    }

    /**
     * 创建规则集
     */
    @PostMapping
    public Long createRuleSet(@RequestBody CreateRuleSetRequest request) {
        RuleSetApplicationService.CreateRuleSetCommand command =
                new RuleSetApplicationService.CreateRuleSetCommand();
        command.setTenantId(request.getTenantId());
        command.setSetCode(request.getSetCode());
        command.setSetName(request.getSetName());
        command.setDescription(request.getDescription());
        command.setSetCategory(request.getSetCategory());
        command.setSpaceId(request.getSpaceId());
        command.setRuleIds(request.getRuleIds());
        command.setExecutionStrategy(request.getExecutionStrategy() != null ?
                com.hkt.iot.rule.domain.model.RuleSet.ExecutionStrategy.valueOf(request.getExecutionStrategy()) :
                com.hkt.iot.rule.domain.model.RuleSet.ExecutionStrategy.ALL);
        command.setCreatedBy(request.getCreatedBy());

        return ruleSetApplicationService.createRuleSet(command);
    }

    /**
     * 更新规则集
     */
    @PutMapping("/{ruleSetId}")
    public void updateRuleSet(
            @PathVariable Long ruleSetId,
            @RequestBody UpdateRuleSetRequest request) {
        RuleSetApplicationService.UpdateRuleSetCommand command =
                new RuleSetApplicationService.UpdateRuleSetCommand();
        command.setSetName(request.getSetName());
        command.setDescription(request.getDescription());
        command.setPriority(request.getPriority());
        command.setUpdatedBy(request.getUpdatedBy());

        ruleSetApplicationService.updateRuleSet(ruleSetId, command);
    }

    /**
     * 添加规则到规则集
     */
    @PostMapping("/{ruleSetId}/rules")
    public void addRules(
            @PathVariable Long ruleSetId,
            @RequestBody AddRulesRequest request) {
        ruleSetApplicationService.addRulesToSet(ruleSetId, request.getRuleIds());
    }

    /**
     * 从规则集移除规则
     */
    @DeleteMapping("/{ruleSetId}/rules")
    public void removeRules(
            @PathVariable Long ruleSetId,
            @RequestBody RemoveRulesRequest request) {
        ruleSetApplicationService.removeRulesFromSet(ruleSetId, request.getRuleIds());
    }

    /**
     * 激活规则集
     */
    @PostMapping("/{ruleSetId}/activate")
    public void activateRuleSet(@PathVariable Long ruleSetId) {
        ruleSetApplicationService.activateRuleSet(ruleSetId);
    }

    /**
     * 停用规则集
     */
    @PostMapping("/{ruleSetId}/deactivate")
    public void deactivateRuleSet(@PathVariable Long ruleSetId) {
        ruleSetApplicationService.deactivateRuleSet(ruleSetId);
    }

    /**
     * 归档规则集
     */
    @PostMapping("/{ruleSetId}/archive")
    public void archiveRuleSet(@PathVariable Long ruleSetId) {
        ruleSetApplicationService.archiveRuleSet(ruleSetId);
    }

    /**
     * 删除规则集
     */
    @DeleteMapping("/{ruleSetId}")
    public void deleteRuleSet(@PathVariable Long ruleSetId) {
        ruleSetApplicationService.deleteRuleSet(ruleSetId);
    }

    /**
     * 查询规则集详情
     */
    @GetMapping("/{ruleSetId}")
    public RuleSetApplicationService.RuleSetDetailDTO getRuleSet(@PathVariable Long ruleSetId) {
        return ruleSetApplicationService.getRuleSetDetail(ruleSetId);
    }

    /**
     * 查询规则集列表
     */
    @GetMapping
    public List<RuleSetApplicationService.RuleSetSummaryDTO> listRuleSets(
            @RequestParam Long tenantId) {
        return ruleSetApplicationService.listRuleSets(tenantId);
    }

    /**
     * 查询激活的规则集
     */
    @GetMapping("/active")
    public List<RuleSetApplicationService.RuleSetSummaryDTO> listActiveRuleSets(
            @RequestParam Long tenantId) {
        return ruleSetApplicationService.listActiveRuleSets(tenantId);
    }

    // ==================== Request DTO类 ====================

    /**
     * 创建规则集请求
     */
    public static class CreateRuleSetRequest {
        private Long tenantId;
        private String setCode;
        private String setName;
        private String description;
        private String setCategory;
        private Long spaceId;
        private Set<Long> ruleIds;
        private String executionStrategy;
        private Long createdBy;

        // Getters and Setters
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public String getSetCode() { return setCode; }
        public void setSetCode(String setCode) { this.setCode = setCode; }
        public String getSetName() { return setName; }
        public void setSetName(String setName) { this.setName = setName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSetCategory() { return setCategory; }
        public void setSetCategory(String setCategory) { this.setCategory = setCategory; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public Set<Long> getRuleIds() { return ruleIds; }
        public void setRuleIds(Set<Long> ruleIds) { this.ruleIds = ruleIds; }
        public String getExecutionStrategy() { return executionStrategy; }
        public void setExecutionStrategy(String executionStrategy) {
            this.executionStrategy = executionStrategy;
        }
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    }

    /**
     * 更新规则集请求
     */
    public static class UpdateRuleSetRequest {
        private String setName;
        private String description;
        private Integer priority;
        private Long updatedBy;

        // Getters and Setters
        public String getSetName() { return setName; }
        public void setSetName(String setName) { this.setName = setName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public Long getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    }

    /**
     * 添加规则请求
     */
    public static class AddRulesRequest {
        private Set<Long> ruleIds;

        public Set<Long> getRuleIds() { return ruleIds; }
        public void setRuleIds(Set<Long> ruleIds) { this.ruleIds = ruleIds; }
    }

    /**
     * 移除规则请求
     */
    public static class RemoveRulesRequest {
        private Set<Long> ruleIds;

        public Set<Long> getRuleIds() { return ruleIds; }
        public void setRuleIds(Set<Long> ruleIds) { this.ruleIds = ruleIds; }
    }
}
