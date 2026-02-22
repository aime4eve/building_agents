-- ============================================================================
-- 华宽通智能体系统 - 用户与租户模块数据库设计
-- 版本: V1.0
-- 数据库: MySQL 8.0+
-- 创建日期: 2026-02-20
-- ============================================================================

-- ============================================================================
-- 1. 租户表 (tenant)
-- 说明: 租户主表，支持多级组织架构（运营商→集团租户→子公司租户→企业租户）
-- ============================================================================
CREATE TABLE tenant (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户标识
    tenant_code           VARCHAR(100) NOT NULL COMMENT '租户编码',
    tenant_name           VARCHAR(200) NOT NULL COMMENT '租户名称',

    -- 租户类型
    tenant_type           VARCHAR(20) NOT NULL COMMENT '租户类型: OPERATOR/GROUP/SUBSIDIARY/ENTERPRISE',

    -- 层级关系
    parent_tenant_id      BIGINT UNSIGNED COMMENT '父租户ID',
    tenant_path           VARCHAR(500) COMMENT '租户路径: /1/2/3',
    tenant_level          TINYINT COMMENT '租户层级',

    -- 联系信息
    contact_person        VARCHAR(100) COMMENT '联系人',
    contact_phone         VARCHAR(50) COMMENT '联系电话',
    contact_email         VARCHAR(200) COMMENT '联系邮箱',
    address               VARCHAR(500) COMMENT '地址',

    -- 业务信息
    industry              VARCHAR(100) COMMENT '行业类型',
    company_size          VARCHAR(50) COMMENT '公司规模',
    business_license      VARCHAR(100) COMMENT '营业执照号',

    -- 配额信息
    max_users             INT DEFAULT 100 COMMENT '最大用户数',
    max_devices           INT DEFAULT 1000 COMMENT '最大设备数',
    max_spaces            INT DEFAULT 100 COMMENT '最大空间数',
    storage_quota         BIGINT DEFAULT 1073741824 COMMENT '存储配额(字节，默认1GB)',

    -- 状态信息
    tenant_status         VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '租户状态: ACTIVE/SUSPENDED/TERMINATED',
    activate_date         DATETIME(3) COMMENT '激活日期',
    expire_date           DATETIME(3) COMMENT '到期日期',

    -- 版本控制(乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人ID',

    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 1-已删除 0-正常',
    deleted_at            DATETIME(3) COMMENT '删除时间',
    deleted_by            BIGINT UNSIGNED COMMENT '删除人ID',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_code (tenant_code, deleted),
    KEY idx_parent_tenant_id (parent_tenant_id),
    KEY idx_tenant_type (tenant_type),
    KEY idx_tenant_status (tenant_status),
    KEY idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';


-- ============================================================================
-- 2. 用户表 (user)
-- 说明: 用户主表
-- ============================================================================
CREATE TABLE `user` (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户关联
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',
    tenant_code           VARCHAR(100) NOT NULL COMMENT '租户编码',

    -- 用户基本信息
    username              VARCHAR(100) NOT NULL COMMENT '用户名',
    real_name             VARCHAR(100) COMMENT '真实姓名',
    email                 VARCHAR(200) NOT NULL COMMENT '邮箱',
    phone                 VARCHAR(50) COMMENT '手机号',

    -- 认证信息
    password              VARCHAR(255) NOT NULL COMMENT '密码(加密)',
    salt                  VARCHAR(100) COMMENT '盐值',

    -- 用户状态
    user_status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态: ACTIVE/INACTIVE/LOCKED',
    account_type          VARCHAR(20) DEFAULT 'NORMAL' COMMENT '账号类型: NORMAL/ADMIN/SUPER_ADMIN',

    -- 安全设置
    is_mfa_enabled        TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用MFA: 1-是 0-否',
    mfa_secret            VARCHAR(100) COMMENT 'MFA密钥',
    password_updated_at   DATETIME(3) COMMENT '密码更新时间',
    last_login_at         DATETIME(3) COMMENT '最后登录时间',
    last_login_ip         VARCHAR(50) COMMENT '最后登录IP',

    -- 账户锁定
    locked_at             DATETIME(3) COMMENT '锁定时间',
    locked_until          DATETIME(3) COMMENT '锁定到期时间',
    failed_login_count    INT DEFAULT 0 COMMENT '失败登录次数',

    -- 用户信息
    avatar                VARCHAR(500) COMMENT '头像URL',
    department            VARCHAR(200) COMMENT '部门',
    position              VARCHAR(100) COMMENT '职位',
    employee_id           VARCHAR(100) COMMENT '员工工号',

    -- 版本控制(乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人ID',

    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 1-已删除 0-正常',
    deleted_at            DATETIME(3) COMMENT '删除时间',
    deleted_by            BIGINT UNSIGNED COMMENT '删除人ID',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_username (tenant_id, username, deleted),
    UNIQUE KEY uk_email (email, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_user_status (user_status),
    KEY idx_phone (phone),
    KEY idx_last_login_at (last_login_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- ============================================================================
-- 3. 角色表 (role)
-- 说明: 角色表
-- ============================================================================
CREATE TABLE role (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户关联
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 角色信息
    role_code             VARCHAR(100) NOT NULL COMMENT '角色编码',
    role_name             VARCHAR(100) NOT NULL COMMENT '角色名称',
    role_type             VARCHAR(50) NOT NULL COMMENT '角色类型: SYSTEM/CUSTOM/BUSINESS',

    -- 角色属性
    description           VARCHAR(500) COMMENT '角色描述',
    is_default            TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认角色: 1-是 0-否',
    display_order         INT DEFAULT 0 COMMENT '显示顺序',

    -- 状态信息
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',

    -- 版本控制(乐观锁)
    version               BIGINT NOT NULL DEFAULT 0 COMMENT '版本号',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    created_by            BIGINT UNSIGNED COMMENT '创建人ID',
    updated_by            BIGINT UNSIGNED COMMENT '更新人ID',

    -- 软删除
    deleted               TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记: 1-已删除 0-正常',
    deleted_at            DATETIME(3) COMMENT '删除时间',
    deleted_by            BIGINT UNSIGNED COMMENT '删除人ID',

    PRIMARY KEY (id),
    UNIQUE KEY uk_tenant_role_code (tenant_id, role_code, deleted),
    KEY idx_tenant_id (tenant_id),
    KEY idx_role_type (role_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';


-- ============================================================================
-- 4. 权限表 (permission)
-- 说明: 权限表，定义系统所有权限
-- ============================================================================
CREATE TABLE permission (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 权限标识
    permission_code       VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_name       VARCHAR(200) NOT NULL COMMENT '权限名称',

    -- 权限分类
    resource_type         VARCHAR(50) NOT NULL COMMENT '资源类型: MENU/API/BUTTON/DATA',
    resource_path         VARCHAR(200) COMMENT '资源路径',
    action                VARCHAR(50) NOT NULL COMMENT '操作: CREATE/READ/UPDATE/DELETE/EXECUTE',

    -- 权限层级
    parent_id             BIGINT UNSIGNED COMMENT '父权限ID',
    permission_path       VARCHAR(500) COMMENT '权限路径',

    -- 权限属性
    description           VARCHAR(500) COMMENT '权限描述',
    display_order         INT DEFAULT 0 COMMENT '显示顺序',

    -- 状态信息
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_permission_code (permission_code),
    KEY idx_resource_type (resource_type),
    KEY idx_parent_id (parent_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';


-- ============================================================================
-- 5. 用户角色关联表 (user_role)
-- 说明: 用户与角色多对多关联表
-- ============================================================================
CREATE TABLE user_role (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户关联
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联用户和角色
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    role_id               BIGINT UNSIGNED NOT NULL COMMENT '角色ID',

    -- 授权信息
    granted_by            BIGINT UNSIGNED COMMENT '授权人ID',
    granted_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间',
    expire_at             DATETIME(3) COMMENT '到期时间(NULL表示永久)',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (user_id, role_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_role_id (role_id),
    KEY idx_expire_at (expire_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';


-- ============================================================================
-- 6. 角色权限关联表 (role_permission)
-- 说明: 角色与权限多对多关联表
-- ============================================================================
CREATE TABLE role_permission (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 租户关联
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 关联角色和权限
    role_id               BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
    permission_id         BIGINT UNSIGNED NOT NULL COMMENT '权限ID',

    -- 授权信息
    granted_by            BIGINT UNSIGNED COMMENT '授权人ID',
    granted_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '授权时间',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';


-- ============================================================================
-- 7. SSO会话表 (sso_session)
-- 说明: SSO单点登录会话表
-- ============================================================================
CREATE TABLE sso_session (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 会话标识
    session_id            VARCHAR(100) NOT NULL COMMENT '会话ID',
    session_token         VARCHAR(255) NOT NULL COMMENT '会话令牌',

    -- 用户信息
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 会话信息
    client_id             VARCHAR(100) NOT NULL COMMENT '客户端ID',
    device_type           VARCHAR(50) COMMENT '设备类型',
    device_id             VARCHAR(100) COMMENT '设备ID',

    -- IP信息
    ip_address            VARCHAR(50) COMMENT 'IP地址',
    user_agent            VARCHAR(500) COMMENT '用户代理',

    -- 会话时间
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    expires_at            DATETIME(3) NOT NULL COMMENT '过期时间',
    last_active_at        DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后活跃时间',

    -- 状态
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/EXPIRED/LOGOUT',

    PRIMARY KEY (id),
    UNIQUE KEY uk_session_id (session_id),
    UNIQUE KEY uk_session_token (session_token),
    KEY idx_user_id (user_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_expires_at (expires_at),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SSO会话表';


-- ============================================================================
-- 8. MFA配置表 (mfa_config)
-- 说明: 多因素认证配置表
-- ============================================================================
CREATE TABLE mfa_config (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 用户信息
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- MFA配置
    mfa_type              VARCHAR(20) NOT NULL COMMENT 'MFA类型: TOTP/SMS/EMAIL/HARDWARE_TOKEN',
    is_enabled            TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用: 1-是 0-否',
    is_primary            TINYINT NOT NULL DEFAULT 0 COMMENT '是否主要方式: 1-是 0-否',

    -- 密钥信息
    secret_key            VARCHAR(255) COMMENT '密钥',
    backup_codes          JSON COMMENT '备用恢复码: ["code1", "code2"]',

    -- 状态
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_mfa_type (mfa_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MFA配置表';


-- ============================================================================
-- 9. MFA设备表 (mfa_device)
-- 说明: MFA设备绑定表（如硬件令牌）
-- ============================================================================
CREATE TABLE mfa_device (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 用户信息
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 设备信息
    device_name           VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type           VARCHAR(50) NOT NULL COMMENT '设备类型: HARDWARE_TOKEN/BIOSENSOR',
    device_identifier     VARCHAR(200) NOT NULL COMMENT '设备标识符',

    -- 设备属性
    is_verified           TINYINT NOT NULL DEFAULT 0 COMMENT '是否已验证: 1-是 0-否',
    last_used_at          DATETIME(3) COMMENT '最后使用时间',

    -- 状态
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE/LOST',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    updated_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_user_device (user_id, device_identifier),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MFA设备表';


-- ============================================================================
-- 10. MFA挑战表 (mfa_challenge)
-- 说明: MFA验证挑战记录表
-- ============================================================================
CREATE TABLE mfa_challenge (
    -- 主键
    id                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 用户信息
    user_id               BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
    tenant_id             BIGINT UNSIGNED NOT NULL COMMENT '租户ID',

    -- 挑战信息
    challenge_code        VARCHAR(100) NOT NULL COMMENT '挑战码',
    mfa_type              VARCHAR(20) NOT NULL COMMENT 'MFA类型',

    -- 验证信息
    code                  VARCHAR(20) COMMENT '验证码',
    verification_method   VARCHAR(50) COMMENT '验证方式',

    -- 状态
    status                VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/VERIFIED/FAILED/EXPIRED',

    -- 时间信息
    expires_at            DATETIME(3) NOT NULL COMMENT '过期时间',
    verified_at           DATETIME(3) COMMENT '验证时间',
    failed_at             DATETIME(3) COMMENT '失败时间',

    -- 尝试次数
    attempt_count         INT DEFAULT 0 COMMENT '尝试次数',
    max_attempts          INT DEFAULT 3 COMMENT '最大尝试次数',

    -- 审计字段
    created_at            DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_code (challenge_code),
    KEY idx_user_id (user_id),
    KEY idx_tenant_id (tenant_id),
    KEY idx_status (status),
    KEY idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MFA挑战表';


-- ============================================================================
-- 分库分表策略说明
-- ============================================================================
/*
用户与租户模块分库分表策略：

1. 分库策略：按租户ID分库
   - 大租户独立数据库，中小租户共享数据库
   - tenant_id作为分库键

2. 分表策略：
   - user表：按tenant_id分表，单租户用户量大时按user_id哈希
   - user_role表：按user_id分表
   - role_permission表：按role_id分表
   - sso_session表：按时间分区，定期清理过期数据

3. 索引优化：
   - user表：建立复合索引 (tenant_id, username), (email)
   - role表：建立复合索引 (tenant_id, role_code)
   - permission表：按resource_type建立索引
   - 关联表：建立唯一索引防止重复关联

4. 数据安全：
   - 密码使用bcrypt加密存储
   - MFA密钥加密存储
   - 敏感字段脱敏处理

5. 会话管理：
   - SSO会话存储在Redis中，MySQL作为持久化备份
   - 定期清理过期会话
*/


-- ============================================================================
-- 表关系说明 (ER图)
-- ============================================================================
/*
                ┌─────────────────────┐
                │       tenant        │ (租户表)
                │─────────────────────│
                │ id (PK)             │◄──┬── parent_tenant_id (FK自关联)
                │ tenant_code (UK)    │   │
                │ tenant_type         │   │
                │ tenant_path         │   │
                └─────────────────────┘   │
                      │                    │
                      │ tenant_id (FK)     │
                      ▼                    │
         ┌─────────────────────────┐      │
         │          user           │      │
         │─────────────────────────│      │
         │ id (PK)                 │      │
         │ tenant_id (FK)          │      │
         │ username (UK)           │      │
         │ email (UK)              │      │
         └─────────────────────────┘      │
               │                          │
               │ user_id (FK)             │
               │                          │
         ┌─────┴──────┬──────────────┐    │
         │            │              │    │
         ▼            ▼              ▼    │
  ┌────────────┐ ┌─────────┐ ┌─────────────┤
  │ user_role  │ │ mfa_    │ │ sso_session │
  │────────────│ │ config  │ │─────────────│
  │ id (PK)    │ └─────────┘ │ id (PK)     │
  │ user_id    │              │ session_id  │
  │ role_id    │              │ (UK)        │
  └────────────┘              └─────────────┘
         │
         │ role_id (FK)
         ▼
  ┌─────────────────────┐     ┌──────────────────┐
  │        role         │────▶│ role_permission  │
  │─────────────────────│     │──────────────────│
  │ id (PK)             │     │ id (PK)          │
  │ role_code (UK)      │     │ role_id (FK)     │
  │ tenant_id           │     │ permission_id    │
  └─────────────────────┘     │ (FK)             │
                              └──────────────────┘
                                     │
                                     │ permission_id (FK)
                                     ▼
                          ┌─────────────────────┐
                          │     permission      │
                          │─────────────────────│
                          │ id (PK)             │
                          │ permission_code(UK) │
                          │ resource_type       │
                          │ parent_id (FK自关联) │
                          └─────────────────────┘
*/
