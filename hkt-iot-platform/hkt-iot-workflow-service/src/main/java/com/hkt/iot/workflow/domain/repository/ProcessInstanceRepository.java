package com.hkt.iot.workflow.domain.repository;

import com.hkt.iot.workflow.domain.model.aggregate.ProcessInstance;
import com.hkt.iot.workflow.domain.model.valueobject.*;

import java.util.List;
import java.util.Optional;

/**
 * 流程实例仓储接口
 *
 * @author HKT IoT Team
 */
public interface ProcessInstanceRepository {

    /**
     * 保存流程实例
     */
    ProcessInstance save(ProcessInstance processInstance);

    /**
     * 根据 ID 查找流程实例
     */
    Optional<ProcessInstance> findById(ProcessInstanceId id);

    /**
     * 根据业务键查找流程实例
     */
    Optional<ProcessInstance> findByBusinessKey(BusinessKey businessKey);

    /**
     * 根据租户 ID 查找流程实例列表
     */
    List<ProcessInstance> findByTenantId(TenantId tenantId);

    /**
     * 根据状态查找流程实例列表
     */
    List<ProcessInstance> findByState(ProcessInstanceState state);

    /**
     * 根据租户 ID 和状态查找流程实例列表
     */
    List<ProcessInstance> findByTenantIdAndState(TenantId tenantId, ProcessInstanceState state);

    /**
     * 删除流程实例
     */
    void delete(ProcessInstance processInstance);

    /**
     * 检查业务键是否已存在
     */
    boolean existsByBusinessKey(BusinessKey businessKey);
}
