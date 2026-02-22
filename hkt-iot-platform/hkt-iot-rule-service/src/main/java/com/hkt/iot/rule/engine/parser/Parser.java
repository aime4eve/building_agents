package com.hkt.iot.rule.engine.parser;

import com.hkt.iot.rule.engine.ast.*;
import com.hkt.iot.rule.engine.lexer.Token;
import com.hkt.iot.rule.engine.lexer.TokenType;
import com.hkt.iot.rule.engine.lexer.Lexer;

import java.util.ArrayList;
import java.util.List;

/**
 * 规则引擎语法分析器
 * 使用递归下降分析技术
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class Parser {
    private final List<Token> tokens;
    private int current;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.current = 0;
    }

    /**
     * 解析表达式并返回AST
     */
    public ASTNode parse() {
        try {
            ASTNode expr = expression();
            if (!isAtEnd()) {
                throw new ParseException("Unexpected token after end of expression: " + peek().getValue());
            }
            return expr;
        } catch (IndexOutOfBoundsException e) {
            throw new ParseException("Unexpected end of input", e);
        }
    }

    /**
     * 解析表达式（最低优先级：OR）
     * expression -> logicalOr
     */
    private ASTNode expression() {
        return logicalOr();
    }

    /**
     * 解析逻辑OR表达式
     * logicalOr -> logicalAnd ( "||" logicalAnd )*
     */
    private ASTNode logicalOr() {
        ASTNode expr = logicalAnd();

        while (match(TokenType.OR)) {
            Token operator = previous();
            ASTNode right = logicalAnd();
            expr = new BinaryExpression(expr, BinaryOperator.OR, right);
        }

        return expr;
    }

    /**
     * 解析逻辑AND表达式
     * logicalAnd -> equality ( "&&" equality )*
     */
    private ASTNode logicalAnd() {
        ASTNode expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            ASTNode right = equality();
            expr = new BinaryExpression(expr, BinaryOperator.AND, right);
        }

        return expr;
    }

    /**
     * 解析相等性表达式
     * equality -> comparison ( ( "==" | "!=" | "contains" | "matches" | "in" | "between" ) comparison )*
     */
    private ASTNode equality() {
        ASTNode expr = comparison();

        while (match(TokenType.EQ, TokenType.NEQ, TokenType.CONTAINS,
                     TokenType.MATCHES, TokenType.IN, TokenType.BETWEEN)) {
            Token operator = previous();
            BinaryOperator binOp = tokenTypeToBinaryOperator(operator.getType());
            ASTNode right = comparison();
            expr = new BinaryExpression(expr, binOp, right);
        }

        return expr;
    }

    /**
     * 解析比较表达式
     * comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
     */
    private ASTNode comparison() {
        ASTNode expr = term();

        while (match(TokenType.GT, TokenType.GTE, TokenType.LT, TokenType.LTE)) {
            Token operator = previous();
            BinaryOperator binOp = tokenTypeToBinaryOperator(operator.getType());
            ASTNode right = term();
            expr = new BinaryExpression(expr, binOp, right);
        }

        return expr;
    }

    /**
     * 解析加减法表达式
     * term -> factor ( ( "+" | "-" ) factor )*
     */
    private ASTNode term() {
        ASTNode expr = factor();

        while (matchBinaryOperator("+", "-")) {
            String opSymbol = previous().getValue();
            BinaryOperator binOp = "+".equals(opSymbol) ? BinaryOperator.ADD : BinaryOperator.SUB;
            ASTNode right = factor();
            expr = new BinaryExpression(expr, binOp, right);
        }

        return expr;
    }

    /**
     * 解析乘除法表达式
     * factor -> unary ( ( "*" | "/" | "%" ) unary )*
     */
    private ASTNode factor() {
        ASTNode expr = unary();

        while (matchBinaryOperator("*", "/", "%")) {
            String opSymbol = previous().getValue();
            BinaryOperator binOp;
            switch (opSymbol) {
                case "*": binOp = BinaryOperator.MUL; break;
                case "/": binOp = BinaryOperator.DIV; break;
                case "%": binOp = BinaryOperator.MOD; break;
                default: binOp = BinaryOperator.MUL;
            }
            ASTNode right = unary();
            expr = new BinaryExpression(expr, binOp, right);
        }

        return expr;
    }

    /**
     * 解析一元表达式
     * unary -> ( "!" | "-" ) unary | primary
     */
    private ASTNode unary() {
        if (match(TokenType.NOT)) {
            return new UnaryExpression(UnaryOperator.NOT, unary());
        }
        if (matchBinaryOperator("-")) {
            return new UnaryExpression(UnaryOperator.NEGATE, unary());
        }

        return primary();
    }

    /**
     * 解析基础表达式
     * primary -> NUMBER | STRING | BOOLEAN | IDENTIFIER | "(" expression ")" | functionCall | array
     */
    private ASTNode primary() {
        // 字面量
        if (match(TokenType.NUMBER)) {
            String value = previous().getValue();
            Object numValue = value.contains(".") ?
                    Double.parseDouble(value) : Integer.parseInt(value);
            return new Literal(numValue);
        }

        if (match(TokenType.STRING)) {
            return new Literal(previous().getValue());
        }

        if (match(TokenType.BOOLEAN)) {
            return new Literal(Boolean.parseBoolean(previous().getValue()));
        }

        if (match(TokenType.NULL)) {
            return new Literal(null);
        }

        // 括号表达式
        if (match(TokenType.LPAREN)) {
            ASTNode expr = expression();
            consume(TokenType.RPAREN, "Expect ')' after expression.");
            return expr;
        }

        // 数组
        if (match(TokenType.LBRACKET)) {
            return parseArray();
        }

        // 标识符或函数调用
        if (match(TokenType.IDENTIFIER)) {
            String identifierName = previous().getValue();

            // 检查是否是函数调用
            if (match(TokenType.LPAREN)) {
                return parseFunctionCall(identifierName);
            }

            return new Identifier(identifierName);
        }

        // 内置函数
        if (match(TokenType.AVG, TokenType.SUM, TokenType.MAX, TokenType.MIN,
                  TokenType.COUNT, TokenType.LAST, TokenType.FIRST,
                  TokenType.DIFF, TokenType.RATE, TokenType.NOW, TokenType.TODAY)) {
            String functionName = previous().getValue();
            consume(TokenType.LPAREN, "Expect '(' after function name.");
            return parseFunctionCall(functionName);
        }

        throw new ParseException("Expect expression, got: " + peek().getValue());
    }

    /**
     * 解析数组
     * array -> "[" ( expression ( "," expression )* )? "]"
     */
    private ArrayLiteral parseArray() {
        List<ASTNode> elements = new ArrayList<>();

        if (!check(TokenType.RBRACKET)) {
            do {
                elements.add(expression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RBRACKET, "Expect ']' after array elements.");
        return new ArrayLiteral(elements);
    }

    /**
     * 解析函数调用
     * functionCall -> IDENTIFIER "(" ( expression ( "," expression )* )? ")"
     */
    private FunctionCall parseFunctionCall(String functionName) {
        List<ASTNode> arguments = new ArrayList<>();

        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expect ')' after function arguments.");
        return new FunctionCall(functionName, arguments);
    }

    /**
     * 将TokenType转换为BinaryOperator
     */
    private BinaryOperator tokenTypeToBinaryOperator(TokenType type) {
        switch (type) {
            case EQ: return BinaryOperator.EQ;
            case NEQ: return BinaryOperator.NEQ;
            case GT: return BinaryOperator.GT;
            case GTE: return BinaryOperator.GTE;
            case LT: return BinaryOperator.LT;
            case LTE: return BinaryOperator.LTE;
            case CONTAINS: return BinaryOperator.CONTAINS;
            case MATCHES: return BinaryOperator.MATCHES;
            case IN: return BinaryOperator.IN;
            case BETWEEN: return BinaryOperator.BETWEEN;
            case AND: return BinaryOperator.AND;
            case OR: return BinaryOperator.OR;
            default: throw new ParseException("Unknown operator: " + type);
        }
    }

    /**
     * 检查当前token是否是给定的类型之一
     */
    private boolean check(TokenType... types) {
        for (TokenType type : types) {
            if (!isAtEnd() && peek().getType() == type) {
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配并消费给定的类型之一
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * 匹配二元操作符（通过字符串值）
     */
    private boolean matchBinaryOperator(String... symbols) {
        if (!isAtEnd() && peek().getType() == TokenType.IDENTIFIER) {
            String value = peek().getValue();
            for (String symbol : symbols) {
                if (symbol.equals(value)) {
                    advance();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 消费指定类型的token，否则抛出异常
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        throw new ParseException(message + " Got: " + peek().getValue());
    }

    /**
     * 获取当前token并前进一步
     */
    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    /**
     * 检查是否到达末尾
     */
    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    /**
     * 获取当前token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * 获取前一个token
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    /**
     * 便捷方法：从DSL字符串解析AST
     */
    public static ASTNode parse(String dsl) {
        Lexer lexer = new Lexer(dsl);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }
}
