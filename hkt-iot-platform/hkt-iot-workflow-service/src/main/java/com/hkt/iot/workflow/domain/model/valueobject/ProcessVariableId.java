package com.hkt.iot.workflow.domain.model.valueobject;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * 流程变量ID值对象
 */
public final class ProcessVariableId implements Serializable {

    private final String value;

    private ProcessVariableId(String value) {
        this.value = Objects.requireNonNull(value, "ProcessVariableId cannot be null");
    }

    public static ProcessVariableId of(String value) {
        return new ProcessVariableId(value);
    }

    public static ProcessVariableId generate() {
        return new ProcessVariableId(UUID.randomUUID().toString());
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessVariableId that = (ProcessVariableId) o;
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
