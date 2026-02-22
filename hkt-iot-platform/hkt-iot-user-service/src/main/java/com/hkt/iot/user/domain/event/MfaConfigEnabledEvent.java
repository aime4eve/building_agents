package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * MFA 配置启用领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class MfaConfigEnabledEvent extends DomainEvent {

    private final Long configId;
    private final Long userId;
    private final String mfaType;
    private final LocalDateTime occurredAt;

    public MfaConfigEnabledEvent(
            Long configId,
            Long userId,
            String mfaType,
            LocalDateTime occurredAt) {
        this.configId = configId;
        this.userId = userId;
        this.mfaType = mfaType;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventType() {
        return "MfaConfigEnabled";
    }
}
