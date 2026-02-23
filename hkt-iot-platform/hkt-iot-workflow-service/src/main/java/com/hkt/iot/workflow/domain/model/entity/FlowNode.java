package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.domain.model.Entity;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 流程节点实体基类
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class FlowNode extends Entity<String> {

    protected FlowNodeId id;
    protected FlowNodeKey nodeKey;
    protected String nodeName;
    protected FlowNodeType nodeType;
    protected WorkflowDefinitionId workflowDefinitionId;
    protected Integer order;
    protected String config;
    protected TenantId tenantId;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    protected FlowNode(
            FlowNodeId id,
            FlowNodeKey nodeKey,
            String nodeName,
            FlowNodeType nodeType,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId) {
        this.id = id;
        this.nodeKey = nodeKey;
        this.nodeName = nodeName;
        this.nodeType = nodeType;
        this.workflowDefinitionId = workflowDefinitionId;
        this.order = order;
        this.config = config;
        this.tenantId = tenantId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static FlowNode create(
            FlowNodeKey nodeKey,
            String nodeName,
            FlowNodeType nodeType,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId) {
        FlowNodeId id = FlowNodeId.generate();
        return new FlowNode(id, nodeKey, nodeName, nodeType, workflowDefinitionId, order, config, tenantId);
    }

    public void updateConfig(String config) {
        this.config = config;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateOrder(Integer order) {
        this.order = order;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String getId() {
        return id != null ? id.getValue() : null;
    }
}
