package com.hkt.iot.rule.engine;

import com.hkt.iot.rule.engine.lexer.Lexer;
import com.hkt.iot.rule.engine.lexer.Token;
import com.hkt.iot.rule.engine.lexer.TokenType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 词法分析器测试
 *
 * @author AI Engineer
 * @since 1.0.0
 */
class LexerTest {

    @Test
    void testSimpleExpression() {
        Lexer lexer = new Lexer("temperature > 30");
        List<Token> tokens = lexer.tokenize();

        assertEquals(4, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("temperature", tokens.get(0).getValue());
        assertEquals(TokenType.GT, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals("30", tokens.get(2).getValue());
        assertEquals(TokenType.EOF, tokens.get(3).getType());
    }

    @Test
    void testLogicalOperators() {
        Lexer lexer = new Lexer("temperature > 30 && humidity < 80");
        List<Token> tokens = lexer.tokenize();

        assertEquals(8, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals(TokenType.GT, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals(TokenType.AND, tokens.get(3).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(4).getType());
        assertEquals(TokenType.LT, tokens.get(5).getType());
        assertEquals(TokenType.NUMBER, tokens.get(6).getType());
        assertEquals(TokenType.EOF, tokens.get(7).getType());
    }

    @Test
    void testComplexExpression() {
        Lexer lexer = new Lexer("(temperature > 30 || humidity > 80) && device.online == true");
        List<Token> tokens = lexer.tokenize();

        // 检查关键token
        boolean foundLParen = false;
        boolean foundRParen = false;
        boolean foundOr = false;
        boolean foundAnd = false;
        boolean foundEq = false;

        for (Token token : tokens) {
            if (token.getType() == TokenType.LPAREN) foundLParen = true;
            if (token.getType() == TokenType.RPAREN) foundRParen = true;
            if (token.getType() == TokenType.OR) foundOr = true;
            if (token.getType() == TokenType.AND) foundAnd = true;
            if (token.getType() == TokenType.EQ) foundEq = true;
        }

        assertTrue(foundLParen, "Should have left parenthesis");
        assertTrue(foundRParen, "Should have right parenthesis");
        assertTrue(foundOr, "Should have OR operator");
        assertTrue(foundAnd, "Should have AND operator");
        assertTrue(foundEq, "Should have EQ operator");
    }

    @Test
    void testFunctionCall() {
        Lexer lexer = new Lexer("avg(temperature, 1h) > 25");
        List<Token> tokens = lexer.tokenize();

        assertEquals(8, tokens.size());
        assertEquals(TokenType.AVG, tokens.get(0).getType());
        assertEquals(TokenType.LPAREN, tokens.get(1).getType());
        assertEquals(TokenType.IDENTIFIER, tokens.get(2).getType());
        assertEquals(TokenType.COMMA, tokens.get(3).getType());
        assertEquals(TokenType.NUMBER, tokens.get(4).getType());
        assertEquals(TokenType.HOUR, tokens.get(5).getType());
        assertEquals(TokenType.RPAREN, tokens.get(6).getType());
        assertEquals(TokenType.EOF, tokens.get(7).getType());
    }

    @Test
    void testStringLiteral() {
        Lexer lexer = new Lexer("name == 'test'");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.STRING, tokens.get(2).getType());
        assertEquals("test", tokens.get(2).getValue());
    }

    @Test
    void testBooleanLiteral() {
        Lexer lexer = new Lexer("active == true");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.BOOLEAN, tokens.get(2).getType());
        assertEquals("true", tokens.get(2).getValue());
    }

    @Test
    void testArrayLiteral() {
        Lexer lexer = new Lexer("value in [1, 2, 3]");
        List<Token> tokens = lexer.tokenize();

        boolean foundLBracket = false;
        boolean foundRBracket = false;

        for (Token token : tokens) {
            if (token.getType() == TokenType.LBRACKET) foundLBracket = true;
            if (token.getType() == TokenType.RBRACKET) foundRBracket = true;
        }

        assertTrue(foundLBracket, "Should have left bracket");
        assertTrue(foundRBracket, "Should have right bracket");
    }

    @Test
    void testComparisonOperators() {
        // 测试所有比较操作符
        assertOperatorLexing("> 5", TokenType.GT);
        assertOperatorLexing(">= 5", TokenType.GTE);
        assertOperatorLexing("< 5", TokenType.LT);
        assertOperatorLexing("<= 5", TokenType.LTE);
        assertOperatorLexing("== 5", TokenType.EQ);
        assertOperatorLexing("!= 5", TokenType.NEQ);
    }

    private void assertOperatorLexing(String input, TokenType expectedType) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(expectedType, tokens.get(0).getType(),
                "Expected " + expectedType + " for input: " + input);
    }

    @Test
    void testDecimalNumber() {
        Lexer lexer = new Lexer("temperature > 25.5");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals("25.5", tokens.get(2).getValue());
    }

    @Test
    void testNegativeNumber() {
        Lexer lexer = new Lexer("temperature > -5");
        List<Token> tokens = lexer.tokenize();

        assertEquals(TokenType.GT, tokens.get(1).getType());
        assertEquals(TokenType.NUMBER, tokens.get(2).getType());
        assertEquals("5", tokens.get(2).getValue());
    }
}
