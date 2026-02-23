package com.hkt.iot.workflow.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * SLA 报告 DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class SLAReportDTO {
    String tenantId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    int totalCount;
    int compliantCount;
    int breachedCount;
    int warningCount;
    double responseSLARate;
    double resolutionSLARate;
}
