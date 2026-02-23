package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 工单持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wo_work_order")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderPO {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "work_order_no", nullable = false, unique = true, length = 32)
    private String workOrderNo;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "priority", nullable = false, length = 20)
    private String priority;

    @Column(name = "process_instance_id", length = 64)
    private String processInstanceId;

    @Column(name = "template_id", length = 64)
    private String templateId;

    @Column(name = "space_id", length = 64)
    private String spaceId;

    @Column(name = "reporter_id", length = 64)
    private String reporterId;

    @Column(name = "assignee_id", length = 64)
    private String assigneeId;

    @Column(name = "handler_id", length = 64)
    private String handlerId;

    @Column(name = "due_time")
    private LocalDateTime dueTime;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
