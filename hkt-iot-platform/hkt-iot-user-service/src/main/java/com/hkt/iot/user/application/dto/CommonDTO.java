package com.hkt.iot.user.application.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 通用响应DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
public class CommonResponse<T> {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 成功响应
     */
    public static <T> CommonResponse<T> success(T data) {
        return CommonResponse.<T>builder()
                .code(200)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> CommonResponse<T> success() {
        return success(null);
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> CommonResponse<T> success(String message, T data) {
        return CommonResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
