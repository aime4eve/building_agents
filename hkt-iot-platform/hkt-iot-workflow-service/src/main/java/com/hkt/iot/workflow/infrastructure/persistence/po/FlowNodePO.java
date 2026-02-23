package com.hkt.iot.workflow.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 流程节点持久化对象
 *
 * @author HKT IoT Team
 */
@Entity
@Table(name = "wf_flow_node")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "node_type", discriminatorType = DiscriminatorType.STRING)
public class FlowNodePO {

    @Id
    @Column(name = "id", nullable = false, length = 64)
    private String id;

    @Column(name = "node_key", nullable = false, length = 255)
    private String nodeKey;

    @Column(name = "node_name", nullable = false, length = 255)
    private String nodeName;

    @Column(name = "node_type", nullable = false, length = 50, insertable = false, updatable = false)
    private String nodeType;

    @Column(name = "workflow_definition_id", nullable = false, length = 64)
    private String workflowDefinitionId;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Column(name = "config", columnDefinition = "TEXT")
    private String config;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;
}
