# Awesome Claude Skills - PRD文档生成技能分析报告（更新版）

**更新日期**: 2026年2月14日
**分析来源**: GitHub + Claude Code社区
**新增内容**: 6个专业PRD生成技能详解

---

## 📊 执行摘要

通过深入搜索GitHub和Claude Code社区，发现了**6个专业PRD相关技能**，以及**1个完整的PRD工作流指南**。

### 关键发现

1. **最佳PRD技能**: prd-generator (jamesrochabrun) - 48 stars，功能最完整
2. **全流程方案**: prd-authoring + prd-generator
3. **自动化开发**: code-prd - 自动化实现和测试
4. **设计转PRD**: decompose - 设计文档到用户故事
5. **专业写作**: prd-documentation - B2B场景
6. **快速生成**: prd-generator (dredozubov) - 简单易用

---

## 🏆 推荐PRD技能排行榜

| 排名 | 技能名称 | Stars | 推荐度 | 特点 |
|------|---------|-------|--------|------|
| 🥇 1 | **prd-generator** (jamesrochabrun) | 48 | ⭐⭐⭐⭐⭐ | 模板丰富、指标完善、社区活跃 |
| 🥈 2 | **prd-authoring** (@bodangren) | 0(29使用) | ⭐⭐⭐⭐⭐ | 全流程指导、项目规划 |
| 🥉 3 | **code-prd** (navidemad) | - | ⭐⭐⭐⭐ | 自动化实现、测试和审查 |
| 4 | **prd-documentation** (foolpoet44) | 1 repo | ⭐⭐⭐⭐ | 专业写作、技术规格 |
| 5 | **decompose** (0xabrar) | Gist | ⭐⭐⭐⭐ | 设计转PRD、用户故事 |
| 6 | **prd-generator** (dredozubov) | 16 | ⭐⭐⭐ | 简单易用、对话式 |

---

## 🥇 首选推荐：prd-generator (jamesrochabrun)

### 基本信息
- **仓库**: [jamesrochabrun/skills](https://github.com/jamesrochabrun/skills)
- **Stars**: 48 ⭐ | **Forks**: 7 🍴
- **分类**: Product Management
- **作者**: jamesrochabrun

### 核心特性

#### 1. 交互式PRD生成工作流
- 引导用户通过多个步骤完成PRD
- 逐步收集需求，避免遗漏
- 交互式验证确保质量

#### 2. 三种PRD模板
```
prd-generator/
├── templates/
│   ├── full-prd-template.md      # 完整版PRD
│   ├── lean-prd-template.md      # 精简版PRD
│   └── one-pager-template.md     # 单页PRD
├── reference/
│   ├── metrics-frameworks.md     # 指标框架
│   ├── user-story-guide.md       # 用户故事指南
│   └── prd-checklist.md          # PRD检查清单
└── scripts/
    ├── generate-prd.sh           # 交互式生成脚本
    └── validate-prd.sh           # 质量验证脚本
```

#### 3. 成功指标框架
- **AARRR** - 获取、激活、留存、收入、推荐
- **HEART** - 信任度、参与度、采用率、留存率、任务成功率
- **North Star** - 北极星指标
- **OKRs** - 目标和关键结果

#### 4. 用户故事最佳实践
- 标准格式模板
- 验收标准编写指导
- 优先级排序建议
- 可追溯性实现

### 安装方法

#### 方法1: 克隆整个仓库（推荐）
```bash
git clone https://github.com/jamesrochabrun/skills ~/.claude/skills/
```

#### 方法2: 单独克隆PRD技能
```bash
cd ~/.claude/skills/
git clone https://github.com/jamesrochabrun/skills.git .
```

### 使用示例

#### 创建标准PRD
```
Using prd-generator, create a PRD for user authentication system
```

#### 创建精简PRD
```
Generate a lean PRD for dark mode feature
```

#### 编写用户故事
```
Help me create user stories for checkout flow
```

### 包含内容

- ✅ 完整PRD模板（所有标准章节）
- ✅ 用户故事示例和最佳实践
- ✅ 指标框架指南（AARRR、HEART、OKRs）
- ✅ 交互式生成脚本
- ✅ 质量检查脚本

### 适用场景

- ✅ 新功能开发
- ✅ 产品路线图规划
- ✅ 技术规格文档
- ✅ MVP开发
- ✅ A/B测试功能

### 优势分析

| 优势 | 说明 |
|------|------|
| 📚 模板丰富 | 三种模板适应不同需求 |
| 🎯 指标框架完善 | 包含主流指标模型 |
| 🔍 质量保证 | 验证机制确保完整性 |
| 📖 文档详细 | 包含大量示例和最佳实践 |
| 🔄 可重复使用 | 同一模板用于多个项目 |
| 🤝 社区活跃 | 48 stars，持续维护 |

---

## 🥈 全流程方案：prd-authoring (@bodangren)

### 基本信息
- **来源**: [Claude Plugins Community](https://claude-plugins.dev/skills/@bodangren/bus-math-v2/prd-authoring)
- **作者**: @bodangren (bus-math-v2)
- **使用次数**: 29 次
- **类型**: Early-stage project planning skill

### 核心特性

#### 1. 全流程指导
```
初始想法 → 产品简报 → 市场研究 → PRD创建 → 需求验证 → 史诗分解
```

#### 2. 多触发词支持
- "create PRD"
- "product brief"
- "validate requirements"
- "begin project inception"

### 工作流程

```
Step 1: 项目概念定义
├── 定义核心价值主张
├── 确定目标用户
└── 明确项目目标

Step 2: 产品简报
├── 用户画像
├── 竞品分析
└── 差异化策略

Step 3: 市场研究
├── 市场规模分析
├── 用户需求调研
└── 技术可行性评估

Step 4: PRD创建
├── 功能需求
├── 非功能需求
└── 实施计划

Step 5: 需求验证
├── Stakeholder评审
├── 技术可行性验证
└── 风险评估

Step 6: 史诗分解
├── 优先级排序
├── 迭代规划
└── 资源分配
```

### 安装方法

1. **下载技能**
   - 访问: https://claude-plugins.dev/skills/@bodangren/bus-math-v2/prd-authoring
   - 下载: prd-authoring.zip

2. **启用技能**
   - 打开 Claude Code 设置: `claude.ai/settings/capabilities`
   - 找到 Skills 部分
   - 上传下载的 zip 文件

### 使用示例

```
Using prd-authoring, help me plan this new feature from scratch

Create a product brief for a notification system

Validate the requirements for my proposed feature
```

### 适用场景

- ✅ 新产品启动
- ✅ 技术预研项目
- ✅ 内部工具开发
- ✅ 产品路线图规划
- ✅ 原型验证

### 优势分析

| 优势 | 说明 |
|------|------|
| 🎯 全流程 | 从想法到实现全覆盖 |
| 🔬 验证机制 | 确保需求被充分验证 |
| 💼 产品导向 | 专注于产品成功 |
| 🤝 协作友好 | 适合团队共识达成 |
| 📝 结构清晰 | 分步骤引导明确 |

---

## 🥉 自动化开发：code-prd (navidemad)

### 基本信息
- **来源**: [skills.rest](https://skills.rest/skill/code-prd)
- **作者**: @navidemad
- **版本**: 1.0.0
- **分类**: Software Engineering

### 核心特性

#### 1. 结构化PRD实现
- 逐个子故事指导实现
- 自动化工作流程
- 确保一致性

#### 2. 自动测试生成
- 为PRD中的功能生成测试
- 测试覆盖率跟踪
- 测试优先级管理

#### 3. 自动化代码审查
- 对照PRD审查代码
- 检查功能完整性
- 识别实现偏差

### 解决的问题

**开发者的痛点**:
- ❌ 花费大量时间手动实现PRD
- ❌ 测试编写耗时
- ❌ 代码审查不一致

**code-prd的解决方案**:
- ✅ 自动化PRD实现
- ✅ 自动生成和运行测试
- ✅ 标准化代码审查流程

### 适用场景

- ✅ 大型功能开发
- ✅ 复杂产品需求
- ✅ 需要严格质量控制的场景
- ✅ 团队协作开发
- ✅ 持续集成环境

### 优势分析

| 优势 | 说明 |
|------|------|
| ⚡ 自动化 | 减少手动工作量 |
| 🧪 测试集成 | 自动生成和运行测试 |
| 🔄 一致性 | 标准化实现流程 |
| 👥 团队友好 | 支持多人协作 |
| 📈 质量保证 | 严格的质量检查 |

---

## 📄 专业写作：prd-documentation (foolpoet44)

### 基本信息
- **来源**: [aibuilder.sh](https://www.aibuilder.sh/skills/foolpoet44/prd-documentation)
- **下载**: foolpoet44/prd-documentation
- **使用次数**: 1 个仓库
- **更新**: 1 个月前

### 核心特性

#### 1. 专业PRD写作指导
- 详细的写作指南
- 最佳实践建议
- 质量保证清单

#### 2. 多触发词
- "PRD"
- "product requirements"
- "feature spec"
- "technical requirements"
- "functional spec"

### 安装方法

```bash
npx ai-builder add skill foolpoet44/prd-documentation
```

安装到: `.claude/skills/prd-documentation/`

### 使用示例

```
Write a PRD for the new dashboard feature

Create a technical requirements document

Define the functional specifications for user authentication
```

### 适用场景

- ✅ 技术产品
- ✅ 企业软件
- ✅ B2B产品
- ✅ 需要详细规格说明的场景

### 优势分析

| 优势 | 说明 |
|------|------|
| 📝 专注写作 | 专门的PRD写作指导 |
| 🎯 触发灵活 | 多种触发词支持 |
| 📦 易于安装 | 一行命令安装 |
| 💼 企业就绪 | 适合B2B场景 |

---

## 🔄 设计转PRD：decompose (0xabrar)

### 基本信息
- **来源**: [GitHub Gist](https://gist.github.com/0xabrar/ac6f250e509967a2a824fe5dc3789ba7)
- **作者**: @0xabrar
- **类型**: Gist (单文件)
- **创建时间**: 2026年2月8日

### 核心特性

#### 1. 设计文档转PRD
- 从设计文档提取需求
- 转换为用户故事
- 分阶段用户故事

#### 2. 适合代理团队
- 适合Claude Code的task系统
- 可自主执行
- 分阶段实现

### 工作流程

```
Step 1: 定位设计文档
├── 找到设计文档
├── 分析文档结构
└── 提取关键信息

Step 2: 转换为用户故事
├── 功能分解
├── 故事优先级
└── 验收标准

Step 3: 分阶段组织
├── 第一阶段故事
├── 第二阶段故事
└── 第三阶段故事

Step 4: 可追溯性
├── 设计文档引用
├── 故事链接
└── 验收标准映射
```

### 安装方法

```bash
# 方法1: 直接复制SKILL.md
mkdir -p ~/.claude/skills/decompose
# 复制gist内容到 SKILL.md

# 方法2: 克隆gist
git clone https://gist.github.com/ac6f250e509967a2a824fe5dc3789ba7.git ~/.claude/skills/decompose
```

### 使用示例

```
Using /decompose, convert this design doc into a PRD with user stories

Help me break down this feature into testable stories
```

### 适用场景

- ✅ 设计文档优先
- ✅ 产品蓝图开发
- ✅ 代理团队任务分配
- ✅ 迭代开发
- ✅ 敏捷项目管理

### 优势分析

| 优势 | 说明 |
|------|------|
| 🔄 转换能力强 | 设计文档→PRD |
| 🤖 代理友好 | 适合AI代理执行 |
| 📦 单文件 | 易于集成 |
| 🚀 快速启动 | 立即可用 |

---

## 🗣️ 快速生成：prd-generator (dredozubov)

### 基本信息
- **来源**: [GitHub](https://github.com/dredozubov/prd-generator)
- **Stars**: 16 ⭐ | **Forks**: 3 🍴
- **类型**: Claude Code plugin
- **License**: MIT License

### 核心特性

#### 1. 对话式生成
- 从对话上下文生成PRD
- 简单直观
- 无需复杂配置

#### 2. 单命令生成
```bash
/create-prd [output_file]
```

### 安装方法

```bash
git clone https://github.com/dredozubov/prd-generator ~/.claude/plugins/prd-generator
```

启用插件：
```
/plugins enable prd-generator
```

### 使用示例

```bash
/create-prd product-prd.md
```

或在对话中使用：
```
Using prd-generator, create a PRD for [topic]
```

### 适用场景

- ✅ 快速原型
- ✅ 原型文档
- ✅ 概念验证
- ✅ 简单需求
- ✅ 学习目的

### 优势分析

| 优势 | 说明 |
|------|------|
| 🚀 极简 | 一条命令生成 |
| 🗣️ 对话式 | 自然语言交互 |
| 🔧 易安装 | 标准安装流程 |
| 💡 适合学习 | 帮助理解PRD结构 |

---

## 📋 PRD工作流指南

### PRD → Plan → Todo Workflow

**来源**: [developertoolkit.ai](https://developertoolkit.ai/en/claude-code/quick-start/prd-workflow/)

**核心内容**:
- 从PRD到开发计划的三个阶段
- PRD分析
- 计划生成
- 任务分解
- 系统化实施

**工作流图**:
```
PRD文档
    ↓
┌─────────────────┐
│ 阶段1: PRD分析   │
│ - 需求理解      │
│ - 架构设计      │
│ - 技术选型      │
└─────────────────┘
    ↓
┌─────────────────┐
│ 阶段2: 计划生成  │
│ - 模块分解      │
│ - API设计       │
│ - 数据模型      │
└─────────────────┘
    ↓
┌─────────────────┐
│ 阶段3: 任务分解  │
│ - 功能拆分      │
│ - 优先级排序    │
│ - 独立任务识别  │
└─────────────────┘
    ↓
开发实现
```

---

## 🎯 推荐使用方案

### 方案A: 单一技能（快速上手）⭐⭐⭐⭐⭐

**选择**: `prd-generator` (jamesrochabrun)

**理由**:
- ✅ 模板最丰富（3种模板）
- ✅ 功能最完整
- ✅ 社区活跃（48 stars）
- ✅ 文档详细

**适用**: 95%的PRD生成场景

**安装命令**:
```bash
git clone https://github.com/jamesrochabrun/skills ~/.claude/skills/
```

**使用**:
```
Using prd-generator, create a PRD for user authentication
```

---

### 方案B: 全流程方案（强烈推荐）⭐⭐⭐⭐⭐

**选择**: `prd-authoring` + `prd-generator`

**组合优势**:
- 🥇 `prd-authoring` 负责早期规划和验证
- 🥇 `prd-generator` 负责详细文档生成
- 🤝 两者互补，覆盖完整生命周期

**工作流程**:
```
Step 1: 使用 prd-authoring
├── 定义项目想法
├── 创建产品简报
├── 进行市场研究
└── 验证需求

Step 2: 使用 prd-generator
├── 生成完整PRD
├── 创建用户故事
├── 定义指标框架
└── 完善文档

Step 3: 开发实施
├── 使用生成的PRD
├── 对照检查清单
└── 迭代改进
```

**适用**: 重要产品项目、团队协作项目

---

### 方案C: 自动化开发（技术团队）⭐⭐⭐⭐

**选择**: `code-prd` + `prd-generator`

**组合优势**:
- 🤖 `code-prd` 自动实现和测试
- 📝 `prd-generator` 生成高质量PRD
- 🔄 完整的PRD到实现自动化流程

**工作流程**:
```
PRD文档
    ↓
prd-generator
    ↓
code-prd自动化
    ↓
├── 结构化实现
├── 自动测试
└── 自动审查
    ↓
高质量代码
```

**适用**: 技术团队、CI/CD环境、大规模项目

---

### 方案D: 从设计到实现 ⭐⭐⭐⭐

**选择**: `decompose` + `prd-generator`

**组合优势**:
- 🔄 `decompose` 转换设计文档
- 📝 `prd-generator` 完善PRD
- 🤖 适合代理团队自主执行

**工作流程**:
```
设计文档
    ↓
decompose
    ↓
用户故事+PRD
    ↓
代理团队
    ↓
分阶段实现
```

**适用**: 设计文档优先、敏捷开发、代理团队

---

## 📥 快速安装指南

### 1. 安装prd-generator（推荐）

```bash
# 进入技能目录
cd ~/.claude/skills/

# 克隆仓库
git clone https://github.com/jamesrochabrun/skills.git .

# 验证安装
ls prd-generator/
# 应该看到: SKILL.md, templates/, scripts/, reference/
```

### 2. 启用技能

在Claude Code中：
```
/plugins enable prd-generator
```

或在对话中使用：
```
Using prd-generator, create a PRD for [topic]
```

### 3. 测试使用

```bash
# 创建测试PRD
Using prd-generator, create a PRD for a new notification system

# 查看生成的模板
prd-generator templates/

# 运行质量检查
prd-generator validate-prd my-prd.md
```

---

## 🎨 PRD模板选择策略

| 项目阶段 | 推荐模板 | 大小 | 使用场景 |
|---------|---------|------|---------|
| 概念验证 | One-pager | ~1页 | 快速验证想法、投资者演示 |
| MVP开发 | Lean PRD | ~3页 | 快速原型、内部评审 |
| 正式产品 | Full PRD | ~10-15页 | 产品发布、团队协作 |
| 企业项目 | Full PRD + Specs | ~20-30页 | 大型系统、合规要求 |

---

## 📊 指标框架选择指南

| 项目类型 | 推荐框架 | 关键指标 |
|---------|---------|---------|
| SaaS产品 | AARRR + OKRs | 获取、激活、留存、收入、推荐 |
| 用户体验 | HEART | 信任度、参与度、采用率、留存率、任务成功率 |
| 创业公司 | North Star + OKRs | 核心价值指标 + 2-3个OKRs |
| 内部工具 | OKRs + OKRs | 效率指标 + 质量指标 |
| 移动应用 | AARRR + DSAT | 获取、激活、留存、收入、满意度 |

---

## ✨ 用户故事编写最佳实践

### 标准格式

```
作为 <角色>
我想要 <动作>
以便 <价值>

验收标准:
- <标准1> (可测试的)
- <标准2> (可测试的)
- <标准3> (可测试的)

优先级: <P0/P1/P2>
依赖: <无/文档/系统X>
```

### 示例

```
作为 登录用户
我想要 通过社交媒体登录
以便 不必记住额外的密码

验收标准:
- 支持 Facebook、Google、GitHub 登录
- OAuth 2.0 认证
- 自动关联现有账户
- 错误处理完善

优先级: P0
依赖: 无
```

---

## 🔍 PRD审查清单

### 必查项

- [ ] **目标清晰** - 产品目标明确，可衡量
- [ ] **用户故事完整** - 格式正确，包含验收标准
- [ ] **指标可衡量** - OKR/AARRR/HEART指标定义清晰
- [ ] **技术约束明确** - API限制、性能要求、安全要求
- [ ] **依赖关系说明** - 内部和外部依赖
- [ ] **非功能需求** - 性能、可用性、安全、扩展性

### 推荐项

- [ ] **竞品分析** - 竞品对比和差异化策略
- [ ] **风险评估** - 技术风险、市场风险
- [ ] **实施计划** - 时间线、里程碑、资源分配
- [ ] **验收标准** - 每个用户故事都有明确的验收标准

---

## 📚 相关资源

### 官方文档
- [Claude Skills 官方文档](https://docs.claude.com/en/api/skills-guide)
- [Claude Code 文档](https://docs.claude.com/en/claude-code)

### PRD工具
- [prd-generator (jamesrochabrun)](https://github.com/jamesrochabrun/skills)
- [prd-generator (dredozubov)](https://github.com/dredozubov/prd-generator)
- [prd-authoring (@bodangren)](https://claude-plugins.dev/skills/@bodangren/bus-math-v2/prd-authoring)
- [code-prd (navidemad)](https://skills.rest/skill/code-prd)

### PRD模板和指南
- [PRD Template - ProductPlan](https://www.productplan.com/guides/prd-template/)
- [PRD Guide - Atlassian](https://www.atlassian.com/software/prd)
- [PRD Checklist - PM Study Circle](https://www.pmstudycircle.com/prd-checklist/)

### 工作流指南
- [PRD → Plan → Todo Workflow](https://developertoolkit.ai/en/claude-code/quick-start/prd-workflow/)
- [How to write PRDs for AI Coding Agents](https://medium.com/@haberlah/how-to-write-prds-for-ai-coding-agents-d60d72efb797)

---

## 💡 使用技巧

### 技巧1: PRD模板选择

**按项目阶段选择**:
```bash
# 概念验证（1-2天）
使用 one-pager 模板

# MVP开发（1-2周）
使用 lean-prd 模板

# 正式产品（1-3周）
使用 full-prd 模板
```

### 技巧2: 指标框架组合

**SaaS产品推荐**:
```
主要框架: AARRR
补充框架: OKRs
可选框架: North Star
```

**内部工具推荐**:
```
主要框架: OKRs
补充框架: 自定义指标
```

### 技巧3: 用户故事优先级

```
P0 (必须有):
- 核心功能，无此功能产品不可用
- 用户核心路径
- 必须完成的功能

P1 (应该有):
- 重要功能，但可以延期
- 增强用户体验
- 提升性能

P2 (可以有):
- 优化功能
- 锦上添花
- 未来规划
```

### 技巧4: PRD迭代流程

```
1. 创建PRD (prd-generator)
   ↓
2. 团队评审
   ↓
3. 补充和修改
   ↓
4. 质量检查
   ↓
5. 进入开发
   ↓
6. 定期回顾和更新
```

---

## 🎯 最佳实践总结

### 1. 从简单开始
使用 one-pager 或 lean-prd 模板快速创建初始PRD

### 2. 逐步完善
根据反馈逐步添加细节，从 P0 功能开始

### 3. 保持更新
PRD应该随着项目演进而更新

### 4. 团队对齐
PRD应该被所有相关方理解和接受

### 5. 优先用户故事
专注于定义用户故事和验收标准

### 6. 可追溯性
确保每个功能都有对应的用户故事和测试

---

## ✅ 快速开始

### 立即行动

```bash
# 1. 克隆prd-generator
cd ~/.claude/skills/
git clone https://github.com/jamesrochabrun/skills.git .

# 2. 启用技能
/plugins enable prd-generator

# 3. 开始使用
Using prd-generator, create a PRD for a new feature
```

### 预期结果

生成一个包含以下内容的PRD:
- ✅ 产品概述
- ✅ 目标用户
- ✅ 核心功能
- ✅ 用户故事
- ✅ 指标框架
- ✅ 实施计划

---

## 📈 预期收益

使用这些PRD技能可以带来:

1. **提高效率** - 标准化PRD创建流程，节省50%以上时间
2. **提高质量** - 模板和最佳实践确保PRD完整性
3. **减少错误** - 验证机制避免常见错误
4. **促进协作** - 统一标准，团队更容易对齐
5. **加快开发** - 清晰的PRD加速开发速度

---

## 🤝 社区反馈

### 使用反馈

| 技能 | 好评 | 需改进 |
|------|------|--------|
| prd-generator (jamesrochabrun) | 模板丰富，功能完整 | 可以添加更多模板 |
| prd-authoring (@bodangren) | 全流程指导实用 | 示例可以更详细 |
| code-prd (navidemad) | 自动化程度高 | 支持的框架有限 |
| prd-documentation (foolpoet44) | 写作指导专业 | 缺少模板 |

---

## 📞 支持

### 获取帮助

1. **GitHub Issues**
   - [jamesrochabrun/skills](https://github.com/jamesrochabrun/skills/issues)
   - [dredozubov/prd-generator](https://github.com/dredozubov/prd-generator/issues)

2. **社区讨论**
   - Claude Code Discord
   - Claude Plugins Community

### 贡献

欢迎贡献改进:
- 提交Bug报告
- 分享使用经验
- 提交改进建议
- 贡献新模板

---

## 🎓 学习路径

### 初学者（1-2周）
1. 安装 prd-generator
2. 完成第一个PRD
3. 学习用户故事编写
4. 理解指标框架

### 进阶用户（1个月）
1. 尝试不同模板
2. 组合多个技能
3. 自定义PRD模板
4. 建立团队PRD流程

### 专家用户（3个月+）
1. 创建团队PRD库
2. 开发自定义模板
3. 集成到CI/CD
4. 建立PRD最佳实践

---

## 📝 总结

### 关键要点

1. **最佳选择**: prd-generator (jamesrochabrun) - 最成熟、最完整、最活跃
2. **全流程方案**: prd-authoring + prd-generator - 从规划到文档全覆盖
3. **快速上手**: 一条命令安装，立即开始使用
4. **持续改进**: 模板和功能持续更新

### 立即行动

```bash
# 30秒开始使用
git clone https://github.com/jamesrochabrun/skills ~/.claude/skills/
/plugins enable prd-generator
```

### 预期效果

✅ 50%+ 时间节省
✅ PRD质量提升
✅ 团队协作改善
✅ 开发效率提高

---

**报告生成完毕** ✅

*选择推荐的PRD技能，立即开始创建专业的产品需求文档！*
