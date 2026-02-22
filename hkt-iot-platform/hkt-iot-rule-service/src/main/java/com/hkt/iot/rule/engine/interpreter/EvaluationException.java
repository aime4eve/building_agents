package com.hkt.iot.rule.engine.interpreter;

/**
 * 求值异常
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class EvaluationException extends RuntimeException {
    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
