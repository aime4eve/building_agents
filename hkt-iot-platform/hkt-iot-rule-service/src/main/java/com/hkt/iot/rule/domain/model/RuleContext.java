package com.hkt.iot.rule.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则上下文
 * 用于规则执行时传递变量数据
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleContext {
    private final Map<String, Object> variables;
    private final LocalDateTime timestamp;
    private final String triggerSource;
    private final Long triggerDeviceId;

    private RuleContext(Map<String, Object> variables, LocalDateTime timestamp,
                       String triggerSource, Long triggerDeviceId) {
        this.variables = variables != null ? new HashMap<>(variables) : new HashMap<>();
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.triggerSource = triggerSource;
        this.triggerDeviceId = triggerDeviceId;
    }

    /**
     * 创建上下文
     */
    public static RuleContext of() {
        return new RuleContext(null, null, null, null);
    }

    /**
     * 创建上下文（带变量）
     */
    public static RuleContext of(Map<String, Object> variables) {
        return new RuleContext(variables, null, null, null);
    }

    /**
     * 创建上下文（完整参数）
     */
    public static RuleContext of(Map<String, Object> variables, String triggerSource, Long triggerDeviceId) {
        return new RuleContext(variables, LocalDateTime.now(), triggerSource, triggerDeviceId);
    }

    /**
     * 添加变量
     */
    public RuleContext put(String key, Object value) {
        this.variables.put(key, value);
        return this;
    }

    /**
     * 获取变量
     */
    public Object get(String key) {
        return variables.get(key);
    }

    /**
     * 获取变量（指定类型）
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = variables.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * 转换为Map（供DSL解析器使用）
     */
    public Map<String, Object> toMap() {
        return new HashMap<>(variables);
    }

    /**
     * 获取时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 获取触发源
     */
    public String getTriggerSource() {
        return triggerSource;
    }

    /**
     * 获取触发设备ID
     */
    public Long getTriggerDeviceId() {
        return triggerDeviceId;
    }

    /**
     * 检查是否包含变量
     */
    public boolean contains(String key) {
        return variables.containsKey(key);
    }
}
