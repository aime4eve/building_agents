package com.hkt.iot.user.application.event;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户登录事件
 *
 * @author HKT IoT Team
 */
@Getter
public class UserLoginEvent {

    private final Long userId;
    private final Long tenantId;
    private final String sessionId;
    private final String ipAddress;
    private final LocalDateTime occurredAt;

    public UserLoginEvent(Long userId, Long tenantId, String sessionId, String ipAddress, LocalDateTime occurredAt) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.occurredAt = occurredAt;
    }
}
