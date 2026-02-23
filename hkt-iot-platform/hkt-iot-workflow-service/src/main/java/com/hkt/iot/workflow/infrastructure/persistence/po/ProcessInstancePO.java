package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 流程实例持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wf_process_instance")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInstancePO {

    @Id
    private String id;

    @Column(name = "business_key", nullable = false, length = 255)
    private String businessKey;

    @Column(name = "process_definition_key", nullable = false, length = 255)
    private String processDefinitionKey;

    @Column(name = "state", nullable = false, length = 20)
    private String state;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "started_by", nullable = false, length = 64)
    private String startedBy;

    @Column(name = "current_activity_id", length = 255)
    private String currentActivityId;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
