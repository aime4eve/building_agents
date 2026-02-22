package com.hkt.iot.scene.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 执行场景请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteSceneRequest {

    private String triggeredBy;

    private Map<String, Object> parameters;
}
