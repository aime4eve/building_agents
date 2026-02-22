# HKT IoT Notification Service

通知中心服务，负责多渠道消息推送与通知管理。

## 功能特性

### 核心功能

- **多渠道推送**：支持APP推送、邮件、短信、站内信、Webhook
- **模板管理**：支持自定义消息模板，支持变量替换
- **幂等重试**：基于dedupeKey的幂等性保证，指数退避重试策略
- **死信队列**：处理发送失败的通知
- **事件驱动**：订阅告警触发事件，发送通知后发布事件

### 通知渠道

| 渠道 | 说明 | 状态 |
|------|------|------|
| PUSH | APP推送（极光/个推） | 已实现 |
| EMAIL | 邮件（SMTP） | 已实现 |
| SMS | 短信（阿里云SMS） | 已实现 |
| IN_APP | 站内信（Redis） | 已实现 |
| WEBHOOK | Webhook回调 | 已实现 |

### 聚合根

- `NotificationTemplate` - 消息模板
- `NotificationRequest` - 通知请求
- `NotificationLog` - 通知日志

### 领域事件

- `NotificationSentEvent` - 通知发送成功事件
- `NotificationFailedEvent` - 通知发送失败事件

## API端点

### 通知管理 (`/api/v1/notifications`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /send | 发送单条通知 |
| POST | /batch-send | 批量发送通知 |
| GET | /requests/{requestId} | 获取请求状态 |
| POST | /requests/{requestId}/cancel | 取消通知 |
| POST | /logs/query | 查询通知日志 |
| GET | /statistics | 获取通知统计 |

### 模板管理 (`/api/v1/notifications/templates`)

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | / | 创建模板 |
| PUT | /{id} | 更新模板 |
| DELETE | /{id} | 删除模板 |
| GET | /{id} | 获取模板详情 |
| GET | / | 获取模板列表 |
| POST | /{id}/enable | 启用模板 |
| POST | /{id}/disable | 禁用模板 |

## 数据库

### 表结构

```sql
-- 通知模板表
notification_template

-- 通知请求表
notification_request

-- 通知日志表
notification_log
```

### 初始化脚本

```bash
mysql -u root -p < src/main/resources/db/migration/V1.0.0__create_notification_tables.sql
```

## 配置

### application.yml

```yaml
notification:
  max-retry: 3              # 最大重试次数
  default-priority: NORMAL  # 默认优先级

  email:
    enabled: true
    from: noreply@hkt-iot.com

  sms:
    enabled: false
    access-key-id: ${ALIYUN_ACCESS_KEY_ID}
    access-key-secret: ${ALIYUN_ACCESS_KEY_SECRET}
    sign-name: 华宽通智能体

  push:
    enabled: false
    app-key: ${JPUSH_APP_KEY}
    master-secret: ${JPUSH_MASTER_SECRET}
```

### 环境变量

| 变量 | 说明 |
|------|------|
| ALIYUN_ACCESS_KEY_ID | 阿里云Access Key ID |
| ALIYUN_ACCESS_KEY_SECRET | 阿里云Access Key Secret |
| JPUSH_APP_KEY | 极光推送App Key |
| JPUSH_MASTER_SECRET | 极光推送Master Secret |

## 消息队列

### 消费队列

- `notification.send.queue` - 通知发送队列
- `notification.retry.queue` - 通知重试队列
- `notification.alarm.queue` - 告警通知队列

### 发布事件

- `notification.sent` - 通知发送成功事件
- `notification.failed` - 通知发送失败事件

## 运行服务

```bash
# 编译
mvn clean package

# 运行
java -jar target/hkt-iot-notification-service-1.0.0-SNAPSHOT.jar

# 或使用Docker
docker-compose up notification-service
```

## 端口

- 服务端口：8086
- 管理端口：8086/actuator

## 依赖服务

- MySQL：hkt_iot_notification
- Redis：localhost:6379
- RabbitMQ：localhost:5672
- Nacos：localhost:8848
