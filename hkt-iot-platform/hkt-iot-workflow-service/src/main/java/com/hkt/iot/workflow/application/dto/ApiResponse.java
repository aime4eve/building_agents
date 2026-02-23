package com.hkt.iot.workflow.application.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 统一 API 响应格式
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class ApiResponse<T> {
    Integer code;
    String message;
    T data;
    String traceId;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .traceId(java.util.UUID.randomUUID().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .traceId(java.util.UUID.randomUUID().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .traceId(java.util.UUID.randomUUID().toString())
                .build();
    }
}
