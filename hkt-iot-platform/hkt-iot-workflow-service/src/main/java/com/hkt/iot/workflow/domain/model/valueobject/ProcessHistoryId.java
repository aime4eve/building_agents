package com.hkt.iot.workflow.domain.model.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程历史ID值对象
 */
public final class ProcessHistoryId implements Serializable {

    private final String value;

    private ProcessHistoryId(String value) {
        this.value = Objects.requireNonNull(value, "ProcessHistoryId cannot be null");
    }

    public static ProcessHistoryId of(String value) {
        return new ProcessHistoryId(value);
    }

    public static ProcessHistoryId generate() {
        return new ProcessHistoryId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessHistoryId that = (ProcessHistoryId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
