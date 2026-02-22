-- =============================================================================
-- Camunda 数据库初始化脚本
-- =============================================================================
-- 说明: Camunda会自动创建核心表，这里创建业务扩展表
-- =============================================================================

-- 设置搜索路径
SET search_path TO public, camunda;

-- ---------------------------------------------------------------------------
-- 1. 流程实例扩展表
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wf_process_instance (
    id VARCHAR(64) PRIMARY KEY COMMENT '流程实例ID',
    business_key VARCHAR(255) NOT NULL COMMENT '业务键',
    process_definition_key VARCHAR(255) NOT NULL COMMENT '流程定义键',
    process_definition_id VARCHAR(64) NOT NULL COMMENT '流程定义ID',
    state VARCHAR(20) NOT NULL COMMENT '流程状态',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    started_by VARCHAR(64) NOT NULL COMMENT '启动人ID',
    current_activity_id VARCHAR(255) COMMENT '当前活动ID',
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    ended_at TIMESTAMP COMMENT '结束时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    version BIGINT DEFAULT 0 COMMENT '版本号（乐观锁）',
    CONSTRAINT uk_business_key UNIQUE (business_key)
);

-- 创建索引
CREATE INDEX idx_wf_pi_tenant_process ON wf_process_instance(tenant_id, process_definition_key);
CREATE INDEX idx_wf_pi_state ON wf_process_instance(state);
CREATE INDEX idx_wf_pi_started_by ON wf_process_instance(started_by);
CREATE INDEX idx_wf_pi_created_at ON wf_process_instance(started_at);

-- 添加表注释
COMMENT ON TABLE wf_process_instance IS '流程实例扩展表';
COMMENT ON COLUMN wf_process_instance.id IS '流程实例ID';
COMMENT ON COLUMN wf_process_instance.business_key IS '业务键（工单号等）';
COMMENT ON COLUMN wf_process_instance.state IS '流程状态：STARTED/RUNNING/SUSPENDED/COMPLETED/FAILED/CANCELLED';

-- ---------------------------------------------------------------------------
-- 2. 工作流任务表
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wf_task (
    id VARCHAR(64) PRIMARY KEY COMMENT '任务ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_definition_key VARCHAR(255) NOT NULL COMMENT '任务定义键',
    task_name VARCHAR(255) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(20) NOT NULL COMMENT '任务类型：USER_TASK/SERVICE_TASK/SEND_TASK/RECEIVE_TASK',
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT '任务状态：CREATED/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELLED',
    assignee VARCHAR(64) COMMENT '处理人ID',
    candidate_groups TEXT COMMENT '候选组（JSON数组）',
    priority INT DEFAULT 50 COMMENT '优先级：0-100',
    due_date TIMESTAMP COMMENT '到期时间',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    completed_at TIMESTAMP COMMENT '完成时间',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    form_key VARCHAR(255) COMMENT '表单键',
    description TEXT COMMENT '任务描述'
);

-- 创建索引
CREATE INDEX idx_wf_task_process_instance ON wf_task(process_instance_id);
CREATE INDEX idx_wf_task_assignee ON wf_task(assignee);
CREATE INDEX idx_wf_task_status ON wf_task(status);
CREATE INDEX idx_wf_task_tenant_process ON wf_task(tenant_id, process_instance_id);
CREATE INDEX idx_wf_task_due_date ON wf_task(due_date);
CREATE INDEX idx_wf_task_created_at ON wf_task(created_at);

-- 添加外键约束
ALTER TABLE wf_task ADD CONSTRAINT fk_wf_task_process_instance
    FOREIGN KEY (process_instance_id) REFERENCES wf_process_instance(id) ON DELETE CASCADE;

-- 添加表注释
COMMENT ON TABLE wf_task IS '工作流任务表';
COMMENT ON COLUMN wf_task.task_type IS '任务类型：USER_TASK-用户任务/SERVICE_TASK-服务任务/SEND_TASK-发送任务/RECEIVE_TASK-接收任务';
COMMENT ON COLUMN wf_task.status IS '任务状态：CREATED-已创建/ASSIGNED-已分配/IN_PROGRESS-进行中/COMPLETED-已完成/CANCELLED-已取消';

-- ---------------------------------------------------------------------------
-- 3. SLA配置表
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wf_sla_config (
    id VARCHAR(64) PRIMARY KEY COMMENT 'SLA配置ID',
    process_definition_key VARCHAR(255) NOT NULL COMMENT '流程定义键',
    task_definition_key VARCHAR(255) COMMENT '任务定义键（为空表示流程级SLA）',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    sla_type VARCHAR(20) NOT NULL COMMENT 'SLA类型：RESPONSE-响应时间/RESOLUTION-解决时间',
    time_limit INT NOT NULL COMMENT '时间限制（秒）',
    priority VARCHAR(20) COMMENT '适用优先级：ALL/HIGH/MEDIUM/LOW',
    working_hours_only BOOLEAN DEFAULT FALSE COMMENT '是否仅计算工作时间',
    calendar_id VARCHAR(64) COMMENT '工作日历ID',
    description TEXT COMMENT 'SLA描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否启用'
);

-- 创建索引
CREATE INDEX idx_wf_sla_config_tenant_process ON wf_sla_config(tenant_id, process_definition_key);
CREATE INDEX idx_wf_sla_config_task ON wf_sla_config(task_definition_key);

-- 添加表注释
COMMENT ON TABLE wf_sla_config IS 'SLA配置表';
COMMENT ON COLUMN wf_sla_config.sla_type IS 'SLA类型：RESPONSE-响应时间（首次响应）/RESOLUTION-解决时间（完全解决）';
COMMENT ON COLUMN wf_sla_config.working_hours_only IS '是否仅计算工作时间（排除非工作时间）';

-- ---------------------------------------------------------------------------
-- 4. SLA监控记录表
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wf_sla_monitor (
    id VARCHAR(64) PRIMARY KEY COMMENT 'SLA监控ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID（流程级SLA为空）',
    sla_config_id VARCHAR(64) NOT NULL COMMENT 'SLA配置ID',
    sla_type VARCHAR(20) NOT NULL COMMENT 'SLA类型',
    sla_deadline TIMESTAMP NOT NULL COMMENT 'SLA截止时间',
    response_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '响应SLA状态：PENDING-待处理/COMPLIANT-已达标/WARNING-预警/BREACHED-已超时',
    resolution_status VARCHAR(20) COMMENT '解决SLA状态',
    actual_response_time TIMESTAMP COMMENT '实际响应时间',
    actual_resolution_time TIMESTAMP COMMENT '实际解决时间',
    overtime_duration INT COMMENT '超时时长（秒）',
    warning_sent BOOLEAN DEFAULT FALSE COMMENT '是否已发送预警',
    breached_sent BOOLEAN DEFAULT FALSE COMMENT '是否已发送超时通知',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID'
);

-- 创建索引
CREATE INDEX idx_wf_sla_monitor_process_instance ON wf_sla_monitor(process_instance_id);
CREATE INDEX idx_wf_sla_monitor_task ON wf_sla_monitor(task_id);
CREATE INDEX idx_wf_sla_monitor_tenant_process ON wf_sla_monitor(tenant_id, process_instance_id);
CREATE INDEX idx_wf_sla_monitor_status ON wf_sla_monitor(response_status);
CREATE INDEX idx_wf_sla_monitor_deadline ON wf_sla_monitor(sla_deadline);

-- 添加外键约束
ALTER TABLE wf_sla_monitor ADD CONSTRAINT fk_wf_sla_monitor_process_instance
    FOREIGN KEY (process_instance_id) REFERENCES wf_process_instance(id) ON DELETE CASCADE;
ALTER TABLE wf_sla_monitor ADD CONSTRAINT fk_wf_sla_monitor_config
    FOREIGN KEY (sla_config_id) REFERENCES wf_sla_config(id);

-- 添加表注释
COMMENT ON TABLE wf_sla_monitor IS 'SLA监控记录表';
COMMENT ON COLUMN wf_sla_monitor.response_status IS 'SLA状态：PENDING-待处理/COMPLIANT-已达标/WARNING-预警（80%）/BREACHED-已超时';

-- ---------------------------------------------------------------------------
-- 5. 工作日历表（用于SLA工作时间计算）
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wf_working_calendar (
    id VARCHAR(64) PRIMARY KEY COMMENT '日历ID',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID',
    calendar_name VARCHAR(100) NOT NULL COMMENT '日历名称',
    calendar_type VARCHAR(20) NOT NULL COMMENT '日历类型：STANDARD-标准/CUSTOM-自定义',
    description TEXT COMMENT '日历描述',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建索引
CREATE INDEX idx_wf_calendar_tenant ON wf_working_calendar(tenant_id);

-- 添加表注释
COMMENT ON TABLE wf_working_calendar IS '工作日历表（用于SLA工作时间计算）';

-- 工作日历详情表
CREATE TABLE IF NOT EXISTS wf_working_calendar_detail (
    id VARCHAR(64) PRIMARY KEY COMMENT '详情ID',
    calendar_id VARCHAR(64) NOT NULL COMMENT '日历ID',
    detail_type VARCHAR(20) NOT NULL COMMENT '详情类型：WORKING_DAY-工作日/HOLIDAY-节假日/WORK_TIME-工作时间',
    detail_date DATE COMMENT '日期（WORKING_DAY/HOLIDAY）',
    day_of_week INT COMMENT '星期几（1-7，1=周一）',
    start_time TIME COMMENT '开始时间（WORK_TIME）',
    end_time TIME COMMENT '结束时间（WORK_TIME）',
    description VARCHAR(255) COMMENT '描述',
    CONSTRAINT fk_wf_calendar_detail_calendar FOREIGN KEY (calendar_id) REFERENCES wf_working_calendar(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_wf_calendar_detail_calendar ON wf_working_calendar_detail(calendar_id);
CREATE INDEX idx_wf_calendar_detail_date ON wf_working_calendar_detail(detail_date);

-- 添加表注释
COMMENT ON TABLE wf_working_calendar_detail IS '工作日历详情表';
COMMENT ON COLUMN wf_working_calendar_detail.detail_type IS '详情类型：WORKING_DAY-工作日/HOLIDAY-节假日/WORK_TIME-每日工作时间段';

-- ---------------------------------------------------------------------------
-- 6. 流程变量历史表
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS wf_variable_history (
    id VARCHAR(64) PRIMARY KEY COMMENT '变量历史ID',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    variable_name VARCHAR(255) NOT NULL COMMENT '变量名',
    variable_type VARCHAR(50) COMMENT '变量类型：STRING/INTEGER/BOOLEAN/DATE/JSON',
    old_value TEXT COMMENT '旧值',
    new_value TEXT COMMENT '新值',
    operation_type VARCHAR(20) NOT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE',
    operator VARCHAR(64) COMMENT '操作人ID',
    operated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    tenant_id VARCHAR(64) NOT NULL COMMENT '租户ID'
);

-- 创建索引
CREATE INDEX idx_wf_var_history_process_instance ON wf_variable_history(process_instance_id);
CREATE INDEX idx_wf_var_history_task ON wf_variable_history(task_id);
CREATE INDEX idx_wf_var_history_tenant ON wf_variable_history(tenant_id);
CREATE INDEX idx_wf_var_history_operated_at ON wf_variable_history(operated_at);

-- 添加表注释
COMMENT ON TABLE wf_variable_history IS '流程变量变更历史表';

-- ---------------------------------------------------------------------------
-- 初始化数据
-- ---------------------------------------------------------------------------

-- 插入默认SLA配置示例
INSERT INTO wf_sla_config (id, process_definition_key, task_definition_key, tenant_id, sla_type, time_limit, priority, description) VALUES
('sla-config-001', 'property-repair-workorder', NULL, 'DEFAULT', 'RESPONSE', 1800, 'HIGH', '物业维修工单 - 响应时间30分钟'),
('sla-config-002', 'property-repair-workorder', NULL, 'DEFAULT', 'RESOLUTION', 7200, 'HIGH', '物业维修工单 - 解决时间2小时'),
('sla-config-003', 'contract-approval', NULL, 'DEFAULT', 'RESPONSE', 3600, 'MEDIUM', '合同审批 - 响应时间1小时'),
('sla-config-004', 'contract-approval', NULL, 'DEFAULT', 'RESOLUTION', 86400, 'MEDIUM', '合同审批 - 解决时间24小时'),
('sla-config-005', 'asset-allocation', NULL, 'DEFAULT', 'RESPONSE', 900, 'HIGH', '资产调拨 - 响应时间15分钟'),
('sla-config-006', 'asset-allocation', NULL, 'DEFAULT', 'RESOLUTION', 3600, 'HIGH', '资产调拨 - 解决时间1小时');

-- 插入默认工作日历
INSERT INTO wf_working_calendar (id, tenant_id, calendar_name, calendar_type, description) VALUES
('calendar-001', 'DEFAULT', '标准工作日历', 'STANDARD', '周一至周五 9:00-18:00，法定节假日除外');

-- 插入标准工作日历详情
INSERT INTO wf_working_calendar_detail (id, calendar_id, detail_type, day_of_week, start_time, end_time, description) VALUES
('detail-001', 'calendar-001', 'WORK_TIME', 1, '09:00:00', '18:00:00', '周一'),
('detail-002', 'calendar-001', 'WORK_TIME', 2, '09:00:00', '18:00:00', '周二'),
('detail-003', 'calendar-001', 'WORK_TIME', 3, '09:00:00', '18:00:00', '周三'),
('detail-004', 'calendar-001', 'WORK_TIME', 4, '09:00:00', '18:00:00', '周四'),
('detail-005', 'calendar-001', 'WORK_TIME', 5, '09:00:00', '18:00:00', '周五');

-- 授权camunda用户访问扩展表
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO camunda;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO camunda;
