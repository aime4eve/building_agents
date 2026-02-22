# 规则引擎使用指南

## 目录

1. [概述](#概述)
2. [规则表达式语法](#规则表达式语法)
3. [规则类型配置](#规则类型配置)
4. [REST API接口](#rest-api接口)
5. [规则配置示例](#规则配置示例)
6. [事件集成](#事件集成)

---

## 概述

规则引擎是华宽通智能体平台的核心组件，负责：

- 实时评估设备遥测数据
- 触发告警、联动、计费、控制动作
- 支持复杂的条件表达式和逻辑运算
- 提供规则版本管理和测试调试功能

---

## 规则表达式语法

### 基本语法

规则表达式基于DSL（领域特定语言），支持以下语法：

#### 1. 比较运算符

| 运算符 | 说明 | 示例 |
|--------|------|------|
| `==` | 等于 | `temperature == 25` |
| `!=` | 不等于 | `status != 'offline'` |
| `>` | 大于 | `temperature > 30` |
| `>=` | 大于等于 | `humidity >= 60` |
| `<` | 小于 | `battery < 20` |
| `<=` | 小于等于 | `pressure <= 100` |

#### 2. 逻辑运算符

| 运算符 | 说明 | 示例 |
|--------|------|------|
| `and` | 并且 | `temp > 30 and humidity < 50` |
| `or` | 或者 | `status == 'alarm' or status == 'warning'` |
| `not` | 非 | `not isOnline` |

#### 3. 内置函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `avg()` | 平均值 | `avg(temperature, 5) > 25` |
| `sum()` | 求和 | `sum(energy, 24) > 100` |
| `max()` | 最大值 | `max(temperature, 10) > 35` |
| `min()` | 最小值 | `min(temperature, 10) < 10` |
| `count()` | 计数 | `count(alerts, 3600) > 5` |
| `first()` | 第一个值 | `first(temperature) > 20` |
| `last()` | 最后一个值 | `last(temperature) > 20` |
| `diff()` | 差值 | `diff(temperature) > 5` |
| `rate()` | 变化率 | `rate(energy) > 0.5` |

#### 4. 时间函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `now()` | 当前时间 | `timestamp > now() - 300` |
| `today()` | 今天日期 | `date == today()` |

#### 5. 字符串函数

| 函数 | 说明 | 示例 |
|------|------|------|
| `toUpper()` | 转大写 | `toUpper(status) == 'ONLINE'` |
| `toLower()` | 转小写 | `toLower(type) == 'sensor'` |
| `length()` | 长度 | `length(message) > 0` |
| `contains()` | 包含 | `contains(message, 'error')` |
| `matches()` | 正则匹配 | `matches(sn, '^DEV-\\d+$')` |

### 复杂表达式示例

```javascript
// 温度告警：温度持续5分钟超过30度
avg(temperature, 5) > 30 and duration > 300

// 设备离线告警：设备离线且电池电量低
not isOnline and battery < 20

// 能耗异常：能耗在过去1小时增长超过50%
rate(energy) > 0.5 and duration > 3600

// 复合条件：温度高或湿度低，且设备在线
(temperature > 35 or humidity < 30) and isOnline

// 时间窗口：工作时间内（9-18点）触发
now() >= today() + 9h and now() <= today() + 18h
```

---

## 规则类型配置

### 1. 告警规则 (ALARM)

当设备数据满足条件时触发告警。

**配置结构：**
```json
{
  "threshold": 30,           // 告警阈值
  "comparison": ">",          // 比较操作符: >, >=, <, <=, ==, !=
  "severity": "WARNING",      // 严重级别: CRITICAL, WARNING, INFO
  "duration": 60,             // 持续时间(秒)，0表示立即触发
  "aggregation": "avg"        // 聚合方式: last, avg, max, min, sum
}
```

**示例：**
```json
{
  "threshold": 30,
  "comparison": ">",
  "severity": "WARNING",
  "duration": 300,
  "aggregation": "avg"
}
```

### 2. 联动规则 (LINKAGE)

当条件满足时，控制多个设备执行联动动作。

**配置结构：**
```json
{
  "triggerCondition": "温度传感器温度超过30度",
  "targetDevices": [1001, 1002],    // 目标设备ID列表
  "actions": [                        // 动作列表
    {
      "deviceId": 1001,
      "action": "TURN_ON"
    },
    {
      "deviceId": 1002,
      "action": "TURN_OFF"
    }
  ],
  "delayMs": 0                        // 延迟执行(毫秒)
}
```

### 3. 计费规则 (BILLING)

根据设备用量或时间周期计算费用。

**配置结构：**
```json
{
  "rate": 0.5,                         // 费率
  "unit": "kWh",                       // 计费单位
  "tieredRates": [                     // 阶梯费率
    {"min": 0, "max": 100, "rate": 0.5},
    {"min": 100, "max": 300, "rate": 0.6},
    {"min": 300, "max": null, "rate": 0.8}
  ],
  "fixedFee": 10.0,                    // 固定费用
  "billingCycle": "MONTHLY"            // 计费周期: DAILY, WEEKLY, MONTHLY
}
```

### 4. 控制规则 (CONTROL)

当条件满足时，控制指定设备执行动作。

**配置结构：**
```json
{
  "controlType": "SWITCH",             // 控制类型: SWITCH, DIMMER, THERMOSTAT
  "targetDevice": 2001,                // 目标设备ID
  "controlValue": true,                // 控制值
  "duration": 0                        // 持续时间(秒)，0表示永久
}
```

---

## REST API接口

### 基础路径
```
http://localhost:8084/api/v1
```

### 规则管理 API

#### 1. 创建规则
```
POST /rules
Content-Type: application/json

{
  "tenantId": 1,
  "ruleCode": "TEMP_ALARM_001",
  "ruleName": "温度告警规则",
  "ruleType": "ALARM",
  "ruleCategory": "环境监控",
  "description": "温度超过30度触发告警",
  "triggerType": "REALTIME",
  "triggerExpression": "temperature > 30",
  "ruleConfig": {
    "threshold": 30,
    "comparison": ">",
    "severity": "WARNING",
    "duration": 300,
    "aggregation": "avg"
  },
  "deviceIds": [1001, 1002],
  "createdBy": 1
}
```

#### 2. 更新规则
```
PUT /rules/{ruleId}
Content-Type: application/json

{
  "ruleName": "温度告警规则（已更新）",
  "description": "温度超过35度触发告警",
  "triggerExpression": "temperature > 35",
  "updatedBy": 1
}
```

#### 3. 启用/禁用规则
```
POST /rules/{ruleId}/enable?operatorId=1
POST /rules/{ruleId}/disable?operatorId=1
```

#### 4. 查询规则列表
```
GET /rules?tenantId=1
GET /rules?tenantId=1&ruleType=ALARM
GET /rules/active?tenantId=1
```

#### 5. 查询规则详情
```
GET /rules/{ruleId}
```

### 规则执行 API

#### 1. 手动执行规则
```
POST /rules/execution/{ruleId}
Content-Type: application/json

{
  "temperature": 32,
  "humidity": 45,
  "deviceId": "DEV001",
  "timestamp": "2026-02-21T10:30:00Z"
}
```

#### 2. 测试规则（不更新统计）
```
POST /rules/{ruleId}/test
Content-Type: application/json

{
  "temperature": 32,
  "humidity": 45
}
```

#### 3. 模拟遥测数据触发
```
POST /rules/execution/simulate-telemetry
Content-Type: application/json

{
  "tenantId": 1,
  "deviceId": "DEV001",
  "deviceSn": "SN20260221001",
  "deviceType": "TEMP_SENSOR",
  "spaceId": 100,
  "telemetryData": {
    "temperature": 32,
    "humidity": 45
  },
  "metadata": {
    "battery": 85,
    "rssi": -60
  }
}
```

### 规则测试 API

#### 1. 测试规则条件
```
POST /rules/test/{ruleId}/condition
Content-Type: application/json

{
  "temperature": 32,
  "humidity": 45
}
```

#### 2. 调试规则
```
POST /rules/test/{ruleId}/debug
Content-Type: application/json

{
  "expression": "temperature > 30 and humidity < 50",
  "context": {
    "temperature": 32,
    "humidity": 45
  }
}
```

#### 3. 批量测试
```
POST /rules/test/{ruleId}/batch-test
Content-Type: application/json

{
  "testCases": [
    {
      "name": "测试用例1：温度高湿度低",
      "context": {"temperature": 35, "humidity": 30},
      "expectedMatch": true
    },
    {
      "name": "测试用例2：温度正常",
      "context": {"temperature": 25, "humidity": 50},
      "expectedMatch": false
    }
  ]
}
```

### 规则配置 API

#### 1. 获取规则类型
```
GET /rules/config/types
```

响应：
```json
{
  "types": ["ALARM", "LINKAGE", "BILLING", "CONTROL"],
  "descriptions": {
    "ALARM": "告警规则 - 设备数据触发告警",
    "LINKAGE": "联动规则 - 多设备协同控制",
    "BILLING": "计费规则 - 基于用量或时间计费",
    "CONTROL": "控制规则 - 单设备控制"
  }
}
```

#### 2. 获取配置模板
```
GET /rules/config/templates/alarm
GET /rules/config/templates/linkage
GET /rules/config/templates/billing
GET /rules/config/templates/control
```

#### 3. 验证配置
```
POST /rules/config/types/ALARM/validate
Content-Type: application/json

{
  "threshold": 30,
  "comparison": ">",
  "severity": "WARNING"
}
```

### 规则集 API

#### 1. 创建规则集
```
POST /rule-sets
Content-Type: application/json

{
  "tenantId": 1,
  "setCode": "ENV_MONITOR_SET",
  "setName": "环境监控规则集",
  "description": "环境监控相关规则",
  "setCategory": "监控",
  "spaceId": 100,
  "ruleIds": [1, 2, 3],
  "executionStrategy": "ALL",
  "createdBy": 1
}
```

#### 2. 管理规则集中的规则
```
POST /rule-sets/{ruleSetId}/rules     // 添加规则
DELETE /rule-sets/{ruleSetId}/rules  // 移除规则
```

### 规则版本 API

#### 1. 获取规则版本列表
```
GET /rules/{ruleId}/versions
```

#### 2. 版本比较
```
GET /rules/{ruleId}/versions/compare?version1=1&version2=2
```

#### 3. 恢复版本
```
POST /rules/{ruleId}/versions/{versionNumber}/restore
Content-Type: application/json

{
  "restoredBy": 1
}
```

---

## 规则配置示例

### 示例1：温度告警规则

```json
{
  "tenantId": 1,
  "ruleCode": "TEMP_HIGH_ALARM",
  "ruleName": "温度过高告警",
  "ruleType": "ALARM",
  "triggerExpression": "temperature > 35",
  "ruleConfig": {
    "threshold": 35,
    "comparison": ">",
    "severity": "WARNING",
    "duration": 300,
    "aggregation": "avg"
  },
  "deviceIds": [1001, 1002, 1003]
}
```

### 示例2：设备离线联动规则

```json
{
  "tenantId": 1,
  "ruleCode": "OFFLINE_LINKAGE",
  "ruleName": "设备离线联动",
  "ruleType": "LINKAGE",
  "triggerExpression": "isOnline == false",
  "ruleConfig": {
    "triggerCondition": "主设备离线",
    "targetDevices": [2001, 2002],
    "actions": [
      {"deviceId": 2001, "action": "TURN_ON"},
      {"deviceId": 2002, "action": "NOTIFY"}
    ],
    "delayMs": 5000
  }
}
```

### 示例3：能耗计费规则

```json
{
  "tenantId": 1,
  "ruleCode": "ENERGY_BILLING",
  "ruleName": "能耗计费",
  "ruleType": "BILLING",
  "triggerExpression": "energy_consumption > 0",
  "ruleConfig": {
    "rate": 0.5,
    "unit": "kWh",
    "tieredRates": [
      {"min": 0, "max": 100, "rate": 0.5},
      {"min": 100, "max": 300, "rate": 0.6},
      {"min": 300, "max": null, "rate": 0.8}
    ],
    "fixedFee": 10.0,
    "billingCycle": "MONTHLY"
  }
}
```

### 示例4：温湿度联动控制规则

```json
{
  "tenantId": 1,
  "ruleCode": "ENV_CONTROL",
  "ruleName": "环境联动控制",
  "ruleType": "LINKAGE",
  "triggerExpression": "(temperature > 30 or humidity < 40) and isOnline",
  "ruleConfig": {
    "triggerCondition": "温湿度异常",
    "targetDevices": [3001],
    "actions": [
      {"deviceId": 3001, "action": "TURN_ON_AC", "params": {"temp": 24}}
    ],
    "delayMs": 0
  }
}
```

---

## 事件集成

规则引擎通过Kafka订阅以下事件：

### 1. 遥测数据接收事件 (TelemetryReceivedEvent)

**Topic:** `device-telemetry`

**事件结构：**
```json
{
  "msgId": "msg-20260221-001",
  "deviceId": "DEV001",
  "deviceSn": "SN20260221001",
  "deviceType": "TEMP_SENSOR",
  "tenantId": 1,
  "spaceId": 100,
  "timestamp": "2026-02-21T10:30:00Z",
  "data": {
    "temperature": 25.5,
    "humidity": 60.2
  },
  "metadata": {
    "battery": 85,
    "rssi": -60
  }
}
```

### 2. 设备状态变化事件 (DeviceStatusChangedEvent)

**Topic:** `device-status`

**事件结构：**
```json
{
  "msgId": "msg-20260221-002",
  "deviceId": 1001,
  "deviceSn": "SN20260221001",
  "tenantId": 1,
  "oldStatus": "ONLINE",
  "newStatus": "OFFLINE",
  "changedAt": "2026-02-21T10:35:00Z"
}
```

### 3. 规则触发事件 (RuleTriggeredEvent)

**Topic:** `rule-triggered`

**事件结构：**
```json
{
  "eventId": "evt-20260221-001",
  "aggregateId": "1",
  "aggregateType": "Rule",
  "eventType": "RuleTriggered",
  "occurredAt": "2026-02-21T10:30:05Z",
  "ruleId": 1,
  "ruleCode": "TEMP_HIGH_ALARM",
  "ruleName": "温度过高告警",
  "tenantId": 1,
  "ruleType": "ALARM",
  "triggerContext": {
    "temperature": 35.5,
    "deviceId": "DEV001"
  }
}
```

### 4. 规则执行失败事件 (RuleExecutionFailedEvent)

**Topic:** `rule-execution-failed`

**事件结构：**
```json
{
  "eventId": "evt-20260221-002",
  "aggregateId": "1",
  "aggregateType": "Rule",
  "eventType": "RuleExecutionFailed",
  "occurredAt": "2026-02-21T10:31:00Z",
  "ruleId": 1,
  "ruleCode": "TEMP_HIGH_ALARM",
  "tenantId": 1,
  "errorMessage": "执行超时",
  "errorType": "TimeoutException"
}
```

---

## 触发方式

规则支持三种触发方式：

| 触发类型 | 说明 | 适用场景 |
|---------|------|----------|
| REALTIME | 实时触发 | 设备数据变化时立即评估 |
| SCHEDULED | 定时触发 | 按Cron表达式周期性执行 |
| MANUAL | 手动触发 | 通过API手动执行 |

### Cron表达式示例

```
0 */5 * * * *     # 每5分钟执行一次
0 0 * * * *       # 每小时执行一次
0 0 0 * * *       # 每天凌晨执行
0 0 9-18 * * MON-FRI  # 工作日9-18点每小时执行
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| RULE_NOT_FOUND | 规则不存在 |
| RULE_CODE_EXISTS | 规则编码已存在 |
| INVALID_EXPRESSION | 表达式语法错误 |
| INVALID_CONFIG | 配置验证失败 |
| RULE_NOT_ACTIVE | 规则未激活 |
| EXECUTION_TIMEOUT | 执行超时 |
| EXECUTION_FAILED | 执行失败 |

---

## 附录

### 常见问题

**Q: 如何调试规则表达式？**
A: 使用 `/rules/test/{ruleId}/debug` 接口，传入表达式和上下文进行调试。

**Q: 如何批量测试规则？**
A: 使用 `/rules/test/{ruleId}/batch-test` 接口，传入多个测试用例。

**Q: 规则执行失败会如何处理？**
A: 系统会发布 `RuleExecutionFailedEvent` 事件，并记录执行日志。

**Q: 如何恢复规则的历史版本？**
A: 使用 `/rules/{ruleId}/versions/{versionNumber}/restore` 接口恢复。

**Q: 规则集和规则有什么区别？**
A: 规则集是一组规则的集合，支持批量管理和统一执行策略。
