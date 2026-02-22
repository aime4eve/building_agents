-- ============================================================================
-- 华宽通智能体系统 - 初始化数据脚本
-- 版本: V1.0
-- 创建日期: 2026-02-20
-- 说明: 初始化系统基础数据（默认租户、管理员用户等）
-- ============================================================================

-- 初始化用户租户数据
USE `hkt_iot_user`;

-- ============================================================================
-- 1. 创建默认租户
-- ============================================================================
INSERT INTO tenant (id, tenant_code, tenant_name, tenant_type, contact_person, contact_email,
                   tenant_status, activate_date, version, created_at, updated_at)
VALUES (1, 'DEFAULT', '默认租户', 'OPERATOR', '系统管理员', 'admin@huakuangtong.com',
        'ACTIVE', NOW(), 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- 2. 创建默认管理员用户
-- 密码: admin123 (BCrypt加密后的值)
-- ============================================================================
INSERT INTO `user` (id, tenant_id, tenant_code, username, real_name, email,
                   password, user_status, account_type, password_updated_at,
                   version, created_at, updated_at)
VALUES (1, 1, 'DEFAULT', 'admin', '系统管理员', 'admin@huakuangtong.com',
        '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi',
        'ACTIVE', 'SUPER_ADMIN', NOW(), 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- 3. 创建默认角色
-- ============================================================================
INSERT INTO role (id, tenant_id, role_code, role_name, role_type, description,
                 is_default, status, version, created_at, updated_at)
VALUES
    (1, 1, 'SUPER_ADMIN', '超级管理员', 'SYSTEM', '系统超级管理员，拥有所有权限', 1, 'ACTIVE', 0, NOW(), NOW()),
    (2, 1, 'TENANT_ADMIN', '租户管理员', 'SYSTEM', '租户管理员，管理租户内所有资源', 1, 'ACTIVE', 0, NOW(), NOW()),
    (3, 1, 'DEVICE_MANAGER', '设备管理员', 'CUSTOM', '设备管理专员，负责设备管理', 0, 'ACTIVE', 0, NOW(), NOW()),
    (4, 1, 'SPACE_MANAGER', '空间管理员', 'CUSTOM', '空间管理专员，负责空间管理', 0, 'ACTIVE', 0, NOW(), NOW()),
    (5, 1, 'OPERATOR', '操作员', 'CUSTOM', '普通操作员，日常运维操作', 0, 'ACTIVE', 0, NOW(), NOW()),
    (6, 1, 'VIEWER', '查看者', 'CUSTOM', '只读用户，仅可查看数据', 0, 'ACTIVE', 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- 4. 创建用户角色关联（admin为超级管理员）
-- ============================================================================
INSERT INTO user_role (id, tenant_id, user_id, role_id, granted_by, granted_at)
VALUES (1, 1, 1, 1, 1, NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- 5. 创建系统权限（Phase 1 核心权限）
-- ============================================================================
INSERT INTO permission (id, permission_code, permission_name, resource_type, resource_path, action,
                      parent_id, description, display_order, status, created_at, updated_at)
VALUES
    -- 设备管理权限
    (1001, 'device:view', '查看设备', 'MENU', '/device', 'READ', NULL, '查看设备列表和详情', 1, 'ACTIVE', NOW(), NOW()),
    (1002, 'device:create', '创建设备', 'API', '/api/device/**', 'CREATE', 1001, '创建新设备', 2, 'ACTIVE', NOW(), NOW()),
    (1003, 'device:update', '更新设备', 'API', '/api/device/**', 'UPDATE', 1001, '更新设备信息', 3, 'ACTIVE', NOW(), NOW()),
    (1004, 'device:delete', '删除设备', 'API', '/api/device/**', 'DELETE', 1001, '删除设备', 4, 'ACTIVE', NOW(), NOW()),
    (1005, 'device:control', '控制设备', 'API', '/api/device/*/control', 'EXECUTE', 1001, '远程控制设备', 5, 'ACTIVE', NOW(), NOW()),

    -- 空间管理权限
    (2001, 'space:view', '查看空间', 'MENU', '/space', 'READ', NULL, '查看空间列表和详情', 10, 'ACTIVE', NOW(), NOW()),
    (2002, 'space:create', '创建空间', 'API', '/api/space/**', 'CREATE', 2001, '创建新空间', 11, 'ACTIVE', NOW(), NOW()),
    (2003, 'space:update', '更新空间', 'API', '/api/space/**', 'UPDATE', 2001, '更新空间信息', 12, 'ACTIVE', NOW(), NOW()),
    (2004, 'space:delete', '删除空间', 'API', '/api/space/**', 'DELETE', 2001, '删除空间', 13, 'ACTIVE', NOW(), NOW()),

    -- 规则引擎权限
    (3001, 'rule:view', '查看规则', 'MENU', '/rule', 'READ', NULL, '查看规则列表和详情', 20, 'ACTIVE', NOW(), NOW()),
    (3002, 'rule:create', '创建规则', 'API', '/api/rule/**', 'CREATE', 3001, '创建新规则', 21, 'ACTIVE', NOW(), NOW()),
    (3003, 'rule:update', '更新规则', 'API', '/api/rule/**', 'UPDATE', 3001, '更新规则配置', 22, 'ACTIVE', NOW(), NOW()),
    (3004, 'rule:delete', '删除规则', 'API', '/api/rule/**', 'DELETE', 3001, '删除规则', 23, 'ACTIVE', NOW(), NOW()),
    (3005, 'rule:enable', '启用/禁用规则', 'API', '/api/rule/*/enable', 'UPDATE', 3001, '启用或禁用规则', 24, 'ACTIVE', NOW(), NOW()),
    (3006, 'rule:execute', '手动执行规则', 'API', '/api/rule/*/execute', 'EXECUTE', 3001, '手动触发规则执行', 25, 'ACTIVE', NOW(), NOW()),

    -- 用户管理权限
    (4001, 'user:view', '查看用户', 'MENU', '/user', 'READ', NULL, '查看用户列表和详情', 30, 'ACTIVE', NOW(), NOW()),
    (4002, 'user:create', '创建用户', 'API', '/api/user/**', 'CREATE', 4001, '创建新用户', 31, 'ACTIVE', NOW(), NOW()),
    (4003, 'user:update', '更新用户', 'API', '/api/user/**', 'UPDATE', 4001, '更新用户信息', 32, 'ACTIVE', NOW(), NOW()),
    (4004, 'user:delete', '删除用户', 'API', '/api/user/**', 'DELETE', 4001, '删除用户', 33, 'ACTIVE', NOW(), NOW()),
    (4005, 'user:reset-password', '重置密码', 'API', '/api/user/*/reset-password', 'UPDATE', 4001, '重置用户密码', 34, 'ACTIVE', NOW(), NOW()),

    -- 角色权限管理
    (5001, 'role:view', '查看角色', 'MENU', '/role', 'READ', NULL, '查看角色列表和详情', 40, 'ACTIVE', NOW(), NOW()),
    (5002, 'role:create', '创建角色', 'API', '/api/role/**', 'CREATE', 5001, '创建新角色', 41, 'ACTIVE', NOW(), NOW()),
    (5003, 'role:update', '更新角色', 'API', '/api/role/**', 'UPDATE', 5001, '更新角色信息', 42, 'ACTIVE', NOW(), NOW()),
    (5004, 'role:delete', '删除角色', 'API', '/api/role/**', 'DELETE', 5001, '删除角色', 43, 'ACTIVE', NOW(), NOW()),
    (5005, 'role:assign-permission', '分配权限', 'API', '/api/role/*/permission', 'UPDATE', 5001, '为角色分配权限', 44, 'ACTIVE', NOW(), NOW()),

    -- 租户管理权限
    (6001, 'tenant:view', '查看租户', 'MENU', '/tenant', 'READ', NULL, '查看租户列表和详情', 50, 'ACTIVE', NOW(), NOW()),
    (6002, 'tenant:create', '创建租户', 'API', '/api/tenant/**', 'CREATE', 6001, '创建新租户', 51, 'ACTIVE', NOW(), NOW()),
    (6003, 'tenant:update', '更新租户', 'API', '/api/tenant/**', 'UPDATE', 6001, '更新租户信息', 52, 'ACTIVE', NOW(), NOW()),
    (6004, 'tenant:delete', '删除租户', 'API', '/api/tenant/**', 'DELETE', 6001, '删除租户', 53, 'ACTIVE', NOW(), NOW()),
    (6005, 'tenant:manage-quota', '管理配额', 'API', '/api/tenant/*/quota', 'UPDATE', 6001, '管理租户配额', 54, 'ACTIVE', NOW(), NOW()),

    -- 系统管理权限
    (7001, 'system:config', '系统配置', 'MENU', '/system/config', 'READ', NULL, '查看和修改系统配置', 60, 'ACTIVE', NOW(), NOW()),
    (7002, 'system:log', '系统日志', 'MENU', '/system/log', 'READ', NULL, '查看系统日志', 61, 'ACTIVE', NOW(), NOW()),
    (7003, 'system:monitor', '系统监控', 'MENU', '/system/monitor', 'READ', NULL, '查看系统监控数据', 62, 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE id=id;

-- ============================================================================
-- 6. 为超级管理员角色分配所有权限
-- ============================================================================
INSERT INTO role_permission (id, tenant_id, role_id, permission_id, granted_by, granted_at)
SELECT 100000 + ROW_NUMBER() OVER (ORDER BY p.id), 1, 1, p.id, 1, NOW()
FROM permission p
WHERE p.id BETWEEN 1001 AND 7003
ON DUPLICATE KEY UPDATE id=id;

-- 显示初始化结果
SELECT 'Phase 1 database initialization completed!' AS status;
SELECT COUNT(*) AS tenant_count FROM tenant;
SELECT COUNT(*) AS user_count FROM `user`;
SELECT COUNT(*) AS role_count FROM role;
SELECT COUNT(*) AS permission_count FROM permission;
SELECT COUNT(*) AS role_permission_count FROM role_permission;
