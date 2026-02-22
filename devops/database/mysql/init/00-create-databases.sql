-- ============================================================================
-- 华宽通智能体系统 - 数据库初始化脚本
-- 版本: V1.0
-- 创建日期: 2026-02-20
-- 说明: 创建Phase 1核心模块所需的数据库
-- ============================================================================

-- 创建主数据库
CREATE DATABASE IF NOT EXISTS `hkt_iot`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    COMMENT '华宽通IoT平台主数据库';

-- 创建设备管理数据库
CREATE DATABASE IF NOT EXISTS `hkt_iot_device`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    COMMENT '设备管理模块数据库';

-- 创建空间管理数据库
CREATE DATABASE IF NOT EXISTS `hkt_iot_space`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    COMMENT '空间管理模块数据库';

-- 创建用户租户数据库
CREATE DATABASE IF NOT EXISTS `hkt_iot_user`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    COMMENT '用户租户管理数据库';

-- 创建规则引擎数据库
CREATE DATABASE IF NOT EXISTS `hkt_iot_rule`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci
    COMMENT '规则引擎模块数据库';

-- 显示创建的数据库
SHOW DATABASES;
