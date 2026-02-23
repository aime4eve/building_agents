package com.hkt.iot.workflow.domain.model.entity;

import com.hkt.iot.workflow.domain.model.valueobject.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 服务调用节点实体
 *
 * @author HKT IoT Team
 */
@Getter
@NoArgsConstructor
public class ServiceNode extends FlowNode {

    private String serviceUrl;
    private HttpMethod httpMethod;
    private String parameterMapping;
    private Integer timeoutSeconds;

    private ServiceNode(
            FlowNodeId id,
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            String serviceUrl,
            HttpMethod httpMethod,
            String parameterMapping,
            Integer timeoutSeconds) {
        super(id, nodeKey, nodeName, FlowNodeType.SERVICE, workflowDefinitionId, order, config, tenantId);
        this.serviceUrl = serviceUrl;
        this.httpMethod = httpMethod;
        this.parameterMapping = parameterMapping;
        this.timeoutSeconds = timeoutSeconds;
    }

    public static ServiceNode create(
            FlowNodeKey nodeKey,
            String nodeName,
            WorkflowDefinitionId workflowDefinitionId,
            Integer order,
            String config,
            TenantId tenantId,
            String serviceUrl,
            HttpMethod httpMethod,
            String parameterMapping,
            Integer timeoutSeconds) {
        FlowNodeId id = FlowNodeId.generate();
        return new ServiceNode(id, nodeKey, nodeName, workflowDefinitionId, order, config, tenantId,
                serviceUrl, httpMethod, parameterMapping, timeoutSeconds);
    }

    public void updateServiceConfig(String serviceUrl, HttpMethod httpMethod, String parameterMapping) {
        this.serviceUrl = serviceUrl;
        this.httpMethod = httpMethod;
        this.parameterMapping = parameterMapping;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    public void updateTimeout(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.updatedAt = java.time.LocalDateTime.now();
    }

    /**
     * HTTP 方法枚举
     */
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH
    }
}
