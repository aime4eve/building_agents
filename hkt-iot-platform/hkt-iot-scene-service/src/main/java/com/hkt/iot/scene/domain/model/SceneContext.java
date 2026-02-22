package com.hkt.iot.scene.domain.model;

import com.hkt.iot.domain.shared.DeviceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.domain.shared.UserId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 场景执行上下文
 *
 * 职责：携带场景执行时的上下文信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SceneContext {

    /**
     * 触发设备ID
     */
    private DeviceId deviceId;

    /**
     * 事件标识
     */
    private String eventIdentifier;

    /**
     * 触发用户ID
     */
    private UserId triggeredBy;

    /**
     * 触发时间
     */
    private LocalDateTime triggeredAt;

    /**
     * 触发租户ID
     */
    private TenantId tenantId;

    /**
     * 扩展参数
     */
    private Map<String, Object> parameters;
}
