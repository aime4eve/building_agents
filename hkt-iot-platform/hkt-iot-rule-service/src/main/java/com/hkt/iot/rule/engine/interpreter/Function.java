package com.hkt.iot.rule.engine.interpreter;

/**
 * 函数接口
 *
 * @author AI Engineer
 * @since 1.0.0
 */
@FunctionalInterface
public interface Function {
    /**
     * 应用函数
     *
     * @param args 参数数组
     * @return 返回值
     * @throws Exception 函数执行异常
     */
    Object apply(Object[] args) throws Exception;
}
