-- ============================================================================
-- 华宽通智能体系统 - 分区管理脚本
-- 版本: V1.0
-- 数据库: MySQL 8.0+
-- 创建日期: 2026-02-20
-- ============================================================================

-- ============================================================================
-- 1. 规则执行日志表分区管理
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 添加新分区（每月执行）
-- ----------------------------------------------------------------------------

-- 添加2026年7月分区
ALTER TABLE rule_execution_log
ADD PARTITION (
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01'))
);

-- 添加2026年8月分区
ALTER TABLE rule_execution_log
ADD PARTITION (
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01'))
);

-- 添加2026年9月分区
ALTER TABLE rule_execution_log
ADD PARTITION (
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01'))
);

-- 添加2026年10月分区
ALTER TABLE rule_execution_log
ADD PARTITION (
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01'))
);

-- 添加2026年11月分区
ALTER TABLE rule_execution_log
ADD PARTITION (
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01'))
);

-- 添加2026年12月分区
ALTER TABLE rule_execution_log
ADD PARTITION (
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01'))
);

-- ----------------------------------------------------------------------------
-- 删除旧分区（数据归档后执行）
-- ----------------------------------------------------------------------------

-- 删除2026年1月分区（数据已归档）
ALTER TABLE rule_execution_log
DROP PARTITION p202601;

-- 删除2026年2月分区（数据已归档）
ALTER TABLE rule_execution_log
DROP PARTITION p202602;

-- ----------------------------------------------------------------------------
-- 重建分区（可选，用于优化）
-- ----------------------------------------------------------------------------

-- 重建特定分区
ALTER TABLE rule_execution_log
REBUILD PARTITION p202603;

-- 优化分区
ALTER TABLE rule_execution_log
OPTIMIZE PARTITION p202603;

-- 分析分区
ALTER TABLE rule_execution_log
ANALYZE PARTITION p202603;

-- 检查分区
ALTER TABLE rule_execution_log
CHECK PARTITION p202603;


-- ============================================================================
-- 2. 规则动作执行日志表分区管理
-- ============================================================================

-- 添加2026年7月分区
ALTER TABLE rule_action_execution_log
ADD PARTITION (
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01'))
);

-- 添加2026年8月分区
ALTER TABLE rule_action_execution_log
ADD PARTITION (
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01'))
);

-- 添加2026年9月分区
ALTER TABLE rule_action_execution_log
ADD PARTITION (
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01'))
);

-- 添加2026年10月分区
ALTER TABLE rule_action_execution_log
ADD PARTITION (
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01'))
);

-- 添加2026年11月分区
ALTER TABLE rule_action_execution_log
ADD PARTITION (
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01'))
);

-- 添加2026年12月分区
ALTER TABLE rule_action_execution_log
ADD PARTITION (
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01'))
);

-- 删除2026年1月分区（数据已归档）
ALTER TABLE rule_action_execution_log
DROP PARTITION p202601;


-- ============================================================================
-- 3. 查看分区信息
-- ============================================================================

-- 查看rule_execution_log分区信息
SELECT
    PARTITION_NAME,
    PARTITION_EXPRESSION,
    PARTITION_DESCRIPTION,
    TABLE_ROWS
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'rule_execution_log'
ORDER BY PARTITION_ORDINAL_POSITION;

-- 查看rule_action_execution_log分区信息
SELECT
    PARTITION_NAME,
    PARTITION_EXPRESSION,
    PARTITION_DESCRIPTION,
    TABLE_ROWS
FROM information_schema.PARTITIONS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'rule_action_execution_log'
ORDER BY PARTITION_ORDINAL_POSITION;


-- ============================================================================
-- 4. 分区维护存储过程
-- ============================================================================

DELIMITER $$

-- ----------------------------------------------------------------------------
-- 自动添加下一月分区的存储过程
-- ----------------------------------------------------------------------------

DROP PROCEDURE IF EXISTS sp_add_next_month_partition$$

CREATE PROCEDURE sp_add_next_month_partition(
    IN p_table_name VARCHAR(100),
    IN p_partition_prefix VARCHAR(20),
    IN p_date_column VARCHAR(50)
)
BEGIN
    DECLARE v_next_month_date DATE;
    DECLARE v_next_month_name VARCHAR(20);
    DECLARE v_next_month_after_date DATE;
    DECLARE v_sql VARCHAR(1000);

    -- 计算下月日期
    SET v_next_month_date = DATE_ADD(CURDATE(), INTERVAL 2 MONTH);
    SET v_next_month_date = DATE_FORMAT(v_next_month_date, '%Y-%m-01');

    -- 计算下月之后的日期
    SET v_next_month_after_date = DATE_ADD(v_next_month_date, INTERVAL 1 MONTH);

    -- 生成分区名称（如 p202607）
    SET v_next_month_name = CONCAT(p_partition_prefix, DATE_FORMAT(v_next_month_date, '%Y%m'));

    -- 检查分区是否已存在
    SET @sql = CONCAT('SELECT COUNT(*) INTO @partition_exists FROM information_schema.PARTITIONS ',
                     'WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ''', p_table_name, ''' ',
                     'AND PARTITION_NAME = ''', v_next_month_name, '''');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- 如果分区不存在，则添加
    IF @partition_exists = 0 THEN
        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' ADD PARTITION (',
                         'PARTITION ', v_next_month_name, ' VALUES LESS THAN (TO_DAYS(''', v_next_month_after_date, ''')))');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SELECT CONCAT('成功添加分区: ', v_next_month_name) AS result;
    ELSE
        SELECT CONCAT('分区已存在: ', v_next_month_name) AS result;
    END IF;
END$$

-- ----------------------------------------------------------------------------
-- 删除指定月份之前分区的存储过程
-- ----------------------------------------------------------------------------

DROP PROCEDURE IF EXISTS sp_drop_old_partitions$$

CREATE PROCEDURE sp_drop_old_partitions(
    IN p_table_name VARCHAR(100),
    IN p_keep_months INT
)
BEGIN
    DECLARE v_partition_name VARCHAR(20);
    DECLARE v_done INT DEFAULT FALSE;
    DECLARE cur CURSOR FOR
        SELECT PARTITION_NAME
        FROM information_schema.PARTITIONS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = p_table_name
          AND PARTITION_NAME != 'pmax'
          AND PARTITION_DESCRIPTION < DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL p_keep_months MONTH), '%Y-%m-01')
        ORDER BY PARTITION_ORDINAL_POSITION;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = TRUE;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_partition_name;
        IF v_done THEN
            LEAVE read_loop;
        END IF;

        SET @sql = CONCAT('ALTER TABLE ', p_table_name, ' DROP PARTITION ', v_partition_name);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;

        SELECT CONCAT('删除分区: ', v_partition_name) AS result;
    END LOOP;

    CLOSE cur;
END$$

DELIMITER ;


-- ============================================================================
-- 5. 分区管理事件（定时任务）
-- ============================================================================

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 每月25日自动添加下一月分区
DROP EVENT IF EXISTS evt_add_next_month_partition;
CREATE EVENT evt_add_next_month_partition
ON SCHEDULE EVERY 1 MONTH
STARTS CONCAT(DATE_FORMAT(NOW(), '%Y-'), '25 00:00:00')
DO
BEGIN
    -- 为rule_execution_log添加下月分区
    CALL sp_add_next_month_partition('rule_execution_log', 'p', 'triggered_at');

    -- 为rule_action_execution_log添加下月分区
    CALL sp_add_next_month_partition('rule_action_execution_log', 'p', 'started_at');
END;

-- 每月1日删除3个月前的旧分区（数据已归档）
DROP EVENT IF EXISTS evt_drop_old_partitions;
CREATE EVENT evt_drop_old_partitions
ON SCHEDULE EVERY 1 MONTH
STARTS CONCAT(DATE_FORMAT(NOW(), '%Y-'), '01 02:00:00')
DO
BEGIN
    -- 删除rule_execution_log的3个月前分区
    CALL sp_drop_old_partitions('rule_execution_log', 3);

    -- 删除rule_action_execution_log的3个月前分区
    CALL sp_drop_old_partitions('rule_action_execution_log', 3);
END;


-- ============================================================================
-- 6. 使用示例
-- ============================================================================

-- 手动添加下月分区
-- CALL sp_add_next_month_partition('rule_execution_log', 'p', 'triggered_at');

-- 手动删除3个月前的分区
-- CALL sp_drop_old_partitions('rule_execution_log', 3);

-- 查看分区信息
-- SELECT * FROM information_schema.PARTITIONS WHERE TABLE_NAME = 'rule_execution_log';
