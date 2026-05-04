-- ============================================================================
-- LEAVE MANAGEMENT MODULE — Migration v2 (Additive)
-- Safe to run multiple times. Uses IF NOT EXISTS / ALTER IGNORE semantics.
-- Run AFTER leave_migration.sql (v1).
-- ============================================================================

USE hrisp_all;

-- -----------------------------------------------------------------------
-- 1. leave_application — new tracking columns
-- -----------------------------------------------------------------------
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND COLUMN_NAME = 'attachment_mime_type'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_application` ADD COLUMN `attachment_mime_type` VARCHAR(100) DEFAULT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND COLUMN_NAME = 'cancelled_by_id'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_application` ADD COLUMN `cancelled_by_id` BIGINT(20) DEFAULT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND COLUMN_NAME = 'cancelled_by_name'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_application` ADD COLUMN `cancelled_by_name` VARCHAR(255) DEFAULT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND COLUMN_NAME = 'cancelled_date'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_application` ADD COLUMN `cancelled_date` DATE DEFAULT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND COLUMN_NAME = 'cancel_reason'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_application` ADD COLUMN `cancel_reason` TEXT DEFAULT NULL',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Performance indexes for leave_application
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND INDEX_NAME = 'idx_la_employee_status'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX `idx_la_employee_status` ON `leave_application` (`employee_id`, `status`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND INDEX_NAME = 'idx_la_date_range'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX `idx_la_date_range` ON `leave_application` (`date_from`, `date_to`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_application'
      AND INDEX_NAME = 'idx_la_applied_dt'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX `idx_la_applied_dt` ON `leave_application` (`applied_date_time`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------
-- 2. leave_type — support annual reset (SPL) and study obligation
-- -----------------------------------------------------------------------
SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_type'
      AND COLUMN_NAME = 'annual_reset_month'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_type` ADD COLUMN `annual_reset_month` TINYINT DEFAULT NULL COMMENT ''Month (1-12) on which annual balance resets (e.g. 1=Jan for SPL)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_type'
      AND COLUMN_NAME = 'service_obligation_days'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `leave_type` ADD COLUMN `service_obligation_days` INT DEFAULT 0 COMMENT ''Return-of-service obligation for Study Leave (days)''',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------
-- 3. leave_notification_log — email/notification audit trail
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `leave_notification_log` (
    `id`              BIGINT(20)    NOT NULL AUTO_INCREMENT,
    `application_id`  BIGINT(20)    NOT NULL,
    `recipient_email` VARCHAR(255)  NOT NULL,
    `recipient_name`  VARCHAR(255)  DEFAULT NULL,
    `event_type`      VARCHAR(50)   NOT NULL
        COMMENT 'APPLIED | ENDORSED | APPROVED | DISAPPROVED | RETURNED | CANCELLED',
    `sent_at`         DATETIME(6)   DEFAULT NULL,
    `status`          VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING | SENT | FAILED',
    `error_message`   TEXT          DEFAULT NULL,
    `created_at`      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    KEY `idx_notif_app` (`application_id`),
    KEY `idx_notif_status` (`status`),
    CONSTRAINT `fk_notif_application`
        FOREIGN KEY (`application_id`) REFERENCES `leave_application` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------
-- 4. leave_balance — performance index
-- -----------------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_balance'
      AND INDEX_NAME = 'idx_lb_employee_year'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX `idx_lb_employee_year` ON `leave_balance` (`employee_id`, `balance_year`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------
-- 5. leave_ledger — performance index
-- -----------------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'leave_ledger'
      AND INDEX_NAME = 'idx_ll_employee_type'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX `idx_ll_employee_type` ON `leave_ledger` (`employee_id`, `leave_type_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------
-- 6. public_holiday — performance index
-- -----------------------------------------------------------------------
SET @idx_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'public_holiday'
      AND INDEX_NAME = 'idx_ph_date_exclude'
);
SET @sql := IF(@idx_exists = 0,
    'CREATE INDEX `idx_ph_date_exclude` ON `public_holiday` (`holiday_date`, `exclude_from_leave`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------
-- Done.
-- -----------------------------------------------------------------------
