package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.entity.SLAConfig;
import com.hkt.iot.workflow.domain.model.valueobject.ProcessDefinitionKey;
import com.hkt.iot.workflow.domain.model.valueobject.ActivityId;
import com.hkt.iot.workflow.domain.model.valueobject.TenantId;

import java.util.List;
import java.util.Optional;

/**
 * SLA 配置仓储接口
 *
 * @author HKT IoT Team
 */
public interface SLAConfigRepository {

    /**
     * 保存 SLA 配置
     */
    SLAConfig save(SLAConfig config);

    /**
     * 根据 ID 查找 SLA 配置
     */
    Optional<SLAConfig> findById(String id);

    /**
     * 根据流程定义键查找 SLA 配置
     */
    List<SLAConfig> findByProcessDefinitionKey(ProcessDefinitionKey processDefinitionKey);

    /**
     * 根据流程定义键和任务定义键查找 SLA 配置
     */
    Optional<SLAConfig> findByProcessDefinitionKeyAndTaskDefinitionKey(
            ProcessDefinitionKey processDefinitionKey,
            ActivityId taskDefinitionKey);

    /**
     * 根据租户 ID 查找 SLA 配置列表
     */
    List<SLAConfig> findByTenantId(TenantId tenantId);

    /**
     * 删除 SLA 配置
     */
    void delete(SLAConfig config);
}
