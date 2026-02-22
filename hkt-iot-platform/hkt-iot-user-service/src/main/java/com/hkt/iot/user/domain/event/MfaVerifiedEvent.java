package com.hkt.iot.user.domain.event;

import com.hkt.iot.domain.event.DomainEvent;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * MFA 验证成功领域事件
 *
 * @author HKT IoT Team
 */
@Getter
public class MfaVerifiedEvent extends DomainEvent {

    private final Long userId;
    private final Long challengeId;
    private final String mfaType;
    private final LocalDateTime occurredAt;

    public MfaVerifiedEvent(
            Long userId,
            Long challengeId,
            String mfaType,
            LocalDateTime occurredAt) {
        this.userId = userId;
        this.challengeId = challengeId;
        this.mfaType = mfaType;
        this.occurredAt = occurredAt;
    }

    @Override
    public String eventType() {
        return "MfaVerified";
    }
}
