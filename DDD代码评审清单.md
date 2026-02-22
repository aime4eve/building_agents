# DDD代码评审清单

**文档版本：** V1.0
**创建日期：** 2026-02-20
**适用项目：** 华宽通智能体系统
**评审角色：** DDD架构专家

---

## 目录

1. [项目脚手架评审清单](#1-项目脚手架评审清单-java-developer)
2. [数据库设计评审清单](#2-数据库设计评审清单-database-expert)
3. [设备接入层设计评审清单](#3-设备接入层设计评审清单-iot-expert)
4. [通用评审准则](#4-通用评审准则)

---

## 1. 项目脚手架评审清单（java-developer）

### 1.1 分层架构评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **目录结构** | 符合DDD四层架构 | domain/、application/、infrastructure/、interfaces/ |
| **依赖方向** | 单向依赖，外层依赖内层 | interfaces→application→domain←infrastructure |
| **领域层独立性** | 不依赖任何框架 | 纯Java代码，无Spring注解 |
| **基础设施隔离** | 通过接口（端口）与领域交互 | Repository接口在domain，实现在infrastructure |

#### 评审命令
```bash
# 检查目录结构
tree -L 3 -I 'target|node_modules'

# 检查依赖方向（领域层不应依赖外部框架）
find domain -name "*.java" -exec grep -l "import org.springframework" {} \;
# 预期：无结果

# 检查领域层独立性
find domain -name "*.java" -exec grep -l "@Entity\|@Table\|@RestController" {} \;
# 预期：无结果
```

### 1.2 领域层评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **聚合根位置** | domain/model/aggregate/ | Device.java, Tenant.java等 |
| **实体位置** | domain/model/entity/ | ThingModel.java等 |
| **值对象位置** | domain/model/valueobject/ | DeviceId.java, DeviceSn.java等 |
| **领域事件位置** | domain/model/domainevent/ | DeviceCreatedEvent.java等 |
| **仓储接口位置** | domain/repository/ | DeviceRepository.java等 |
| **聚合根命名** | 无Aggregate后缀 | Device而非DeviceAggregate |
| **值对象不可变** | 使用@Value或final | 所有值对象不可变 |
| **ID值对象** | 专用类型而非String | DeviceId而非String deviceId |

#### 评审要点
- [ ] 聚合根包含domainEvents集合和管理方法
- [ ] 聚合根使用version字段进行乐观锁控制
- [ ] 值对象使用@Value注解或手动实现不可变
- [ ] 值对象构造函数包含业务规则验证
- [ ] 领域事件命名使用过去分词+Event后缀
- [ ] 领域事件包含必需字段（eventId, occurredAt, aggregateId等）

### 1.3 应用层评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **应用服务位置** | application/service/ | DeviceApplicationService.java等 |
| **命令对象位置** | application/command/ | CreateDeviceCommand.java等 |
| **查询对象位置** | application/query/ | DeviceQuery.java等 |
| **事务边界** | 应用服务使用@Transactional | 不在领域层使用事务 |
| **DTO转换** | 使用Assembler或Converter | DeviceAssembler等 |

#### 评审要点
- [ ] 应用服务只编排用例，不包含业务逻辑
- [ ] 应用服务方法命名：动词+名词（createDevice, updateDevice）
- [ ] 命令对象使用@Value注解确保不可变
- [ ] 异常处理在应用服务层完成
- [ ] 返回DTO而非领域对象

### 1.4 基础设施层评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **仓储实现位置** | infrastructure/persistence/ | DeviceRepositoryImpl.java等 |
| **PO对象位置** | infrastructure/persistence/po/ | DevicePO.java等 |
| **消息发布位置** | infrastructure/messaging/ | DomainEventPublisherImpl.java等 |
| **外部服务位置** | infrastructure/external/ | ExternalServiceAdapter.java等 |

#### 评审要点
- [ ] 仓储实现类命名：XxxRepositoryImpl
- [ ] PO对象包含领域模型转换方法（toDomain/fromDomain）
- [ ] 仓储实现负责发布领域事件
- [ ] 外部服务使用防腐层模式

### 1.5 接口层评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **控制器位置** | interfaces/rest/ | DeviceController.java等 |
| **请求DTO位置** | interfaces/rest/dto/request/ | CreateDeviceRequest.java等 |
| **响应DTO位置** | interfaces/rest/dto/response/ | DeviceResponse.java等 |
| **转换器位置** | interfaces/assembler/ | DeviceAssembler.java等 |

#### 评审要点
- [ ] 控制器只处理HTTP相关逻辑
- [ ] 控制器委托应用服务处理业务
- [ ] 使用@Valid进行参数校验
- [ ] 统一异常处理
- [ ] Swagger/OpenAPI文档完整

### 1.6 配置评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **包扫描配置** | 正确扫描各层组件 | @ComponentScan配置 |
| **事务配置** | 启用注解事务 | @EnableTransactionManagement |
| **异常转换** | JPA异常转换 | @Repository注解 |
| **依赖注入** | 构造函数注入 | @RequiredArgsConstructor |

### 1.7 测试评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **领域层测试** | 单元测试覆盖业务逻辑 | DeviceTest.java等 |
| **应用层测试** | 集成测试覆盖用例 | DeviceApplicationServiceTest.java等 |
| **仓储测试** | 使用@DataJpaTest | DeviceRepositoryTest.java等 |
| **控制器测试** | 使用@WebMvcTest | DeviceControllerTest.java等 |

---

## 2. 数据库设计评审清单（database-expert）

### 2.1 表设计评审

| 棚查项 | 标准 | 预期结果 |
|--------|------|----------|
| **表命名** | 小写+下划线 | device, device_telemetry等 |
| **主键设计** | 使用业务ID或UUID | id VARCHAR(64) PRIMARY KEY |
| **外键设计** | 跨表引用使用ID | tenant_id, space_id等 |
| **版本字段** | 乐观锁版本号 | version BIGINT DEFAULT 0 |
| **时间戳** | 创建和更新时间 | created_at, updated_at |
| **软删除** | 租户级软删除 | deleted BOOLEAN DEFAULT FALSE |

#### 评审要点
- [ ] 表结构与聚合根一一对应
- [ ] 值对象字段内联到聚合根表
- [ ] 一对多关系使用外键而非中间表（除非多对多）
- [ ] 枚举类型使用VARCHAR存储（便于扩展）
- [ ] 字段类型与Java类型对应正确

### 2.2 索引设计评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **唯一索引** | 业务唯一键 | device_tenant_sn_idx |
| **外键索引** | 关联查询字段 | tenant_id, space_id |
| **查询索引** | 常用查询条件 | status, type等 |
| **租户隔离** | 租户ID索引 | tenant_id必须索引 |
| **复合索引** | 多条件查询 | (tenant_id, status) |

#### 评审要点
- [ ] 租户ID字段必须有索引（多租户隔离）
- [ ] 序列号字段有唯一索引（device表的sn字段）
- [ ] 状态字段有索引（查询在线/离线设备）
- [ ] 时间字段有索引（时间范围查询）
- [ ] 复合索引字段顺序正确（高选择性在前）

### 2.3 时序数据设计评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **时序表分离** | 遥测数据独立表 | device_telemetry_history |
| **分区策略** | 按时间分区 | RANGE partitioning by created_at |
| **数据保留** | 定期清理旧数据 | 保留策略配置 |
| **读写分离** | CQRS模式 | 写侧快照+读侧时序 |

#### 评审要点
- [ ] 历史数据表使用时间分区
- [ ] 遥测数据表设计支持高效写入
- [ ] 事件数据表支持高效查询
- [ ] 配置数据保留策略

### 2.4 数据一致性评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **乐观锁** | version字段 | 更新时检查version |
| **事务隔离** | READ_COMMITTED | 默认隔离级别 |
| **外键约束** | 根据业务需求 | 不强制使用外键约束 |
| **级联删除** | 软删除优先 | 逻辑删除而非物理删除 |

#### 评审要点
- [ ] 聚合根表有version字段
- [ ] 更新操作使用CAS（Compare And Set）
- [ ] 跨聚合数据使用最终一致性
- [ ] 删除操作标记为软删除

### 2.5 多租户设计评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **租户隔离** | 所有表包含tenant_id | 租户级数据隔离 |
| **租户索引** | tenant_id索引 | 查询性能优化 |
| **租户默认值** | 插入时自动填充 | 触发器或应用层填充 |
| **租户校验** | 查询时强制过滤 | Row-Level Security |

#### 评审要点
- [ ] 所有业务表包含tenant_id字段
- [ ] tenant_id字段建立索引
- [ ] 查询自动添加租户过滤条件
- [ ] 防止跨租户数据访问

---

## 3. 设备接入层设计评审清单（iot-expert）

### 3.1 协议适配评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **协议抽象** | 设备协议接口定义 | DeviceProtocol接口 |
| **MQTT适配** | MQTT协议实现 | MqttDeviceProtocol |
| **HTTP适配** | HTTP协议实现 | HttpDeviceProtocol |
| **CoAP适配** | CoAP协议实现 | CoapDeviceProtocol |
| **自定义协议** | 扩展点设计 | CustomDeviceProtocol |

#### 评审要点
- [ ] 协议抽象层在domain层定义
- [ ] 协议实现在infrastructure层
- [ ] 支持动态协议注册
- [ ] 协议切换不影响领域模型

### 3.2 设备连接管理评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **连接池** | 设备连接复用 | 连接池管理 |
| **心跳检测** | 在线状态监控 | 定时心跳检查 |
| **断线重连** | 自动重连机制 | 指数退避策略 |
| **连接限流** | 防止连接过载 | 最大连接数限制 |

#### 评审要点
- [ ] 连接管理器是基础设施组件
- [ ] 通过领域事件通知设备上线/离线
- [ ] 重连策略可配置
- [ ] 连接状态与设备状态同步

### 3.3 消息路由评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **消息解析** | 物模型驱动解析 | ThingModel-based |
| **数据验证** | 数据类型校验 | Schema validation |
| **消息转换** | 协议消息→领域事件 | Message→DomainEvent |
| **错误处理** | 异常消息处理 | 死信队列 |

#### 评审要点
- [ ] 消息解析使用物模型定义
- [ ] 数据验证在领域层完成
- [ ] 消息转换通过应用服务
- [ ] 错误消息记录到审计日志

### 3.4 设备控制评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **命令发送** | 异步发送 | 非阻塞IO |
| **命令确认** | 响应处理 | 超时机制 |
| **批量控制** | 批量操作支持 | BatchDeviceControlTask |
| **控制结果** | 状态同步 | 事件通知 |

#### 评审要点
- [ ] 命令发送异步处理
- [ ] 命令超时可配置
- [ ] 批量控制有并发限制
- [ ] 控制结果通过事件通知

### 3.5 设备认证评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **设备证书** | TLS双向认证 | X.509证书 |
| **密钥管理** | 密钥存储加密 | 密钥轮换机制 |
| **访问控制** | 租户隔离 | 设备只能访问租户资源 |
| **审计日志** | 操作记录 | 完整审计跟踪 |

#### 评审要点
- [ ] 设备认证在接入层完成
- [ ] 认证信息不泄露到领域层
- [ ] 认证失败记录审计日志
- [ ] 支持设备黑名单

---

## 4. 通用评审准则

### 4.1 DDD原则评审

| 原则 | 检查项 | 预期结果 |
|------|--------|----------|
| **分层** | 依赖方向正确 | 外层依赖内层 |
| **隔离** | 领域层独立 | 无框架依赖 |
| ** ubiquity | 统一语言 | 代码与文档一致 |
| **边界** | 限界上下文清晰 | 模块职责明确 |

### 4.2 代码质量评审

| 检查项 | 标准 | 工具 |
|--------|------|------|
| **代码风格** | 遵循阿里巴巴规范 | CheckStyle |
| **代码覆盖** | 核心模块80%+ | JaCoCo |
| **代码重复** | 重复率<5% | SonarQube |
| **复杂度** | 圈复杂度<10 | PMD |

### 4.3 安全评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **输入验证** | 所有输入验证 | @Valid注解 |
| **SQL注入** | 参数化查询 | JPA/MyBatis |
| **XSS防护** | 输出编码 | Jsoup |
| **权限检查** | 接口层鉴权 | @PreAuthorize |

### 4.4 性能评审

| 检查项 | 标准 | 预期结果 |
|--------|------|----------|
| **N+1查询** | 批量查询 | JOIN优化 |
| **大事务** | 拆分小事务 | 事务范围最小化 |
| **缓存策略** | 多级缓存 | Redis+Local |
| **异步处理** | 非核心流程异步 | @Async/MQ |

---

## 附录：评审报告模板

```markdown
# DDD代码评审报告

**评审对象：** [模块名称]
**评审人：** DDD架构专家
**评审日期：** YYYY-MM-DD
**版本：** V1.0

## 评审摘要

| 类别 | 通过 | 警告 | 失败 |
|------|------|------|------|
| 架构设计 | X | X | X |
| 领域模型 | X | X | X |
| 代码质量 | X | X | X |
| 安全性 | X | X | X |
| 性能 | X | X | X |

## 详细评审结果

### 1. 架构设计

#### ✅ 通过项
- [说明]

#### ⚠️ 警告项
- [说明]

#### ❌ 失败项
- [说明]

### 2. 领域模型

#### ✅ 通过项
- [说明]

#### ⚠️ 警告项
- [说明]

#### ❌ 失败项
- [说明]

## 改进建议

1. [具体建议]
2. [具体建议]

## 后续行动

| 优先级 | 改进项 | 负责人 | 截止日期 |
|--------|--------|--------|----------|
| 高 | [改进项] | [姓名] | YYYY-MM-DD |
| 中 | [改进项] | [姓名] | YYYY-MM-DD |
| 低 | [改进项] | [姓名] | YYYY-MM-DD |

## 评审结论

[通过/有条件通过/不通过]

**评审人签字：** DDD架构专家
**日期：** YYYY-MM-DD
```

---

**文档结束**
