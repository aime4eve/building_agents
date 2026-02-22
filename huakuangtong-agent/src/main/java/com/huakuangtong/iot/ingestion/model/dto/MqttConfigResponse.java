package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * MQTT配置响应
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class MqttConfigResponse {

    private String broker;
    private Integer port;
    private String protocol;
    private Map<String, String> topics;
}
