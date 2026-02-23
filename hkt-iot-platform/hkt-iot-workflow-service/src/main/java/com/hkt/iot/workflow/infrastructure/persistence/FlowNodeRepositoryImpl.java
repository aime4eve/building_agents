package com.hkt.iot.workflow.infrastructure.persistence;

import com.hkt.iot.workflow.domain.model.entity.FlowNode;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.FlowNodeRepository;
import com.hkt.iot.workflow.infrastructure.persistence.jpa.FlowNodeJpaRepository;
import com.hkt.iot.workflow.infrastructure.persistence.mapper.FlowNodeMapper;
import com.hkt.iot.workflow.infrastructure.persistence.po.FlowNodePO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 流程节点仓储实现
 *
 * @author HKT IoT Team
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class FlowNodeRepositoryImpl implements FlowNodeRepository {

    private final FlowNodeJpaRepository jpaRepository;
    private final FlowNodeMapper mapper;

    @Override
    @Transactional
    public FlowNode save(FlowNode flowNode) {
        FlowNodePO po = mapper.toPO(flowNode);
        FlowNodePO savedPO = jpaRepository.save(po);
        log.debug("Saved FlowNode: id={}, nodeKey={}, nodeType={}", 
                savedPO.getId(), savedPO.getNodeKey(), savedPO.getNodeType());
        return mapper.toDomain(savedPO);
    }

    @Override
    public Optional<FlowNode> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<FlowNode> findByWorkflowDefinitionId(WorkflowDefinitionId workflowDefinitionId) {
        return jpaRepository.findByWorkflowDefinitionId(workflowDefinitionId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FlowNode> findByWorkflowDefinitionIdAndNodeKey(
            WorkflowDefinitionId workflowDefinitionId,
            FlowNodeKey nodeKey) {
        return jpaRepository.findByWorkflowDefinitionIdAndNodeKey(
                        workflowDefinitionId.getValue(),
                        nodeKey.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<FlowNode> findByNodeKey(FlowNodeKey nodeKey) {
        return jpaRepository.findByNodeKey(nodeKey.getValue())
                .map(mapper::toDomain);
    }

    @Override
    public List<FlowNode> findByTenantId(TenantId tenantId) {
        return jpaRepository.findByTenantId(tenantId.getValue()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(FlowNode flowNode) {
        FlowNodePO po = mapper.toPO(flowNode);
        jpaRepository.delete(po);
        log.debug("Deleted FlowNode: id={}", flowNode.getId());
    }

    @Override
    @Transactional
    public void deleteByWorkflowDefinitionId(WorkflowDefinitionId workflowDefinitionId) {
        jpaRepository.deleteByWorkflowDefinitionId(workflowDefinitionId.getValue());
        log.debug("Deleted all FlowNodes for workflowDefinitionId={}", workflowDefinitionId.getValue());
    }
}
