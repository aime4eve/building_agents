package com.hkt.iot.workflow.infrastructure.persistence.jpa;

import com.hkt.iot.workflow.infrastructure.persistence.po.SLAConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * SLA 配置 JPA Repository
 *
 * @author HKT IoT Team
 */
@Repository
public interface SLAConfigJpaRepository extends JpaRepository<SLAConfigPO, String> {

    /**
     * 根据流程定义键查找 SLA 配置
     */
    List<SLAConfigPO> findByProcessDefinitionKey(String processDefinitionKey);

    /**
     * 根据流程定义键和任务定义键查找 SLA 配置
     */
    Optional<SLAConfigPO> findByProcessDefinitionKeyAndTaskDefinitionKey(
            String processDefinitionKey,
            String taskDefinitionKey);

    /**
     * 根据租户 ID 查找 SLA 配置列表
     */
    List<SLAConfigPO> findByTenantId(String tenantId);
}
