package com.hkt.iot.workflow.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 工单编号 - 值对象
 *
 * @author HKT IoT Team
 */
@Value
@EqualsAndHashCode(of = "value")
public class WorkOrderNo {
    String value;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong SEQUENCE = new AtomicLong(1);

    private WorkOrderNo(String value) {
        this.value = Objects.requireNonNull(value, "WorkOrderNo cannot be null");
    }

    public static WorkOrderNo of(String value) {
        return new WorkOrderNo(value);
    }

    public static WorkOrderNo generate(WorkOrderType type) {
        String dateStr = LocalDate.now().format(DATE_FORMATTER);
        String typePrefix = type.getPrefix();
        long seq = SEQUENCE.getAndIncrement() % 10000;
        String seqStr = String.format("%04d", seq);
        return new WorkOrderNo(String.format("WO%s%s%s", typePrefix, dateStr, seqStr));
    }
}
