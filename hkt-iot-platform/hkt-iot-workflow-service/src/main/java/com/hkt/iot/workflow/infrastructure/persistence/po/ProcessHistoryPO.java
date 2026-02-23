package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程历史持久化对象
 */
@Data
@Entity
@Table(name = "wf_process_history")
public class ProcessHistoryPO {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "process_instance_id", length = 64, nullable = false)
    private String processInstanceId;

    @Column(name = "task_id", length = 64)
    private String taskId;

    @Column(name = "type", length = 30, nullable = false)
    private String type;

    @Column(name = "activity_id", length = 255)
    private String activityId;

    @Column(name = "activity_name", length = 255)
    private String activityName;

    @Column(name = "from_state", length = 50)
    private String fromState;

    @Column(name = "to_state", length = 50)
    private String toState;

    @Column(name = "operator_id", length = 64)
    private String operatorId;

    @Column(name = "operator_name", length = 100)
    private String operatorName;

    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables;

    @Column(name = "duration_millis")
    private Long durationMillis;

    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
