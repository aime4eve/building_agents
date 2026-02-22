# 规则引擎API接口文档

## 基础信息

- **服务名称**: hkt-iot-rule-service
- **服务端口**: 8084
- **API版本**: v1
- **基础路径**: /api/v1
- **协议**: HTTP/HTTPS
- **数据格式**: JSON

---

## 通用响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误描述",
  "errors": ["详细错误信息"]
}
```

---

## 1. 规则管理 API

### 1.1 创建规则

**接口地址**: `POST /rules`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tenantId | Long | 是 | 租户ID |
| ruleCode | String | 是 | 规则编码（唯一） |
| ruleName | String | 是 | 规则名称 |
| ruleType | String | 是 | 规则类型: ALARM/LINKAGE/BILLING/CONTROL |
| ruleCategory | String | 否 | 规则分类 |
| description | String | 否 | 规则描述 |
| triggerType | String | 是 | 触发类型: REALTIME/SCHEDULED/MANUAL |
| triggerExpression | String | 是 | 触发表达式 |
| ruleConfig | Map | 否 | 规则配置 |
| deviceIds | List<Long> | 否 | 关联设备ID列表 |
| effectiveTime | DateTime | 否 | 生效时间 |
| expireTime | DateTime | 否 | 过期时间 |
| cronExpression | String | 否 | Cron表达式（定时触发） |
| createdBy | Long | 是 | 创建人ID |

**响应**:
```json
{
  "code": 200,
  "message": "规则创建成功",
  "data": 123  // 规则ID
}
```

### 1.2 更新规则

**接口地址**: `PUT /rules/{ruleId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleName | String | 否 | 规则名称 |
| description | String | 否 | 规则描述 |
| triggerExpression | String | 否 | 触发表达式 |
| ruleConfig | Map | 否 | 规则配置 |
| deviceIds | List<Long> | 否 | 关联设备ID列表 |
| updatedBy | Long | 是 | 更新人ID |

**响应**:
```json
{
  "code": 200,
  "message": "规则更新成功"
}
```

### 1.3 启用规则

**接口地址**: `POST /rules/{ruleId}/enable`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operatorId | Long | 是 | 操作人ID |

**响应**:
```json
{
  "code": 200,
  "message": "规则已启用"
}
```

### 1.4 禁用规则

**接口地址**: `POST /rules/{ruleId}/disable`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operatorId | Long | 是 | 操作人ID |

**响应**:
```json
{
  "code": 200,
  "message": "规则已禁用"
}
```

### 1.5 归档规则

**接口地址**: `POST /rules/{ruleId}/archive`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| operatorId | Long | 是 | 操作人ID |

**响应**:
```json
{
  "code": 200,
  "message": "规则已归档"
}
```

### 1.6 删除规则

**接口地址**: `DELETE /rules/{ruleId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deletedBy | Long | 是 | 删除人ID |

**响应**:
```json
{
  "code": 200,
  "message": "规则已删除"
}
```

### 1.7 查询规则详情

**接口地址**: `GET /rules/{ruleId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 123,
    "ruleCode": "TEMP_ALARM_001",
    "ruleName": "温度告警规则",
    "ruleType": "ALARM",
    "ruleCategory": "环境监控",
    "description": "温度超过30度触发告警",
    "triggerType": "REALTIME",
    "triggerExpression": "temperature > 30",
    "ruleConfig": { ... },
    "deviceIds": [1001, 1002],
    "ruleStatus": "ACTIVE",
    "isEnabled": true,
    "rulePriority": 5,
    "totalExecutions": 100,
    "successExecutions": 95,
    "failedExecutions": 5,
    "lastExecutionTime": "2026-02-21T10:30:00",
    "createdAt": "2026-02-01T00:00:00",
    "updatedAt": "2026-02-21T10:30:00"
  }
}
```

### 1.8 查询规则列表

**接口地址**: `GET /rules`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tenantId | Long | 是 | 租户ID |
| ruleType | String | 否 | 规则类型筛选 |

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 123,
      "ruleCode": "TEMP_ALARM_001",
      "ruleName": "温度告警规则",
      "ruleType": "ALARM",
      "ruleStatus": "ACTIVE",
      "isEnabled": true,
      "rulePriority": 5,
      "createdAt": "2026-02-01T00:00:00",
      "updatedAt": "2026-02-21T10:30:00"
    }
  ]
}
```

### 1.9 查询激活规则列表

**接口地址**: `GET /rules/active`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tenantId | Long | 是 | 租户ID |

**响应**: 同1.8

---

## 2. 规则执行 API

### 2.1 手动执行规则

**接口地址**: `POST /rules/execution/{ruleId}/execute`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**请求参数**:
```json
{
  "temperature": 32,
  "humidity": 45,
  "deviceId": "DEV001",
  "timestamp": "2026-02-21T10:30:00Z"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "status": "SUCCESS",
    "message": "规则执行成功",
    "matched": true,
    "actionResults": [
      {
        "actionId": 1,
        "actionType": "ALARM",
        "success": true,
        "result": { ... }
      }
    ],
    "executedAt": "2026-02-21T10:30:05"
  }
}
```

### 2.2 测试规则（不更新统计）

**接口地址**: `POST /rules/{ruleId}/test`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**请求参数**: 同2.1

**响应**: 同2.1

### 2.3 模拟遥测数据触发

**接口地址**: `POST /rules/execution/simulate-telemetry`

**请求参数**:
```json
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

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "status": "SUCCESS",
      "message": "规则执行成功",
      "matched": true,
      "executedAt": "2026-02-21T10:30:05"
    }
  ]
}
```

### 2.4 批量执行激活规则

**接口地址**: `POST /rules/execution/tenant/{tenantId}/execute-all`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tenantId | Long | 是 | 租户ID |

**请求参数**:
```json
{
  "temperature": 32,
  "humidity": 45
}
```

**响应**: 同2.3

---

## 3. 规则测试 API

### 3.1 测试规则条件

**接口地址**: `POST /rules/test/{ruleId}/condition`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**请求参数**:
```json
{
  "temperature": 32,
  "humidity": 45
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "matched": true,
    "status": "MATCHED",
    "message": "条件匹配",
    "errorType": null
  }
}
```

### 3.2 调试规则

**接口地址**: `POST /rules/test/{ruleId}/debug`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**请求参数**:
```json
{
  "expression": "temperature > 30 and humidity < 50",
  "context": {
    "temperature": 32,
    "humidity": 45
  }
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "success": true,
    "message": "Debug completed",
    "expressionValid": true,
    "conditionMatched": true,
    "contextSnapshot": { ... },
    "errors": []
  }
}
```

### 3.3 批量测试规则

**接口地址**: `POST /rules/test/{ruleId}/batch-test`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**请求参数**:
```json
{
  "testCases": [
    {
      "name": "测试用例1",
      "context": {"temperature": 35, "humidity": 30},
      "expectedMatch": true
    },
    {
      "name": "测试用例2",
      "context": {"temperature": 25, "humidity": 50},
      "expectedMatch": false
    }
  ]
}
```

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "testName": "测试用例1",
      "success": true,
      "matched": true,
      "status": "MATCHED",
      "error": null
    }
  ]
}
```

### 3.4 获取规则变量

**接口地址**: `GET /rules/test/{ruleId}/variables`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**响应**:
```json
{
  "code": 200,
  "data": {
    "ruleId": 123,
    "expression": "temperature > 30 and humidity < 50",
    "variables": ["temperature", "humidity"],
    "extractedAt": "2026-02-21T10:30:00"
  }
}
```

---

## 4. 规则配置 API

### 4.1 获取规则类型列表

**接口地址**: `GET /rules/config/types`

**响应**:
```json
{
  "code": 200,
  "data": {
    "types": ["ALARM", "LINKAGE", "BILLING", "CONTROL"],
    "descriptions": {
      "ALARM": "告警规则 - 设备数据触发告警",
      "LINKAGE": "联动规则 - 多设备协同控制",
      "BILLING": "计费规则 - 基于用量或时间计费",
      "CONTROL": "控制规则 - 单设备控制"
    }
  }
}
```

### 4.2 获取默认配置

**接口地址**: `GET /rules/config/types/{ruleType}/default`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleType | String | 是 | 规则类型 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "threshold": 0,
    "comparison": ">",
    "severity": "WARNING",
    "duration": 0,
    "aggregation": "last"
  }
}
```

### 4.3 验证配置

**接口地址**: `POST /rules/config/types/{ruleType}/validate`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleType | String | 是 | 规则类型 |

**请求参数**:
```json
{
  "threshold": 30,
  "comparison": ">",
  "severity": "WARNING"
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "valid": true,
    "errorMessage": null,
    "errors": []
  }
}
```

### 4.4 获取配置模板

**接口地址**:
- `GET /rules/config/templates/alarm`
- `GET /rules/config/templates/linkage`
- `GET /rules/config/templates/billing`
- `GET /rules/config/templates/control`

**响应**:
```json
{
  "code": 200,
  "data": {
    "typeName": "告警规则",
    "description": "当设备数据满足条件时触发告警",
    "configSchema": { ... },
    "example": { ... },
    "expressionExample": "temperature > 30 and humidity < 50"
  }
}
```

---

## 5. 规则集 API

### 5.1 创建规则集

**接口地址**: `POST /rule-sets`

**请求参数**:
```json
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

**响应**:
```json
{
  "code": 200,
  "data": 456  // 规则集ID
}
```

### 5.2 更新规则集

**接口地址**: `PUT /rule-sets/{ruleSetId}`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleSetId | Long | 是 | 规则集ID |

**请求参数**:
```json
{
  "setName": "环境监控规则集（已更新）",
  "description": "更新后的描述",
  "priority": 10,
  "updatedBy": 1
}
```

### 5.3 添加规则到规则集

**接口地址**: `POST /rule-sets/{ruleSetId}/rules`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleSetId | Long | 是 | 规则集ID |

**请求参数**:
```json
{
  "ruleIds": [4, 5, 6]
}
```

### 5.4 从规则集移除规则

**接口地址**: `DELETE /rule-sets/{ruleSetId}/rules`

**路径参数**: 同5.3

**请求参数**: 同5.3

### 5.5 激活/停用规则集

**接口地址**:
- `POST /rule-sets/{ruleSetId}/activate`
- `POST /rule-sets/{ruleSetId}/deactivate`

### 5.6 查询规则集列表

**接口地址**: `GET /rule-sets`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| tenantId | Long | 是 | 租户ID |

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 456,
      "setCode": "ENV_MONITOR_SET",
      "setName": "环境监控规则集",
      "setCategory": "监控",
      "setStatus": "ACTIVE",
      "priority": 5,
      "ruleCount": 3,
      "executionStrategy": "ALL"
    }
  ]
}
```

---

## 6. 规则版本 API

### 6.1 获取规则版本列表

**接口地址**: `GET /rules/{ruleId}/versions`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |

**响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": 1001,
      "ruleId": 123,
      "versionNumber": 2,
      "triggerExpression": "temperature > 35",
      "changeDescription": "提高阈值",
      "changeType": "UPDATE",
      "isCurrent": true,
      "createdAt": "2026-02-21T10:00:00"
    },
    {
      "id": 1000,
      "ruleId": 123,
      "versionNumber": 1,
      "triggerExpression": "temperature > 30",
      "changeDescription": "初始版本",
      "changeType": "CREATE",
      "isCurrent": false,
      "createdAt": "2026-02-01T00:00:00"
    }
  ]
}
```

### 6.2 获取当前版本

**接口地址**: `GET /rules/{ruleId}/versions/current`

### 6.3 获取指定版本

**接口地址**: `GET /rules/{ruleId}/versions/{versionNumber}`

### 6.4 版本比较

**接口地址**: `GET /rules/{ruleId}/versions/compare`

**查询参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| version1 | Integer | 是 | 版本号1 |
| version2 | Integer | 是 | 版本号2 |

**响应**:
```json
{
  "code": 200,
  "data": {
    "version1": 1,
    "version2": 2,
    "hasExpressionChanged": true,
    "expression1": "temperature > 30",
    "expression2": "temperature > 35",
    "description1": "初始版本",
    "description2": "提高阈值"
  }
}
```

### 6.5 恢复到指定版本

**接口地址**: `POST /rules/{ruleId}/versions/{versionNumber}/restore`

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| ruleId | Long | 是 | 规则ID |
| versionNumber | Integer | 是 | 目标版本号 |

**请求参数**:
```json
{
  "restoredBy": 1
}
```

### 6.6 创建版本快照

**接口地址**: `POST /rules/{ruleId}/versions/snapshot`

**请求参数**:
```json
{
  "changeDescription": "保存当前配置快照",
  "changeType": "UPDATE",
  "createdBy": 1
}
```

**响应**:
```json
{
  "code": 200,
  "data": {
    "id": 1002,
    "versionNumber": 3,
    ...
  }
}
```

---

## 7. 错误码

| 错误码 | HTTP状态码 | 说明 |
|--------|-----------|------|
| RULE_NOT_FOUND | 404 | 规则不存在 |
| RULE_CODE_EXISTS | 400 | 规则编码已存在 |
| RULE_SET_NOT_FOUND | 404 | 规则集不存在 |
| RULE_SET_CODE_EXISTS | 400 | 规则集编码已存在 |
| INVALID_EXPRESSION | 400 | 表达式语法错误 |
| INVALID_CONFIG | 400 | 配置验证失败 |
| RULE_NOT_ACTIVE | 400 | 规则未激活 |
| EXECUTION_TIMEOUT | 408 | 执行超时 |
| EXECUTION_FAILED | 500 | 执行失败 |
| VERSION_NOT_FOUND | 404 | 版本不存在 |

---

## 8. 附录

### 8.1 规则类型枚举

| 类型值 | 说明 |
|--------|------|
| ALARM | 告警规则 |
| LINKAGE | 联动规则 |
| BILLING | 计费规则 |
| CONTROL | 控制规则 |

### 8.2 触发类型枚举

| 类型值 | 说明 |
|--------|------|
| REALTIME | 实时触发 |
| SCHEDULED | 定时触发 |
| MANUAL | 手动触发 |

### 8.3 规则状态枚举

| 状态值 | 说明 |
|--------|------|
| DRAFT | 草稿 |
| ACTIVE | 激活 |
| INACTIVE | 停用 |
| ARCHIVED | 归档 |

### 8.4 执行策略枚举

| 策略值 | 说明 |
|--------|------|
| ALL | 执行所有规则 |
| ANY | 任一规则匹配即停止 |
| FIRST | 第一个规则匹配后停止 |
| SEQUENTIAL | 按顺序执行，失败则停止 |
