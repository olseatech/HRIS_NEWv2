-- ============================================================================
-- LEAVE MANAGEMENT MODULE — Database Migration
-- Run this script once against your MySQL database (hrisp_all / hris_java)
-- All statements use IF NOT EXISTS / INSERT IGNORE so re-running is safe.
-- ============================================================================

-- -----------------------------------------------------------------------
-- 1. LEAVE TYPE  (configurable leave types managed by HR Admin)
-- -----------------------------------------------------------------------
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
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_leave_type_code` (`leave_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------
-- 2. LEAVE APPLICATION  (CSC Form No. 6)
-- -----------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `leave_application` (
    `id`                        BIGINT(20)      NOT NULL AUTO_INCREMENT,
    -- Section A: Employee
    `employee_id`               BIGINT(20)      NOT NULL,
    `leave_type_id`             BIGINT(20)      NOT NULL,
    -- Section B: Details
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
    -- Section C Part 1: HRMO Certification
    `cert_days_with_pay`        DOUBLE          DEFAULT NULL,
    `cert_days_without_pay`     DOUBLE          DEFAULT NULL,
    `hrmo_id`                   BIGINT(20)      DEFAULT NULL,
    `hrmo_name`                 VARCHAR(255)    DEFAULT NULL,
    `hrmo_cert_date`            DATE            DEFAULT NULL,
    -- Section C Part 2: Supervisor Endorsement
    `supervisor_recommendation` VARCHAR(30)     DEFAULT NULL,
    `supervisor_remarks`        TEXT            DEFAULT NULL,
    `supervisor_id`             BIGINT(20)      DEFAULT NULL,
    `supervisor_name`           VARCHAR(255)    DEFAULT NULL,
    `supervisor_action_date`    DATE            DEFAULT NULL,
    -- Section C Part 3: Final Approval
    `final_days_with_pay`       DOUBLE          DEFAULT NULL,
    `final_days_without_pay`    DOUBLE          DEFAULT NULL,
    `commutation_approved`      BIT(1)          NOT NULL DEFAULT 0,
    -- Workflow
    `status`                    VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    `with_pay`                  BIT(1)          NOT NULL DEFAULT 1,
    -- Approver
    `approved_by_id`            BIGINT(20)      DEFAULT NULL,
    `approved_by_name`          VARCHAR(255)    DEFAULT NULL,
    `approved_date`             DATE            DEFAULT NULL,
    `remarks`                   TEXT            DEFAULT NULL,
    -- Audit
    `applied_date_time`         DATETIME(6)     DEFAULT NULL,
    `ledger_posted`             BIT(1)          NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `FK_la_employee`  (`employee_id`),
    KEY `FK_la_leavetype` (`leave_type_id`),
    CONSTRAINT `FK_la_employee`  FOREIGN KEY (`employee_id`)  REFERENCES `employee`  (`id`),
    CONSTRAINT `FK_la_leavetype` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------
-- 3. LEAVE BALANCE  (running totals per employee/type/year)
-- -----------------------------------------------------------------------
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
    KEY `FK_lb_employee`  (`employee_id`),
    KEY `FK_lb_leavetype` (`leave_type_id`),
    CONSTRAINT `FK_lb_employee`  FOREIGN KEY (`employee_id`)  REFERENCES `employee`  (`id`),
    CONSTRAINT `FK_lb_leavetype` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------
-- 4. LEAVE LEDGER  (immutable transaction log of all balance movements)
-- -----------------------------------------------------------------------
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
    KEY `FK_ll_employee`  (`employee_id`),
    KEY `FK_ll_leavetype` (`leave_type_id`),
    CONSTRAINT `FK_ll_employee`  FOREIGN KEY (`employee_id`)  REFERENCES `employee`  (`id`),
    CONSTRAINT `FK_ll_leavetype` FOREIGN KEY (`leave_type_id`) REFERENCES `leave_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------
-- 5. LEAVE STATUS HISTORY  (workflow audit trail)
-- -----------------------------------------------------------------------
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
    KEY `idx_lsh_application` (`application_id`),
    CONSTRAINT `FK_lsh_application` FOREIGN KEY (`application_id`) REFERENCES `leave_application` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------------------------
-- 6. PUBLIC HOLIDAY  (for working-day calculation)
-- -----------------------------------------------------------------------
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

-- -----------------------------------------------------------------------
-- 7. SEED DEFAULT LEAVE TYPES (Philippine Government standard types)
--    INSERT IGNORE skips duplicates on re-run.
-- -----------------------------------------------------------------------
INSERT IGNORE INTO `leave_type`
    (`leave_code`,`leave_name`,`description`,`max_days_per_year`,`accrual_per_month`,
     `carry_over_max`,`commutable`,`lwop_type`,`requires_med_cert`,
     `eligible_employment_types`,`active`,`sort_order`)
VALUES
    ('VL',  'Vacation Leave',
     'CSC MC No. 41 s.1998. Earned at 1.25 days/month. Carry-over max 30 days.',
     0, 1.25, 30, 1, 0, 0, '', 1, 1),

    ('SL',  'Sick Leave',
     'CSC MC No. 41 s.1998. Earned at 1.25 days/month. Carry-over max 30 days. Med cert required >5 days.',
     0, 1.25, 30, 0, 0, 1, '', 1, 2),

    ('FL',  'Forced/Mandatory Leave',
     '5 working days mandatory per year charged against VL. All permanent employees.',
     5, 0, 0, 0, 0, 0, 'PLANTILLA', 1, 3),

    ('SPL', 'Special Privilege Leave',
     '3 days per year for personal milestones, no accrual, non-cumulative.',
     3, 0, 0, 0, 0, 0, '', 1, 4),

    ('ML',  'Maternity Leave',
     'RA 11210. 105 days with pay (120 days for solo parents). Female employees only.',
     120, 0, 0, 0, 0, 1, '', 1, 5),

    ('PL',  'Paternity Leave',
     'RA 8187. 7 days for married male employees, per childbirth, max 4 deliveries.',
     7, 0, 0, 0, 0, 0, '', 1, 6),

    ('SLB', 'Special Leave Benefit (Gynecological)',
     'RA 9710 Magna Carta for Women. Max 2 months for gynaecological surgery.',
     60, 0, 0, 0, 0, 1, '', 1, 7),

    ('SoP', 'Solo Parent Leave',
     'RA 8972. 7 days per year. Solo parent employees only.',
     7, 0, 0, 0, 0, 0, '', 1, 8),

    ('SdL', 'Study Leave',
     'CSC. Up to 6 months with pay for bar/board review or masters. With service obligation.',
     180, 0, 0, 0, 0, 0, 'PLANTILLA', 1, 9),

    ('RL',  'Rehabilitation Leave',
     'For employees injured in the line of duty. Duration per medical recommendation.',
     0, 0, 0, 0, 0, 1, '', 1, 10),

    ('LWOP','Leave Without Pay',
     'When leave credits are exhausted. Affects service credit computation.',
     0, 0, 0, 0, 1, 0, '', 1, 11);

-- -----------------------------------------------------------------------
-- Done. Tables created and seeded.
-- -----------------------------------------------------------------------
SELECT CONCAT('Migration complete. Leave types in DB: ', COUNT(*)) AS result
FROM leave_type;
