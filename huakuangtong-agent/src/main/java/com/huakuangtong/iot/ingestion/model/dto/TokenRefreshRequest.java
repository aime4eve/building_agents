package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Token刷新请求
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class TokenRefreshRequest {

    @NotBlank(message = "RefreshToken不能为空")
    private String refreshToken;
}
