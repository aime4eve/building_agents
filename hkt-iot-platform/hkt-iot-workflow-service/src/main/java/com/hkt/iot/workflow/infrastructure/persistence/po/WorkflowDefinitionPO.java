package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 流程定义持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wf_workflow_definition")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowDefinitionPO {

    @Id
    private String id;

    @Column(name = "definition_key", nullable = false, length = 255, unique = true)
    private String definitionKey;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "version", nullable = false, length = 50)
    private String version;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "created_by", nullable = false, length = 64)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Version
    private Long versionLock;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
