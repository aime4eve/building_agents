package com.hkt.iot.domain.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * 实体基类
 * 所有实体的父类，提供ID和相等性判断
 *
 * @author HKT IoT Team
 */
public abstract class Entity<ID extends Serializable> {

    /**
     * 实体ID
     */
    protected ID id;

    protected Entity() {
    }

    protected Entity(ID id) {
        this.id = id;
    }

    /**
     * 获取实体ID
     *
     * @return 实体ID
     */
    public ID getId() {
        return id;
    }

    /**
     * 判断实体是否相等（基于ID）
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }

    /**
     * 计算哈希值（基于ID）
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
