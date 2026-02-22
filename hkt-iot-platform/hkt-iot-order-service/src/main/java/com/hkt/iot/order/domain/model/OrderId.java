package com.hkt.iot.order.domain.model;

import com.hkt.iot.domain.model.ValueObject;
import lombok.Getter;

import java.io.Serial;
import java.util.Objects;
import java.util.UUID;

/**
 * 订单ID值对象
 *
 * @author HKT IoT Team
 */
@Getter
public class OrderId extends ValueObject {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;

    public OrderId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("订单ID无效");
        }
        this.id = id;
    }

    public static OrderId of(Long id) {
        return new OrderId(id);
    }

    @Override
    protected Object[] getEqualityComponents() {
        return new Object[]{id};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(id, orderId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
