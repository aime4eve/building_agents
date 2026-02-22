package com.hkt.iot.rule.engine.parser;

/**
 * 语法分析异常
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class ParseException extends RuntimeException {
    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
