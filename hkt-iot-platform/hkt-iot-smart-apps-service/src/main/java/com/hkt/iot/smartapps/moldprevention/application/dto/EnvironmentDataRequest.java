package com.hkt.iot.smartapps.moldprevention.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 环境数据请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentDataRequest {

    /**
     * 温度
     */
    private Double temperature;

    /**
     * 湿度
     */
    private Double humidity;

    /**
     * 时间戳
     */
    private String timestamp;
}
