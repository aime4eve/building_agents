# 优秀的PRD（产品需求文档）生成技能清单

**生成日期**: 2026年2月14日
**来源**: GitHub & Claude Code社区
**目的**: 为您推荐好用的PRD生成技能

---

## 📊 执行摘要

通过搜索GitHub和Claude Code社区，找到了**6个优秀的PRD相关技能**，涵盖PRD生成的完整生命周期：从需求分析到文档编写，再到实现和测试。

### 推荐优先级

| 排名 | 技能名称 | 推荐 | 核心优势 |
|------|---------|------|---------|
| 🥇 1 | **prd-generator** (jamesrochabrun) | ⭐⭐⭐⭐⭐ | 交互式流程、模板丰富、指标框架 |
| 🥈 2 | **prd-authoring** (@bodangren) | ⭐⭐⭐⭐⭐ | 全流程指导、项目规划、验证 |
| 🥉 3 | **code-prd** (navidemad) | ⭐⭐⭐⭐ | 自动化实现、测试和审查 |
| 4 | **prd-documentation** (foolpoet44) | ⭐⭐⭐⭐ | 专业PRD写作、技术规格 |
| 5 | **decompose** (0xabrar) | ⭐⭐⭐⭐ | 设计文档转PRD、用户故事 |
| 6 | **prd-generator** (dredozubov) | ⭐⭐⭐ | 简单易用、对话式生成 |

---

## 🥇 首选推荐：prd-generator (jamesrochabrun)

### 基本信息
- **仓库**: [jamesrochabrun/skills](https://github.com/jamesrochabrun/skills)
- **Stars**: 48 ⭐ | **Forks**: 7 🍴
- **分类**: Product Management
- **路径**: `skills/skills/prd-generator`

### 核心特性

1. **交互式PRD生成工作流**
   - 引导用户通过多个步骤完成PRD
   - 逐步收集需求，避免遗漏
   - 交互式验证确保质量

2. **多种PRD模板**
   - **完整版PRD模板** - 包含所有标准章节
   - **精简版PRD** - 快速原型规划
   - **单页PRD** - 一页纸概述

3. **用户故事创建**
   - 标准化用户故事格式
   - 包含验收标准
   - 支持优先级排序

4. **成功指标框架**
   - **AARRR框架** - 获取、激活、留存、收入、推荐
   - **HEART框架** - 信任度、参与度、采用率、留存率、任务成功率
   - **North Star Metric** - 北极星指标
   - **OKRs** - 目标和关键结果

5. **PRD验证和完整性检查**
   - 自动检查缺失章节
   - 验证用户故事完整性
   - 确保指标可衡量

### 安装方法

#### 方法1: 克隆整个仓库（推荐）
```bash
git clone https://github.com/jamesrochabrun/skills ~/.claude/skills/
```

然后启用该技能：
```
/plugins enable prd-generator
```

#### 方法2: 仅克隆PRD技能
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

---

## 🥈 强力推荐：prd-authoring (@bodangren)

### 基本信息
- **来源**: [Claude Plugins Community](https://claude-plugins.dev/skills/@bodangren/bus-math-v2/prd-authoring)
- **Stars**: 0 ⭐ | **使用次数**: 29 次
- **分类**: Early-stage project planning
- **作者**: @bodangren (bus-math-v2)

### 核心特性

1. **全流程指导**
   - 从初始项目想法到产品简报
   - 市场研究
   - PRD创建
   - 需求验证
   - 史诗分解

2. **早期阶段专注**
   - 专门针对项目启动阶段
   - 帮助团队达成共识
   - 降低项目失败风险

3. **多触发词支持**
   - "create PRD"
   - "product brief"
   - "validate requirements"
   - "begin project inception"

### 安装方法

1. **下载技能**
   ```
   访问: https://claude-plugins.dev/skills/@bodangren/bus-math-v2/prd-authoring
   下载: prd-authoring.zip
   ```

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

## 🥉 实现自动化：code-prd (navidemad)

### 基本信息
- **来源**: [skills.rest](https://skills.rest/skill/code-prd)
- **作者**: @navidemad
- **版本**: 1.0.0
- **分类**: Software Engineering

### 核心特性

1. **结构化PRD实现**
   - 逐个子故事指导实现
   - 自动化工作流程
   - 确保一致性

2. **自动测试生成**
   - 为PRD中的功能生成测试
   - 测试覆盖率跟踪
   - 测试优先级管理

3. **自动化代码审查**
   - 对照PRD审查代码
   - 检查功能完整性
   - 识别实现偏差

4. **AI结对编程**
   - 带有过程知识的AI配对程序员
   - 减少手动工作
   - 降低认知负荷

### 解决的问题

- 开发者花费大量时间手动实现PRD
- 测试编写耗时
- 代码审查不一致

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

1. **专业PRD写作**
   - 详细的写作指导
   - 最佳实践建议
   - 质量保证清单

2. **多触发词**
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

## 🔄 转换工具：decompose (0xabrar)

### 基本信息
- **来源**: [GitHub Gist](https://gist.github.com/0xabrar/ac6f250e509967a2a824fe5dc3789ba7)
- **作者**: @0xabrar
- **类型**: Gist (单文件)
- **创建时间**: 2026年2月8日

### 核心特性

1. **设计文档转PRD**
   - 从设计文档提取需求
   - 转换为用户故事
   - 分阶段用户故事

2. **适合代理团队**
   - 适合Claude Code的task系统
   - 可自主执行
   - 分阶段实现

3. **结构化输出**
   - Markdown格式
   - 包含验收标准
   - 可追溯性

### 安装方法

```bash
# 方法1: 直接复制SKILL.md到技能目录
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

- ✅ 设计文档转需求
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

## 🗣️ 简单易用：prd-generator (dredozubov)

### 基本信息
- **来源**: [GitHub](https://github.com/dredozubov/prd-generator)
- **Stars**: 16 ⭐ | **Forks**: 3 🍴
- **类型**: Claude Code plugin
- **License**: MIT License

### 核心特性

1. **对话式生成**
   - 从对话上下文生成PRD
   - 简单直观
   - 无需复杂配置

2. **单命令**
   - `/create-prd [output_file]`
   - 简单明了
   - 快速生成

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

## 📚 额外资源：PRD工作流指南

### PRD → Plan → Todo Workflow

**来源**: [developertoolkit.ai](https://developertoolkit.ai/en/claude-code/quick-start/prd-workflow/)

**核心内容**:
- 从PRD到开发计划的三个阶段
- PRD分析
- 计划生成
- 任务分解
- 系统化实施

**适用场景**: Claude Code特定工作流，帮助团队更快速地实现PRD

---

## 🏆 最终推荐方案

### 方案A: 单一技能（快速上手）
**推荐**: `prd-generator` (jamesrochabrun)

**理由**:
- ✅ 模板最丰富（3种模板）
- ✅ 功能最完整
- ✅ 社区活跃（48 stars）
- ✅ 文档详细

**适用**: 95%的PRD生成场景

### 方案B: 全流程方案（推荐）
**组合**: `prd-authoring` + `prd-generator`

**理由**:
- 🥇 `prd-authoring` 负责早期规划和验证
- 🥇 `prd-generator` 负责详细文档生成
- 🤝 两者互补，覆盖完整生命周期

**适用**: 重要产品项目、团队协作项目

### 方案C: 自动化开发（技术团队）
**组合**: `code-prd` + `prd-generator`

**理由**:
- 🤖 `code-prd` 自动实现和测试
- 📝 `prd-generator` 生成高质量PRD
- 🔄 完整的PRD到实现自动化流程

**适用**: 技术团队、CI/CD环境、大规模项目

### 方案D: 从设计到实现
**组合**: `decompose` + `prd-generator`

**理由**:
- 🔄 `decompose` 转换设计文档
- 📝 `prd-generator` 完善PRD
- 🤖 适合代理团队自主执行

**适用**: 设计文档优先、敏捷开发、代理团队

---

## 📥 快速安装指南

### Claude Code安装

```bash
# 1. 进入技能目录
cd ~/.claude/skills/

# 2. 克隆推荐的技能
git clone https://github.com/jamesrochabrun/skills.git .

# 3. 或单独克隆prd-generator
git clone https://github.com/jamesrochabrun/skills.git .
```

### 验证安装

```bash
# 检查技能是否安装
ls ~/.claude/skills/prd-generator/

# 应该看到这些文件：
# SKILL.md
# templates/
# reference/
# scripts/
```

### 启用技能

在Claude Code中：
```
/plugins enable prd-generator
```

或在对话中使用：
```
Using prd-generator, create a PRD for [topic]
```

---

## 🎯 使用技巧

### 1. PRD模板选择策略

| 项目阶段 | 推荐模板 |
|---------|---------|
| 概念验证 | 单页PRD |
| MVP开发 | 精简版PRD |
| 正式产品 | 完整版PRD |
| 企业项目 | 完整版+技术规格 |

### 2. 指标框架选择

| 项目类型 | 推荐框架 |
|---------|---------|
| SaaS产品 | AARRR + OKRs |
| 用户体验 | HEART |
| 创业公司 | North Star + OKRs |
| 内部工具 | OKRs + OKRs |

### 3. 用户故事编写

**标准格式**:
```
作为 <角色>
我想要 <动作>
以便 <价值>

验收标准:
- <标准1>
- <标准2>
- <标准3>
```

### 4. PRD审查清单

- ✅ 目标清晰
- ✅ 用户故事完整
- ✅ 验收标准可测试
- ✅ 指标可衡量
- ✅ 技术约束明确
- ✅ 依赖关系说明
- ✅ 非功能需求定义

---

## 📖 相关资源

### 官方资源
- [Claude Skills 官方文档](https://docs.claude.com/en/api/skills-guide)
- [Claude Code 文档](https://docs.claude.com/en/claude-code)

### PRD最佳实践
- [PRD Template - ProductPlan](https://www.productplan.com/guides/prd-template/)
- [PRD Guide - Atlassian](https://www.atlassian.com/software/prd)
- [PRD Checklist - PM Study Circle](https://www.pmstudycircle.com/prd-checklist/)

### 工作流指南
- [PRD → Plan → Todo Workflow](https://developertoolkit.ai/en/claude-code/quick-start/prd-workflow/)
- [How to write PRDs for AI Coding Agents](https://medium.com/@haberlah/how-to-write-prds-for-ai-coding-agents-d60d72efb797)

---

## 🤝 社区支持

### 技能评分

| 技能 | Stars | 用途 | 推荐度 |
|------|-------|------|--------|
| prd-generator (jamesrochabrun) | 48 | 生成PRD | ⭐⭐⭐⭐⭐ |
| prd-generator (dredozubov) | 16 | 快速生成 | ⭐⭐⭐ |
| prd-authoring (@bodangren) | 0 (29 uses) | 规划验证 | ⭐⭐⭐⭐⭐ |
| code-prd (navidemad) | - | 自动化实现 | ⭐⭐⭐⭐ |
| prd-documentation (foolpoet44) | - (1 repo) | 专业写作 | ⭐⭐⭐⭐ |
| decompose (0xabrar) | - | 文档转换 | ⭐⭐⭐⭐ |

---

## ✅ 总结

### 最佳选择

**立即安装**:
```bash
git clone https://github.com/jamesrochabrun/skills.git ~/.claude/skills/
```

**启用技能**:
```
/plugins enable prd-generator
```

**开始使用**:
```
Using prd-generator, create a PRD for my new feature
```

### 为什么选择prd-generator？

1. ✅ **最成熟** - 48 stars，经过充分验证
2. ✅ **最完整** - 包含所有必要功能
3. ✅ **最灵活** - 3种模板适应不同需求
4. ✅ **最专业** - 指标框架完善
5. ✅ **最易用** - 交互式流程简单明了

### 下一步

1. 安装prd-generator技能
2. 尝试创建第一个PRD
3. 根据项目需求调整模板
4. 集成到团队工作流

---

**报告生成完毕** ✅

*建议根据您的具体需求选择合适的技能，推荐从prd-generator开始，它是目前最成熟和最完整的PRD生成工具。*
