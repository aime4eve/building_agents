# 华宽通智能体平台

## 项目简介

华宽通智能体平台是基于DDD（领域驱动设计）架构的物联网智能管理平台，采用Spring Cloud Alibaba微服务架构，支持设备接入、空间管理、规则引擎、场景联动等核心功能。

## 技术栈

| 类别 | 技术选型 | 版本 |
|------|----------|------|
| Java | JDK | 17 |
| Spring Boot |  | 3.2.5 |
| Spring Cloud |  | 2023.0.1 |
| Spring Cloud Alibaba |  | 2023.0.1.0 |
| Nacos | 服务注册/配置中心 | 2.3.x |
| MySQL | 关系型数据库 | 8.0+ |
| Redis | 缓存 | 7.2+ |
| RabbitMQ | 消息队列 | 3.12+ |
| MQTT | 设备接入协议 | EMQX 5.x |
| MyBatis Plus | ORM框架 | 3.5.5 |
| Knife4j | API文档 | 4.4.0 |

## 模块说明

```
hkt-iot-platform/
├── hkt-iot-parent/           # 父POM，统一依赖管理
├── hkt-iot-common/           # 公共模块
│   ├── exception/            # 统一异常处理
│   ├── result/               # 统一响应结果
│   └── web/                  # Web层通用组件
├── hkt-iot-domain/           # 领域层基础模块
│   ├── model/                # 聚合根、实体、值对象基类
│   ├── event/                # 领域事件
│   └── repository/           # 仓储接口定义
├── hkt-iot-user-service/     # 用户与租户服务（BC-01）
├── hkt-iot-device-service/   # 设备管理服务（BC-04）
├── hkt-iot-space-service/    # 空间管理服务（BC-03）
├── hkt-iot-rule-service/     # 规则引擎服务（BC-05）
└── hkt-iot-gateway/          # API网关
```

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- RabbitMQ 3.12+
- Nacos 2.3+
- EMQX 5.x (可选，用于设备接入)

## 快速启动

### 1. 启动基础设施

```bash
# 启动Nacos（默认端口8848）
# 启动MySQL（默认端口3306）
# 启动Redis（默认端口6379）
# 启动RabbitMQ（默认端口5672）
```

### 2. 编译项目

```bash
cd hkt-iot-platform
mvn clean package -DskipTests
```

### 3. 启动服务

```bash
# 启动网关服务
java -jar hkt-iot-gateway/target/hkt-iot-gateway-1.0.0-SNAPSHOT.jar

# 启动用户服务
java -jar hkt-iot-user-service/target/hkt-iot-user-service-1.0.0-SNAPSHOT.jar

# 启动设备服务
java -jar hkt-iot-device-service/target/hkt-iot-device-service-1.0.0-SNAPSHOT.jar

# 启动空间服务
java -jar hkt-iot-space-service/target/hkt-iot-space-service-1.0.0-SNAPSHOT.jar

# 启动规则引擎服务
java -jar hkt-iot-rule-service/target/hkt-iot-rule-service-1.0.0-SNAPSHOT.jar
```

### 4. 访问服务

- API网关：http://localhost:8080
- Knife4j文档：http://localhost:8080/doc.html
- Nacos控制台：http://localhost:8848/nacos

## 限界上下文映射

| 限界上下文 | 服务名称 | 端口 | 说明 |
|-----------|----------|------|------|
| BC-01 用户与租户管理 | hkt-iot-user-service | 8081 | 租户、用户、角色、权限管理 |
| BC-04 设备管理 | hkt-iot-device-service | 8082 | 设备接入、控制、遥测数据采集 |
| BC-03 空间管理 | hkt-iot-space-service | 8083 | 园区、建筑、楼层、房间管理 |
| BC-05 规则引擎 | hkt-iot-rule-service | 8084 | 规则定义、条件判断、动作执行 |

## 开发规范

### DDD分层结构

```
src/main/java/com/hkt/iot/{service}/
├── domain/                    # 领域层
│   ├── model/                # 聚合根、实体、值对象
│   ├── repository/           # 仓储接口
│   └── service/              # 领域服务
├── application/              # 应用层
│   ├── service/              # 应用服务
│   ├── command/              # 命令对象
│   ├── query/                # 查询对象
│   └── event/                # 领域事件处理
├── infrastructure/           # 基础设施层
│   ├── persistence/          # 仓储实现
│   ├── messaging/            # 消息队列
│   ├── mqtt/                 # MQTT接入
│   └── cache/                # 缓存实现
└── interfaces/               # 接口层
    ├── rest/                 # REST控制器
    ├── mqtt/                 # MQTT控制器
    └── dto/                  # 数据传输对象
```

### 命名规范

- 聚合根：以业务含义命名，如`Device`、`Tenant`、`User`
- 实体：以业务含义命名，如`DeviceItem`、`OrderItem`
- 值对象：以属性含义命名，如`DeviceId`、`Money`、`Address`
- 仓储接口：`{Aggregate}Repository`，如`DeviceRepository`
- 应用服务：`{Aggregate}ApplicationService`，如`DeviceApplicationService`
- 控制器：`{Aggregate}Controller`，如`DeviceController`

## License

Copyright © 2026 HKT IoT Team
