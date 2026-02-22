package com.huakuangtong.ruleengine.interpreter;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 函数注册表
 * 注册和管理内置函数
 *
 * @author AI Engineer
 * @since 1.0.0
 */
public class FunctionRegistry {
    private final Map<String, Function> functions = new HashMap<>();

    public FunctionRegistry() {
        registerBuiltInFunctions();
    }

    /**
     * 注册函数
     */
    public void registerFunction(String name, Function function) {
        functions.put(name, function);
    }

    /**
     * 获取函数
     */
    public Function getFunction(String name) {
        return functions.get(name);
    }

    /**
     * 注册内置函数
     */
    private void registerBuiltInFunctions() {
        // avg(数值数组) - 计算平均值
        registerFunction("avg", args -> {
            validateArgCount(args, "avg", 1, 2);
            Object data = args[0];
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                return list.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .average()
                        .orElse(0);
            }
            if (data.getClass().isArray()) {
                Object[] array = (Object[]) data;
                double sum = 0;
                int count = 0;
                for (Object item : array) {
                    if (item instanceof Number) {
                        sum += ((Number) item).doubleValue();
                        count++;
                    }
                }
                return count > 0 ? sum / count : 0;
            }
            throw new IllegalArgumentException("avg() requires an array or list");
        });

        // sum(数值数组) - 计算总和
        registerFunction("sum", args -> {
            validateArgCount(args, "sum", 1);
            Object data = args[0];
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                return list.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .sum();
            }
            if (data.getClass().isArray()) {
                Object[] array = (Object[]) data;
                double sum = 0;
                for (Object item : array) {
                    if (item instanceof Number) {
                        sum += ((Number) item).doubleValue();
                    }
                }
                return sum;
            }
            throw new IllegalArgumentException("sum() requires an array or list");
        });

        // max(数值数组) - 计算最大值
        registerFunction("max", args -> {
            validateArgCount(args, "max", 1);
            Object data = args[0];
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                return list.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .max()
                        .orElse(0);
            }
            throw new IllegalArgumentException("max() requires an array or list");
        });

        // min(数值数组) - 计算最小值
        registerFunction("min", args -> {
            validateArgCount(args, "min", 1);
            Object data = args[0];
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                return list.stream()
                        .filter(v -> v instanceof Number)
                        .mapToDouble(v -> ((Number) v).doubleValue())
                        .min()
                        .orElse(0);
            }
            throw new IllegalArgumentException("min() requires an array or list");
        });

        // count(数组) - 计算数量
        registerFunction("count", args -> {
            validateArgCount(args, "count", 1);
            Object data = args[0];
            if (data instanceof List) {
                return ((List<?>) data).size();
            }
            if (data.getClass().isArray()) {
                return ((Object[]) data).length;
            }
            return 1;
        });

        // last(数组) - 获取最后一个元素
        registerFunction("last", args -> {
            validateArgCount(args, "last", 1);
            Object data = args[0];
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                return list.isEmpty() ? null : list.get(list.size() - 1);
            }
            if (data.getClass().isArray()) {
                Object[] array = (Object[]) data;
                return array.length > 0 ? array[array.length - 1] : null;
            }
            return data;
        });

        // first(数组) - 获取第一个元素
        registerFunction("first", args -> {
            validateArgCount(args, "first", 1);
            Object data = args[0];
            if (data instanceof List) {
                List<?> list = (List<?>) data;
                return list.isEmpty() ? null : list.get(0);
            }
            if (data.getClass().isArray()) {
                Object[] array = (Object[]) data;
                return array.length > 0 ? array[0] : null;
            }
            return data;
        });

        // diff(a, b) - 计算差值
        registerFunction("diff", args -> {
            validateArgCount(args, "diff", 2);
            if (!(args[0] instanceof Number) || !(args[1] instanceof Number)) {
                throw new IllegalArgumentException("diff() requires numeric arguments");
            }
            return ((Number) args[0]).doubleValue() - ((Number) args[1]).doubleValue();
        });

        // rate(value, timeWindow) - 计算变化率
        registerFunction("rate", args -> {
            validateArgCount(args, "rate", 2);
            if (!(args[0] instanceof Number) || !(args[1] instanceof Number)) {
                throw new IllegalArgumentException("rate() requires numeric arguments");
            }
            double value = ((Number) args[0]).doubleValue();
            double timeWindow = ((Number) args[1]).doubleValue();
            return timeWindow > 0 ? value / timeWindow : 0;
        });

        // now() - 获取当前时间
        registerFunction("now", args -> {
            validateArgCount(args, "now", 0);
            return LocalDateTime.now();
        });

        // today() - 获取今天日期
        registerFunction("today", args -> {
            validateArgCount(args, "today", 0);
            return LocalDate.now();
        });

        // toUpper(str) - 转大写
        registerFunction("toUpper", args -> {
            validateArgCount(args, "toUpper", 1);
            return args[0].toString().toUpperCase();
        });

        // toLower(str) - 转小写
        registerFunction("toLower", args -> {
            validateArgCount(args, "toLower", 1);
            return args[0].toString().toLowerCase();
        });

        // length(str|array) - 获取长度
        registerFunction("length", args -> {
            validateArgCount(args, "length", 1);
            Object arg = args[0];
            if (arg instanceof String) {
                return ((String) arg).length();
            }
            if (arg instanceof List) {
                return ((List<?>) arg).size();
            }
            if (arg.getClass().isArray()) {
                return ((Object[]) arg).length;
            }
            return 0;
        });

        // abs(num) - 绝对值
        registerFunction("abs", args -> {
            validateArgCount(args, "abs", 1);
            if (!(args[0] instanceof Number)) {
                throw new IllegalArgumentException("abs() requires a numeric argument");
            }
            return Math.abs(((Number) args[0]).doubleValue());
        });
    }

    private void validateArgCount(Object[] args, String funcName, int expected) {
        if (args.length != expected) {
            throw new IllegalArgumentException(
                    funcName + "() requires " + expected + " argument(s), got: " + args.length);
        }
    }

    private void validateArgCount(Object[] args, String funcName, int min, int max) {
        if (args.length < min || args.length > max) {
            throw new IllegalArgumentException(
                    funcName + "() requires " + min + "-" + max + " argument(s), got: " + args.length);
        }
    }
}
