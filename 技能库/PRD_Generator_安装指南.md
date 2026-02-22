# PRD Generator - 安装和使用指南

**安装日期**: 2026年2月14日
**安装位置**: D:\ai-agentic\skills\skills\prd-generator
**状态**: ✅ 已成功安装

---

## 📦 安装确认

### 目录结构
```
D:\ai-agentic\skills\
└── skills/
    ├── prd-generator/
    │   ├── SKILL.md          ✅ 技能定义文件
    │   ├── scripts/           ✅ 生成和验证脚本
    │   └── references/        ✅ 参考文档和模板
    ├── anthropic-architect/
    ├── anthropic-prompt-engineer/
    └── ... (其他24个技能)
```

### 已安装内容

| 内容 | 路径 | 状态 |
|------|------|------|
| 技能定义 | `SKILL.md` | ✅ 已安装 |
| PRD模板 | `references/prd_template.md` | ✅ 已安装 |
| 用户故事示例 | `references/user_story_examples.md` | ✅ 已安装 |
| 指标框架指南 | `references/metrics_frameworks.md` | ✅ 已安装 |
| 生成脚本 | `scripts/generate_prd.sh` | ✅ 已安装 |
| 验证脚本 | `scripts/validate_prd.sh` | ✅ 已安装 |

---

## 🚀 使用方法

### 方法1: 直接对话（推荐）

在Claude对话中直接使用：

```
Using prd-generator, create a PRD for user authentication
```

### 方法2: 指定PRD类型

```
Using prd-generator, create a lean PRD for dark mode feature
```

```
Using prd-generator, create a technical PRD for payment integration
```

### 方法3: 交互式流程

```
Using prd-generator, create a PRD for a new analytics dashboard
```

然后系统会引导您完成以下步骤：

1. **Step 1: 收集信息**
   ```
   1. What problem are you trying to solve?
   2. Who is the primary user/audience for this feature?
   3. What are the key business objectives?
   4. Are there any technical constraints?
   5. What does success look like?
   6. What's the timeline for this feature?
   ```

2. **Step 2: 生成PRD结构**
   - Executive Summary
   - Problem Statement
   - Goals & Objectives
   - User Personas
   - User Stories & Requirements
   - Success Metrics
   - Scope
   - Technical Considerations
   - Design & UX Requirements
   - Timeline & Milestones
   - Risks & Mitigation
   - Dependencies & Assumptions

3. **Step 3: 创建用户故事**
   - 格式: "作为 [用户类型]，我想要 [动作]，以便 [价值]"
   - 包含验收标准

4. **Step 4: 定义成功指标**
   - AARRR (Pirate Metrics)
   - HEART Framework
   - North Star Metric
   - OKRs

5. **Step 5: 验证和完善**
   - 运行验证脚本
   - 检查完整性
   - 迭代改进

---

## 📋 PRD模板类型

### 1. 标准PRD (Standard PRD)
**适用场景**: 正式产品发布、团队协作项目

**特点**:
- 10-15页完整文档
- 包含所有标准章节
- 适合产品经理、工程师、设计师共享

### 2. 精简PRD (Lean PRD)
**适用场景**: 敏捷开发、快速迭代

**特点**:
- 3-5页
- 专注于核心需求
- 快速评审和变更

### 3. 单页PRD (One-Pager)
**适用场景**: 概念验证、投资者演示

**特点**:
- 1页文档
- 高层级概述
- 快速决策

### 4. 技术PRD (Technical PRD)
**适用场景**: 工程团队、系统设计

**特点**:
- 侧重技术要求
- 架构设计
- 技术约束和依赖

### 5. 设计PRD (Design PRD)
**适用场景**: UI/UX设计、用户体验研究

**特点**:
- 侧重UX/UI
- 用户流程
- 交互规范

---

## 💡 使用示例

### 示例1: 移动功能PRD

**用户请求**:
```
Create a PRD for adding biometric authentication to our mobile app
```

**生成内容**:
```
# PRD: Mobile App Biometric Authentication

## Executive Summary
Adding biometric authentication (Face ID/Touch ID) to improve security and user experience.

## Problem Statement
Users forget passwords, leading to support tickets and friction in login process.

## User Stories
As a logged-in user,
I want to use Face ID to log in,
So that I don't need to remember my password.

Acceptance Criteria:
- Face ID authentication succeeds for verified users
- Touch ID supported on devices with Touch ID
- Automatic fallback to password if biometric unavailable
- Biometric preferences saved and syncable across devices

## Success Metrics
- Login success rate with biometric: >90%
- Support tickets related to password reset: -40%
- User satisfaction with login experience: +25%
```

### 示例2: Web平台增强

**用户请求**:
```
Write requirements for improving checkout conversion rate
```

**生成内容**:
```
# PRD: Checkout Flow Optimization

## Current State
Average conversion rate: 2.3%
Top drop-off points:
- Step 1: Shipping info (35%)
- Step 2: Payment selection (28%)
- Step 3: Guest checkout (22%)

## Proposed Enhancements
1. Guest checkout option
2. Saved payment methods
3. Progress indicator
4. Trust badges

## Success Metrics
- Conversion rate: 2.3% → 3.5% (+52%)
- Average checkout time: -30%
- Cart abandonment rate: -25%
```

### 示例3: B2B产品PRD

**用户请求**:
```
Create a PRD for an admin dashboard for enterprise customers
```

**生成内容**:
```
# PRD: Enterprise Admin Dashboard

## Executive Summary
Comprehensive admin interface for enterprise customers to manage their organizations.

## User Personas
- **Super Admin**: Full access, multiple organizations
- **Organization Admin**: Team management
- **Analyst**: Reporting and insights

## Key Features
- Multi-tenant management
- Role-based access control
- Advanced reporting
- Integration capabilities (SSO, SCIM)

## Success Metrics
- Admin efficiency: +40% task completion
- Support tickets: -35% (self-service)
- Customer satisfaction: 4.5/5 stars
```

---

## 🔍 PRD验证

### 使用验证脚本

```bash
# 基本验证
cd D:\ai-agentic\skills\skills\prd-generator
scripts/validate_prd.sh my_prd.md

# 详细输出
scripts/validate_prd.sh my_prd.md --verbose

# 检查特定章节
scripts/validate_prd.sh my_prd.md --sections "user-stories,metrics"
```

### 验证检查项

脚本会检查：
- ✅ 所有必需章节是否存在
- ✅ 用户故事格式是否正确
- ✅ 成功指标是否定义
- ✅ 范围是否清晰
- ✅ 是否有占位符文本

---

## 📚 参考资源

### 内置参考文档

1. **PRD模板** (`references/prd_template.md`)
   - 标准PRD结构
   - 13个必需章节
   - 格式示例

2. **用户故事示例** (`references/user_story_examples.md`)
   - 常见用户故事模式
   - 最佳实践
   - 反面示例

3. **指标框架** (`references/metrics_frameworks.md`)
   - AARRR详解
   - HEART详解
   - OKRs详解
   - 使用场景指南

### 外部资源

- [PRD Best Practices](https://www.productplan.com/guides/prd-template/)
- [User Story Best Practices](https://www.atlassian.com agile/user-stories)
- [OKR Framework](https://www.atlassian.com/ agile/okrs)

---

## ⚡ 快速开始

### 30秒创建第一个PRD

1. **打开Claude**
2. **输入命令**:
   ```
   Using prd-generator, create a PRD for a new notification system
   ```
3. **回答发现问题**
4. **获取完整PRD**

### 5分钟创建专业PRD

1. **选择PRD类型**:
   ```
   Create a lean PRD for [your feature]
   ```

2. **回答关键问题**:
   - What problem are you solving?
   - Who is the target user?
   - What are business goals?
   - What does success look like?

3. **Review and iterate**
4. **Validate with script**

---

## 🎯 最佳实践

### 写作质量要求

**好的需求应该是**:
- ✅ **具体的**: 清晰明确
- ✅ **可衡量的**: 可验证/测试
- ✅ **可实现的**: 技术可行
- ✅ **相关的**: 关联用户/业务价值
- ✅ **有时间限制的**: 明确时间线

**避免**:
- ❌ 模糊语言 ("快", "易", "直观")
- ❌ 实现细节 (让工程师决定如何)
- ❌ 需求蔓延 (坚持核心需求)
- ❌ 未验证的假设

### 用户故事最佳实践

**DO**:
- 专注于用户价值，而非功能
- 从用户角度编写
- 包含清晰的验收标准
- 保持故事独立和简洁
- 使用一致格式

**DON'T**:
- 编写技术实现细节
- 创建故事间依赖
- 让故事太大 (史诗)
- 使用内部行话
- 跳过验收标准

---

## 🐛 常见问题

### Q1: PRD太长怎么办？

**A**: 创建 "Lean PRD"，专注于：
- 问题陈述
- 解决方案方法
- 验收标准
- 成功指标

保留完整PRD用于重大举措。

### Q2: 需求太模糊怎么办？

**A**: 添加具体示例，使用具体数字，包含视觉参考。
将 "快" 替换为 "在2秒内加载"。

### Q3: 利益相关者不统一怎么办？

**A**: 提前分享PRD草稿，收集反馈，当面演示，正式批准前获得明确签名。

### Q4: 范围不断扩展怎么办？

**A**: 积极使用 "Out of Scope" 部分，为未来阶段创建单独的PRD，将范围与时间线约束绑定。

### Q5: 工程师说不可行怎么办？

**A**: 更早让工程参与流程，对解决方案方法保持灵活，专注于问题而非实现。

---

## 📈 预期收益

使用PRD Generator可以带来：

1. **时间节省**: 50%以上时间节省在PRD创建上
2. **质量提升**: 模板和最佳实践确保完整性
3. **减少错误**: 验证机制避免常见问题
4. **促进协作**: 统一标准，团队更容易对齐
5. **加快开发**: 清晰的PRD加速开发速度

---

## 🔧 高级用法

### PRD模板定制

要创建自定义模板：
1. 复制 `references/prd_template.md`
2. 根据团队需求修改
3. 更新 `SKILL.md` 中的模板引用

### 集成到工作流

```bash
# 开发前
Using prd-generator, create a PRD for [feature]
# 获取PRD

# 开发中
prd-generator validate-prd my_prd.md
# 验证完整性

# 发布前
prd-generator validate-prd my_prd.md --verbose
# 深度检查
```

---

## 📞 支持和反馈

### 获取帮助

1. **查看SKILL.md**: 完整使用说明
2. **参考文档**: `references/` 目录
3. **GitHub Issues**: 报告问题或建议

### 贡献

欢迎改进PRD Generator：
- 提交问题报告
- 分享使用经验
- 提交改进建议
- 贡献新模板

---

## ✅ 检查清单

安装后确认：

- [ ] ✅ prd-generator技能已安装
- [ ] ✅ SKILL.md文件存在
- [ ] ✅ scripts/目录包含生成和验证脚本
- [ ] ✅ references/目录包含参考文档
- [ ] ✅ 能够使用 `/plugins enable prd-generator` 启用
- [ ] ✅ 能够使用 "Using prd-generator, create a PRD for..." 开始使用

---

**安装完成！现在可以开始创建专业的PRD了！** 🎉

**下一步**: 尝试创建您的第一个PRD
```
Using prd-generator, create a PRD for [your feature]
```
