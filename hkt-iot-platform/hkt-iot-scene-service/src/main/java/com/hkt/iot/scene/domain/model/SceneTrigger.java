package com.hkt.iot.scene.domain.model;

import com.hkt.iot.domain.shared.DeviceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 场景触发条件实体
 *
 * 职责：定义场景的触发条件
 * 支持的触发类型：
 * - DEVICE_EVENT: 设备事件触发
 * - TIME: 时间条件触发
 * - MANUAL: 手动触发
 * - CONDITION: 条件表达式触发
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneTrigger implements Comparable<SceneTrigger> {

    private TriggerId id;
    private TriggerType type;
    private String condition;
    private DeviceId deviceId;
    private String eventIdentifier;
    private int priority;
    private Map<String, Object> parameters;

    /**
     * 判断触发条件是否匹配
     */
    public boolean matches(SceneContext context) {
        if (this.type == TriggerType.DEVICE_EVENT && this.deviceId != null) {
            return this.deviceId.equals(context.getDeviceId())
                    && (this.eventIdentifier == null
                        || this.eventIdentifier.equals(context.getEventIdentifier()));
        }

        if (this.type == TriggerType.CONDITION && this.condition != null) {
            // 简化实现，实际应使用SpEL或其他表达式引擎
            return evaluateCondition(this.condition, context);
        }

        return this.type == TriggerType.MANUAL;
    }

    private boolean evaluateCondition(String condition, SceneContext context) {
        // TODO: 实现条件表达式解析和评估
        // 可以使用SpEL、MVEL或其他表达式引擎
        return true;
    }

    @Override
    public int compareTo(SceneTrigger other) {
        return Integer.compare(other.priority, this.priority); // 优先级高的排在前面
    }
}
