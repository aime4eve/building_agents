package com.hkt.iot.rule.infrastructure.messaging.event;

import com.hkt.iot.rule.domain.event.RuleTriggeredEvent;
import com.hkt.iot.rule.domain.event.RuleExecutionFailedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 规则事件发布服务
 * 发布规则相关的领域事件到Kafka
 *
 * @author HKT IoT Team
 */
@Slf4j
@Service
public class RuleEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RuleEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * 发布规则触发事件
     */
    public void publishRuleTriggered(RuleTriggeredEvent event) {
        try {
            String topic = "rule-triggered";
            kafkaTemplate.send(topic, event.getRuleId().toString(), event);
            log.debug("发布规则触发事件: ruleId={}, event={}", event.getRuleId(), event.eventType());
        } catch (Exception e) {
            log.error("发布规则触发事件失败: ruleId={}", event.getRuleId(), e);
        }
    }

    /**
     * 发布规则执行失败事件
     */
    public void publishRuleExecutionFailed(RuleExecutionFailedEvent event) {
        try {
            String topic = "rule-execution-failed";
            kafkaTemplate.send(topic, event.getRuleId().toString(), event);
            log.debug("发布规则执行失败事件: ruleId={}, error={}",
                    event.getRuleId(), event.getErrorMessage());
        } catch (Exception e) {
            log.error("发布规则执行失败事件失败: ruleId={}", event.getRuleId(), e);
        }
    }

    /**
     * 事务后发布规则触发事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRuleTriggeredAfterCommit(RuleTriggeredEvent event) {
        publishRuleTriggered(event);
    }

    /**
     * 事务后发布规则执行失败事件
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRuleExecutionFailedAfterCommit(RuleExecutionFailedEvent event) {
        publishRuleExecutionFailed(event);
    }
}
