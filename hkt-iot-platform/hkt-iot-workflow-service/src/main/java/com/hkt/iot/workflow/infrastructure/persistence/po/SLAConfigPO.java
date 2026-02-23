package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * SLA 配置持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wf_sla_config")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SLAConfigPO {

    @Id
    private String id;

    @Column(name = "process_definition_key", nullable = false, length = 255)
    private String processDefinitionKey;

    @Column(name = "task_definition_key", length = 255)
    private String taskDefinitionKey;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "response_time_limit", nullable = false)
    private Long responseTimeLimit;

    @Column(name = "resolution_time_limit", nullable = false)
    private Long resolutionTimeLimit;

    @Column(name = "priority", length = 20)
    private String priority;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
