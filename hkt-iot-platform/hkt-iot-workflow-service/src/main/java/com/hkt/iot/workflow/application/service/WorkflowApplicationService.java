package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.command.*;
import com.hkt.iot.workflow.application.dto.ProcessInstanceDTO;
import com.hkt.iot.workflow.application.dto.TaskDTO;
import com.hkt.iot.workflow.application.query.ProcessInstanceQuery;
import com.hkt.iot.workflow.domain.model.aggregate.ProcessInstance;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.ProcessInstanceRepository;
import com.hkt.iot.workflow.infrastructure.camunda.CamundaProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.runtime.ProcessInstance as CamundaProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程应用服务
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowApplicationService {

    private final ProcessInstanceRepository processInstanceRepository;
    private final CamundaProcessService camundaProcessService;

    /**
     * 启动流程实例
     */
    @Transactional
    public ProcessInstanceDTO startProcess(StartProcessCommand command) {
        // 1. 检查业务键是否已存在
        BusinessKey businessKey = BusinessKey.of(command.getBusinessKey());
        if (processInstanceRepository.existsByBusinessKey(businessKey)) {
            throw new IllegalStateException("流程实例已存在：" + command.getBusinessKey());
        }

        // 2. 创建领域聚合根
        ProcessInstance instance = ProcessInstance.start(
                ProcessDefinitionKey.of(command.getProcessDefinitionKey()),
                businessKey,
                TenantId.of(command.getTenantId()),
                UserId.of(command.getStartedBy()),
                command.getVariables()
        );

        // 3. 保存到数据库
        ProcessInstance saved = processInstanceRepository.save(instance);

        // 4. 启动 Camunda 流程
        CamundaProcessInstance camundaInstance = camundaProcessService.startProcess(
                command.getProcessDefinitionKey(),
                command.getBusinessKey(),
                command.getVariables()
        );

        // 5. 更新当前活动
        saved.updateCurrentActivity(ActivityId.of(camundaInstance.getActivityId()));
        processInstanceRepository.save(saved);

        log.info("Started process: processInstanceId={}, businessKey={}", saved.getId().getValue(), command.getBusinessKey());

        // 6. 发布领域事件（由调用方处理）
        // saved.getDomainEvents().forEach(eventPublisher::publish);
        // saved.clearDomainEvents();

        return toDTO(saved);
    }

    /**
     * 查询流程实例
     */
    public ProcessInstanceDTO getProcessInstance(String processInstanceId) {
        ProcessInstance instance = processInstanceRepository.findById(ProcessInstanceId.of(processInstanceId))
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在：" + processInstanceId));
        return toDTO(instance);
    }

    /**
     * 根据业务键查询流程实例
     */
    public ProcessInstanceDTO getProcessInstanceByBusinessKey(String businessKey) {
        ProcessInstance instance = processInstanceRepository.findByBusinessKey(BusinessKey.of(businessKey))
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在：" + businessKey));
        return toDTO(instance);
    }

    /**
     * 查询流程实例列表
     */
    public List<ProcessInstanceDTO> listProcessInstances(ProcessInstanceQuery query) {
        List<ProcessInstance> instances;
        if (query.getTenantId() != null && query.getState() != null) {
            instances = processInstanceRepository.findByTenantIdAndState(
                    TenantId.of(query.getTenantId()),
                    ProcessInstanceState.valueOf(query.getState()));
        } else if (query.getTenantId() != null) {
            instances = processInstanceRepository.findByTenantId(TenantId.of(query.getTenantId()));
        } else if (query.getState() != null) {
            instances = processInstanceRepository.findByState(ProcessInstanceState.valueOf(query.getState()));
        } else {
            instances = processInstanceRepository.findByTenantId(TenantId.of(query.getTenantId()));
        }
        return instances.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 挂起流程
     */
    @Transactional
    public void suspendProcess(SuspendProcessCommand command) {
        ProcessInstance instance = processInstanceRepository.findById(ProcessInstanceId.of(command.getProcessInstanceId()))
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在：" + command.getProcessInstanceId()));
        instance.suspend();
        processInstanceRepository.save(instance);
        camundaProcessService.suspendProcess(command.getProcessInstanceId());
        log.info("Suspended process: processInstanceId={}", command.getProcessInstanceId());
    }

    /**
     * 恢复流程
     */
    @Transactional
    public void resumeProcess(ResumeProcessCommand command) {
        ProcessInstance instance = processInstanceRepository.findById(ProcessInstanceId.of(command.getProcessInstanceId()))
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在：" + command.getProcessInstanceId()));
        instance.resume();
        processInstanceRepository.save(instance);
        camundaProcessService.activateProcess(command.getProcessInstanceId());
        log.info("Resumed process: processInstanceId={}", command.getProcessInstanceId());
    }

    /**
     * 取消流程
     */
    @Transactional
    public void cancelProcess(CancelProcessCommand command) {
        ProcessInstance instance = processInstanceRepository.findById(ProcessInstanceId.of(command.getProcessInstanceId()))
                .orElseThrow(() -> new IllegalArgumentException("流程实例不存在：" + command.getProcessInstanceId()));
        instance.cancel(command.getReason());
        processInstanceRepository.save(instance);
        camundaProcessService.cancelProcess(command.getProcessInstanceId(), command.getReason());
        log.info("Cancelled process: processInstanceId={}, reason={}", command.getProcessInstanceId(), command.getReason());
    }

    /**
     * 转换为 DTO
     */
    private ProcessInstanceDTO toDTO(ProcessInstance instance) {
        return ProcessInstanceDTO.builder()
                .processInstanceId(instance.getId().getValue())
                .businessKey(instance.getBusinessKey().getValue())
                .processDefinitionKey(instance.getProcessDefinitionKey().getValue())
                .state(instance.getState().name())
                .tenantId(instance.getTenantId().getValue())
                .startedBy(instance.getStartedBy().getValue())
                .currentActivityId(instance.getCurrentActivityId() != null ?
                        instance.getCurrentActivityId().getValue() : null)
                .startedAt(instance.getStartedAt())
                .endedAt(instance.getEndedAt())
                .updatedAt(instance.getUpdatedAt())
                .build();
    }
}
