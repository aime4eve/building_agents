package com.hkt.iot.workflow.application.service;

import com.hkt.iot.workflow.application.command.AssignWorkOrderCommand;
import com.hkt.iot.workflow.application.command.CompleteWorkOrderCommand;
import com.hkt.iot.workflow.application.command.CreateWorkOrderCommand;
import com.hkt.iot.workflow.application.dto.WorkOrderDTO;
import com.hkt.iot.workflow.application.query.WorkOrderQuery;
import com.hkt.iot.workflow.domain.model.aggregate.WorkOrder;
import com.hkt.iot.workflow.domain.model.entity.AutoAssignRule;
import com.hkt.iot.workflow.domain.model.valueobject.*;
import com.hkt.iot.workflow.domain.repository.AutoAssignRuleRepository;
import com.hkt.iot.workflow.domain.repository.WorkOrderRepository;
import com.hkt.iot.workflow.exception.BusinessException;
import com.hkt.iot.workflow.exception.WorkflowErrorCode;
import com.hkt.iot.workflow.infrastructure.messaging.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 工单应用服务
 *
 * @author HKT IoT Team
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class WorkOrderApplicationService {

    private final WorkOrderRepository workOrderRepository;
    private final AutoAssignRuleRepository autoAssignRuleRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public WorkOrderDTO createWorkOrder(CreateWorkOrderCommand command) {
        log.info("Creating work order: title={}, type={}, tenantId={}", 
                command.getTitle(), command.getType(), command.getTenantId());

        WorkOrder workOrder = WorkOrder.create(
                command.getTitle(),
                command.getDescription(),
                WorkOrderType.valueOf(command.getType()),
                command.getPriority() != null ? WorkOrderPriority.valueOf(command.getPriority()) : WorkOrderPriority.NORMAL,
                command.getTemplateId(),
                command.getSpaceId(),
                command.getReporterId() != null ? UserId.of(command.getReporterId()) : null,
                TenantId.of(command.getTenantId())
        );

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order created: id={}, workOrderNo={}", 
                savedWorkOrder.getId(), savedWorkOrder.getWorkOrderNo().getValue());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO assignWorkOrder(AssignWorkOrderCommand command) {
        log.info("Assigning work order: workOrderId={}, assigneeId={}", 
                command.getWorkOrderId(), command.getAssigneeId());

        WorkOrder workOrder = findWorkOrderById(command.getWorkOrderId());
        
        workOrder.assign(
                UserId.of(command.getAssigneeId()),
                command.getAssignedBy() != null ? UserId.of(command.getAssignedBy()) : null
        );

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order assigned: workOrderId={}, assigneeId={}", 
                savedWorkOrder.getId(), savedWorkOrder.getAssigneeId().getValue());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO autoAssignWorkOrder(String workOrderId) {
        log.info("Auto assigning work order: workOrderId={}", workOrderId);

        WorkOrder workOrder = findWorkOrderById(workOrderId);
        
        List<AutoAssignRule> rules = autoAssignRuleRepository.findEnabledByWorkOrderType(workOrder.getType());
        
        if (rules.isEmpty()) {
            log.warn("No auto assign rules found for work order type: {}", workOrder.getType());
            workOrder.autoAssign(null);
        } else {
            AutoAssignRule selectedRule = selectBestRule(rules);
            UserId assigneeId = determineAssignee(selectedRule);
            
            if (assigneeId != null) {
                workOrder.assign(assigneeId, null);
            } else {
                workOrder.autoAssign(null);
            }
        }

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order auto assigned: workOrderId={}", savedWorkOrder.getId());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO startProcessing(String workOrderId, String handlerId) {
        log.info("Starting work order processing: workOrderId={}, handlerId={}", workOrderId, handlerId);

        WorkOrder workOrder = findWorkOrderById(workOrderId);
        
        workOrder.startProcess(UserId.of(handlerId));

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order processing started: workOrderId={}", savedWorkOrder.getId());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO submitForConfirmation(String workOrderId) {
        log.info("Submitting work order for confirmation: workOrderId={}", workOrderId);

        WorkOrder workOrder = findWorkOrderById(workOrderId);
        
        workOrder.submitForConfirmation();

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order submitted for confirmation: workOrderId={}", savedWorkOrder.getId());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO completeWorkOrder(CompleteWorkOrderCommand command) {
        log.info("Completing work order: workOrderId={}", command.getWorkOrderId());

        WorkOrder workOrder = findWorkOrderById(command.getWorkOrderId());
        
        workOrder.complete();

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order completed: workOrderId={}", savedWorkOrder.getId());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO cancelWorkOrder(String workOrderId, String cancelledBy, String reason) {
        log.info("Cancelling work order: workOrderId={}, cancelledBy={}", workOrderId, cancelledBy);

        WorkOrder workOrder = findWorkOrderById(workOrderId);
        
        workOrder.cancel(
                cancelledBy != null ? UserId.of(cancelledBy) : null,
                reason
        );

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order cancelled: workOrderId={}", savedWorkOrder.getId());

        return toDTO(savedWorkOrder);
    }

    @Transactional
    public WorkOrderDTO rejectWorkOrder(String workOrderId, String reason) {
        log.info("Rejecting work order: workOrderId={}", workOrderId);

        WorkOrder workOrder = findWorkOrderById(workOrderId);
        
        workOrder.reject(reason);

        WorkOrder savedWorkOrder = workOrderRepository.save(workOrder);
        
        publishDomainEvents(savedWorkOrder);

        log.info("Work order rejected: workOrderId={}", savedWorkOrder.getId());

        return toDTO(savedWorkOrder);
    }

    public WorkOrderDTO getWorkOrder(String workOrderId) {
        WorkOrder workOrder = findWorkOrderById(workOrderId);
        return toDTO(workOrder);
    }

    public WorkOrderDTO getWorkOrderByNo(String workOrderNo) {
        WorkOrder workOrder = workOrderRepository.findByWorkOrderNo(WorkOrderNo.of(workOrderNo))
                .orElseThrow(() -> new BusinessException(WorkflowErrorCode.WORK_ORDER_NOT_FOUND, 
                        "Work order not found: " + workOrderNo));
        return toDTO(workOrder);
    }

    public List<WorkOrderDTO> listWorkOrders(WorkOrderQuery query) {
        List<WorkOrder> workOrders;

        if (query.getAssigneeId() != null) {
            workOrders = workOrderRepository.findByAssigneeId(UserId.of(query.getAssigneeId()));
        } else if (query.getReporterId() != null) {
            workOrders = workOrderRepository.findByReporterId(UserId.of(query.getReporterId()));
        } else if (query.getTenantId() != null && query.getStatus() != null) {
            workOrders = workOrderRepository.findByTenantIdAndStatus(
                    TenantId.of(query.getTenantId()), 
                    WorkOrderStatus.valueOf(query.getStatus())
            );
        } else if (query.getTenantId() != null) {
            workOrders = workOrderRepository.findByTenantId(TenantId.of(query.getTenantId()));
        } else if (Boolean.TRUE.equals(query.getOverdue())) {
            workOrders = workOrderRepository.findOverdue();
        } else {
            workOrders = workOrderRepository.findByStatus(WorkOrderStatus.CREATED);
        }

        return workOrders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<WorkOrderDTO> listOverdueWorkOrders() {
        return workOrderRepository.findOverdue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private WorkOrder findWorkOrderById(String workOrderId) {
        return workOrderRepository.findById(WorkOrderId.of(workOrderId))
                .orElseThrow(() -> new BusinessException(WorkflowErrorCode.WORK_ORDER_NOT_FOUND, 
                        "Work order not found: " + workOrderId));
    }

    private AutoAssignRule selectBestRule(List<AutoAssignRule> rules) {
        return rules.stream()
                .filter(rule -> Boolean.TRUE.equals(rule.getEnabled()))
                .max((r1, r2) -> Integer.compare(
                        r1.getPriority() != null ? r1.getPriority() : 0,
                        r2.getPriority() != null ? r2.getPriority() : 0))
                .orElse(null);
    }

    private UserId determineAssignee(AutoAssignRule rule) {
        if (rule == null || rule.getRuleConfig() == null) {
            return null;
        }
        
        return switch (rule.getRuleType()) {
            case SKILL -> determineAssigneeBySkill(rule.getRuleConfig());
            case AREA -> determineAssigneeByArea(rule.getRuleConfig());
            case WORKLOAD -> determineAssigneeByWorkload(rule.getRuleConfig());
        };
    }

    private UserId determineAssigneeBySkill(String ruleConfig) {
        return null;
    }

    private UserId determineAssigneeByArea(String ruleConfig) {
        return null;
    }

    private UserId determineAssigneeByWorkload(String ruleConfig) {
        return null;
    }

    private void publishDomainEvents(WorkOrder workOrder) {
        workOrder.getDomainEvents().forEach(event -> {
            domainEventPublisher.publish(event);
            log.debug("Published domain event: {}", event.getEventType());
        });
        workOrder.clearDomainEvents();
    }

    private WorkOrderDTO toDTO(WorkOrder workOrder) {
        return WorkOrderDTO.builder()
                .id(workOrder.getId())
                .workOrderNo(workOrder.getWorkOrderNo().getValue())
                .title(workOrder.getTitle())
                .description(workOrder.getDescription())
                .type(workOrder.getType().name())
                .typeDesc(workOrder.getType().getDescription())
                .status(workOrder.getStatus().name())
                .statusDesc(workOrder.getStatus().getDescription())
                .priority(workOrder.getPriority().name())
                .priorityDesc(workOrder.getPriority().getDescription())
                .processInstanceId(workOrder.getProcessInstanceId())
                .templateId(workOrder.getTemplateId())
                .spaceId(workOrder.getSpaceId())
                .reporterId(workOrder.getReporterId() != null ? workOrder.getReporterId().getValue() : null)
                .assigneeId(workOrder.getAssigneeId() != null ? workOrder.getAssigneeId().getValue() : null)
                .handlerId(workOrder.getHandlerId() != null ? workOrder.getHandlerId().getValue() : null)
                .dueTime(workOrder.getDueTime())
                .completedAt(workOrder.getCompletedAt())
                .tenantId(workOrder.getTenantId().getValue())
                .createdAt(workOrder.getCreatedAt())
                .updatedAt(workOrder.getUpdatedAt())
                .overdue(workOrder.isOverdue())
                .build();
    }
}
