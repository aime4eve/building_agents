# Claude Code Agent Teams 使用指南

## 目录

1. [功能概述](#功能概述)
2. [启用方法](#启用方法)
3. [核心概念](#核心概念)
4. [使用方法](#使用方法)
5. [实际案例](#实际案例)
6. [最佳实践](#最佳实践)
7. [注意事项](#注意事项)

---

## 功能概述

**Agent Teams** 是 Claude Code 的实验性功能，允许你创建多个 AI 代理组成"开发团队"，并行协作完成复杂任务。

### 核心优势

| 优势 | 说明 |
|------|------|
| **并行处理** | 多个 agent 同时工作，大幅提升效率 |
| **独立上下文** | 每个 agent 拥有 200K 独立上下文窗口 |
| **任务协调** | 共享任务列表，支持依赖管理和自动解锁 |
| **直接通信** | Agent 之间可以相互发送消息 |

---

## 启用方法

### 方式一：环境变量

```bash
# Linux / macOS
export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1

# Windows PowerShell
$env:CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS="1"

# Windows CMD
set CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1
```

### 方式二：配置文件（推荐）

编辑 `~/.claude/settings.json`（Windows: `C:\Users\你的用户名\.claude\settings.json`）：

```json
{
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1"
  }
}
```

> **注意**：修改配置后需重启 Claude Code 终端会话才能生效。

---

## 核心概念

### 架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      Team Leader                            │
│           (你的主 Claude Code 会话)                          │
│  ┌─────────────┬─────────────┬─────────────┬─────────────┐ │
│  │  Task List  │  Mailbox    │  Coord.     │  Monitor    │ │
│  └──────┬──────┴──────┬──────┴──────┬──────┴──────┬──────┘ │
└─────────┼─────────────┼─────────────┼─────────────┼────────┘
          │             │             │             │
          ▼             ▼             ▼             ▼
    ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
    │ Agent 1 │   │ Agent 2 │   │ Agent 3 │   │ Agent 4 │
    │(Frontend)│   │(Backend)│   │(Testing)│   │(Docs)   │
    └─────────┘   └─────────┘   └─────────┘   └─────────┘
       200K          200K          200K          200K
      tokens        tokens        tokens        tokens
```

### 角色说明

| 角色 | 职责 |
|------|------|
| **Team Leader** | 主会话，负责任务分配和协调 |
| **Teammate** | 独立工作的 agent，执行具体任务 |
| **Task List** | 共享任务板，跟踪进度和依赖 |
| **Mailbox** | 邮箱系统，agent 间消息传递 |

### 显示模式

| 模式 | 说明 | 适用场景 |
|------|------|----------|
| **In-process** | 所有 agent 在同一终端，Shift+↑/↓ 切换 | 默认模式，简单快速 |
| **Split Panes** | 每个 agent 在独立窗格中 | 需要同时监控多个 agent |

---

## 使用方法

### 基本流程

```
1. 启用 Agent Teams
   ↓
2. 在 Claude Code 中请求创建团队
   ↓
3. 定义任务并分配给不同的 agent
   ↓
4. Agent 并行工作，通过任务列表协调
   ↓
5. Team Leader 汇总结果
```

### 常用命令

```bash
# 查看任务列表
/ tasks

# 查看特定任务
/ task get <taskId>

# 更新任务状态
/ task update <taskId> --status completed

# 向特定 agent 发送消息
/ message send --to <agentName> --content "..."
```

---

## 实际案例

### 案例 1：前后端并行开发

**场景**：开发一个用户认证功能，需要同时完成前端页面和后端 API。

```
你：帮我创建一个 agent team 来开发用户认证功能

Claude：我会创建一个包含三个 agent 的团队：
- frontend-agent: 负责前端登录/注册页面
- backend-agent: 负责认证 API 和数据库
- test-agent: 负责编写测试用例
```

**执行过程**：

```
┌─────────────────── Task List ───────────────────┐
│ [ ] 1. 设计认证 API 接口 (backend-agent)       │
│ [ ] 2. 创建数据库模型 (backend-agent)          │
│ [ ] 3. 实现登录页面 (frontend-agent)           │
│ [ ] 4. 实现注册页面 (frontend-agent)           │
│      ↑ 依赖任务 1                              │
│ [ ] 5. 编写 API 测试 (test-agent)              │
│      ↑ 依赖任务 1, 2                           │
│ [ ] 6. 编写前端测试 (test-agent)               │
│      ↑ 依赖任务 3, 4                           │
└────────────────────────────────────────────────┘
```

### 案例 2：代码库全面审查

**场景**：对大型代码库进行多维度审查。

```
你：用 agent team 审查这个项目的代码质量

Claude：我会创建专门的审查团队：
- security-agent: 安全漏洞检查
- performance-agent: 性能问题分析
- style-agent: 代码风格一致性
- docs-agent: 文档完整性检查
```

**并行审查**：

| Agent | 审查重点 | 输出 |
|-------|----------|------|
| security-agent | SQL注入、XSS、认证 | 安全问题报告 |
| performance-agent | 算法效率、资源使用 | 性能优化建议 |
| style-agent | 命名规范、格式 | 风格改进清单 |
| docs-agent | API文档、注释 | 文档补充建议 |

### 案例 3：多模块重构

**场景**：重构一个包含多个模块的 monorepo 项目。

```
项目结构：
├── packages/
│   ├── auth/          ← 认证模块
│   ├── database/      ← 数据库模块
│   ├── api/          ← API 层
│   └── frontend/     ← 前端应用
```

**任务分配**：

```bash
# 创建团队并分配任务
你：创建 agent team 重构这四个模块

# Claude 自动创建任务
Task 1: [auth-agent] 重构认证模块，移除废弃依赖
Task 2: [database-agent] 优化数据库查询，添加索引
Task 3: [api-agent] 更新 API 版本，保持向后兼容
Task 4: [frontend-agent] 更新前端以适配新 API
         ↑ 依赖 Task 3
Task 5: [integration-agent] 编写集成测试
         ↑ 依赖 Task 1, 2, 3, 4
```

### 案例 4：并行调试

**场景**：系统出现多个互相关联的问题。

```
你：应用有三个问题需要同时排查：
1. 用户登录失败
2. 数据库连接超时
3. API 响应缓慢

Claude：我会分配给三个专门的调试 agent
```

**调试过程**：

```
┌─────────────────────────────────────────────────┐
│  debug-auth    │  "检查认证日志，分析失败原因"  │
│  debug-db      │  "检查数据库连接池配置"       │
│  debug-api     │  "分析 API 响应时间"          │
└─────────────────────────────────────────────────┘
         ↓                   ↓                   ↓
    发现 JWT 过期        发现连接数不足      发现 N+1 查询
         ↓                   ↓                   ↓
         └───────────┬───────────────┘
                     ↓
              ┌──────────────┐
              │  汇总诊断结果 │
              │  综合解决方案  │
              └──────────────┘
```

### 案例 5：文档生成与代码审查并行

**场景**：为现有项目生成完整文档，同时进行代码审查。

```
你：为这个项目生成文档，并审查代码质量

团队配置：
┌─────────────────────────────────────────────────┐
│  docs-api        │  API 文档生成                 │
│  docs-user       │  用户指南编写                 │
│  review-security │  安全审查                     │
│  review-quality  │  代码质量审查                 │
└─────────────────────────────────────────────────┘
```

**输出结构**：

```
docs/
├── api/                    ← docs-api 生成
│   ├── endpoints.md
│   ├── schemas.md
│   └── examples.md
├── user/                   ← docs-user 生成
│   ├── getting-started.md
│   ├── configuration.md
│   └── troubleshooting.md
└── reviews/                ← review-* 输出
    ├── security-report.md
    └── quality-report.md
```

---

## 最佳实践

### 1. 合理定义任务依赖

```json
{
  "tasks": [
    {
      "id": "1",
      "name": "设计 API",
      "agent": "backend-agent"
    },
    {
      "id": "2",
      "name": "实现前端",
      "agent": "frontend-agent",
      "blockedBy": ["1"]  // 等待 API 设计完成
    }
  ]
}
```

### 2. 选择合适的显示模式

| 场景 | 推荐模式 |
|------|----------|
| 快速任务 | In-process |
| 需要实时监控 | Split Panes (tmux) |

### 3. 任务粒度控制

```
❌ 太粗粒度：
"完成整个后端开发"

✅ 合适粒度：
"设计用户认证 API 接口"
"实现登录端点"
"添加单元测试"
```

### 4. 定期同步

```
你：请所有 agent 报告当前进度

结果：
┌──────────────────────────────────────┐
│ frontend: 已完成 60%，遇到样式问题  │
│ backend: 已完成 80%，等待前端确认   │
│ testing: 等待 backend 完成           │
└──────────────────────────────────────┘
```

---

## 注意事项

### Token 消耗

Agent Teams 会显著增加 token 使用量：

| Agent 数量 | 预估倍数 |
|-----------|---------|
| 1 (默认) | 1x |
| 2 | ~2.5x |
| 3 | ~4x |
| 4 | ~5.5x |

> 建议仅在复杂任务中使用，简单任务使用单 agent 即可。

### 适用场景

| ✅ 适合使用 | ❌ 不适合使用 |
|------------|-------------|
| 多模块开发 | 单文件编辑 |
| 并行调试 | 变量重命名 |
| 代码审查 | 简单问答 |
| 文档生成 | 快速修复 |

### 系统要求

- Claude Code 2.1+
- Claude Opus 4.6 或 Sonnet 4.5
- 建议安装 tmux（用于 Split Panes 模式）

### 故障排查

| 问题 | 解决方法 |
|------|----------|
| 功能未启用 | 检查 settings.json 配置 |
| Agent 无响应 | 检查网络连接和 API 配额 |
| 任务卡住 | 使用 `/task update` 手动更新状态 |

---

## 快速参考

### 常用命令速查

```bash
# 启用功能
export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1

# 创建团队（在 Claude Code 对话中）
"创建一个 agent team 来..."

# 查看任务
/ tasks
/ task get <id>

# 更新任务
/ task update <id> --status in_progress
/ task update <id> --status completed

# 发送消息
/ message send --to <agent> --content "..."

# 终止 agent
/ agent stop <name>
```

### 配置模板

```json
{
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1",
    "CLAUDE_CODE_TEAMS_DISPLAY_MODE": "split"  // 可选: split | in-process
  }
}
```

---

## 进阶技巧

### 自定义 Agent 专长

```
你：创建专门处理 TypeScript 类型问题的 agent

Claude：我会创建 type-expert agent，专注于：
- 类型定义设计
- 泛型优化
- 类型错误诊断
```

### 跨 Agent 文件共享

```
# Agent 1 创建文件
/tmp/shared/design-spec.md

# Agent 2 读取并实现
基于 /tmp/shared/design-spec.md 实现 API
```

### 分阶段执行

```
Phase 1: 设计阶段 (2 agents)
   ↓
Phase 2: 实现阶段 (4 agents)
   ↓
Phase 3: 测试阶段 (2 agents)
   ↓
Phase 4: 文档阶段 (1 agent)
```

---

*文档版本: 2026.2*
*基于 Claude Code 2.1+*
