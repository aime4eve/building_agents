package com.hkt.iot.domain.model;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聚合根基类
 * 所有聚合根的父类，提供领域事件发布能力
 *
 * @author HKT IoT Team
 */
@Getter
public abstract class AggregateRoot<ID extends Serializable> extends Entity<ID> {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 领域事件列表
     */
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * 版本号（用于乐观锁）
     */
    private Long version;

    protected AggregateRoot() {
    }

    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * 添加领域事件
     *
     * @param event 领域事件
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    /**
     * 获取领域事件列表（只读）
     *
     * @return 领域事件列表
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * 清空领域事件列表
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
