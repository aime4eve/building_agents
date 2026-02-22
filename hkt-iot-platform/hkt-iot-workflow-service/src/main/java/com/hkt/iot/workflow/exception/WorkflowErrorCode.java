package com.hkt.iot.workflow.exception;

/**
 * 工作流错误码定义
 *
 * @author HKT IoT Team
 */
public enum WorkflowErrorCode {

    // 通用错误 (W01xx)
    UNKNOWN_ERROR("W0100", "未知错误"),
    PROCESS_NOT_FOUND("W0101", "流程定义不存在"),
    PROCESS_INSTANCE_NOT_FOUND("W0102", "流程实例不存在"),
    TASK_NOT_FOUND("W0103", "任务不存在"),
    VARIABLE_NOT_FOUND("W0104", "流程变量不存在"),

    // 规则引擎错误 (W02xx)
    RULE_ENGINE_ERROR("W0200", "规则引擎调用失败"),
    RULE_EVALUATION_FAILED("W0201", "规则评估失败"),
    RULE_NOT_FOUND("W0202", "规则不存在"),

    // 服务调用错误 (W03xx)
    SERVICE_CALL_ERROR("W0300", "服务调用失败"),
    SERVICE_METHOD_NOT_FOUND("W0301", "服务方法不存在"),
    SERVICE_TIMEOUT("W0302", "服务调用超时"),

    // 业务错误 (W04xx)
    WORK_ORDER_ALREADY_ASSIGNED("W0400", "工单已派发"),
    CONTRACT_ALREADY_SIGNED("W0401", "合同已签署"),
    ASSET_NOT_TRANSFERABLE("W0402", "资产不可调拨"),
    SPACE_NOT_AVAILABLE("W0403", "空间不可用"),

    // SLA错误 (W05xx)
    SLA_CONFIG_NOT_FOUND("W0500", "SLA配置不存在"),
    SLA_DEADLINE_EXCEEDED("W0501", "SLA截止时间已超期"),
    SLA_MONITORING_FAILED("W0502", "SLA监控失败"),

    // 通知错误 (W06xx)
    NOTIFICATION_SEND_FAILED("W0600", "通知发送失败"),
    NOTIFICATION_CONFIG_ERROR("W0601", "通知配置错误"),

    // 租户隔离错误 (W07xx)
    TENANT_ID_REQUIRED("W0700", "租户ID不能为空"),
    TENANT_ISOLATION_VIOLATION("W0701", "违反租户隔离规则"),
    TENANT_NOT_FOUND("W0702", "租户不存在");

    private final String code;
    private final String message;

    WorkflowErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
