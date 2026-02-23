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
 * 流程定义聚合根
 * 管理流程定义的生命周期，包括创建、发布、归档等操作
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class WorkflowDefinition extends AggregateRoot<String> {

    private WorkflowDefinitionId id;

    private WorkflowDefinitionKey key;

    private String name;

    private String version;

    private WorkflowDefinitionStatus status;

    private String description;

    private TenantId tenantId;

    private UserId createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    private Long versionLock;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public static WorkflowDefinition create(
            WorkflowDefinitionKey key,
            String name,
            String version,
            String description,
            TenantId tenantId,
            UserId createdBy) {
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.id = WorkflowDefinitionId.generate();
        definition.key = Objects.requireNonNull(key, "WorkflowDefinitionKey cannot be null");
        definition.name = Objects.requireNonNull(name, "Name cannot be null");
        definition.version = Objects.requireNonNull(version, "Version cannot be null");
        definition.description = description;
        definition.tenantId = Objects.requireNonNull(tenantId, "TenantId cannot be null");
        definition.createdBy = Objects.requireNonNull(createdBy, "CreatedBy cannot be null");
        definition.status = WorkflowDefinitionStatus.DRAFT;
        definition.createdAt = LocalDateTime.now();
        definition.updatedAt = LocalDateTime.now();
        definition.versionLock = 0L;

        definition.registerDomainEvent(new WorkflowDefinitionCreatedEvent(
                definition.id,
                definition.key,
                definition.name,
                definition.version,
                definition.tenantId,
                definition.createdBy,
                definition.createdAt
        ));

        return definition;
    }

    public void publish(UserId publishedBy) {
        if (this.status != WorkflowDefinitionStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT workflow definition can be published, current status: " + this.status);
        }

        this.status = WorkflowDefinitionStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkflowDefinitionPublishedEvent(
                this.id,
                this.key,
                this.name,
                this.version,
                this.tenantId,
                publishedBy,
                this.publishedAt
        ));
    }

    public void archive(UserId archivedBy) {
        if (this.status != WorkflowDefinitionStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED workflow definition can be archived, current status: " + this.status);
        }

        this.status = WorkflowDefinitionStatus.ARCHIVED;
        this.updatedAt = LocalDateTime.now();

        registerDomainEvent(new WorkflowDefinitionArchivedEvent(
                this.id,
                this.key,
                this.name,
                this.version,
                this.tenantId,
                archivedBy,
                this.updatedAt
        ));
    }

    public void updateVersion(String newVersion) {
        if (this.status == WorkflowDefinitionStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot update version of ARCHIVED workflow definition");
        }

        this.version = Objects.requireNonNull(newVersion, "Version cannot be null");
        this.updatedAt = LocalDateTime.now();
    }

    public void updateInfo(String name, String description) {
        if (this.status == WorkflowDefinitionStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot update ARCHIVED workflow definition");
        }

        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    protected void registerDomainEvent(DomainEvent event) {
        this.domainEvents.add(Objects.requireNonNull(event));
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
