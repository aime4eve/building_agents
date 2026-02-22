package com.hkt.iot.rule.engine.lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * Token类型枚举
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public enum TokenType {
    // 操作符
    GT(">"),
    GTE(">="),
    LT("<"),
    LTE("<="),
    EQ("=="),
    NEQ("!="),
    CONTAINS("contains"),
    MATCHES("matches"),
    IN("in"),
    BETWEEN("between"),

    // 逻辑操作符
    AND("&&"),
    OR("||"),
    NOT("!"),

    // 分隔符
    LPAREN("("),
    RPAREN(")"),
    COMMA(","),
    LBRACKET("["),
    RBRACKET("]"),

    // 字面量
    NUMBER("NUMBER"),
    STRING("STRING"),
    BOOLEAN("BOOLEAN"),
    NULL("null"),

    // 标识符
    IDENTIFIER("IDENTIFIER"),

    // 内置函数
    AVG("avg"),
    SUM("sum"),
    MAX("max"),
    MIN("min"),
    COUNT("count"),
    LAST("last"),
    FIRST("first"),
    DIFF("diff"),
    RATE("rate"),
    NOW("now"),
    TODAY("today"),

    // 时间单位
    HOUR("h"),
    MINUTE("m"),
    SECOND("s"),
    DAY("d"),

    // 特殊
    EOF("EOF"),
    WHITESPACE("WHITESPACE");

    private final String value;

    TokenType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private static final Map<String, TokenType> KEYWORD_MAP = new HashMap<>();

    static {
        // 关键字映射
        for (TokenType type : values()) {
            if (type != NUMBER && type != STRING && type != BOOLEAN &&
                type != IDENTIFIER && type != WHITESPACE && type != EOF) {
                KEYWORD_MAP.put(type.getValue(), type);
            }
        }
    }

    public static TokenType fromKeyword(String keyword) {
        return KEYWORD_MAP.get(keyword);
    }

    public static boolean isKeyword(String text) {
        return KEYWORD_MAP.containsKey(text);
    }
}
