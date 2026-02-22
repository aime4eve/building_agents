# BPMN流程定义合集

## 文档信息

| 项目 | 内容 |
|------|------|
| 文档版本 | v1.1 |
| 创建日期 | 2026-02-20 |
| 更新日期 | 2026-02-20 |
| 作者 | 工作流引擎专家 |
| 适用阶段 | Phase 3: 智慧园区平台化 |

---

## 修订历史

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| v1.0 | 2026-02-20 | 初始版本 | workflow-expert |
| v1.1 | 2026-02-20 | 根据DDD评审意见修订：<br>1. 统一流程变量类型定义<br>2. 补充领域事件映射关系<br>3. 补充租户隔离实现说明<br>4. 补充批量调拨处理逻辑<br>5. 增加通用Delegate异常处理 | workflow-expert |

---

## 目录

1. [流程变量类型定义规范](#流程变量类型定义规范)
2. [物业维修工单流程](#2-物业维修工单流程)
3. [租赁合同审批流程](#3-租赁合同审批流程)
4. [资产调拨流程](#4-资产调拨流程)
5. [通用组件设计](#5-通用组件设计)
6. [流程版本管理](#6-流程版本管理)
7. [部署清单](#7-部署清单)

---

## 流程变量类型定义规范

### 标准类型映射表

| Java类型 | BPMN类型 | 说明 | 示例 |
|---------|---------|------|------|
| String | string | 文本 | tenantId, reporterId |
| Integer | int | 整数 | priority, duration |
| Long | long | 长整数 | timestamp |
| Double | double | 浮点数 | totalAmount, unitPrice |
| Boolean | boolean | 布尔值 | isApproved, spaceAvailable |
| Date | date | 日期 | leaseStartDate, expectedTransferDate |
| DateTime | datetime | 日期时间 | responseDeadline, resolutionDeadline |
| Enum | enum | 枚举 | workOrderCategory, satisfaction |
| List<String> | list | 字符串列表 | assetIds |
| JsonObject | json | JSON对象 | metadata |

### 流程变量定义格式

```
变量名 | 类型(子类型) | 必填 | 默认值 | 可选值/说明
```

### 租户隔离实现

所有流程必须包含 `tenantId` 变量，用于实现多租户数据隔离：

- **流程启动时**：从请求上下文中获取租户ID，设置到流程变量
- **数据库查询**：所有查询必须包含 `tenantId` 条件
- **事件发布**：领域事件必须包含 `tenantId` 属性
- **权限验证**：基于租户ID进行数据访问权限验证

---

## 2. 物业维修工单流程

### 2.1 流程概述

| 属性 | 值 |
|------|-----|
| 流程编号 | `property-repair-workorder` |
| 流程名称 | 物业维修工单 |
| 业务场景 | 租户报修、物业维修处理 |
| 目标SLA | 响应时间30分钟，解决时间4小时 |
| 优先级 | 根据报修类型自动判断 |
| 限界上下文 | 智慧园区 - 物业管理 |
| 聚合根 | WorkOrder |
| 相关领域事件 | WorkOrderCreatedEvent, WorkOrderAssignedEvent, WorkOrderAcceptedEvent, WorkOrderCompletedEvent, WorkOrderConfirmedEvent |

### 2.2 流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           物业维修工单流程                                   │
│                   (property-repair-workorder)                                │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │   开始节点    │
                    │  (start)     │
                    └──────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  工单创建       │
                  │  (create)       │
                  │  租户提交报修    │
                  └────────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  报修分类       │
                  │  (分类网关)     │
                  └────┬───────┬───┘
         ┌─────────┤         ├─────────┐
         │         │         │         │
      紧急      一般      投诉      咨询
         │         │         │         │
         ▼         ▼         ▼         ▼
    ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
    │SLA:15分│ │SLA:30分│ │SLA:2小时│ │SLA:1天 │
    └───┬────┘ └───┬────┘ └───┬────┘ └───┬────┘
        └─────────┴─────────┴─────────┘
                       │
                       ▼
              ┌────────────────┐
              │  自动派单       │
              │ (服务任务)      │
              │ 调用规则引擎    │
              └────────┬───────┘
                       │
                       ▼
              ┌────────────────┐
              │  派单结果判断   │
              │   (排他网关)    │
              └────┬───────┬───┘
               成功│       │失败
                   ▼       ▼
          ┌──────────┐ ┌──────────┐
          │ 维修接单  │ │主管派单  │
          │(用户任务)│ │(用户任务)│
          └────┬─────┘ └────┬─────┘
               │            │
               └────┬───────┘
                    │
                    ▼
          ┌────────────────┐
          │  SLA计时开始    │
          │  (监听器)       │
          └────────┬───────┘
                   │
                   ▼
          ┌────────────────┐
          │  维修处理       │
          │  (用户任务)     │
          │  维修人员执行   │
          └────────┬───────┘
                   │
                   ▼
          ┌────────────────┐
          │  上传维修照片   │
          │  (服务任务)     │
          │  调用存储服务   │
          └────────┬───────┘
                   │
                   ▼
          ┌────────────────┐
          │  填写维修结果   │
          │  (用户任务)     │
          └────────┬───────┘
                   │
                   ▼
          ┌────────────────┐
          │  租户确认评价   │
          │  (用户任务)     │
          └────────┬───────┘
                   │
                   ▼
          ┌────────────────┐
          │  满意度判断     │
          │   (排他网关)    │
          └────┬───────┬───┘
             满意│       │不满意
                 ▼       ▼
          ┌──────────┐ ┌──────────┐
          │  工单关闭 │ │ 回访处理  │
          │ (结束节点)│ │(用户任务)│
          └──────────┘ └────┬─────┘
                            │
                            ▼
                     ┌──────────┐
                     │  工单关闭 │
                     │ (结束节点)│
                     └──────────┘
```

### 2.3 流程变量定义

| 变量名 | 类型(子类型) | 必填 | 默认值 | 可选值/说明 |
|--------|-------------|------|--------|-------------|
| **租户信息** ||||
| tenantId | String | 是 | - | 租户ID，用于数据隔离 |
| reporterId | String | 是 | - | 报修人ID |
| **工单信息** ||||
| workOrderNo | String | 自动 | - | 工单编号（自动生成） |
| description | String | 是 | - | 报修描述 |
| contactPhone | String | 否 | - | 联系电话 |
| workOrderCategory | String(enum) | 是 | NORMAL | URGENT(紧急报修) / NORMAL(一般报修) / COMPLAINT(投诉) / INQUIRY(咨询) |
| spaceId | String | 是 | - | 空间ID |
| **派单信息** ||||
| assigneeId | String | 自动 | - | 派单维修人员ID |
| assignSuccess | Boolean | 自动 | false | 自动派单是否成功 |
| **SLA信息** ||||
| priority | String(enum) | 自动 | MEDIUM | CRITICAL(紧急) / HIGH(高) / MEDIUM(中) / LOW(低) |
| responseDeadline | DateTime | 自动 | - | 响应时间截止 |
| resolutionDeadline | DateTime | 自动 | - | 解决时间截止 |
| **维修信息** ||||
| repairResult | String(enum) | 是 | - | COMPLETED(已完成) / PENDING(需配件) / ESCALATED(需升级) |
| repairDescription | String | 是 | - | 处理说明 |
| partsUsed | String | 否 | - | 使用配件 |
| laborHours | Double | 否 | - | 工时 |
| **评价信息** ||||
| satisfaction | String(enum) | 是 | - | VERY_SATISFIED(非常满意) / SATISFIED(满意) / NEUTRAL(一般) / DISSATISFIED(不满意) / VERY_DISSATISFIED(非常不满意) |
| feedback | String | 否 | - | 反馈意见 |

### 2.4 领域事件映射

| 流程节点 | 领域事件 | 触发时机 | 携带数据 |
|---------|---------|---------|---------|
| 工单创建 | WorkOrderCreatedEvent | create-workorder节点完成 | workOrderId, tenantId, reporterId, description, workOrderCategory |
| 自动派单成功 | WorkOrderAssignedEvent | auto-assign节点完成，assignSuccess=true | workOrderId, assigneeId, assignmentType(AUTO), assignedAt |
| 维修接单 | WorkOrderAcceptedEvent | repair-accept节点完成 | workOrderId, assigneeId, acceptedAt |
| 维修完成 | WorkOrderCompletedEvent | repair-processing节点完成 | workOrderId, assigneeId, repairResult, repairDescription, completedAt |
| 租户确认 | WorkOrderConfirmedEvent | tenant-confirm节点完成 | workOrderId, satisfaction, feedback, confirmedAt |
| 工单关闭 | WorkOrderClosedEvent | end节点触发 | workOrderId, closedAt, totalDuration |

### 2.5 SLA配置

| 报修类型 | 响应时间 | 解决时间 | 优先级 |
|---------|---------|---------|--------|
| 紧急报修(URGENT) | 15分钟 | 2小时 | CRITICAL |
| 一般报修(NORMAL) | 30分钟 | 4小时 | MEDIUM |
| 投诉(COMPLAINT) | 2小时 | 24小时 | HIGH |
| 咨询(INQUIRY) | 4小时 | 24小时 | LOW |

---

## 3. 租赁合同审批流程

### 3.1 流程概述

| 属性 | 值 |
|------|-----|
| 流程编号 | `lease-contract-approval` |
| 流程名称 | 租赁合同审批 |
| 业务场景 | 招商租赁合同签订 |
| 目标SLA | 审批3个工作日 |
| 限界上下文 | 智慧园区 - 招商租赁 |
| 聚合根 | LeaseContract |
| 相关领域事件 | LeaseContractCreatedEvent, LeaseContractApprovedEvent, LeaseContractSignedEvent |

### 3.2 流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         租赁合同审批流程                                     │
│                     (lease-contract-approval)                                │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │   开始节点    │
                    │  (start)     │
                    └──────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  创建合同草稿   │
                  │ (create-draft)  │
                  │ 销售人员填写    │
                  └────────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  空间可用性检查 │
                  │  (服务任务)     │
                  │ 调用空间服务    │
                  └────────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  空间是否可用   │
                  │   (排他网关)    │
                  └────┬───────┬───┘
                   可用│       │不可用
                       ▼       ▼
              ┌──────────┐ ┌──────────┐
              │ 计算租金  │ │ 流程终止  │
              │(服务任务) │ │ (结束节点)│
              └─────┬────┘ └──────────┘
                    │
                    ▼
              ┌──────────┐
              │部门主管审批│
              │(用户任务) │
              └─────┬────┘
                    │
                    ▼
              ┌────────────────┐
              │  审批结果判断   │
              │   (排他网关)    │
              └────┬───────┬───┘
              通过│       │拒绝
                  ▼       ▼
          ┌──────────┐ ┌──────────┐
          │ 金额判断  │ │流程终止  │
          │(排他网关) │ │ (结束)   │
          └────┬─────┘ └──────────┘
               │
      ┌────────┴────────┐
      │                 │
  ≤50万              >50万
      │                 │
      ▼                 ▼
┌──────────┐      ┌──────────┐
│ 合同签署  │      │ 总经理审批│
│(用户任务)│      │(用户任务) │
└─────┬────┘      └─────┬────┘
      │                 │
      └────────┬────────┘
               │
               ▼
      ┌────────────────┐
      │   生成正式合同  │
      │   (服务任务)    │
      └────────┬───────┘
               │
               ▼
      ┌────────────────┐
      │   签约提醒     │
      │   (服务任务)    │
      │   调用通知中心  │
      └────────┬───────┘
               │
               ▼
      ┌────────────────┐
      │   流程结束     │
      │   (结束节点)    │
      └────────────────┘
```

### 3.3 流程变量定义

| 变量名 | 类型(子类型) | 必填 | 默认值 | 可选值/说明 |
|--------|-------------|------|--------|-------------|
| **租户信息** ||||
| tenantId | String | 是 | - | 租户ID，用于数据隔离 |
| salespersonId | String | 是 | - | 销售人员ID |
| **空间信息** ||||
| spaceId | String | 是 | - | 空间ID |
| spaceAvailable | Boolean | 自动 | - | 空间是否可用（系统检查） |
| **合同信息** ||||
| contractNo | String | 自动 | - | 合同编号（自动生成） |
| contractType | String(enum) | 是 | OFFICE | OFFICE(办公室) / WAREHOUSE(仓库) / STORE(商铺) / FACTORY(厂房) / SHOWROOM(展厅) |
| leaseStartDate | Date | 是 | - | 租赁开始日期 |
| leaseEndDate | Date | 是 | - | 租赁结束日期 |
| leaseArea | Double | 是 | - | 租赁面积（平米） |
| unitPrice | Double | 是 | - | 单价（元/平米/月） |
| totalAmount | Double | 自动 | - | 合同总额（自动计算） |
| **付款信息** ||||
| paymentCycle | String(enum) | 是 | MONTHLY | MONTHLY(月付) / QUARTERLY(季付) / SEMI_ANNUALLY(半年付) / ANNUALLY(年付) |
| depositMonths | Integer | 是 | - | 押金月数 |
| **审批信息** ||||
| managerApproval | String(enum) | 是 | - | APPROVED(同意) / REJECTED(拒绝) / RETURNED(退回修改) |
| managerComment | String | 否 | - | 部门主管审批意见 |
| gmApproval | String(enum) | 否 | - | APPROVED(同意) / REJECTED(拒绝) |
| gmComment | String | 否 | - | 总经理审批意见 |
| **签约信息** ||||
| signingMethod | String(enum) | 是 | ELECTRONIC | ELECTRONIC(电子签章) / PHYSICAL(纸质签署) |
| contractFileUrl | String | 是 | - | 合同文件URL |

### 3.4 领域事件映射

| 流程节点 | 领域事件 | 触发时机 | 携带数据 |
|---------|---------|---------|---------|
| 合同草稿创建 | LeaseContractCreatedEvent | create-draft节点完成 | contractId, tenantId, salespersonId, spaceId, contractType, totalAmount |
| 空间检查通过 | SpaceAvailabilityCheckedEvent | check-space-availability节点完成 | contractId, spaceId, isAvailable |
| 部门主管审批通过 | LeaseContractApprovedEvent | manager-approval节点完成，审批结果=APPROVED | contractId, approverId, approvalLevel(DEPARTMENT), approvedAt |
| 总经理审批通过 | LeaseContractApprovedEvent | gm-approval节点完成，审批结果=APPROVED | contractId, approverId, approvalLevel(GENERAL_MANAGER), approvedAt |
| 合同签署 | LeaseContractSignedEvent | contract-signing节点完成 | contractId, signingMethod, contractFileUrl, signedAt |

### 3.5 租户隔离实现

- **空间可用性检查**：通过 `tenantId` + `spaceId` 查询该租户的空间占用情况
- **审批权限**：基于 `tenantId` 查询租户组织架构中的审批人员
- **合同编号**：按租户隔离生成，格式：`{tenantId}-{YYYYMMDD}-{序号}`
- **文件存储**：合同文件按租户ID分目录存储

---

## 4. 资产调拨流程

### 4.1 流程概述

| 属性 | 值 |
|------|-----|
| 流程编号 | `asset-transfer` |
| 流程名称 | 资产调拨 |
| 业务场景 | 资产在不同空间或责任人之间的调拨 |
| 目标SLA | 审批2个工作日 |
| 限界上下文 | 智慧园区 - 资产管理 |
| 聚合根 | Asset |
| 相关领域事件 | AssetTransferCreatedEvent, AssetTransferredEvent |
| 批量支持 | 是 |

### 4.2 流程图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           资产调拨流程                                       │
│                          (asset-transfer)                                    │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │   开始节点    │
                    │  (start)     │
                    └──────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  创建调拨申请   │
                  │ (create-request)│
                  │  申请人填写      │
                  └────────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  资产状态检查   │
                  │  (服务任务)     │
                  │ 调用资产服务    │
                  └────────┬───────┘
                           │
                           ▼
                  ┌────────────────┐
                  │  资产状态判断   │
                  │   (排他网关)    │
                  └────┬───────┬───┘
               可调拨│       │不可调拨
                       ▼       ▼
              ┌──────────┐ ┌──────────┐
              │价值判断   │ │流程终止  │
              │(排他网关) │ │ (结束)   │
              └────┬─────┘ └──────────┘
                   │
      ┌────────────┴────────────┐
      │                          │
   ≤5000元                   >5000元
      │                          │
      ▼                          ▼
┌──────────┐              ┌──────────┐
│资产主管审批│              │部门经理审批│
│(用户任务) │              │(用户任务) │
└─────┬────┘              └─────┬────┘
      │                          │
      └────────────┬─────────────┘
                   │
                   ▼
          ┌────────────────┐
          │  审批结果判断   │
          │   (排他网关)    │
          └────┬───────┬───┘
        通过│       │拒绝
            ▼       ▼
      ┌──────────┐ ┌──────────┐
      │资产移交   │ │流程终止  │
      │(用户任务) │ │ (结束)   │
      └─────┬────┘ └──────────┘
            │
            ▼
      ┌────────────────┐
      │  资产接收确认  │
      │  (用户任务)    │
      └────────┬───────┘
               │
               ▼
      ┌────────────────┐
      │  更新资产状态   │
      │  (服务任务)     │
      └────────┬───────┘
               │
               ▼
      ┌────────────────┐
      │  记录调拨历史   │
      │  (服务任务)     │
      └────────┬───────┘
               │
               ▼
      ┌────────────────┐
      │   流程结束     │
      │   (结束节点)    │
      └────────────────┘
```

### 4.3 流程变量定义

| 变量名 | 类型(子类型) | 必填 | 默认值 | 可选值/说明 |
|--------|-------------|------|--------|-------------|
| **租户信息** ||||
| tenantId | String | 是 | - | 租户ID，用于数据隔离 |
| applicantId | String | 是 | - | 申请人ID |
| **资产信息** ||||
| assetIds | List<String> | 是 | - | 资产ID列表（支持批量） |
| transferReason | String | 是 | - | 调拨原因 |
| totalAssetValue | Double | 自动 | - | 资产总价值（自动计算） |
| assetStatus | String(enum) | 自动 | - | IDLE(闲置) / IN_USE(使用中) / MAINTENANCE(维护中) / SCRAPPED(已报废) |
| currentCustodianId | String | 自动 | - | 当前保管人ID |
| **目标信息** ||||
| targetSpaceId | String | 是 | - | 目标空间ID |
| targetCustodianId | String | 是 | - | 目标保管人ID |
| expectedTransferDate | Date | 是 | - | 期望调拨日期 |
| urgency | String(enum) | 是 | NORMAL | URGENT(紧急) / NORMAL(普通) |
| **审批信息** ||||
| supervisorApproval | String(enum) | 是 | - | APPROVED(同意) / REJECTED(拒绝) |
| supervisorComment | String | 否 | - | 主管审批意见 |
| managerApproval | String(enum) | 是 | - | APPROVED(同意) / REJECTED(拒绝) |
| managerComment | String | 否 | - | 经理审批意见 |
| **移交信息** ||||
| handoverPhoto | String | 是 | - | 移交照片URL |
| assetCondition | String(enum) | 是 | - | EXCELLENT(优良) / GOOD(良好) / FAIR(一般) / POOR(较差) |
| handoverNote | String | 否 | - | 移交备注 |
| accessories | String | 否 | - | 附件清单 |
| **接收信息** ||||
| receiveConfirmation | Boolean | 是 | - | 接收确认 |
| receiveComment | String | 否 | - | 接收备注 |
| exceptions | String | 否 | - | 异常说明 |

### 4.4 领域事件映射

| 流程节点 | 领域事件 | 触发时机 | 携带数据 |
|---------|---------|---------|---------|
| 调拨申请创建 | AssetTransferCreatedEvent | create-request节点完成 | transferId, tenantId, applicantId, assetIds, targetSpaceId, targetCustodianId |
| 资产状态检查完成 | AssetStatusCheckedEvent | check-asset-status节点完成 | transferId, assetIds, assetStatuses, allTransferable |
| 审批通过 | AssetTransferApprovedEvent | supervisor-approval或manager-approval节点完成，审批结果=APPROVED | transferId, approverId, approvalLevel, approvedAt |
| 资产移交完成 | AssetHandoverCompletedEvent | asset-handover节点完成 | transferId, handoverPhoto, assetCondition, handoverAt |
| 资产接收确认 | AssetReceivedEvent | asset-receive节点完成 | transferId, receiveConfirmation, receiveComment, receivedAt |
| 资产状态更新 | AssetTransferredEvent | update-asset-status节点完成 | transferId, assetIds, fromSpaceId, toSpaceId, fromCustodianId, toCustodianId |

### 4.5 批量调拨处理逻辑

当 `assetIds` 包含多个资产时，按以下逻辑处理：

| 场景 | 条件 | 处理方式 |
|------|------|---------|
| **全部通过** | 所有资产状态检查通过，且审批通过 | 批准所有资产调拨，生成统一的调拨单 |
| **部分通过** | 部分资产状态检查通过 | 分流：通过的资产进入审批流程，不通过的资产终止并通知申请人 |
| **全部拒绝** | 所有资产状态检查失败或审批拒绝 | 拒绝整个调拨申请，生成拒绝通知 |

**批量审批决策：**
- 审批人可以针对整个申请进行批准/拒绝
- 如果需要部分批准，审批人可以修改 `assetIds` 列表

### 4.6 租户隔离实现

- **资产查询**：通过 `tenantId` + `assetIds` 查询资产信息
- **审批权限**：基于 `tenantId` 查询租户组织架构中的审批人员
- **调拨历史**：按租户ID记录，支持租户内查询
- **移交确认**：确保 `currentCustodianId` 和 `targetCustodianId` 属于同一租户

---

## 5. 通用组件设计

### 5.1 通用监听器

```java
/**
 * SLA监控监听器
 */
public class SLAMonitorListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String slaConfigKey = (String) execution.getVariable("slaConfigKey");
        String tenantId = (String) execution.getVariable("tenantId");

        slaMonitoringService.startSLAClock(processInstanceId, slaConfigKey, tenantId);
    }
}

/**
 * 通知发送监听器
 */
public class NotificationListener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String assignee = delegateTask.getAssignee();
        String taskName = delegateTask.getName();
        String tenantId = (String) delegateTask.getVariable("tenantId");

        notificationService.sendTaskNotification(tenantId, assignee, taskName);
    }
}

/**
 * 流程变量审计监听器
 */
public class AuditLogListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String eventName = execution.getEventName();
        String tenantId = (String) execution.getVariable("tenantId");
        Map<String, Object> variables = execution.getVariables();

        auditLogService.logProcessVariableChange(processInstanceId, eventName, tenantId, variables);
    }
}
```

### 5.2 通用Delegate（含异常处理）

```java
/**
 * 调用规则引擎通用Delegate
 */
@Slf4j
public class RuleEngineDelegate implements JavaDelegate {

    @Autowired
    private RuleEngineApi ruleEngineApi;

    private String ruleSetKey;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            String tenantId = (String) execution.getVariable("tenantId");
            Map<String, Object> facts = execution.getVariables();

            RuleEvaluationRequest request = RuleEvaluationRequest.builder()
                .tenantId(TenantId.of(tenantId))
                .ruleSetKey(this.ruleSetKey)
                .facts(facts)
                .build();

            RuleEvaluationResponse response = ruleEngineApi.evaluateRule(request);

            // 设置结果到流程变量
            if (response.isSuccess()) {
                execution.getVariables().putAll(response.getResults());
                execution.setVariable("delegateSuccess", true);
            } else {
                execution.setVariable("delegateSuccess", false);
                execution.setVariable("delegateMessage", response.getMessage());
            }

        } catch (Exception e) {
            log.error("规则引擎调用异常", e);
            execution.setVariable("delegateError", e.getMessage());
            execution.setVariable("delegateSuccess", false);
            throw new BpmnError("RULE_ENGINE_ERROR", "规则引擎调用失败: " + e.getMessage());
        }
    }

    public void setRuleSetKey(String ruleSetKey) {
        this.ruleSetKey = ruleSetKey;
    }
}

/**
 * 调用Feign API通用Delegate
 */
@Slf4j
public class ServiceCallDelegate implements JavaDelegate {

    @Autowired
    private ApplicationContext applicationContext;

    private String serviceBeanName;
    private String methodName;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            Object service = applicationContext.getBean(serviceBeanName);
            Method method = service.getClass().getMethod(methodName, DelegateExecution.class);
            method.invoke(service, execution);

            execution.setVariable("delegateSuccess", true);

        } catch (NoSuchMethodException e) {
            log.error("服务方法不存在: {}.{}", serviceBeanName, methodName);
            execution.setVariable("delegateError", "方法不存在: " + methodName);
            execution.setVariable("delegateSuccess", false);
            throw new BpmnError("SERVICE_METHOD_NOT_FOUND", "服务方法不存在");

        } catch (Exception e) {
            log.error("服务调用异常", e);
            execution.setVariable("delegateError", e.getMessage());
            execution.setVariable("delegateSuccess", false);
            throw new BpmnError("SERVICE_CALL_ERROR", "服务调用失败: " + e.getMessage());
        }
    }

    public void setServiceBeanName(String serviceBeanName) {
        this.serviceBeanName = serviceBeanName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
```

### 5.3 Delegate异常处理规范

所有Delegate必须包含以下异常处理逻辑：

```java
@Override
public void execute(DelegateExecution execution) {
    try {
        // 1. 业务逻辑处理
        doExecute(execution);

        // 2. 设置成功标志
        execution.setVariable("delegateSuccess", true);

    } catch (BusinessException e) {
        // 业务异常：设置错误信息，不中断流程
        log.warn("业务异常: {}", e.getMessage());
        execution.setVariable("delegateError", e.getMessage());
        execution.setVariable("delegateSuccess", false);
        execution.setVariable("delegateErrorCode", e.getErrorCode());

    } catch (SystemException e) {
        // 系统异常：设置错误信息，抛出BPMN错误
        log.error("系统异常", e);
        execution.setVariable("delegateError", e.getMessage());
        execution.setVariable("delegateSuccess", false);
        throw new BpmnError(e.getErrorCode(), "系统异常: " + e.getMessage());

    } catch (Exception e) {
        // 未知异常：设置错误信息，抛出BPMN错误
        log.error("未知异常", e);
        execution.setVariable("delegateError", e.getMessage());
        execution.setVariable("delegateSuccess", false);
        throw new BpmnError("DELEGATE_ERROR", "Delegate执行失败: " + e.getMessage());
    }
}
```

### 5.4 异常类型定义

```java
/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

/**
 * 系统异常
 */
public class SystemException extends RuntimeException {
    private final String errorCode;

    public SystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
```

---

## 6. 流程版本管理

### 6.1 版本策略

```
process-definition-key:version
例如：property-repair-workorder:1:001
                      property-repair-workorder:2:001
```

### 6.2 升级策略

| 场景 | 升级策略 | 说明 |
|------|---------|------|
| 流程定义修改 | 新版本，新流程使用新版本 | 运行中的流程保持原版本 |
| 运行中流程 | 保持原版本，完成后再升级 | 确保流程实例的连续性 |
| Bug修复 | 滚动升级，暂停后重启 | 需要评估影响范围 |

### 6.3 版本兼容性检查

在部署新版本流程前，执行以下检查：

```java
public class ProcessVersionCompatibilityChecker {

    public CompatibilityReport checkCompatibility(
        ProcessDefinition oldVersion,
        ProcessDefinition newVersion
    ) {
        // 1. 检查流程变量兼容性
        List<String> removedVariables = checkRemovedVariables(oldVersion, newVersion);

        // 2. 检查节点兼容性
        List<String> removedNodes = checkRemovedNodes(oldVersion, newVersion);

        // 3. 检查SLA配置兼容性
        List<String> slaChanges = checkSLAChanges(oldVersion, newVersion);

        return CompatibilityReport.builder()
            .compatible(removedVariables.isEmpty() && removedNodes.isEmpty())
            .removedVariables(removedVariables)
            .removedNodes(removedNodes)
            .slaChanges(slaChanges)
            .build();
    }
}
```

---

## 7. 部署清单

### 7.1 部署顺序

1. 部署Camunda BPM Platform
2. 配置数据库（PostgreSQL）
3. 部署流程定义（.bpmn文件）
4. 配置SLA规则
5. 配置通知渠道
6. 测试流程执行

### 7.2 检查清单

- [ ] Camunda服务正常启动
- [ ] 流程定义成功部署
- [ ] 监听器正确注册
- [ ] SLA监控服务就绪
- [ ] 通知中心连接正常
- [ ] 规则引擎连接正常
- [ ] 数据库表创建完成
- [ ] 租户隔离配置验证
- [ ] 批量处理逻辑验证
- [ ] Delegate异常处理验证

---

## 8. 测试用例

### 8.1 物业维修工单流程测试

| 测试场景 | 输入 | 预期输出 |
|---------|------|---------|
| 紧急报修-自动派单成功 | workOrderCategory=URGENT | assignSuccess=true, priority=CRITICAL |
| 报修分类-投诉 | workOrderCategory=COMPLAINT | priority=HIGH, SLA=2小时/24小时 |
| 维修完成-租户满意 | repairResult=COMPLETED, satisfaction=VERY_SATISFIED | 流程结束 |
| 维修完成-租户不满意 | repairResult=COMPLETED, satisfaction=DISSATISFIED | 进入回访处理 |

### 8.2 租赁合同审批流程测试

| 测试场景 | 输入 | 预期输出 |
|---------|------|---------|
| 空间可用-合同审批通过 | spaceAvailable=true, totalAmount=30000 | 流程到签署节点 |
| 空间不可用 | spaceAvailable=false | 流程终止 |
| 合同金额>50万 | totalAmount=600000 | 需要总经理审批 |

### 8.3 资产调拨流程测试

| 测试场景 | 输入 | 预期输出 |
|---------|------|---------|
| 单资产调拨-审批通过 | assetIds=[asset-001], totalAssetValue=3000 | 流程到移交节点 |
| 批量调拨-全部通过 | assetIds=[asset-001,asset-002], 所有状态=IDLE | 批量批准 |
| 批量调拨-部分通过 | assetIds=[asset-001(IDLE),asset-002(MAINTENANCE)] | asset-001进入审批，asset-002终止 |

---

## 参考资料

- [Camunda BPMN 2.0 参考文档](https://docs.camunda.org/manual/latest/reference/bpmn20/)
- 华宽通智能体系统设计说明书 v1.0
- 工作流引擎集成方案设计 v1.1
- BPMN流程定义 - DDD评审报告 v1.0
