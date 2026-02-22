package com.hkt.iot.scene.application.service;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.application.dto.*;
import com.hkt.iot.scene.domain.model.*;

import java.util.List;

/**
 * 场景应用服务
 *
 * 职责：协调领域对象完成业务用例
 */
public interface SceneApplicationService {

    /**
     * 创建场景
     */
    SceneDTO createScene(CreateSceneRequest request, TenantId tenantId);

    /**
     * 更新场景
     */
    SceneDTO updateScene(SceneId sceneId, UpdateSceneRequest request);

    /**
     * 删除场景
     */
    void deleteScene(SceneId sceneId);

    /**
     * 获取场景详情
     */
    SceneDTO getScene(SceneId sceneId);

    /**
     * 获取租户下的场景列表
     */
    List<SceneDTO> getScenesByTenant(TenantId tenantId);

    /**
     * 获取空间下的场景列表
     */
    List<SceneDTO> getScenesBySpace(SpaceId spaceId);

    /**
     * 激活场景
     */
    void activateScene(SceneId sceneId);

    /**
     * 停用场景
     */
    void deactivateScene(SceneId sceneId);

    /**
     * 添加触发条件
     */
    void addTrigger(SceneId sceneId, SceneTriggerDTO trigger);

    /**
     * 移除触发条件
     */
    void removeTrigger(SceneId sceneId, TriggerId triggerId);

    /**
     * 添加执行动作
     */
    void addAction(SceneId sceneId, SceneActionDTO action);

    /**
     * 移除执行动作
     */
    void removeAction(SceneId sceneId, ActionId actionId);

    /**
     * 手动执行场景
     */
    SceneExecutionResultDTO executeScene(SceneId sceneId, ExecuteSceneRequest request);

    /**
     * 获取场景执行日志
     */
    List<SceneExecutionLogDTO> getSceneExecutionLogs(SceneId sceneId, int page, int size);

    /**
     * 复制场景
     */
    SceneDTO copyScene(SceneId sceneId, SceneName newName, SceneCode newCode);
}
