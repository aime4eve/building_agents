# DDD相关技能清单

> 来源：https://github.com/ComposioHQ/awesome-claude-skills
> 生成时间：2026-02-14
> 说明：从awesome-claude-skills仓库和相关资源中找到的Domain-Driven Design (DDD) 技能

---

## 概述

ComposioHQ/awesome-claude-skills 是一个精选的Claude技能列表仓库，但该仓库本身主要是目录性质，实际的DDD技能托管在独立的仓库中。

以下是从该资源及相关位置找到的4个DDD相关技能。

---

## 1. 软件架构开发技能 (Software Architecture Development Skill) ✅ 已验证

### 基本信息
- **来源仓库**: NeoLabHQ/context-engineering-kit
- **状态**: ✅ 已验证可用
- **下载链接**: https://raw.githubusercontent.com/NeoLabHQ/context-engineering-kit/master/plugins/ddd/skills/software-architecture/SKILL.md

### 功能描述

该技能为质量导向的软件开发和架构提供指导，基于Clean Architecture（整洁架构）和Domain-Driven Design（领域驱动设计）原则。

#### 核心能力

**代码风格规则**
- **通用原则**:
  - 优先使用早期返回模式（early return），而非嵌套条件
  - 通过创建可重用函数和模块避免代码重复
  - 将长组件/函数（超过80行）分解为更小的组件
  - 尽可能使用箭头函数

- **最佳实践**:
  - **库优先方法**: 在编写自定义代码前先搜索现有解决方案
  - **架构与设计**:
    - 遵循领域驱动设计和统一语言（Ubiquitous Language）
    - 将领域实体与基础设施关注点分离
    - 保持业务逻辑独立于框架
    - 清晰定义用例并保持隔离
    - 使用领域特定的命名（如 OrderCalculator、UserAuthenticator）
    - 遵循限界上下文命名模式
  - **职责分离**:
    - 不要将业务逻辑与UI组件混合
    - 保持数据库查询不在控制器中
    - 维护上下文之间的清晰边界

- **需避免的反模式**:
  - NIH（非此发明）综合症：不要在Auth0/Supabase存在时构建自定义认证
  - **糟糕的架构选择**:
    - 将业务逻辑与UI组件混合
    - 数据库查询直接在控制器中
    - 缺乏清晰的职责分离
  - **通用命名反模式**: 避免 utils、helpers、common、shared 等通用名称
  - 记住：每一行自定义代码都是需要维护、测试和文档的负债

- **代码质量**:
  - 适当的错误处理（类型化catch块）
  - 将复杂逻辑分解为更小的可重用函数
  - 避免深层嵌套（最多3层）
  - 保持函数专注且尽可能在50行以内
  - 保持文件专注且尽可能在200行代码以内

### 适用场景
- 需要编写高质量、可维护的代码
- 遵循DDD和Clean Architecture原则
- 进行代码审查和架构设计
- 重构遗留代码库

---

## 2. DDD架构师 (DDD Architect) ✅ 已验证

### 基本信息
- **来源**: MCPMarket
- **状态**: ✅ 已验证可用
- **网站链接**: https://mcpmarket.com/tools/skills/ddd-architect

### 功能描述

该技能使开发者能够通过强制执行领域驱动设计（DDD）和数据访问层（DAL）分离来维持高质量的软件标准。

#### 核心能力

**自动化功能**
- 自动生成DDD文件夹结构（Domain、Application、Infrastructure）
- 实时依赖方向验证和架构违规警告
- 实体（Entities）、值对象（Value Objects）、聚合（Aggregates）和仓储（Repositories）的脚手架
- 领域级合规性的全面架构审查

**架构保障**
- 强制执行数据访问层（DAL）分离原则
- 防止架构漂移

### 适用场景
- 重构遗留代码库以将业务逻辑与数据库依赖隔离
- 为新微服务或模块搭建脚手架，遵循整洁架构模式
- 执行自动化代码审查以检测层之间的非法导入

### 安装方式
```bash
npx skillfish add u9401066/copilot-capability-manager ddd-architect
```

---

## 3. V3 DDD架构 (V3 DDD Architecture) ✅ 已列出

### 基本信息
- **来源仓库**: ruvnet/claude-flow
- **状态**: ✅ 已列出
- **安装命令**:
  ```bash
  npx skills add https://github.com/ruvnet/claude-flow --skill V3 DDD Architecture
  ```

### 功能描述

该技能专门为claude-flow v3设计和实现领域驱动设计（DDD）架构。

#### 核心能力

**DDD实现策略**
- **当前架构分析**:
  - 将巨型对象（god objects）分解为限界上下文
  - 实现整洁架构模式
  - 启用模块化、可测试的代码结构

- **领域分解**:
  - 任务管理域（Task Management Domain）
  - 会话管理域（Session Management Domain）
  - 健康监控域（Health Monitoring Domain）
  - 生命周期管理域（Lifecycle Management Domain）
  - 事件协调域（Event Coordination Domain）

#### 快速开始示例
```markdown
# 初始化DDD架构分析
Task("Architecture analysis", "Analyze current architecture and design DDD boundaries", "core-architect")

# 领域建模（并行）
Task("Domain decomposition", "Break down orchestrator god object into domains", "core-architect")
Task("Context mapping", "Map bounded contexts and relationships", "core-architect")
Task("Interface design", "Design clean domain interfaces", "core-architect")
```

### 适用场景
- 为claude-flow项目实施DDD架构
- 分解巨型对象（God Objects）
- 建立模块化、可测试的领域模型

---

## 4. 架构模式 (Architecture Patterns) ✅ 已列出

### 基本信息
- **来源仓库**: secondsky/claude-skills
- **状态**: ✅ 已列出
- **安装命令**:
  ```bash
  npx skills add https://github.com/secondsky/claude-skills --skill architecture-patterns
  ```

### 功能描述

掌握经过验证的后端架构模式，包括Clean Architecture（整洁架构）、Hexagonal Architecture（六边形架构）和Domain-Driven Design（领域驱动设计），用于构建可维护、可测试和可扩展的系统。

#### 核心概念

**1. Clean Architecture（整洁架构 - Uncle Bob）**

**分层结构（依赖向内流动）**:
- **实体**: 核心业务模型
- **用例**: 应用业务规则
- **接口适配器**: 控制器、展示器、网关
- **框架和驱动器**: UI、数据库、外部服务

**关键原则**:
- 依赖指向内部
- 内层对外层一无所知
- 业务逻辑独立于框架
- 无需UI、数据库或外部服务即可测试

**2. Hexagonal Architecture（六边形架构/端口和适配器）**

**组件**:
- **领域核心**: 业务逻辑
- **端口**: 定义交互的接口
- **适配器**: 端口的实现（数据库、REST、消息队列）

**优势**:
- 轻松交换实现（用于测试的mock）
- 技术不可知的核心
- 清晰的职责分离

**3. Domain-Driven Design（领域驱动设计）**

**战略模式**:
- **限界上下文（Bounded Contexts）**: 不同域的独立模型
- **上下文映射（Context Mapping）**: 上下文之间的关系
- **领域事件（Domain Events）**: 跨上下文通信
- **聚合根（Aggregate Roots）**: 一致性边界

**战术模式**:
- **实体（Entities）**: 具有唯一标识的对象
- **值对象（Value Objects）**: 通过属性值定义的对象
- **仓储（Repositories）**: 访问聚合的抽象
- **工厂（Factories）**: 复杂对象创建
- **领域服务（Domain Services）**: 不属于实体的业务逻辑

### 适用场景
- 从头开始设计新的后端系统
- 重构单体应用以获得更好的可维护性
- 为团队建立架构标准
- 从紧耦合架构迁移到松耦合架构
- 实施领域驱动设计原则
- 创建可测试和可模拟的代码库
- 规划微服务分解

---

## 总结对比

| 技能名称 | 来源 | DDD支持程度 | 特色功能 | 推荐度 |
|---------|------|------------|---------|--------|
| 软件架构开发技能 | NeoLabHQ/context-engineering-kit | ⭐⭐⭐⭐⭐ | 全面的代码风格、架构指导、反模式避免 | ⭐⭐⭐⭐⭐ |
| DDD架构师 | MCPMarket | ⭐⭐⭐⭐⭐ | 自动化脚手架、依赖验证、架构审查 | ⭐⭐⭐⭐⭐ |
| V3 DDD架构 | ruvnet/claude-flow | ⭐⭐⭐⭐ | 专门针对claude-flow的DDD实现 | ⭐⭐⭐⭐ |
| 架构模式 | secondsky/claude-skills | ⭐⭐⭐⭐⭐ | 涵盖Clean、六边形、DDD三大模式 | ⭐⭐⭐⭐⭐ |

---

## 推荐使用顺序

### 对于DDD初学者
1. 先学习 **架构模式** - 理解Clean Architecture、六边形架构和DDD的核心概念
2. 使用 **软件架构开发技能** - 在实际项目中应用这些原则
3. 根据需要使用 **DDD架构师** - 自动生成脚手架和验证架构

### 对于有经验的开发者
1. **DDD架构师** - 快速搭建符合DDD规范的项目结构
2. **软件架构开发技能** - 确保代码质量和架构一致性
3. **架构模式** - 作为参考手册查阅具体模式

### 对于claude-flow项目
1. 直接使用 **V3 DDD架构** - 专门为claude-flow优化的DDD实现

---

## 安装指南

### 方式1：直接下载技能文件

```bash
# 下载软件架构开发技能
curl -o software-architecture-skill.md https://raw.githubusercontent.com/NeoLabHQ/context-engineering-kit/master/plugins/ddd/skills/software-architecture/SKILL.md
```

### 方式2：使用skills命令行工具

```bash
# 安装V3 DDD架构
npx skills add https://github.com/ruvnet/claude-flow --skill "V3 DDD Architecture"

# 安装架构模式
npx skills add https://github.com/secondsky/claude-skills --skill architecture-patterns
```

### 方式3：使用skillfish（用于DDD架构师）

```bash
# 安装DDD架构师
npx skillfish add u9401066/copilot-capability-manager ddd-architect
```

---

## 注意事项

1. **技能托管位置**: ComposioHQ/awesome-claude-skills 是一个目录/索引仓库，实际技能文件托管在各自的独立仓库中。

2. **验证状态**:
   - ✅ 已验证：技能文件存在且可以访问
   - ✅ 已列出：技能在官网被列出，但文件路径可能需要进一步验证

3. **版本兼容性**: 不同技能可能适用于不同版本的Claude Code或不同项目类型，使用前请确认兼容性。

4. **技能组合**: 多个技能可以组合使用，例如用"DDD架构师"搭建脚手架，用"软件架构开发技能"进行代码审查。

---

## 参考资料

- [ComposioHQ/awesome-claude-skills](https://github.com/ComposioHQ/awesome-claude-skills) - Claude技能精选列表
- [NeoLabHQ/context-engineering-kit](https://github.com/NeoLabHQ/context-engineering-kit) - 软件架构开发技能
- [MCPMarket - DDD Architect](https://mcpmarket.com/tools/skills/ddd-architect) - DDD架构师
- [ruvnet/claude-flow](https://github.com/ruvnet/claude-flow) - V3 DDD架构
- [secondsky/claude-skills](https://github.com/secondsky/claude-skills) - 架构模式

---

**文档版本**: v1.0
**最后更新**: 2026-02-14
