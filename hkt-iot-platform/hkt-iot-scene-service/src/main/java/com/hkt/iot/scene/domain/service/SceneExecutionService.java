package com.hkt.iot.scene.domain.service;

import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.domain.model.*;

/**
 * 场景执行领域服务接口
 *
 * 职责：处理场景的执行逻辑
 */
public interface SceneExecutionService {

    /**
     * 执行场景
     *
     * @param sceneId 场景ID
     * @param context 执行上下文
     * @return 执行结果
     */
    SceneExecutionResult execute(SceneId sceneId, SceneContext context);

    /**
     * 手动触发场景
     *
     * @param sceneId 场景ID
     * @param triggeredBy 触发用户
     * @return 执行结果
     */
    SceneExecutionResult trigger(SceneId sceneId, com.hkt.iot.domain.shared.UserId triggeredBy);

    /**
     * 检查触发条件是否匹配
     *
     * @param sceneId 场景ID
     * @param trigger 触发条件
     * @param context 上下文
     * @return 是否匹配
     */
    boolean checkTrigger(SceneId sceneId, SceneTrigger trigger, SceneContext context);

    /**
     * 处理设备事件触发
     *
     * @param tenantId 租户ID
     * @param deviceId 设备ID
     * @param eventIdentifier 事件标识
     * @param eventData 事件数据
     * @return 触发的场景执行结果列表
     */
    java.util.List<SceneExecutionResult> handleDeviceEvent(
            TenantId tenantId,
            com.hkt.iot.domain.shared.DeviceId deviceId,
            String eventIdentifier,
            java.util.Map<String, Object> eventData
    );

    /**
     * 批量执行场景
     *
     * @param sceneIds 场景ID列表
     * @param context 执行上下文
     * @return 执行结果列表
     */
    java.util.List<SceneExecutionResult> batchExecute(
            java.util.List<SceneId> sceneIds,
            SceneContext context
    );
}
