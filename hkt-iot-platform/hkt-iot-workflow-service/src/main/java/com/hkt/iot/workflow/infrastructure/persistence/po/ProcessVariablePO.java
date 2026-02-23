package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程变量持久化对象
 */
@Data
@Entity
@Table(name = "wf_process_variable")
public class ProcessVariablePO {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "process_instance_id", length = 64, nullable = false)
    private String processInstanceId;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "value", columnDefinition = "TEXT")
    private String value;

    @Column(name = "type", length = 20, nullable = false)
    private String type;

    @Column(name = "tenant_id", length = 64, nullable = false)
    private String tenantId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version_lock")
    private Long versionLock;
}
