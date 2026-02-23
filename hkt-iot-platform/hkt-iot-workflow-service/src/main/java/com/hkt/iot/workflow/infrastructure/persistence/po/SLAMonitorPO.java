package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SLA 监控记录持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wf_sla_monitor")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SLAMonitorPO {

    @Id
    private String id;

    @Column(name = "process_instance_id", nullable = false, length = 64)
    private String processInstanceId;

    @Column(name = "task_id", length = 64)
    private String taskId;

    @Column(name = "sla_config_id", nullable = false, length = 64)
    private String slaConfigId;

    @Column(name = "sla_deadline", nullable = false)
    private LocalDateTime slaDeadline;

    @Column(name = "response_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private String responseStatus;

    @Column(name = "resolution_status", length = 20)
    @Enumerated(EnumType.STRING)
    private String resolutionStatus;

    @Column(name = "actual_response_time")
    private LocalDateTime actualResponseTime;

    @Column(name = "actual_resolution_time")
    private LocalDateTime actualResolutionTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
