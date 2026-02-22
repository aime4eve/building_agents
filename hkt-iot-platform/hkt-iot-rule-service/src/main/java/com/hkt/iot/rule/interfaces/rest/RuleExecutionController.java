package com.hkt.iot.rule.interfaces.rest;

import com.hkt.iot.rule.application.service.RuleApplicationService;
import com.hkt.iot.rule.domain.event.TelemetryReceivedEvent;
import com.hkt.iot.rule.domain.model.RuleExecutionResult;
import com.hkt.iot.rule.domain.service.RuleEventProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * 规则执行REST API控制器
 * 提供规则执行、测试和调试接口
 *
 * @author HKT IoT Team
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/rules/execution")
public class RuleExecutionController {

    private final RuleApplicationService ruleApplicationService;
    private final RuleEventProcessingService eventProcessingService;

    public RuleExecutionController(
            RuleApplicationService ruleApplicationService,
            RuleEventProcessingService eventProcessingService) {
        this.ruleApplicationService = ruleApplicationService;
        this.eventProcessingService = eventProcessingService;
    }

    /**
     * 手动执行规则
     */
    @PostMapping("/{ruleId}/execute")
    public RuleExecutionResult executeRule(
            @PathVariable Long ruleId,
            @RequestBody Map<String, Object> context) {
        log.info("手动执行规则: ruleId={}", ruleId);
        return ruleApplicationService.executeRule(ruleId, context);
    }

    /**
     * 测试规则（不更新统计）
     */
    @PostMapping("/{ruleId}/test")
    public RuleExecutionResult testRule(
            @PathVariable Long ruleId,
            @RequestBody Map<String, Object> context) {
        log.info("测试规则: ruleId={}", ruleId);
        return ruleApplicationService.testRule(ruleId, context);
    }

    /**
     * 模拟遥测数据触发规则
     */
    @PostMapping("/simulate-telemetry")
    public List<RuleExecutionResult> simulateTelemetry(
            @RequestBody TelemetrySimulationRequest request) {
        log.info("模拟遥测数据触发规则: tenantId={}, deviceId={}",
                request.getTenantId(), request.getDeviceId());

        TelemetryReceivedEvent event = new TelemetryReceivedEvent(
                request.getDeviceId(),
                request.getDeviceSn(),
                request.getDeviceType(),
                request.getTenantId(),
                request.getSpaceId(),
                request.getTelemetryData(),
                Instant.now(),
                request.getMetadata()
        );

        return eventProcessingService.processTelemetryEvent(event);
    }

    /**
     * 批量执行租户的激活规则
     */
    @PostMapping("/tenant/{tenantId}/execute-all")
    public List<RuleExecutionResult> executeAllActiveRules(
            @PathVariable Long tenantId,
            @RequestBody Map<String, Object> context) {
        log.info("批量执行租户激活规则: tenantId={}", tenantId);
        return eventProcessingService.executeAllActiveRules(tenantId, context);
    }

    /**
     * 验证规则表达式
     */
    @PostMapping("/validate-expression")
    public ValidationResult validateExpression(
            @RequestBody ValidateExpressionRequest request) {
        var result = ruleApplicationService.validateExpression(request.getExpression());
        return new ValidationResult(
                result.isValid(),
                result.getErrorMessage(),
                result.getErrors()
        );
    }

    /**
     * 提取表达式变量
     */
    @PostMapping("/extract-variables")
    public ExtractVariablesResponse extractVariables(
            @RequestBody ExtractVariablesRequest request) {
        var variables = ruleApplicationService.extractVariables(request.getExpression());
        return new ExtractVariablesResponse(variables);
    }

    // ==================== Request/Response DTO类 ====================

    /**
     * 遥测模拟请求
     */
    public static class TelemetrySimulationRequest {
        private String deviceId;
        private String deviceSn;
        private String deviceType;
        private Long tenantId;
        private Long spaceId;
        private Map<String, Object> telemetryData;
        private Map<String, Object> metadata;

        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        public String getDeviceSn() { return deviceSn; }
        public void setDeviceSn(String deviceSn) { this.deviceSn = deviceSn; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        public Long getTenantId() { return tenantId; }
        public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
        public Long getSpaceId() { return spaceId; }
        public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
        public Map<String, Object> getTelemetryData() { return telemetryData; }
        public void setTelemetryData(Map<String, Object> telemetryData) { this.telemetryData = telemetryData; }
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    /**
     * 验证表达式请求
     */
    public static class ValidateExpressionRequest {
        private String expression;

        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
    }

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final List<String> errors;

        public ValidationResult(boolean valid, String errorMessage, List<String> errors) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getErrors() { return errors; }
    }

    /**
     * 提取变量请求
     */
    public static class ExtractVariablesRequest {
        private String expression;

        public String getExpression() { return expression; }
        public void setExpression(String expression) { this.expression = expression; }
    }

    /**
     * 提取变量响应
     */
    public static class ExtractVariablesResponse {
        private final java.util.Set<String> variables;

        public ExtractVariablesResponse(java.util.Set<String> variables) {
            this.variables = variables;
        }

        public java.util.Set<String> getVariables() { return variables; }
    }
}
