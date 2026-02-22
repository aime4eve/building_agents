package com.hkt.iot.rule.interfaces.rest;

import com.hkt.iot.rule.domain.model.Rule;
import com.hkt.iot.rule.domain.model.RuleVersion;
import com.hkt.iot.rule.domain.repository.RuleRepository;
import com.hkt.iot.rule.domain.service.RuleVersionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则版本管理REST API控制器
 *
 * @author HKT IoT Team
 */
@RestController
@RequestMapping("/api/v1/rules/{ruleId}/versions")
public class RuleVersionController {

    private final RuleVersionService versionService;
    private final RuleRepository ruleRepository;

    public RuleVersionController(
            RuleVersionService versionService,
            RuleRepository ruleRepository) {
        this.versionService = versionService;
        this.ruleRepository = ruleRepository;
    }

    /**
     * 获取规则的所有版本
     */
    @GetMapping
    public List<RuleVersion> getRuleVersions(@PathVariable Long ruleId) {
        return versionService.getRuleVersions(ruleId);
    }

    /**
     * 获取规则的当前版本
     */
    @GetMapping("/current")
    public RuleVersion getCurrentVersion(@PathVariable Long ruleId) {
        return versionService.getCurrentVersion(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("当前版本不存在"));
    }

    /**
     * 获取指定版本
     */
    @GetMapping("/{versionNumber}")
    public RuleVersion getVersion(
            @PathVariable Long ruleId,
            @PathVariable Integer versionNumber) {
        return versionService.getVersion(ruleId, versionNumber)
                .orElseThrow(() -> new IllegalArgumentException("版本不存在: " + versionNumber));
    }

    /**
     * 比较两个版本的差异
     */
    @GetMapping("/compare")
    public RuleVersionService.VersionDiff compareVersions(
            @PathVariable Long ruleId,
            @RequestParam Integer version1,
            @RequestParam Integer version2) {
        return versionService.compareVersions(ruleId, version1, version2);
    }

    /**
     * 恢复到指定版本
     */
    @PostMapping("/{versionNumber}/restore")
    public void restoreToVersion(
            @PathVariable Long ruleId,
            @PathVariable Integer versionNumber,
            @RequestBody RestoreRequest request) {
        versionService.restoreToVersion(ruleId, versionNumber, request.getRestoredBy());
    }

    /**
     * 创建当前规则的版本快照
     */
    @PostMapping("/snapshot")
    public RuleVersion createSnapshot(
            @PathVariable Long ruleId,
            @RequestBody SnapshotRequest request) {
        Rule rule = ruleRepository.findById(() -> ruleId)
                .orElseThrow(() -> new IllegalArgumentException("规则不存在: " + ruleId));

        return versionService.createVersion(
                rule,
                request.getChangeDescription(),
                RuleVersion.ChangeType.valueOf(request.getChangeType()),
                request.getCreatedBy()
        );
    }

    // ==================== Request DTO类 ====================

    /**
     * 恢复请求
     */
    public static class RestoreRequest {
        private Long restoredBy;

        public Long getRestoredBy() { return restoredBy; }
        public void setRestoredBy(Long restoredBy) { this.restoredBy = restoredBy; }
    }

    /**
     * 快照请求
     */
    public static class SnapshotRequest {
        private String changeDescription;
        private String changeType;
        private Long createdBy;

        public String getChangeDescription() { return changeDescription; }
        public void setChangeDescription(String changeDescription) {
            this.changeDescription = changeDescription;
        }
        public String getChangeType() { return changeType; }
        public void setChangeType(String changeType) { this.changeType = changeType; }
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    }
}
