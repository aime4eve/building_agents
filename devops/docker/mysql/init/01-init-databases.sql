-- =============================================================================
-- 华宽通智能体系统 - MySQL 数据库初始化脚本
-- 版本: V1.1
-- 创建日期: 2026-02-20
-- 说明: 在MySQL容器首次启动时自动执行
-- =============================================================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- =============================================================================
-- 创建数据库
-- =============================================================================

-- 创建Nacos配置库
CREATE DATABASE IF NOT EXISTS `nacos_config` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建业务数据库
CREATE DATABASE IF NOT EXISTS `huakuantong_agent` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 切换到业务数据库
USE `huakuantong_agent`;

-- =============================================================================
-- 创建用户和权限
-- =============================================================================

-- 创建应用用户（如果不存在）
CREATE USER IF NOT EXISTS 'huakuantong'@'%' IDENTIFIED BY 'hkt123456';
CREATE USER IF NOT EXISTS 'huakuantong'@'localhost' IDENTIFIED BY 'hkt123456';

-- 授予权限
GRANT ALL PRIVILEGES ON huakuantong_agent.* TO 'huakuantong'@'%';
GRANT ALL PRIVILEGES ON huakuantong_agent.* TO 'huakuantong'@'localhost';
GRANT SELECT ON nacos_config.* TO 'huakuantong'@'%';
GRANT SELECT ON nacos_config.* TO 'huakuantong'@'localhost';
FLUSH PRIVILEGES;

-- =============================================================================
-- 插入初始化数据
-- =============================================================================

-- 插入默认租户
INSERT INTO `tenant` (`tenant_code`, `tenant_name`, `tenant_type`, `tenant_status`) VALUES
('SYSTEM', '系统租户', 'OPERATOR', 'ACTIVE'),
('DEMO', '演示租户', 'ENTERPRISE', 'ACTIVE')
ON DUPLICATE KEY UPDATE `tenant_name` = VALUES(`tenant_name`);

-- 插入默认角色
INSERT INTO `role` (`role_code`, `role_name`, `role_type`, `status`, `tenant_id`) VALUES
('SUPER_ADMIN', '超级管理员', 'SYSTEM', 'ACTIVE', 1),
('TENANT_ADMIN', '租户管理员', 'CUSTOM', 'ACTIVE', 2),
('NORMAL_USER', '普通用户', 'CUSTOM', 'ACTIVE', 2)
ON DUPLICATE KEY UPDATE `role_name` = VALUES(`role_name`);

-- 插入默认用户（密码：admin123）
INSERT INTO `user` (`username`, `email`, `password`, `real_name`, `user_status`, `tenant_id`) VALUES
('admin', 'admin@huakuantong.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '系统管理员', 'ACTIVE', 1),
('demo', 'demo@huakuantong.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '演示用户', 'ACTIVE', 2)
ON DUPLICATE KEY UPDATE `email` = VALUES(`email`);

-- 插入用户角色关联
INSERT INTO `user_role` (`user_id`, `role_id`, `tenant_id`) VALUES
(1, 1, 1),
(2, 3, 2)
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 完成提示
-- =============================================================================

SELECT 'MySQL initialization completed successfully!' AS `Status`;
