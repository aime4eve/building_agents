package com.hkt.iot.scene.domain.repository;

import com.hkt.iot.domain.shared.SpaceId;
import com.hkt.iot.domain.shared.TenantId;
import com.hkt.iot.scene.domain.model.Scene;
import com.hkt.iot.scene.domain.model.SceneCode;
import com.hkt.iot.scene.domain.model.SceneId;
import com.hkt.iot.scene.domain.model.SceneStatus;

import java.util.List;
import java.util.Optional;

/**
 * 场景仓储接口
 *
 * 职责：管理场景聚合根的持久化
 */
public interface SceneRepository {

    /**
     * 保存场景
     */
    Scene save(Scene scene);

    /**
     * 根据ID查找场景
     */
    Optional<Scene> findById(SceneId id);

    /**
     * 根据编码查找场景
     */
    Optional<Scene> findByCode(SceneCode code);

    /**
     * 根据租户查找场景列表
     */
    List<Scene> findByTenant(TenantId tenantId);

    /**
     * 根据租户和状态查找场景列表
     */
    List<Scene> findByTenantAndStatus(TenantId tenantId, SceneStatus status);

    /**
     * 根据空间查找场景列表
     */
    List<Scene> findBySpace(SpaceId spaceId);

    /**
     * 根据状态查找场景列表
     */
    List<Scene> findByStatus(SceneStatus status);

    /**
     * 查找租户下激活的场景列表
     */
    List<Scene> findActiveScenes(TenantId tenantId);

    /**
     * 根据租户和场景类型查找场景列表
     */
    List<Scene> findByTenantAndType(TenantId tenantId, com.hkt.iot.scene.domain.model.SceneType type);

    /**
     * 删除场景
     */
    void delete(Scene scene);

    /**
     * 根据ID删除场景
     */
    void deleteById(SceneId id);

    /**
     * 检查编码是否存在
     */
    boolean existsByCode(SceneCode code);

    /**
     * 检查编码是否存在（排除指定ID）
     */
    boolean existsByCodeAndIdNot(SceneCode code, SceneId id);
}
