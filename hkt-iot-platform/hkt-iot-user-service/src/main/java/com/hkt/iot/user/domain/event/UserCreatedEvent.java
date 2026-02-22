package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 用户创建领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class UserCreatedEvent extends DomainEvent {

    private final Long userId;
    private final String username;
    private final String email;
    private final Long tenantId;
    private final LocalDateTime occurredAt;

    public UserCreatedEvent(
            Long userId,
            String username,
            String email,
            Long tenantId,
            LocalDateTime occurredAt) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventType() {
        return "UserCreated";
    }
}
