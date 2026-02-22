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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 场景聚合根
 *
 * 职责：管理场景的定义、触发条件和执行动作
 * 业务规则：
 * - 草稿状态的场景不能执行
 * - 激活状态的场景才能被触发
 * - 场景执行可以顺序或并行执行动作
 */
@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Scene {

    private SceneId id;
    private SceneName name;
    private SceneCode code;
    private SceneType type;
    private SceneStatus status;
    private List<SceneTrigger> triggers;
    private List<SceneAction> actions;
    private SceneExecutionMode executionMode;
    private TenantId tenantId;
    private SpaceId spaceId;
    private String description;
    private AuditLog auditLog;
    private Long version;

    /**
     * 激活场景
     */
    public void activate() {
        if (this.status == SceneStatus.ACTIVE) {
            return;
        }
        if (this.triggers == null || this.triggers.isEmpty()) {
            throw new IllegalStateException("场景必须配置触发条件后才能激活");
        }
        if (this.actions == null || this.actions.isEmpty()) {
            throw new IllegalStateException("场景必须配置执行动作后才能激活");
        }
        this.status = SceneStatus.ACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "场景激活");
    }

    /**
     * 停用场景
     */
    public void deactivate() {
        if (this.status == SceneStatus.INACTIVE) {
            return;
        }
        this.status = SceneStatus.INACTIVE;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "场景停用");
    }

    /**
     * 归档场景
     */
    public void archive() {
        if (this.status == SceneStatus.ARCHIVED) {
            return;
        }
        this.status = SceneStatus.ARCHIVED;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "场景归档");
    }

    /**
     * 添加触发条件
     */
    public void addTrigger(SceneTrigger trigger) {
        if (this.triggers == null) {
            this.triggers = new ArrayList<>();
        }
        if (this.triggers.stream().anyMatch(t -> t.getId().equals(trigger.getId()))) {
            throw new IllegalArgumentException("触发条件已存在");
        }
        this.triggers.add(trigger);
        this.auditLog = AuditLog.create(LocalDateTime.now(), "添加触发条件: " + trigger.getType());
    }

    /**
     * 移除触发条件
     */
    public void removeTrigger(TriggerId triggerId) {
        if (this.triggers == null) {
            return;
        }
        this.triggers.removeIf(t -> t.getId().equals(triggerId));
        this.auditLog = AuditLog.create(LocalDateTime.now(), "移除触发条件: " + triggerId.getValue());
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
        // 重新排序
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
     * 验证场景是否可执行
     */
    public boolean canExecute() {
        return this.status == SceneStatus.ACTIVE
                && this.triggers != null
                && !this.triggers.isEmpty()
                && this.actions != null
                && !this.actions.isEmpty();
    }

    /**
     * 更新场景基本信息
     */
    public void updateInfo(SceneName name, String description) {
        this.name = name;
        this.description = description;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新场景信息");
    }

    /**
     * 更新执行模式
     */
    public void updateExecutionMode(SceneExecutionMode executionMode) {
        this.executionMode = executionMode;
        this.auditLog = AuditLog.create(LocalDateTime.now(), "更新执行模式: " + executionMode);
    }

    /**
     * 执行场景（返回执行结果）
     */
    public SceneExecutionResult execute(SceneContext context) {
        if (!canExecute()) {
            throw new IllegalStateException("场景不可执行，当前状态: " + this.status);
        }

        LocalDateTime startedAt = LocalDateTime.now();
        List<ActionExecutionResult> actionResults = new ArrayList<>();

        try {
            if (executionMode == SceneExecutionMode.SEQUENTIAL) {
                // 顺序执行
                for (SceneAction action : this.actions) {
                    actionResults.add(executeAction(action, context));
                }
            } else {
                // 并行执行（此处为简化实现，实际需要线程池）
                for (SceneAction action : this.actions) {
                    actionResults.add(executeAction(action, context));
                }
            }

            LocalDateTime completedAt = LocalDateTime.now();
            Duration duration = Duration.between(startedAt, completedAt);

            // 判断整体执行结果
            ExecutionResult result = determineOverallResult(actionResults);

            return SceneExecutionResult.builder()
                    .executionId(ExecutionId.generate())
                    .sceneId(this.id)
                    .result(result)
                    .actionResults(actionResults)
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .duration(duration)
                    .context(context)
                    .build();

        } catch (Exception e) {
            LocalDateTime completedAt = LocalDateTime.now();
            Duration duration = Duration.between(startedAt, completedAt);

            return SceneExecutionResult.builder()
                    .executionId(ExecutionId.generate())
                    .sceneId(this.id)
                    .result(ExecutionResult.FAILED)
                    .actionResults(actionResults)
                    .startedAt(startedAt)
                    .completedAt(completedAt)
                    .duration(duration)
                    .context(context)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    private ActionExecutionResult executeAction(SceneAction action, SceneContext context) {
        LocalDateTime startedAt = LocalDateTime.now();
        try {
            // 延迟执行
            if (action.getDelaySeconds() > 0) {
                Thread.sleep(action.getDelaySeconds() * 1000L);
            }

            // 这里只是生成执行结果，实际执行由应用层处理
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
     * 检查触发条件是否匹配
     */
    public boolean matchTrigger(SceneTrigger trigger, SceneContext context) {
        return this.triggers != null
                && this.triggers.stream()
                .anyMatch(t -> t.getId().equals(trigger.getId()) && t.matches(context));
    }

    /**
     * 创建新场景
     */
    public static Scene create(SceneName name, SceneCode code, SceneType type,
                               TenantId tenantId, SpaceId spaceId, String description) {
        return Scene.builder()
                .id(SceneId.generate())
                .name(name)
                .code(code)
                .type(type)
                .status(SceneStatus.DRAFT)
                .triggers(new ArrayList<>())
                .actions(new ArrayList<>())
                .executionMode(SceneExecutionMode.SEQUENTIAL)
                .tenantId(tenantId)
                .spaceId(spaceId)
                .description(description)
                .auditLog(AuditLog.create(LocalDateTime.now(), "创建场景"))
                .version(0L)
                .build();
    }
}
