package com.huakuangtong.ruleengine;

import com.huakuangtong.ruleengine.ast.ASTNode;
import com.huakuangtong.ruleengine.interpreter.EvaluationException;
import com.huakuangtong.ruleengine.interpreter.FunctionRegistry;
import com.huakuangtong.ruleengine.interpreter.Interpreter;

import java.util.Map;

/**
 * 编译后的规则
 * 可以重复执行，避免重复解析
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class CompiledRule {
    private final ASTNode ast;
    private final FunctionRegistry functionRegistry;

    public CompiledRule(ASTNode ast, FunctionRegistry functionRegistry) {
        this.ast = ast;
        this.functionRegistry = functionRegistry;
    }

    /**
     * 执行编译后的规则
     *
     * @param context 上下文变量
     * @return 执行结果
     */
    public Object execute(Map<String, Object> context) {
        try {
            Interpreter interpreter = new Interpreter(context, functionRegistry);
            return interpreter.interpret(ast);
        } catch (EvaluationException e) {
            throw new RuleEngineException("规则执行失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行布尔规则
     *
     * @param context 上下文变量
     * @return 布尔结果
     */
    public boolean evaluate(Map<String, Object> context) {
        Object result = execute(context);
        if (!(result instanceof Boolean)) {
            throw new RuleEngineException("规则表达式未返回布尔值，实际返回: " + result.getClass().getSimpleName());
        }
        return (Boolean) result;
    }

    /**
     * 获取AST
     */
    public ASTNode getAst() {
        return ast;
    }
}
