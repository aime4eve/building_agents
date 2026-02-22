package com.huakuangtong.ruleengine;

/**
 * 规则引擎异常
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleEngineException extends RuntimeException {
    public RuleEngineException(String message) {
        super(message);
    }

    public RuleEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}
