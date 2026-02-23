package com.hkt.iot.workflow.domain.model.aggregate;

import com.hkt.iot.domain.model.AggregateRoot;
import com.hkt.iot.domain.event.DomainEvent;
import com.hkt.iot.workflow.domain.model.domainevent.*;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 流程实例聚合根
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class ProcessInstance extends AggregateRoot<String> {

    // ========== 聚合根标识 ==========
    private ProcessInstanceId id;

    // ========== 值对象 ==========
    private BusinessKey businessKey;
    private ProcessDefinitionKey processDefinitionKey;
    private ProcessInstanceState state;
    private TenantId tenantId;
    private UserId startedBy;
    private ActivityId currentActivityId;

    // ========== 时间戳 ==========
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime updatedAt;

    // ========== 版本号（乐观锁） ==========
    private Long version;

    // ========== 领域事件 ==========
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // ========== 工厂方法：启动流程实例 ==========
    public static ProcessInstance start(
            ProcessDefinitionKey processDefinitionKey,
            BusinessKey businessKey,
            TenantId tenantId,
            UserId startedBy,
            Map<String, Object> variables) {
        ProcessInstance instance = new ProcessInstance();
        instance.id = ProcessInstanceId.generate();
        instance.businessKey = businessKey;
        instance.processDefinitionKey = processDefinitionKey;
        instance.tenantId = tenantId;
        instance.startedBy = startedBy;
        instance.state = ProcessInstanceState.STARTED;
        instance.startedAt = LocalDateTime.now();
        instance.updatedAt = LocalDateTime.now();
        instance.version = 0L;

        // 注册领域事件
        instance.registerDomainEvent(new ProcessInstanceStartedEvent(
                instance.id,
                instance.processDefinitionKey,
                instance.businessKey,
                instance.tenantId,
                instance.startedBy,
                variables,
                instance.startedAt
        ));

        return instance;
    }

    // ========== 业务方法：完成任务 ==========
    public void completeTask(TaskId taskId, Map<String, Object> variables) {
        validateState();

        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new TaskCompletedEvent(
                this.id,
                taskId,
                this.tenantId,
                variables,
                this.updatedAt
        ));
    }

    // ========== 业务方法：完成流程 ==========
    public void complete() {
        if (this.state == ProcessInstanceState.COMPLETED) {
            return; // 幂等性
        }

        ProcessInstanceState previousState = this.state;
        this.state = ProcessInstanceState.COMPLETED;
        this.endedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new ProcessInstanceCompletedEvent(
                this.id,
                this.processDefinitionKey,
                this.businessKey,
                this.tenantId,
                this.startedAt,
                this.endedAt
        ));
    }

    // ========== 业务方法：挂起流程 ==========
    public void suspend() {
        if (this.state != ProcessInstanceState.RUNNING && this.state != ProcessInstanceState.STARTED) {
            throw new IllegalStateException("Only running or started process can be suspended");
        }

        this.state = ProcessInstanceState.SUSPENDED;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new ProcessInstanceSuspendedEvent(
                this.id,
                this.tenantId,
                this.updatedAt
        ));
    }

    // ========== 业务方法：恢复流程 ==========
    public void resume() {
        if (this.state != ProcessInstanceState.SUSPENDED) {
            throw new IllegalStateException("Only suspended process can be resumed");
        }

        ProcessInstanceState previousState = this.state;
        this.state = ProcessInstanceState.RUNNING;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new ProcessInstanceStateChangedEvent(
                this.id,
                this.processDefinitionKey,
                this.businessKey,
                this.tenantId,
                previousState,
                this.state,
                this.updatedAt
        ));
    }

    // ========== 业务方法：取消流程 ==========
    public void cancel(String reason) {
        if (this.state == ProcessInstanceState.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed process");
        }

        this.state = ProcessInstanceState.CANCELLED;
        this.endedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new ProcessInstanceCancelledEvent(
                this.id,
                this.processDefinitionKey,
                this.businessKey,
                this.tenantId,
                reason,
                this.endedAt
        ));
    }

    // ========== 业务方法：更新当前活动 ==========
    public void updateCurrentActivity(ActivityId activityId) {
        ActivityId previousActivity = this.currentActivityId;
        this.currentActivityId = activityId;
        this.updatedAt = LocalDateTime.now();

        // 状态变更时发布事件
        if (this.state == ProcessInstanceState.STARTED) {
            ProcessInstanceState previousState = this.state;
            this.state = ProcessInstanceState.RUNNING;

            registerDomainEvent(new ProcessInstanceStateChangedEvent(
                    this.id,
                    this.processDefinitionKey,
                    this.businessKey,
                    this.tenantId,
                    previousState,
                    this.state,
                    this.updatedAt
            ));
        }
    }

    // ========== 领域事件管理 ==========
    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(Objects.requireNonNull(event));
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // ========== 私有辅助方法 ==========
    private void validateState() {
        if (this.state == ProcessInstanceState.COMPLETED ||
                this.state == ProcessInstanceState.CANCELLED ||
                this.state == ProcessInstanceState.FAILED) {
            throw new IllegalStateException(
                    "Process is in terminal state: " + this.state);
        }
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
