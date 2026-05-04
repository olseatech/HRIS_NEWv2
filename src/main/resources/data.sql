-- ============================================================
-- Admin password is managed by AdminPasswordInitializer.java
-- It auto-BCrypt-encodes on startup if plain text is detected.
-- ============================================================

-- ============================================================
-- LEAVE MANAGEMENT — ensure tables exist (safe to re-run)
-- ============================================================

CREATE TABLE IF NOT EXISTS `leave_type` (
    `id`                        BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `leave_code`                VARCHAR(20)     NOT NULL,
    `leave_name`                VARCHAR(255)    NOT NULL,
    `description`               TEXT            DEFAULT NULL,
    `max_days_per_year`         DOUBLE          NOT NULL DEFAULT 0,
    `accrual_per_month`         DOUBLE          NOT NULL DEFAULT 0,
    `carry_over_max`            DOUBLE          NOT NULL DEFAULT 0,
    `commutable`                BIT(1)          NOT NULL DEFAULT 0,
    `lwop_type`                 BIT(1)          NOT NULL DEFAULT 0,
    `requires_med_cert`         BIT(1)          NOT NULL DEFAULT 0,
    `eligible_employment_types` VARCHAR(500)    DEFAULT NULL,
    `active`                    BIT(1)          NOT NULL DEFAULT 1,
    `sort_order`                INT(11)         NOT NULL DEFAULT 0,
    `annual_reset_month`        TINYINT         DEFAULT NULL,
    `service_obligation_days`   INT             DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_leave_type_code` (`leave_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `leave_application` (
    `id`                        BIGINT(20)      NOT NULL AUTO_INCREMENT,
    `employee_id`               BIGINT(20)      NOT NULL,
    `leave_type_id`             BIGINT(20)      NOT NULL,
    `date_from`                 DATE            NOT NULL,
    `date_to`                   DATE            NOT NULL,
    `expected_return_date`      DATE            DEFAULT NULL,
    `number_of_days`            DOUBLE          NOT NULL DEFAULT 0,
    `leave_sub_type`            VARCHAR(50)     DEFAULT NULL,
    `leave_details`             TEXT            DEFAULT NULL,
    `reason`                    TEXT            DEFAULT NULL,
    `requested_commutation`     BIT(1)          NOT NULL DEFAULT 0,
    `attachment_path`           VARCHAR(500)    DEFAULT NULL,
    `attachment_file_name`      VARCHAR(255)    DEFAULT NULL,
    `attachment_mime_type`      VARCHAR(100)    DEFAULT NULL,
    `cert_days_with_pay`        DOUBLE          DEFAULT NULL,
    `cert_days_without_pay`     DOUBLE          DEFAULT NULL,
    `hrmo_id`                   BIGINT(20)      DEFAULT NULL,
    `hrmo_name`                 VARCHAR(255)    DEFAULT NULL,
    `hrmo_cert_date`            DATE            DEFAULT NULL,
    `supervisor_recommendation` VARCHAR(30)     DEFAULT NULL,
    `supervisor_remarks`        TEXT            DEFAULT NULL,
    `supervisor_id`             BIGINT(20)      DEFAULT NULL,
    `supervisor_name`           VARCHAR(255)    DEFAULT NULL,
    `supervisor_action_date`    DATE            DEFAULT NULL,
    `final_days_with_pay`       DOUBLE          DEFAULT NULL,
    `final_days_without_pay`    DOUBLE          DEFAULT NULL,
    `commutation_approved`      BIT(1)          NOT NULL DEFAULT 0,
    `status`                    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    `with_pay`                  BIT(1)          NOT NULL DEFAULT 1,
    `approved_by_id`            BIGINT(20)      DEFAULT NULL,
    `approved_by_name`          VARCHAR(255)    DEFAULT NULL,
    `approved_date`             DATE            DEFAULT NULL,
    `remarks`                   TEXT            DEFAULT NULL,
    `applied_date_time`         DATETIME(6)     DEFAULT NULL,
    `ledger_posted`             BIT(1)          NOT NULL DEFAULT 0,
    `cancelled_by_id`           BIGINT(20)      DEFAULT NULL,
    `cancelled_by_name`         VARCHAR(255)    DEFAULT NULL,
    `cancelled_date`            DATE            DEFAULT NULL,
    `cancel_reason`             TEXT            DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FK_la_employee`  (`employee_id`),
    KEY `FK_la_leavetype` (`leave_type_id`),
    CONSTRAINT `FK_la_employee`  FOREIGN KEY (`employee_id`)  REFERENCES `employee`  (`id`),
    CONSTRAINT `FK_la_leavetype` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `leave_balance` (
    `id`            BIGINT(20)  NOT NULL AUTO_INCREMENT,
    `employee_id`   BIGINT(20)  DEFAULT NULL,
    `leave_type_id` BIGINT(20)  DEFAULT NULL,
    `balance_year`  INT(11)     NOT NULL,
    `total_earned`  DOUBLE      NOT NULL DEFAULT 0,
    `total_used`    DOUBLE      NOT NULL DEFAULT 0,
    `balance`       DOUBLE      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_lb_emp_type_year` (`employee_id`, `leave_type_id`, `balance_year`),
    CONSTRAINT `FK_lb_employee`  FOREIGN KEY (`employee_id`)  REFERENCES `employee`  (`id`),
    CONSTRAINT `FK_lb_leavetype` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `leave_ledger` (
    `id`               BIGINT(20)   NOT NULL AUTO_INCREMENT,
    `employee_id`      BIGINT(20)   DEFAULT NULL,
    `leave_type_id`    BIGINT(20)   DEFAULT NULL,
    `transaction_date` DATE         DEFAULT NULL,
    `transaction_type` VARCHAR(20)  DEFAULT NULL,
    `days`             DOUBLE       NOT NULL DEFAULT 0,
    `running_balance`  DOUBLE       NOT NULL DEFAULT 0,
    `reference`        VARCHAR(255) DEFAULT NULL,
    `remarks`          TEXT         DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_ll_employee`  FOREIGN KEY (`employee_id`)  REFERENCES `employee`  (`id`),
    CONSTRAINT `FK_ll_leavetype` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `leave_status_history` (
    `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
    `application_id` BIGINT(20)   NOT NULL,
    `from_status`    VARCHAR(20)  DEFAULT NULL,
    `to_status`      VARCHAR(20)  NOT NULL,
    `actor_id`       BIGINT(20)   DEFAULT NULL,
    `actor_name`     VARCHAR(255) DEFAULT NULL,
    `actor_role`     VARCHAR(50)  DEFAULT NULL,
    `remarks`        TEXT         DEFAULT NULL,
    `changed_at`     DATETIME(6)  NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_lsh_application` FOREIGN KEY (`application_id`) REFERENCES `leave_application` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `leave_notification_log` (
    `id`              BIGINT(20)    NOT NULL AUTO_INCREMENT,
    `application_id`  BIGINT(20)    NOT NULL,
    `recipient_email` VARCHAR(255)  NOT NULL,
    `recipient_name`  VARCHAR(255)  DEFAULT NULL,
    `event_type`      VARCHAR(50)   NOT NULL,
    `sent_at`         DATETIME(6)   DEFAULT NULL,
    `status`          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    `error_message`   TEXT          DEFAULT NULL,
    `created_at`      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_notif_application`
        FOREIGN KEY (`application_id`) REFERENCES `leave_application` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `public_holiday` (
    `id`                 BIGINT(20)   NOT NULL AUTO_INCREMENT,
    `holiday_date`       DATE         NOT NULL,
    `holiday_name`       VARCHAR(200) NOT NULL,
    `holiday_type`       VARCHAR(20)  DEFAULT 'REGULAR',
    `exclude_from_leave` BIT(1)       NOT NULL DEFAULT 1,
    `remarks`            VARCHAR(300) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_ph_date` (`holiday_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
