package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Objects;

/**
 * 任务 ID - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class TaskId {
    String value;

    private TaskId(String value) {
        this.value = Objects.requireNonNull(value, "TaskId cannot be null");
    }

    public static TaskId of(String value) {
        return new TaskId(value);
    }
}
