package com.hkt.iot.scene.domain.model;

import com.hkt.iot.domain.shared.DeviceId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 场景执行动作实体
 *
 * 职责：定义场景触发后的执行动作
 * 支持的动作类型：
 * - DEVICE_CONTROL: 设备控制
 * - SCENE_SWITCH: 场景联动（触发其他场景）
 * - NOTIFY: 发送通知
 * - DELAY: 延迟执行
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneAction implements Comparable<SceneAction> {

    private ActionId id;
    private ActionType type;
    private DeviceId deviceId;
    private String serviceIdentifier;
    private Map<String, Object> params;
    private int delaySeconds;
    private int order;

    @Override
    public int compareTo(SceneAction other) {
        return Integer.compare(this.order, other.order);
    }
}
