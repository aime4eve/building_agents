# 华宽通智能体系统 - 验证清单

## 基础框架验证
- [ ] Checkpoint 1: 项目能正常启动并响应健康检查请求
- [ ] Checkpoint 2: 多租户上下文能正确在请求链路中传递
- [ ] Checkpoint 3: DDD分层架构(接口层、应用层、领域层、基础设施层)清晰划分
- [ ] Checkpoint 4: 消息队列基础设施配置正确,能正常发送和接收消息
- [ ] Checkpoint 5: 数据库基础设施配置正确,能正常读写数据

## 用户与租户管理验证
- [ ] Checkpoint 6: 租户CRUD操作全部正常
- [ ] Checkpoint 7: 多级组织架构(运营商→集团→子公司→入驻企业)能正确创建和查询
- [ ] Checkpoint 8: 用户CRUD操作全部正常
- [ ] Checkpoint 9: RBAC角色权限分配正常,权限校验生效
- [ ] Checkpoint 10: SSO单点登录流程完整且正常
- [ ] Checkpoint 11: MFA多因素认证流程完整且正常

## 空间管理验证
- [ ] Checkpoint 12: 园区→楼栋→楼层→房间的层级结构能正确创建
- [ ] Checkpoint 13: 空间拓扑关系能正确维护和查询
- [ ] Checkpoint 14: 抽象资源能正确绑定和解绑到空间
- [ ] Checkpoint 15: 逻辑空间分组功能正常,支持按应用/租户/业务线等分组

## 设备管理验证
- [ ] Checkpoint 16: 设备CRUD操作全部正常
- [ ] Checkpoint 17: 设备能正确绑定到空间
- [ ] Checkpoint 18: 设备状态(在线/离线/故障/维护)能正确更新和查询
- [ ] Checkpoint 19: 物模型(ThingModel)能正确定义和应用
- [ ] Checkpoint 20: 设备遥测数据读写分离正常,写侧保存最新快照,读侧提供时序查询
- [ ] Checkpoint 21: 设备事件读写分离正常
- [ ] Checkpoint 22: 单设备控制命令能正确下发和响应
- [ ] Checkpoint 23: 批量设备控制任务能正确创建(≤50台)
- [ ] Checkpoint 24: 批量设备控制任务超时机制正常(5分钟超时)
- [ ] Checkpoint 25: 批量设备控制任务失败重试机制正常(最多3次)
- [ ] Checkpoint 26: 批量设备控制任务状态机转换正常
- [ ] Checkpoint 27: 设备License管理正常
- [ ] Checkpoint 28: OTA升级功能正常

## 通知中心验证
- [ ] Checkpoint 29: 通知模板引擎能正确渲染模板
- [ ] Checkpoint 30: 多渠道消息推送(短信、邮件等)能正常发送
- [ ] Checkpoint 31: 通知幂等性保证正常,重复通知不会重复发送
- [ ] Checkpoint 32: 通知重试机制正常(指数退避:1min/5min/15min)
- [ ] Checkpoint 33: 通知死信队列处理正常,超过最大重试次数的通知进入DLQ
- [ ] Checkpoint 34: 告警与通知事件契约正确实现

## 规则引擎验证
- [ ] Checkpoint 35: 规则CRUD操作全部正常
- [ ] Checkpoint 36: 规则DSL解析器能正确解析表达式
- [ ] Checkpoint 37: 规则条件评估引擎能正确评估条件
- [ ] Checkpoint 38: 规则动作执行器能正确执行动作
- [ ] Checkpoint 39: 告警规则能正确触发
- [ ] Checkpoint 40: 联动规则能正确执行
- [ ] Checkpoint 41: 规则内置函数(avg、sum、max、min等)能正常工作

## 工作流引擎验证
- [ ] Checkpoint 42: 工作流流程定义CRUD正常
- [ ] Checkpoint 43: 流程实例能正常启动和执行
- [ ] Checkpoint 44: 审批流程能正常流转
- [ ] Checkpoint 45: SLA管理和监控正常

## 智慧园区验证
- [ ] Checkpoint 46: 招商租赁业务流程完整且正常
- [ ] Checkpoint 47: 物业管理业务流程完整且正常
- [ ] Checkpoint 48: 资产业务流程完整且正常
- [ ] Checkpoint 49: 与工作流引擎集成正常
- [ ] Checkpoint 50: 与通知中心集成正常

## 智慧建筑验证
- [ ] Checkpoint 51: 建筑管理功能正常
- [ ] Checkpoint 52: 场景联动规则能正确配置和执行
- [ ] Checkpoint 53: 定时计划能正确配置和执行
- [ ] Checkpoint 54: 订阅服务功能正常
- [ ] Checkpoint 55: 与设备管理集成正常
- [ ] Checkpoint 56: 与规则引擎集成正常
- [ ] Checkpoint 57: 与通知中心集成正常

## 智能应用验证
- [ ] Checkpoint 58: 霉菌风险评估能正确计算
- [ ] Checkpoint 59: 自动湿度调节能正常触发和执行
- [ ] Checkpoint 60: 防霉告警通知能正常发送
- [ ] Checkpoint 61: 牲畜健康监测正常
- [ ] Checkpoint 62: 电子围栏越界告警正常
- [ ] Checkpoint 63: 与设备管理集成正常
- [ ] Checkpoint 64: 与规则引擎集成正常
- [ ] Checkpoint 65: 与通知中心集成正常

## 第三方系统ACL验证
- [ ] Checkpoint 66: 涂鸦系统ACL适配器能正常对接
- [ ] Checkpoint 67: 停车系统ACL适配器能正常对接
- [ ] Checkpoint 68: 送餐机器人ACL适配器能正常对接
- [ ] Checkpoint 69: 错误隔离机制正常,外部系统错误不影响主流程
- [ ] Checkpoint 70: 幂等与重试机制正常
- [ ] Checkpoint 71: 限流与熔断机制正常
- [ ] Checkpoint 72: 数据映射审计正常,保留原始载荷与映射结果对照

## 审计日志验证
- [ ] Checkpoint 73: 操作日志能正确记录
- [ ] Checkpoint 74: 审计日志能正确查询
- [ ] Checkpoint 75: 删除报告能正确生成
- [ ] Checkpoint 76: 定时任务日志能正确记录

## 订单与交易中心验证(Phase 2)
- [ ] Checkpoint 77: 订单CRUD操作全部正常
- [ ] Checkpoint 78: 订单状态机转换正常,合法转换能执行
- [ ] Checkpoint 79: 非法订单状态转换能被正确阻止
- [ ] Checkpoint 80: 能耗账单能自动生成
- [ ] Checkpoint 81: 支付集成正常
- [ ] Checkpoint 82: 发票能正常生成

## 集成与系统测试验证
- [ ] Checkpoint 83: 各限界上下文之间的同步通信(Feign API)正常
- [ ] Checkpoint 84: 各限界上下文之间的异步通信(MQ事件)正常
- [ ] Checkpoint 85: 集成测试通过率≥95%
- [ ] Checkpoint 86: 系统测试关键路径全部通过
- [ ] Checkpoint 87: 批量控制50台设备能在5分钟内完成
- [ ] Checkpoint 88: 安全渗透测试未发现高危漏洞
- [ ] Checkpoint 89: 系统支持水平扩展
- [ ] Checkpoint 90: 多租户数据隔离正确,租户间数据互不影响
