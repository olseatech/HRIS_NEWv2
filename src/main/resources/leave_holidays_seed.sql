-- ============================================================================
-- PUBLIC HOLIDAYS SEED DATA - Philippine Government holidays for leave calculations
-- Run AFTER leave_migration.sql
-- ============================================================================

SET SQL_SAFE_UPDATES = 0;

-- 2024 Regular Holidays (used in working day calculations)
INSERT IGNORE INTO public_holiday (holiday_date, holiday_name, holiday_type, exclude_from_leave) VALUES
('2024-01-01', 'New Years Day', 'REGULAR', 1),
('2024-04-09', 'Araw ng Kagitingan', 'REGULAR', 1),
('2024-05-01', 'Labor Day', 'REGULAR', 1),
('2024-06-12', 'Independence Day', 'REGULAR', 1),
('2024-07-21', 'Ninoy Aquino Day', 'REGULAR', 1),
('2024-08-21', 'Ninoy Aquino Day 2', 'REGULAR', 1),
('2024-11-30', 'Bonifacio Day', 'REGULAR', 1),
('2024-12-25', 'Christmas Day', 'REGULAR', 1),
('2024-12-30', 'Rizal Day', 'REGULAR', 1);

-- 2025 Regular Holidays
INSERT IGNORE INTO public_holiday (holiday_date, holiday_name, holiday_type, exclude_from_leave) VALUES
('2025-01-01', 'New Years Day', 'REGULAR', 1),
('2025-04-09', 'Araw ng Kagitingan', 'REGULAR', 1),
('2025-05-01', 'Labor Day', 'REGULAR', 1),
('2025-06-12', 'Independence Day', 'REGULAR', 1),
('2025-07-21', 'Ninoy Aquino Day', 'REGULAR', 1),
('2025-08-21', 'Ninoy Aquino Day 2', 'REGULAR', 1),
('2025-11-30', 'Bonifacio Day', 'REGULAR', 1),
('2025-12-25', 'Christmas Day', 'REGULAR', 1),
('2025-12-30', 'Rizal Day', 'REGULAR', 1);

-- Verification
SELECT 'Holidays seeded:' AS Message, COUNT(*) AS Count FROM public_holiday;

SET SQL_SAFE_UPDATES = 1;
