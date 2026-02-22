package com.hkt.iot.domain.repository;

import com.hkt.iot.common.exception.BizException;
import com.hkt.iot.common.exception.ErrorCode;

/**
 * 乐观锁仓储接口
 * 支持版本号控制的并发更新
 *
 * @author HKT IoT Team
 */
public interface OptimisticLockRepository<T, ID> extends BaseRepository<T, ID> {

    /**
     * 保存实体（带乐观锁检查）
     * 如果版本号不匹配，抛出OptimisticLockException
     *
     * @param entity 实体
     * @return 保存后的实体
     * @throws BizException 乐观锁冲突时抛出
     */
    default T saveWithVersion(T entity) {
        T existing = findById(getId(entity)).orElseThrow(
                () -> new BizException(ErrorCode.RESOURCE_NOT_FOUND)
        );

        if (getVersion(existing) != getVersion(entity)) {
            throw new BizException(ErrorCode.DB_OPTIMISTIC_LOCK_FAILED);
        }

        return save(entity);
    }

    /**
     * 批量保存实体（带乐观锁检查）
     *
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    default java.util.List<T> saveAllWithVersion(java.util.List<T> entities) {
        return entities.stream()
                .map(this::saveWithVersion)
                .toList();
    }

    /**
     * 刷新实体状态
     *
     * @param entity 实体
     */
    void refresh(T entity);

    /**
     * 获取实体当前版本号
     *
     * @param id 实体ID
     * @return 版本号
     */
    Long getCurrentVersion(ID id);

    /**
     * 获取实体ID（子类需要实现）
     *
     * @param entity 实体
     * @return 实体ID
     */
    ID getId(T entity);

    /**
     * 获取实体版本号（子类需要实现）
     *
     * @param entity 实体
     * @return 版本号
     */
    Long getVersion(T entity);
}
