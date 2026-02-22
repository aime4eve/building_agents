package com.hkt.iot.domain.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 值对象基类
 * 所有值对象的父类，提供基于值的相等性判断
 *
 * 值对象特点：
 * 1. 不可变（Immutable）
 * 2. 基于值判断相等性，而非ID
 * 3. 可以被共享
 *
 * @author HKT IoT Team
 */
public abstract class ValueObject implements Serializable {

    /**
     * 判断值对象是否相等（基于所有属性）
     *
     * @param o 另一个对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true; // 具体实现由子类完成
    }

    /**
     * 计算哈希值（基于所有属性）
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return Objects.hash();
    }

    /**
     * 获取值对象的属性值数组（用于相等性判断）
     * 子类应该重写此方法，返回所有用于判断相等性的属性
     *
     * @return 属性值数组
     */
    protected Object[] getEqualityComponents() {
        return new Object[0];
    }
}
