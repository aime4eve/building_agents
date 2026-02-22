package com.hkt.iot.device.interfaces.rest.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 遥测查询请求DTO
 *
 * @author HKT IoT Team
 */
@Data
public class TelemetryQueryRequest {

    private Long deviceId;
    private String deviceSn;
    private List<String> dataKeys;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long limit;
    private String aggregation;  // mean, max, min, sum, count
}
