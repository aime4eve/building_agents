package com.hkt.iot.workflow.service;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程启动服务
 * 负责启动各种工作流程
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
public class ProcessStartService {

    @Autowired
    private RuntimeService runtimeService;

    /**
     * 启动物业维修工单流程
     */
    public ProcessInstance startPropertyRepairProcess(String tenantId, Long spaceId, 
            String repairType, String description, Long createdBy) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("tenantId", tenantId);
        variables.put("spaceId", spaceId);
        variables.put("repairType", repairType);
        variables.put("description", description);
        variables.put("createdBy", createdBy);
        variables.put("startTime", LocalDateTime.now().toString());
        
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
            "propertyRepairProcess",
            tenantId + "-" + System.currentTimeMillis(),
            variables
        );
        
        log.info("Started property repair process: instanceId={}, tenantId={}, spaceId={}", 
            instance.getId(), tenantId, spaceId);
        
        return instance;
    }

    /**
     * 启动租赁合同审批流程
     */
    public ProcessInstance startLeaseApprovalProcess(String tenantId, Long contractId,
            String contractType, Double amount, Long createdBy) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("tenantId", tenantId);
        variables.put("contractId", contractId);
        variables.put("contractType", contractType);
        variables.put("amount", amount);
        variables.put("createdBy", createdBy);
        variables.put("startTime", LocalDateTime.now().toString());
        
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
            "leaseApprovalProcess",
            "lease-" + contractId,
            variables
        );
        
        log.info("Started lease approval process: instanceId={}, contractId={}", 
            instance.getId(), contractId);
        
        return instance;
    }

    /**
     * 启动资产调拨流程
     */
    public ProcessInstance startAssetTransferProcess(String tenantId, Long assetId,
            String transferType, Long fromSpaceId, Long toSpaceId, Long createdBy) {
        
        Map<String, Object> variables = new HashMap<>();
        variables.put("tenantId", tenantId);
        variables.put("assetId", assetId);
        variables.put("transferType", transferType);
        variables.put("fromSpaceId", fromSpaceId);
        variables.put("toSpaceId", toSpaceId);
        variables.put("createdBy", createdBy);
        variables.put("startTime", LocalDateTime.now().toString());
        
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
            "assetTransferProcess",
            "transfer-" + assetId + "-" + System.currentTimeMillis(),
            variables
        );
        
        log.info("Started asset transfer process: instanceId={}, assetId={}", 
            instance.getId(), assetId);
        
        return instance;
    }

    /**
     * 通用流程启动方法
     */
    public ProcessInstance startProcess(String processKey, String businessKey,
            Map<String, Object> variables) {
        
        if (variables == null) {
            variables = new HashMap<>();
        }
        
        // 添加启动时间
        variables.put("processStartTime", LocalDateTime.now().toString());
        
        ProcessInstance instance = runtimeService.startProcessInstanceByKey(
            processKey,
            businessKey,
            variables
        );
        
        log.info("Started process: processKey={}, instanceId={}, businessKey={}", 
            processKey, instance.getId(), businessKey);
        
        return instance;
    }
}
