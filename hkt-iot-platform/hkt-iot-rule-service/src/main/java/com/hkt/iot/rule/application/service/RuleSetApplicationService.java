package com.hkt.iot.rule.application.service;

import com.hkt.iot.rule.domain.model.RuleSet;
import com.hkt.iot.rule.domain.repository.RuleSetRepository;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 规则集应用服务
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
@Transactional
public class RuleSetApplicationService {

    private final RuleSetRepository ruleSetRepository;
    private final RuleRepository ruleRepository;

    public RuleSetApplicationService(
            RuleSetRepository ruleSetRepository,
            RuleRepository ruleRepository) {
        this.ruleSetRepository = ruleSetRepository;
        this.ruleRepository = ruleRepository;
    }

    /**
     * 创建规则集
     */
    public Long createRuleSet(CreateRuleSetCommand command) {
        // 验证规则集编码唯一性
        if (ruleSetRepository.existsByTenantIdAndSetCode(command.getTenantId(), command.getSetCode())) {
            throw new IllegalArgumentException("规则集编码已存在: " + command.getSetCode());
        }

        // 验证规则是否存在
        if (command.getRuleIds() != null && !command.getRuleIds().isEmpty()) {
            for (Long ruleId : command.getRuleIds()) {
                if (ruleRepository.findById(() -> ruleId).isEmpty()) {
                    throw new IllegalArgumentException("规则不存在: " + ruleId);
                }
            }
        }

        RuleSet ruleSet = RuleSet.create(
                command.getTenantId(),
                command.getSetCode(),
                command.getSetName(),
                command.getDescription(),
                command.getSetCategory(),
                command.getSpaceId(),
                command.getExecutionStrategy(),
                command.getCreatedBy()
        );

        // 添加规则到规则集
        if (command.getRuleIds() != null && !command.getRuleIds().isEmpty()) {
            ruleSet.addRules(command.getRuleIds());
        }

        return ruleSetRepository.save(ruleSet).getId();
    }

    /**
     * 更新规则集
     */
    public void updateRuleSet(Long ruleSetId, UpdateRuleSetCommand command) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));

        ruleSet.updateInfo(
                command.getSetName(),
                command.getDescription(),
                command.getPriority()
        );

        ruleSet.setUpdatedBy(command.getUpdatedBy());
        ruleSetRepository.save(ruleSet);
    }

    /**
     * 添加规则到规则集
     */
    public void addRulesToSet(Long ruleSetId, Set<Long> ruleIds) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));

        // 验证规则是否存在
        for (Long ruleId : ruleIds) {
            if (ruleRepository.findById(() -> ruleId).isEmpty()) {
                throw new IllegalArgumentException("规则不存在: " + ruleId);
            }
        }

        ruleSet.addRules(ruleIds);
        ruleSetRepository.save(ruleSet);
    }

    /**
     * 从规则集移除规则
     */
    public void removeRulesFromSet(Long ruleSetId, Set<Long> ruleIds) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));

        for (Long ruleId : ruleIds) {
            ruleSet.removeRule(ruleId);
        }

        ruleSetRepository.save(ruleSet);
    }

    /**
     * 激活规则集
     */
    public void activateRuleSet(Long ruleSetId) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));
        ruleSet.activate();
        ruleSetRepository.save(ruleSet);
    }

    /**
     * 停用规则集
     */
    public void deactivateRuleSet(Long ruleSetId) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));
        ruleSet.deactivate();
        ruleSetRepository.save(ruleSet);
    }

    /**
     * 归档规则集
     */
    public void archiveRuleSet(Long ruleSetId) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));
        ruleSet.archive();
        ruleSetRepository.save(ruleSet);
    }

    /**
     * 删除规则集
     */
    public void deleteRuleSet(Long ruleSetId) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));
        ruleSetRepository.delete(ruleSet);
    }

    /**
     * 查询规则集详情
     */
    @Transactional(readOnly = true)
    public RuleSetDetailDTO getRuleSetDetail(Long ruleSetId) {
        RuleSet ruleSet = ruleSetRepository.findById(() -> ruleSetId)
                .orElseThrow(() -> new IllegalArgumentException("规则集不存在: " + ruleSetId));
        return RuleSetDetailDTO.from(ruleSet);
    }

    /**
     * 查询租户的规则集列表
     */
    @Transactional(readOnly = true)
    public List<RuleSetSummaryDTO> listRuleSets(Long tenantId) {
        List<RuleSet> ruleSets = ruleSetRepository.findByTenantIdOrderByPriorityDesc(tenantId);
        return ruleSets.stream()
                .map(RuleSetSummaryDTO::from)
                .toList();
    }

    /**
     * 查询激活的规则集
     */
    @Transactional(readOnly = true)
    public List<RuleSetSummaryDTO> listActiveRuleSets(Long tenantId) {
        List<RuleSet> ruleSets = ruleSetRepository.findByTenantIdAndSetStatus(
                tenantId, RuleSet.RuleSetStatus.ACTIVE);
        return ruleSets.stream()
                .map(RuleSetSummaryDTO::from)
                .toList();
    }

    // ==================== DTO类 ====================

    /**
     * 创建规则集命令
     */
    public static class CreateRuleSetCommand {
        private Long tenantId;
        private String setCode;
        private String setName;
        private String description;
        private String setCategory;
        private Long spaceId;
        private Set<Long> ruleIds;
        private RuleSet.ExecutionStrategy executionStrategy;
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
        public RuleSet.ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
        public void setExecutionStrategy(RuleSet.ExecutionStrategy executionStrategy) {
            this.executionStrategy = executionStrategy;
        }
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    }

    /**
     * 更新规则集命令
     */
    public static class UpdateRuleSetCommand {
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
     * 规则集摘要DTO
     */
    public static class RuleSetSummaryDTO {
        private Long id;
        private String setCode;
        private String setName;
        private String setCategory;
        private String setStatus;
        private Integer priority;
        private Integer ruleCount;
        private String executionStrategy;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime updatedAt;

        public static RuleSetSummaryDTO from(RuleSet ruleSet) {
            RuleSetSummaryDTO dto = new RuleSetSummaryDTO();
            dto.id = ruleSet.getId();
            dto.setCode = ruleSet.getSetCode();
            dto.setName = ruleSet.getSetName();
            dto.setCategory = ruleSet.getSetCategory();
            dto.setStatus = ruleSet.getSetStatus().name();
            dto.priority = ruleSet.getPriority();
            dto.ruleCount = ruleSet.getRuleCount();
            dto.executionStrategy = ruleSet.getExecutionStrategy() != null ?
                    ruleSet.getExecutionStrategy().name() : null;
            dto.createdAt = ruleSet.getCreatedAt();
            dto.updatedAt = ruleSet.getUpdatedAt();
            return dto;
        }

        // Getters
        public Long getId() { return id; }
        public String getSetCode() { return setCode; }
        public String getSetName() { return setName; }
        public String getSetCategory() { return setCategory; }
        public String getSetStatus() { return setStatus; }
        public Integer getPriority() { return priority; }
        public Integer getRuleCount() { return ruleCount; }
        public String getExecutionStrategy() { return executionStrategy; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; }
        public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    }

    /**
     * 规则集详情DTO
     */
    public static class RuleSetDetailDTO extends RuleSetSummaryDTO {
        private String description;
        private Long spaceId;
        private Set<Long> ruleIds;
        private Boolean parallelEnabled;
        private Integer maxParallel;
        private Integer timeoutSeconds;
        private Long createdBy;
        private Long updatedBy;

        public static RuleSetDetailDTO from(RuleSet ruleSet) {
            RuleSetDetailDTO dto = new RuleSetDetailDTO();
            dto.id = ruleSet.getId();
            dto.setCode = ruleSet.getSetCode();
            dto.setName = ruleSet.getSetName();
            dto.setCategory = ruleSet.getSetCategory();
            dto.setStatus = ruleSet.getSetStatus().name();
            dto.priority = ruleSet.getPriority();
            dto.ruleCount = ruleSet.getRuleCount();
            dto.executionStrategy = ruleSet.getExecutionStrategy() != null ?
                    ruleSet.getExecutionStrategy().name() : null;
            dto.createdAt = ruleSet.getCreatedAt();
            dto.updatedAt = ruleSet.getUpdatedAt();
            dto.description = ruleSet.getDescription();
            dto.spaceId = ruleSet.getSpaceId();
            dto.ruleIds = ruleSet.getRuleIds();
            dto.parallelEnabled = ruleSet.getParallelEnabled();
            dto.maxParallel = ruleSet.getMaxParallel();
            dto.timeoutSeconds = ruleSet.getTimeoutSeconds();
            dto.createdBy = ruleSet.getCreatedBy();
            dto.updatedBy = ruleSet.getUpdatedBy();
            return dto;
        }

        // Getters
        public String getDescription() { return description; }
        public Long getSpaceId() { return spaceId; }
        public Set<Long> getRuleIds() { return ruleIds; }
        public Boolean getParallelEnabled() { return parallelEnabled; }
        public Integer getMaxParallel() { return maxParallel; }
        public Integer getTimeoutSeconds() { return timeoutSeconds; }
        public Long getCreatedBy() { return createdBy; }
        public Long getUpdatedBy() { return updatedBy; }
    }
}
