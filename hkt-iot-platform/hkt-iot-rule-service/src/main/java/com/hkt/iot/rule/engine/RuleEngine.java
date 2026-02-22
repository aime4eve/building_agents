package com.hkt.iot.rule.engine;

import com.hkt.iot.rule.engine.ast.ASTNode;
import com.hkt.iot.rule.engine.interpreter.EvaluationException;
import com.hkt.iot.rule.engine.interpreter.FunctionRegistry;
import com.hkt.iot.rule.engine.interpreter.Interpreter;
import com.hkt.iot.rule.engine.lexer.Lexer;
import com.hkt.iot.rule.engine.lexer.LexicalException;
import com.hkt.iot.rule.engine.parser.ParseException;
import com.hkt.iot.rule.engine.parser.Parser;

import java.util.Map;

/**
 * 规则引擎统一入口
 * 提供简洁的DSL规则执行API
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class RuleEngine {

    private final FunctionRegistry functionRegistry;

    public RuleEngine() {
        this.functionRegistry = new FunctionRegistry();
    }

    public RuleEngine(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    /**
     * 执行规则并返回结果
     *
     * @param rule DSL规则表达式
     * @param context 上下文变量
     * @return 执行结果
     * @throws RuleEngineException 规则执行异常
     */
    public Object execute(String rule, Map<String, Object> context) {
        try {
            // 1. 词法分析
            Lexer lexer = new Lexer(rule);
            var tokens = lexer.tokenize();

            // 2. 语法分析
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            // 3. 解释执行
            Interpreter interpreter = new Interpreter(context, functionRegistry);
            return interpreter.interpret(ast);

        } catch (LexicalException e) {
            throw new RuleEngineException("词法分析失败: " + e.getMessage(), e);
        } catch (ParseException e) {
            throw new RuleEngineException("语法分析失败: " + e.getMessage(), e);
        } catch (EvaluationException e) {
            throw new RuleEngineException("规则执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行布尔规则并返回结果
     *
     * @param rule DSL规则表达式
     * @param context 上下文变量
     * @return 布尔结果
     * @throws RuleEngineException 规则执行异常
     */
    public boolean evaluate(String rule, Map<String, Object> context) {
        Object result = execute(rule, context);
        if (!(result instanceof Boolean)) {
            throw new RuleEngineException("规则表达式未返回布尔值，实际返回: " + result.getClass().getSimpleName());
        }
        return (Boolean) result;
    }

    /**
     * 注册自定义函数
     *
     * @param name 函数名
     * @param function 函数实现
     */
    public void registerFunction(String name, com.huakuangtong.ruleengine.interpreter.Function function) {
        functionRegistry.registerFunction(name, function);
    }

    /**
     * 解析规则为AST（用于调试和优化）
     *
     * @param rule DSL规则表达式
     * @return AST节点
     */
    public ASTNode parse(String rule) {
        try {
            Lexer lexer = new Lexer(rule);
            var tokens = lexer.tokenize();
            Parser parser = new Parser(tokens);
            return parser.parse();
        } catch (LexicalException e) {
            throw new RuleEngineException("词法分析失败: " + e.getMessage(), e);
        } catch (ParseException e) {
            throw new RuleEngineException("语法分析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 编译规则（缓存AST以提高性能）
     *
     * @param rule DSL规则表达式
     * @return 编译后的规则
     */
    public CompiledRule compile(String rule) {
        ASTNode ast = parse(rule);
        return new CompiledRule(ast, functionRegistry);
    }
}
