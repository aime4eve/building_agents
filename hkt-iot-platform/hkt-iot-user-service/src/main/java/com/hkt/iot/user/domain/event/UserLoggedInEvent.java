package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户登录领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class UserLoggedInEvent extends DomainEvent {

    private final Long userId;
    private final String username;
    private final Long tenantId;
    private final String ipAddress;
    private final LocalDateTime occurredAt;

    public UserLoggedInEvent(
            Long userId,
            String username,
            Long tenantId,
            String ipAddress,
            LocalDateTime occurredAt) {
        this.userId = userId;
        this.username = username;
        this.tenantId = tenantId;
        this.ipAddress = ipAddress;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventType() {
        return "UserLoggedIn";
    }
}
