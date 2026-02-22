package com.hkt.iot.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统统一的错误码
 *
 * @author HKT IoT Team
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用错误码 1xxx ==========
    SUCCESS("0000", "成功"),
    SYSTEM_ERROR("1000", "系统错误"),
    PARAM_ERROR("1001", "参数错误"),
    PARAM_MISSING("1002", "缺少必要参数"),
    PARAM_INVALID("1003", "参数格式不合法"),
    OPERATION_FAILED("1004", "操作失败"),

    // ========== 认证授权错误码 2xxx ==========
    UNAUTHORIZED("2001", "未认证"),
    TOKEN_EXPIRED("2002", "令牌已过期"),
    TOKEN_INVALID("2003", "令牌无效"),
    PERMISSION_DENIED("2004", "权限不足"),
    ACCOUNT_LOCKED("2005", "账户已锁定"),
    ACCOUNT_DISABLED("2006", "账户已禁用"),

    // ========== 业务错误码 3xxx ==========
    RESOURCE_NOT_FOUND("3001", "资源不存在"),
    RESOURCE_ALREADY_EXISTS("3002", "资源已存在"),
    RESOURCE_CONFLICT("3003", "资源冲突"),
    OPERATION_NOT_ALLOWED("3004", "不允许的操作"),
    STATE_ERROR("3005", "状态错误"),

    // ========== 租户/用户错误码 31xx ==========
    TENANT_NOT_FOUND("3101", "租户不存在"),
    TENANT_DISABLED("3102", "租户已禁用"),
    TENANT_CODE_DUPLICATE("3103", "租户编码已存在"),
    USER_NOT_FOUND("3104", "用户不存在"),
    USER_PASSWORD_ERROR("3105", "用户密码错误"),
    USER_DUPLICATE("3106", "用户已存在"),

    // ========== 设备错误码 32xx ==========
    DEVICE_NOT_FOUND("3201", "设备不存在"),
    DEVICE_OFFLINE("3202", "设备离线"),
    DEVICE_BUSY("3203", "设备忙碌"),
    DEVICE_CONTROL_FAILED("3204", "设备控制失败"),
    DEVICE_TYPE_NOT_MATCH("3205", "设备类型不匹配"),
    THING_MODEL_NOT_FOUND("3206", "物模型不存在"),

    // ========== 空间错误码 33xx ==========
    SPACE_NOT_FOUND("3301", "空间不存在"),
    SPACE_HAS_DEVICES("3302", "空间下存在设备，无法删除"),
    SPACE_PARENT_INVALID("3303", "父空间无效"),
    SPACE_LOOP_DETECTED("3304", "检测到空间循环引用"),

    // ========== 规则引擎错误码 34xx ==========
    RULE_NOT_FOUND("3401", "规则不存在"),
   _RULE_DISABLED("3402", "规则已禁用"),
    RULE_PARSE_ERROR("3403", "规则解析错误"),
    RULE_EXECUTE_ERROR("3404", "规则执行错误"),
    ACTION_EXECUTE_FAILED("3405", "动作执行失败"),

    // ========== 订单/订阅错误码 35xx ==========
    ORDER_NOT_FOUND("3501", "订单不存在"),
    ORDER_STATUS_ERROR("3502", "订单状态错误"),
    PAYMENT_FAILED("3503", "支付失败"),
    SUBSCRIPTION_NOT_FOUND("3504", "订阅不存在"),
    QUOTA_EXCEEDED("3505", "配额已用尽"),

    // ========== 防霉管控错误码 36xx ==========
    MOLD_ZONE_NOT_FOUND("3601", "防霉管控区域不存在"),
    MOLD_SENSOR_OFFLINE("3602", "防霉传感器离线"),
    MOLD_HUMIDITY_HIGH("3603", "湿度过高"),

    // ========== 水资源检测错误码 37xx ==========
    WATER_LEAK_DETECTED("3701", "检测到水资源渗漏"),
    WATER_SENSOR_OFFLINE("3702", "水资源传感器离线"),

    // ========== 数据库错误码 5xxx ==========
    DB_ERROR("5001", "数据库错误"),
    DB_DUPLICATE_KEY("5002", "数据重复"),
    DB_OPTIMISTIC_LOCK_FAILED("5003", "乐观锁冲突，数据已被其他用户修改"),

    // ========== 外部服务错误码 6xxx ==========
    EXTERNAL_SERVICE_ERROR("6001", "外部服务错误"),
    MQTT_PUBLISH_FAILED("6002", "MQTT消息发布失败"),
    MESSAGE_SEND_FAILED("6003", "消息发送失败");

    /**
     * 错误码
     */
    private final String code;

    /**
     * 错误消息
     */
    private final String message;
}
