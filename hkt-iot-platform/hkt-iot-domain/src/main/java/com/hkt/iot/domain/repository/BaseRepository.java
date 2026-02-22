package com.hkt.iot.domain.repository;

import java.util.List;
import java.util.Optional;

/**
 * 仓储接口基类
 * 定义通用的CRUD和批量操作方法
 *
 * @param <T> 实体类型
 * @param <ID> 实体ID类型
 * @author HKT IoT Team
 */
public interface BaseRepository<T, ID> {

    /**
     * 保存实体
     *
     * @param entity 实体
     * @return 保存后的实体
     */
    T save(T entity);

    /**
     * 批量保存实体
     *
     * @param entities 实体列表
     * @return 保存后的实体列表
     */
    List<T> saveAll(List<T> entities);

    /**
     * 根据ID查找实体
     *
     * @param id 实体ID
     * @return 实体
     */
    Optional<T> findById(ID id);

    /**
     * 根据ID列表查找实体
     *
     * @param ids 实体ID列表
     * @return 实体列表
     */
    List<T> findByIds(List<ID> ids);

    /**
     * 查找所有实体
     *
     * @return 实体列表
     */
    List<T> findAll();

    /**
     * 判断实体是否存在
     *
     * @param id 实体ID
     * @return 是否存在
     */
    boolean existsById(ID id);

    /**
     * 统计实体数量
     *
     * @return 实体数量
     */
    long count();

    /**
     * 根据ID删除实体
     *
     * @param id 实体ID
     */
    void deleteById(ID id);

    /**
     * 删除实体
     *
     * @param entity 实体
     */
    void delete(T entity);

    /**
     * 批量删除实体
     *
     * @param entities 实体列表
     */
    void deleteAll(List<T> entities);

    /**
     * 删除所有实体
     */
    void deleteAll();
}
