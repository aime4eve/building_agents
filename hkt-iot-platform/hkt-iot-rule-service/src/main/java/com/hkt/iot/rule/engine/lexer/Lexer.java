package com.hkt.iot.rule.engine.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则引擎词法分析器
 * 将DSL字符串转换为Token流
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class Lexer {
    private final String input;
    private int position;
    private int line;
    private int column;

    // 正则表达式模式
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?");
    private static final Pattern STRING_PATTERN = Pattern.compile("^'[^']*'|^\"[^\"]*\"");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_.]*");

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
        this.line = 1;
        this.column = 1;
    }

    /**
     * 将输入字符串转换为Token列表
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char current = peek();

            // 跳过空白字符
            if (Character.isWhitespace(current)) {
                consumeWhitespace();
                continue;
            }

            // 单字符操作符和分隔符
            if (current == '(') {
                tokens.add(createToken(TokenType.LPAREN, "("));
                advance();
                continue;
            }
            if (current == ')') {
                tokens.add(createToken(TokenType.RPAREN, ")"));
                advance();
                continue;
            }
            if (current == ',') {
                tokens.add(createToken(TokenType.COMMA, ","));
                advance();
                continue;
            }
            if (current == '[') {
                tokens.add(createToken(TokenType.LBRACKET, "["));
                advance();
                continue;
            }
            if (current == ']') {
                tokens.add(createToken(TokenType.RBRACKET, "]"));
                advance();
                continue;
            }
            if (current == '!') {
                if (peekNext() == '=') {
                    tokens.add(createToken(TokenType.NEQ, "!="));
                    advance();
                    advance();
                } else {
                    tokens.add(createToken(TokenType.NOT, "!"));
                    advance();
                }
                continue;
            }
            if (current == '&') {
                if (peekNext() == '&') {
                    tokens.add(createToken(TokenType.AND, "&&"));
                    advance();
                    advance();
                }
                continue;
            }
            if (current == '|') {
                if (peekNext() == '|') {
                    tokens.add(createToken(TokenType.OR, "||"));
                    advance();
                    advance();
                }
                continue;
            }
            if (current == '=') {
                if (peekNext() == '=') {
                    tokens.add(createToken(TokenType.EQ, "=="));
                    advance();
                    advance();
                }
                continue;
            }
            if (current == '<') {
                if (peekNext() == '=') {
                    tokens.add(createToken(TokenType.LTE, "<="));
                    advance();
                    advance();
                } else {
                    tokens.add(createToken(TokenType.LT, "<"));
                    advance();
                }
                continue;
            }
            if (current == '>') {
                if (peekNext() == '=') {
                    tokens.add(createToken(TokenType.GTE, ">="));
                    advance();
                    advance();
                } else {
                    tokens.add(createToken(TokenType.GT, ">"));
                    advance();
                }
                continue;
            }

            // 数字字面量
            if (Character.isDigit(current) || current == '.') {
                tokens.add(readNumber());
                continue;
            }

            // 字符串字面量
            if (current == '\'' || current == '"') {
                tokens.add(readString());
                continue;
            }

            // 标识符或关键字
            if (Character.isLetter(current) || current == '_') {
                tokens.add(readIdentifierOrKeyword());
                continue;
            }

            throw new LexicalException("Unexpected character: '" + current + "' at line " + line + ", column " + column);
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private Token readNumber() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (position < input.length() &&
               (Character.isDigit(peek()) || peek() == '.')) {
            sb.append(peek());
            advance();
        }

        String value = sb.toString();
        return new Token(TokenType.NUMBER, value, startLine, startColumn);
    }

    private Token readString() {
        int startLine = line;
        int startColumn = column;
        char quote = peek();
        advance(); // 跳过开始引号

        StringBuilder sb = new StringBuilder();
        while (position < input.length() && peek() != quote) {
            sb.append(peek());
            advance();
        }

        if (position >= input.length()) {
            throw new LexicalException("Unterminated string at line " + startLine);
        }

        advance(); // 跳过结束引号
        return new Token(TokenType.STRING, sb.toString(), startLine, startColumn);
    }

    private Token readIdentifierOrKeyword() {
        int startLine = line;
        int startColumn = column;
        StringBuilder sb = new StringBuilder();

        while (position < input.length() &&
               (Character.isLetterOrDigit(peek()) || peek() == '_' || peek() == '.')) {
            sb.append(peek());
            advance();
        }

        String value = sb.toString();

        // 检查是否是布尔值
        if ("true".equals(value) || "false".equals(value)) {
            return new Token(TokenType.BOOLEAN, value, startLine, startColumn);
        }

        // 检查是否是关键字
        TokenType keywordType = TokenType.fromKeyword(value);
        if (keywordType != null) {
            return new Token(keywordType, value, startLine, startColumn);
        }

        return new Token(TokenType.IDENTIFIER, value, startLine, startColumn);
    }

    private void consumeWhitespace() {
        while (position < input.length() && Character.isWhitespace(peek())) {
            if (peek() == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
            position++;
        }
    }

    private Token createToken(TokenType type, String value) {
        return new Token(type, value, line, column);
    }

    private char peek() {
        return input.charAt(position);
    }

    private char peekNext() {
        if (position + 1 < input.length()) {
            return input.charAt(position + 1);
        }
        return '\0';
    }

    private void advance() {
        column++;
        position++;
    }
}
