package com.huakuangtong.iot.ingestion.model.dto;

import lombok.Data;

/**
 * Token刷新响应
 *
 * @author IoT Expert
 * @since 2026-02-20
 */
@Data
public class TokenRefreshResponse {

    private String token;
    private String refreshToken;
    private Long expiresAt;
}
