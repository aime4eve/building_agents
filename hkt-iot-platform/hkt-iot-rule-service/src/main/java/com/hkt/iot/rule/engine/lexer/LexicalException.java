package com.hkt.iot.rule.engine.lexer;

/**
 * 词法分析异常
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class LexicalException extends RuntimeException {
    public LexicalException(String message) {
        super(message);
    }

    public LexicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
