package com.huakuangtong.ruleengine;

import com.huakuangtong.ruleengine.lexer.Lexer;
import com.huakuangtong.ruleengine.lexer.Token;
import com.huakuangtong.ruleengine.lexer.TokenType;
import com.huakuangtong.ruleengine.parser.Parser;
import com.huakuangtong.ruleengine.ast.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 语法分析器测试
 *
 * @author AI Engineer
 * @since 1.0.0
 */
class ParserTest {

    private ASTNode parse(String input) {
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        return parser.parse();
    }

    @Test
    void testSimpleComparison() {
        ASTNode ast = parse("temperature > 30");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(Identifier.class, expr.getLeft());
        assertInstanceOf(Literal.class, expr.getRight());
        assertEquals(BinaryOperator.GT, expr.getOperator());
    }

    @Test
    void testLogicalAnd() {
        ASTNode ast = parse("temperature > 30 && humidity < 80");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.AND, expr.getOperator());

        assertInstanceOf(BinaryExpression.class, expr.getLeft());
        assertInstanceOf(BinaryExpression.class, expr.getRight());
    }

    @Test
    void testLogicalOr() {
        ASTNode ast = parse("temperature > 30 || humidity < 80");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.OR, expr.getOperator());
    }

    @Test
    void testLogicalNot() {
        ASTNode ast = parse("!active");

        assertInstanceOf(UnaryExpression.class, ast);
        UnaryExpression expr = (UnaryExpression) ast;
        assertEquals(UnaryOperator.NOT, expr.getOperator());
        assertInstanceOf(Identifier.class, expr.getOperand());
    }

    @Test
    void testParentheses() {
        ASTNode ast = parse("(temperature > 30)");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.GT, expr.getOperator());
    }

    @Test
    void testComplexExpression() {
        ASTNode ast = parse("(temperature > 30 || humidity > 80) && device.online == true");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.AND, expr.getOperator());

        // 左侧是OR表达式
        assertInstanceOf(BinaryExpression.class, expr.getLeft());
        BinaryExpression leftExpr = (BinaryExpression) expr.getLeft();
        assertEquals(BinaryOperator.OR, leftExpr.getOperator());

        // 右侧是EQ表达式
        assertInstanceOf(BinaryExpression.class, expr.getRight());
        BinaryExpression rightExpr = (BinaryExpression) expr.getRight();
        assertEquals(BinaryOperator.EQ, rightExpr.getOperator());
    }

    @Test
    void testFunctionCall() {
        ASTNode ast = parse("avg(temperature, 1h)");

        assertInstanceOf(FunctionCall.class, ast);
        FunctionCall call = (FunctionCall) ast;
        assertEquals("avg", call.getFunctionName());
        assertEquals(2, call.getArguments().size());
    }

    @Test
    void testFunctionInComparison() {
        ASTNode ast = parse("avg(temperature, 1h) > 25");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(FunctionCall.class, expr.getLeft());
        assertInstanceOf(Literal.class, expr.getRight());
    }

    @Test
    void testStringLiteral() {
        ASTNode ast = parse("name == 'test'");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(Literal.class, expr.getRight());
        assertEquals("test", ((Literal) expr.getRight()).getValue());
    }

    @Test
    void testBooleanLiteral() {
        ASTNode ast = parse("active == true");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(Literal.class, expr.getRight());
        assertEquals(true, ((Literal) expr.getRight()).getValue());
    }

    @Test
    void testArrayLiteral() {
        ASTNode ast = parse("value in [1, 2, 3]");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(ArrayLiteral.class, expr.getRight());

        ArrayLiteral array = (ArrayLiteral) expr.getRight();
        assertEquals(3, array.getElements().size());
    }

    @Test
    void testNestedProperty() {
        ASTNode ast = parse("device.temperature > 30");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(Identifier.class, expr.getLeft());

        Identifier identifier = (Identifier) expr.getLeft();
        assertEquals("device.temperature", identifier.getName());
    }

    @Test
    void testContainsOperator() {
        ASTNode ast = parse("name contains 'test'");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.CONTAINS, expr.getOperator());
    }

    @Test
    void testInOperator() {
        ASTNode ast = parse("value in [1, 2, 3]");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.IN, expr.getOperator());
    }

    @Test
    void testBetweenOperator() {
        ASTNode ast = parse("value between [1, 10]");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.BETWEEN, expr.getOperator());
        assertInstanceOf(ArrayLiteral.class, expr.getRight());
    }

    @Test
    void testArithmeticOperators() {
        // 加法
        ASTNode addAst = parse("a + b");
        assertInstanceOf(BinaryExpression.class, addAst);
        assertEquals(BinaryOperator.ADD, ((BinaryExpression) addAst).getOperator());

        // 减法
        ASTNode subAst = parse("a - b");
        assertInstanceOf(BinaryExpression.class, subAst);
        assertEquals(BinaryOperator.SUB, ((BinaryExpression) subAst).getOperator());

        // 乘法
        ASTNode mulAst = parse("a * b");
        assertInstanceOf(BinaryExpression.class, mulAst);
        assertEquals(BinaryOperator.MUL, ((BinaryExpression) mulAst).getOperator());

        // 除法
        ASTNode divAst = parse("a / b");
        assertInstanceOf(BinaryExpression.class, divAst);
        assertEquals(BinaryOperator.DIV, ((BinaryExpression) divAst).getOperator());

        // 取模
        ASTNode modAst = parse("a % b");
        assertInstanceOf(BinaryExpression.class, modAst);
        assertEquals(BinaryOperator.MOD, ((BinaryExpression) modAst).getOperator());
    }

    @Test
    void testOperatorPrecedence() {
        ASTNode ast = parse("a + b * c");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.ADD, expr.getOperator());

        // 右侧应该是乘法（更高优先级）
        assertInstanceOf(BinaryExpression.class, expr.getRight());
        BinaryExpression rightExpr = (BinaryExpression) expr.getRight();
        assertEquals(BinaryOperator.MUL, rightExpr.getOperator());
    }

    @Test
    void testEmptyFunctionCall() {
        ASTNode ast = parse("now()");

        assertInstanceOf(FunctionCall.class, ast);
        FunctionCall call = (FunctionCall) ast;
        assertEquals("now", call.getFunctionName());
        assertTrue(call.getArguments().isEmpty());
    }

    @Test
    void testUnaryNegation() {
        ASTNode ast = parse("temperature > -5");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertInstanceOf(UnaryExpression.class, expr.getRight());

        UnaryExpression unary = (UnaryExpression) expr.getRight();
        assertEquals(UnaryOperator.NEGATE, unary.getOperator());
    }

    @Test
    void testComplexNestedExpression() {
        ASTNode ast = parse("(a > b || c < d) && e == f && !g");

        assertInstanceOf(BinaryExpression.class, ast);
        BinaryExpression expr = (BinaryExpression) ast;
        assertEquals(BinaryOperator.AND, expr.getOperator());
    }
}
