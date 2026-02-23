package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 工作流任务持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wf_task")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowTaskPO {

    @Id
    private String id;

    @Column(name = "process_instance_id", nullable = false, length = 64)
    private String processInstanceId;

    @Column(name = "task_definition_key", nullable = false, length = 255)
    private String taskDefinitionKey;

    @Column(name = "task_name", nullable = false, length = 255)
    private String taskName;

    @Column(name = "task_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private String taskType;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private String status;

    @Column(name = "assignee", length = 64)
    private String assignee;

    @Column(name = "candidate_groups", columnDefinition = "TEXT")
    private String candidateGroups;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Version
    private Long version;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
