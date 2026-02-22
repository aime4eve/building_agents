# 规则引擎DSL解析器

## 概述

规则引擎DSL解析器是华宽通智能体系统的核心组件，用于支持防霉管控、智慧畜牧等场景的规则判定。

## 架构设计

```
DSL字符串
    ↓
词法分析器 (Lexer) → Token流
    ↓
语法分析器 (Parser) → AST (抽象语法树)
    ↓
解释器 (Interpreter) → 执行结果
```

## 支持的语法

### 操作符

| 类型 | 操作符 | 说明 |
|------|--------|------|
| 比较操作符 | `>`, `>=`, `<`, `<=`, `==`, `!=` | 数值比较 |
| 字符串操作符 | `contains`, `matches` | 字符串匹配 |
| 集合操作符 | `in`, `between` | 成员判断 |
| 逻辑操作符 | `&&`, `\|\|`, `!` | 逻辑运算 |
| 算术操作符 | `+`, `-`, `*`, `/`, `%` | 数学运算 |

### 内置函数

| 函数 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `avg(array)` | 数值数组 | Number | 计算平均值 |
| `sum(array)` | 数值数组 | Number | 计算总和 |
| `max(array)` | 数值数组 | Number | 计算最大值 |
| `min(array)` | 数值数组 | Number | 计算最小值 |
| `count(array)` | 数组 | Integer | 计算元素数量 |
| `first(array)` | 数组 | Object | 获取第一个元素 |
| `last(array)` | 数组 | Object | 获取最后一个元素 |
| `diff(a, b)` | Number, Number | Number | 计算差值 |
| `rate(value, window)` | Number, Number | Number | 计算变化率 |
| `now()` | 无 | DateTime | 当前时间 |
| `today()` | 无 | Date | 今天日期 |
| `toUpper(str)` | String | String | 转大写 |
| `toLower(str)` | String | String | 转小写 |
| `value)` | String/Array | Integer | 获取长度 |
| `abs(num)` | Number | Number | 绝对值 |

### 示例规则

```java
// 设备温度告警规则
"temperature > 30"

// 使用函数的规则
"avg(temperatures, 1h) > 25"

// 复合条件
"(temperature > 30 || humidity > 80) && device.online == true"

// 防霉管控场景
"temperature > 30 && humidity > 80"

// 智慧畜牧场景
"healthScore < 70 || bodyTemperature > 39"

// 字符串匹配
"message contains 'error'"

// 数组操作
"value in [1, 2, 3]"
"value between [1, 10]"
```

## 使用方法

### 基本使用

```java
// 创建规则引擎
RuleEngine ruleEngine = new RuleEngine();

// 准备上下文数据
Map<String, Object> context = new HashMap<>();
context.put("temperature", 35);
context.put("humidity", 85);

// 执行规则
boolean result = ruleEngine.evaluate("temperature > 30 && humidity > 80", context);
```

### 编译规则（性能优化）

```java
// 编译规则（只解析一次）
CompiledRule rule = ruleEngine.compile("temperature > 30 && humidity > 80");

// 重复执行（避免重复解析）
Map<String, Object> context = new HashMap<>();
context.put("temperature", 35);

boolean result = rule.evaluate(context);
```

### 自定义函数

```java
RuleEngine customEngine = new RuleEngine();
customEngine.registerFunction("double", args -> {
    double value = ((Number) args[0]).doubleValue();
    return value * 2;
});

Object result = customEngine.execute("double(value)", context);
```

## 性能指标

| 场景 | 平均耗时 | 吞吐量 |
|------|----------|--------|
| 简单规则 | <50μs | >20000次/秒 |
| 复杂规则 | <100μs | >10000次/秒 |
| 编译规则 | <10μs | >100000次/秒 |
| 1000条规则 | <100ms | - |

## 文件结构

```
com/hkt/iot/rule/engine/
├── lexer/              # 词法分析器
│   ├── TokenType.java  # Token类型枚举
│   ├── Token.java      # Token类
│   ├── Lexer.java      # 词法分析器
│   └── LexicalException.java
├── parser/             # 语法分析器
│   ├── Parser.java     # 语法分析器
│   └── ParseException.java
├── ast/                # 抽象语法树
│   ├── ASTNode.java    # AST节点接口
│   ├── ASTVisitor.java # 访问者接口
│   ├── BinaryExpression.java
│   ├── UnaryExpression.java
│   ├── FunctionCall.java
│   ├── Identifier.java
│   ├── Literal.java
│   └── ArrayLiteral.java
├── interpreter/         # 解释器
│   ├── Interpreter.java
│   ├── Function.java
│   ├── FunctionRegistry.java
│   └── EvaluationException.java
├── RuleEngine.java     # 统一入口
├── CompiledRule.java   # 编译规则
└── RuleEngineException.java
```

## 测试

运行单元测试：
```bash
mvn test -Dtest=LexerTest
mvn test -Dtest=ParserTest
mvn test -Dtest=InterpreterTest
mvn test -Dtest=PerformanceTest
```

运行演示程序：
```bash
java com.hkt.iot.rule.engine.demo.RuleEngineDemo
```

## 与系统集成

规则引擎将集成到以下服务：
- **hkt-iot-rule-service**: 规则引擎服务，提供规则管理和执行能力
- **hkt-iot-device-service**: 设备管理服务，用于设备状态规则判定
- **场景联动引擎**: 触发条件判定

## 扩展性

### 添加新的操作符

1. 在 `TokenType.java` 添加新的Token类型
2. 在 `Lexer.java` 添加词法识别
3. 在 `Parser.java` 添加语法解析
4. 在 `Interpreter.java` 添加求值逻辑

### 添加新的内置函数

在 `FunctionRegistry.java` 的 `registerBuiltInFunctions()` 方法中注册：

```java
registerFunction("myFunction", args -> {
    // 函数实现
    return result;
});
```

## 设计文档

详见《华宽通智能体-系统设计说明书.md》中规则引擎相关章节。

## 作者

AI Engineer

## 版本

1.0.0
