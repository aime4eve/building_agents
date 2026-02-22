package com.hkt.iot.scene.domain.repository;

import com.hkt.iot.scene.domain.model.ExecutionId;
import com.hkt.iot.scene.domain.model.SceneExecutionLog;
import com.hkt.iot.scene.domain.model.SceneId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 场景执行日志仓储接口
 *
 * 职责：管理场景执行日志的持久化
 */
public interface SceneExecutionLogRepository {

    /**
     * 保存场景执行日志
     */
    SceneExecutionLog save(SceneExecutionLog log);

    /**
     * 根据ID查找场景执行日志
     */
    Optional<SceneExecutionLog> findById(ExecutionId id);

    /**
     * 根据场景ID查找执行日志列表
     */
    List<SceneExecutionLog> findByScene(SceneId sceneId);

    /**
     * 根据时间范围查找执行日志列表
     */
    List<SceneExecutionLog> findByTimeRange(LocalDateTime from, LocalDateTime to);

    /**
     * 根据场景ID和时间范围查找执行日志列表
     */
    List<SceneExecutionLog> findBySceneAndTimeRange(SceneId sceneId, LocalDateTime from, LocalDateTime to);

    /**
     * 根据执行结果查找执行日志列表
     */
    List<SceneExecutionLog> findByResult(com.hkt.iot.scene.domain.model.ExecutionResult result);

    /**
     * 删除执行日志
     */
    void delete(SceneExecutionLog log);

    /**
     * 根据ID删除执行日志
     */
    void deleteById(ExecutionId id);

    /**
     * 删除指定时间之前的日志
     */
    int deleteByCompletedAtBefore(LocalDateTime before);
}
