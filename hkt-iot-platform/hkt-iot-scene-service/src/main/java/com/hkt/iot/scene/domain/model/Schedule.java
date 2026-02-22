package com.hkt.iot.scene.domain.model;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.AuditLog;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 定时计划聚合根
 *
 * 职责：管理定时计划的定义和执行调度
 * 业务规则：
 * - 定时计划按照Cron表达式或固定周期执行
 * - 支持生效时间范围控制
 * - 记录最后执行和下次执行时间
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Schedule {

    private ScheduleId id;
    private ScheduleName name;
    private ScheduleCode code;
    private CronExpression cronExpression;
    private ScheduleType type;
    private List<SceneAction> actions;
    private ScheduleStatus status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private TenantId tenantId;
    private SpaceId spaceId;
    private String description;
    private LocalDateTime lastExecutedAt;
    private LocalDateTime nextExecuteAt;
    private AuditLog auditLog;
    private Long version;

    /**
     * 激活定时计划
     */
    public void activate() {
        if (this.status == ScheduleStatus.ACTIVE) {
            return;
        }
        if (this.actions == null || this.actions.isEmpty()) {
            throw new IllegalStateException("定时计划必须配置执行动作后才能激活");
        }
        this.status = ScheduleStatus.ACTIVE;
        this.calculateNextExecuteTime();
        this.auditLog = AuditLog.create(LocalDateTime.now(), "定时计划激活");
    }

    /**
     * 停用定时计划
     */
    public void deactivate() {
        if (this.status == ScheduleStatus.INACTIVE) {
            return;
        }
        this.status = ScheduleStatus.INACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "定时计划停用");
    }

    /**
     * 归档定时计划
     */
    public void archive() {
        if (this.status == ScheduleStatus.ARCHIVED) {
            return;
        }
        this.status = ScheduleStatus.ARCHIVED;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "定时计划归档");
    }

    /**
     * 更新Cron表达式
     */
    public void updateCronExpression(CronExpression expression) {
        this.cronExpression = expression;
        this.calculateNextExecuteTime();
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新Cron表达式: " + expression.getValue());
    }

    /**
     * 添加执行动作
     */
    public void addAction(SceneAction action) {
        if (this.actions == null) {
            this.actions = new ArrayList<>();
        }
        if (this.actions.stream().anyMatch(a -> a.getId().equals(action.getId()))) {
            throw new IllegalArgumentException("执行动作已存在");
        }
        this.actions.add(action);
        Collections.sort(this.actions);
        this.auditLog = AuditLog.create(LocalDateTime.now(), "添加执行动作: " + action.getType());
    }

    /**
     * 移除执行动作
     */
    public void removeAction(ActionId actionId) {
        if (this.actions == null) {
            return;
        }
        this.actions.removeIf(a -> a.getId().equals(actionId));
        this.auditLog = AuditLog.create(LocalDateTime.now(), "移除执行动作: " + actionId.getValue());
    }

    /**
     * 更新生效时间范围
     */
    public void updateValidityPeriod(LocalDateTime validFrom, LocalDateTime validTo) {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new IllegalArgumentException("生效开始时间不能晚于结束时间");
        }
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.calculateNextExecuteTime();
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新生效时间范围");
    }

    /**
     * 检查定时计划是否在有效期内
     */
    public boolean isValidNow() {
        LocalDateTime now = LocalDateTime.now();
        if (validFrom != null && now.isBefore(validFrom)) {
            return false;
        }
        if (validTo != null && now.isAfter(validTo)) {
            return false;
        }
        return true;
    }

    /**
     * 检查是否应该执行
     */
    public boolean shouldExecute() {
        return this.status == ScheduleStatus.ACTIVE
                && isValidNow()
                && this.nextExecuteAt != null
                && !LocalDateTime.now().isBefore(this.nextExecuteAt);
    }

    /**
     * 计算下次执行时间（简化版，使用cron4j或Quartz的实际实现）
     */
    public void calculateNextExecuteTime() {
        if (this.status != ScheduleStatus.ACTIVE || !isValidNow()) {
            this.nextExecuteAt = null;
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 根据定时类型计算下次执行时间
        switch (this.type) {
            case DAILY:
                this.nextExecuteAt = now.plusDays(1).truncatedTo(ChronoUnit.DAYS);
                break;
            case WEEKLY:
                this.nextExecuteAt = now.plusWeeks(1).truncatedTo(ChronoUnit.DAYS);
                break;
            case MONTHLY:
                this.nextExecuteAt = now.plusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                break;
            case CUSTOM:
                // 自定义Cron表达式解析（实际应使用Quartz的CronExpression）
                if (this.cronExpression != null) {
                    // 简化实现：每分钟执行一次
                    this.nextExecuteAt = now.plusMinutes(1).truncatedTo(ChronoUnit.MINUTES);
                } else {
                    this.nextExecuteAt = null;
                }
                break;
            default:
                this.nextExecuteAt = null;
        }
    }

    /**
     * 执行定时计划
     */
    public ScheduleExecutionResult execute() {
        if (!shouldExecute()) {
            throw new IllegalStateException("定时计划不满足执行条件");
        }

        LocalDateTime startedAt = LocalDateTime.now();
        List<ActionExecutionResult> actionResults = new ArrayList<>();

        try {
            for (SceneAction action : this.actions) {
                actionResults.add(executeAction(action));
            }

            LocalDateTime completedAt = LocalDateTime.now();
            Duration duration = Duration.between(startedAt, completedAt);

            this.lastExecutedAt = startedAt;
            this.calculateNextExecuteTime();

            ExecutionResult result = determineOverallResult(actionResults);

            return ScheduleExecutionResult.builder()
                    .executionId(ExecutionId.generate())
                    .scheduleId(this.id)
                    .result(result)
                    .actionResults(actionResults)
                    .executedAt(startedAt)
                    .duration(duration)
                    .build();

        } catch (Exception e) {
            LocalDateTime completedAt = LocalDateTime.now();
            Duration duration = Duration.between(startedAt, completedAt);

            return ScheduleExecutionResult.builder()
                    .executionId(ExecutionId.generate())
                    .scheduleId(this.id)
                    .result(ExecutionResult.FAILED)
                    .actionResults(actionResults)
                    .executedAt(startedAt)
                    .duration(duration)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private ActionExecutionResult executeAction(SceneAction action) {
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            if (action.getDelaySeconds() > 0) {
                Thread.sleep(action.getDelaySeconds() * 1000L);
            }

            return ActionExecutionResult.builder()
                    .actionId(action.getId())
                    .result(ExecutionResult.SUCCESS)
                    .startedAt(startedAt)
                    .completedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            return ActionExecutionResult.builder()
                    .actionId(action.getId())
                    .result(ExecutionResult.FAILED)
                    .startedAt(startedAt)
                    .completedAt(LocalDateTime.now())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private ExecutionResult determineOverallResult(List<ActionExecutionResult> results) {
        if (results.isEmpty()) {
            return ExecutionResult.SUCCESS;
        }

        long successCount = results.stream()
                .filter(r -> r.getResult() == ExecutionResult.SUCCESS)
                .count();
        long failedCount = results.size() - successCount;

        if (failedCount == 0) {
            return ExecutionResult.SUCCESS;
        } else if (successCount > 0) {
            return ExecutionResult.PARTIAL_SUCCESS;
        } else {
            return ExecutionResult.FAILED;
        }
    }

    /**
     * 更新定时计划基本信息
     */
    public void updateInfo(ScheduleName name, String description) {
        this.name = name;
        this.description = description;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新定时计划信息");
    }

    /**
     * 创建新定时计划
     */
    public static Schedule create(ScheduleName name, ScheduleCode code, ScheduleType type,
                                  CronExpression cronExpression, TenantId tenantId, SpaceId spaceId,
                                  String description, LocalDateTime validFrom, LocalDateTime validTo) {
        return Schedule.builder()
                .id(ScheduleId.generate())
                .name(name)
                .code(code)
                .type(type)
                .cronExpression(cronExpression)
                .status(ScheduleStatus.DRAFT)
                .actions(new ArrayList<>())
                .tenantId(tenantId)
                .spaceId(spaceId)
                .description(description)
                .validFrom(validFrom)
                .validTo(validTo)
                .auditLog(AuditLog.create(LocalDateTime.now(), "创建定时计划"))
                .version(0L)
                .build();
    }
}
