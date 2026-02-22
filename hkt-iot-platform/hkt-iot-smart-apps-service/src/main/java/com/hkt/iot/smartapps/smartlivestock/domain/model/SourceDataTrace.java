package com.hkt.iot.smartapps.smartlivestock.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 数据来源追踪值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceDataTrace {

    private String sourceId;
    private String sourceType;
    private LocalDateTime collectedFrom;
    private LocalDateTime collectedTo;
    private int dataPointCount;
}
