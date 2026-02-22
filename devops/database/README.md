# 华宽通智能体系统 - 数据库服务部署文档

## 版本信息

- **版本**: V1.0
- **创建日期**: 2026-02-20
- **负责人**: DevOps Engineer

---

## 目录结构

```
D:\ai-agentic\devops\database\
├── docker-compose.yml           # Docker Compose 配置文件
├── mysql/
│   ├── conf/
│   │   └── my.cnf              # MySQL 配置文件
│   └── init/
│       ├── 00-create-databases.sql     # 创建数据库
│       ├── 01-device-management.sql    # 设备管理模块表结构
│       ├── 02-space-management.sql     # 空间管理模块表结构
│       ├── 03-user-tenant.sql          # 用户租户模块表结构
│       ├── 04-rule-engine.sql          # 规则引擎模块表结构
│       └── 99-init-data.sql            # 初始化数据
├── redis/
│   └── redis.conf            # Redis 配置文件
└── README.md                 # 本文档
```

---

## 服务说明

### MySQL 8.0

| 配置项 | 值 |
|--------|-----|
| 镜像 | mysql:8.0 |
| 端口 | 3306 |
| Root密码 | root123456 |
| 数据库 | hkt_iot, hkt_iot_device, hkt_iot_space, hkt_iot_user, hkt_iot_rule |
| 数据目录 | ./mysql-data (Docker Volume) |

### Redis 7.2

| 配置项 | 值 |
|--------|-----|
| 镜像 | redis:7.2-alpine |
| 端口 | 6379 |
| 密码 | (无，生产环境请设置) |
| 持久化 | AOF + RDB混合 |
| 数据目录 | ./redis-data (Docker Volume) |

### 管理工具

| 工具 | 端口 | 说明 |
|------|------|------|
| phpMyAdmin | 8080 | MySQL Web管理界面 |
| Redis Commander | 8081 | Redis Web管理界面 |

---

## 快速开始

### 1. 启动服务

```bash
cd D:\ai-agentic\devops\database
docker-compose up -d
```

### 2. 查看服务状态

```bash
docker-compose ps
```

### 3. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看MySQL日志
docker-compose logs -f mysql

# 查看Redis日志
docker-compose logs -f redis
```

### 4. 验证数据库初始化

```bash
# 连接MySQL
docker exec -it hkt-mysql mysql -uroot -proot123456

# 查看所有数据库
SHOW DATABASES;

# 查看设备管理表
USE hkt_iot_device;
SHOW TABLES;

# 查看空间管理表
USE hkt_iot_space;
SHOW TABLES;

# 查看用户租户表
USE hkt_iot_user;
SHOW TABLES;

# 查看规则引擎表
USE hkt_iot_rule;
SHOW TABLES;
```

### 5. 验证初始数据

```sql
USE hkt_iot_user;

-- 查看默认租户
SELECT * FROM tenant;

-- 查看管理员用户
SELECT id, username, real_name, email, user_status FROM `user`;

-- 查看默认角色
SELECT * FROM role;

-- 查看系统权限数量
SELECT COUNT(*) AS permission_count FROM permission;
```

---

## 数据库连接配置

### MySQL

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/${database_name}?useUnicode=true&characterEncoding=utf8mb4&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root123456
```

### Redis

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 5000ms
    lettuce:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

---

## 数据库结构说明

### 1. 设备管理模块 (hkt_iot_device)

| 表名 | 说明 |
|------|------|
| device_thing_model | 设备物模型定义表 |
| device | 设备主表 |
| device_license | 设备License表 |
| device_telemetry_snapshot | 设备遥测数据快照表 |

### 2. 空间管理模块 (hkt_iot_space)

| 表名 | 说明 |
|------|------|
| space | 空间层级结构表 |
| space_resource | 空间资源关联表 |
| logical_space_group | 逻辑空间分组表 |
| logical_space_group_member | 逻辑空间组成员表 |

### 3. 用户租户模块 (hkt_iot_user)

| 表名 | 说明 |
|------|------|
| tenant | 租户主表 |
| user | 用户主表 |
| role | 角色表 |
| permission | 权限表 |
| user_role | 用户角色关联表 |
| role_permission | 角色权限关联表 |
| sso_session | SSO会话表 (分区表) |
| mfa_config | MFA配置表 |
| mfa_device | MFA设备表 |
| mfa_challenge | MFA挑战表 |

### 4. 规则引擎模块 (hkt_iot_rule)

| 表名 | 说明 |
|------|------|
| rule | 规则主表 |
| rule_condition | 规则条件表 |
| rule_action | 规则动作表 |
| rule_execution_log | 规则执行日志表 (分区表) |
| rule_action_execution_log | 规则动作执行日志表 (分区表) |
| rule_variable | 规则变量表 |

---

## 分区表管理

### 分区表列表

1. **sso_session** - 按created_at字段按月分区
2. **rule_execution_log** - 按triggered_at字段按月分区
3. **rule_action_execution_log** - 按started_at字段按月分区

### 添加新分区

```sql
-- 为SSO会话表添加2026年7月分区
ALTER TABLE sso_session ADD PARTITION (
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01'))
);
```

### 删除旧分区

```sql
-- 删除SSO会话表2026年1月分区
ALTER TABLE sso_session DROP PARTITION p202601;
```

---

## 数据备份与恢复

### 备份所有数据库

```bash
docker exec hkt-mysql mysqldump -uroot -proot1236 --all-databases > backup.sql
```

### 恢复数据库

```bash
docker exec -i hkt-mysql mysql -uroot -proot123456 < backup.sql
```

### 备份单个数据库

```bash
docker exec hkt-mysql mysqldump -uroot -proot123456 hkt_iot_device > device_backup.sql
```

---

## 常用操作

### 重启服务

```bash
docker-compose restart
```

### 停止服务

```bash
docker-compose stop
```

### 停止并删除容器

```bash
docker-compose down
```

### 停止并删除容器和数据卷

```bash
docker-compose down -v
```

### 进入MySQL容器

```bash
docker exec -it hkt-mysql bash
```

### 进入Redis容器

```bash
docker exec -it hkt-redis sh
```

### Redis CLI

```bash
docker exec -it hkt-redis redis-cli
```

---

## 故障排查

### MySQL无法启动

```bash
# 查看详细错误日志
docker-compose logs mysql
docker exec hkt-mysql cat /var/log/mysql/error.log
```

### Redis连接失败

```bash
# 测试Redis连接
docker exec hkt-redis redis-cli ping
```

### 端口冲突

如果端口被占用，可以修改`docker-compose.yml`中的端口映射：

```yaml
ports:
  - "13306:3306"  # 将主机端口改为13306
```

---

## 生产环境部署建议

1. **修改默认密码**
   - MySQL root密码
   - Redis密码

2. **资源限制**
   - 根据实际负载调整MySQL配置
   - 根据数据量调整Redis maxmemory

3. **数据备份**
   - 定期备份MySQL数据
   - 配置Redis持久化策略

4. **监控告警**
   - 配置Prometheus监控
   - 设置慢查询告警

5. **高可用**
   - 配置MySQL主从复制
   - 配置Redis哨兵或集群模式

---

## 附录：默认账号

| 服务 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| MySQL | root | root123456 | 数据库管理员 |
| 业务系统 | admin | admin123 | 超级管理员账号 |

---

## 联系方式

如有问题，请联系 DevOps Team。
