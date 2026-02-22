package com.hkt.iot.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应DTO
 *
 * @author HKT IoT Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponse<T> {

    private String code;
    private String message;
    private T data;
    private Long timestamp;

    public CommonResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> CommonResponse<T> success() {
        return new CommonResponse<>("200", "操作成功", null);
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>("200", "操作成功", data);
    }

    public static <T> CommonResponse<T> success(String message, T data) {
        return new CommonResponse<>("200", message, data);
    }

    public static <T> CommonResponse<T> error(String code, String message) {
        return new CommonResponse<>(code, message, null);
    }

    public static <T> CommonResponse<T> error(String message) {
        return new CommonResponse<>("500", message, null);
    }

    public boolean isSuccess() {
        return "200".equals(this.code);
    }
}
