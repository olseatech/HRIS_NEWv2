-- ============================================================================
-- LEAVE MODULE SEED DATA - For Philippine Government holidays
-- Run this AFTER leave_migration.sql to seed public holidays
-- These holidays affect working-day calculations (Mon-Fri, excluding holidays)
-- ============================================================================

-- Disable strict SQL mode for this import
SET SQL_SAFE_UPDATES = 0;

-- ============================================================================
-- PUBLIC HOLIDAYS SEED DATA - 2024
-- ============================================================================
INSERT IGNORE INTO public_holiday (holiday_date, holiday_name, holiday_type, exclude_from_leave, remarks) VALUES
('2024-01-01', 'New Year''s Day', 'REGULAR', 1, 'Regular holiday'),
('2024-01-02', 'Special Non-Working Day', 'SPECIAL', 0, 'Declared holiday'),
('2024-02-10', 'Chinese New Year', 'SPECIAL', 0, 'Chinese New Year'),
('2024-02-14', 'Valentine''s Day', 'SPECIAL', 0, 'Simple celebration'),
('2024-03-01', 'Women''s Month', 'SPECIAL', 0, 'Women's Month celebration'),
('2024-03-20', 'March Equinox', 'SPECIAL', 0, 'Astronomical event'),
('2024-04-09', 'Araw ng Kagitingan (Bataan Day)', 'REGULAR', 1, 'Regular holiday - Summer camp 1942'),
('2024-04-11', 'Maundy Thursday', 'REGULAR', 1, 'Holy Week'),
('2024-04-12', 'Good Friday', 'REGULAR', 1, 'Holy Week'),
('2024-04-13', 'Black Saturday', 'SPECIAL', 0, 'Holy Week'),
('2024-04-14', 'Easter Sunday', 'SPECIAL', 0, 'Easter'),
('2024-04-15', ' Eid al-Fitr', 'REGULAR', 1, 'End of Ramadan (estimated)'),
('2024-04-29', 'Maute Rebellion Commemoration', 'SPECIAL', 0, 'Local holiday - Marawi'),
('2024-05-01', 'Labor Day', 'REGULAR', 1, 'Regular holiday'),
('2024-05-03', 'Mother''s Day', 'SPECIAL', 0, 'Mother''s Day celebration'),
('2024-05-11', 'Aidilfitri (Eid al-Fitr)', 'REGULAR', 1, 'End of Ramadan'),
('2024-05-12', 'Eid al-Fitr Holiday', 'REGULAR', 1, 'End of Ramadan'),
('2024-06-12', 'Independence Day', 'REGULAR', 1, 'Regular holiday'),
('2024-06-16', 'Father''s Day', 'SPECIAL', 0, 'Father''s Day celebration'),
('2024-06-21', 'June Solstice', 'SPECIAL', 0, 'Astronomical event'),
('2024-07-21', 'Ninoy Aquino Day', 'SPECIAL', 0, 'Special non-working'),
('2024-07-22', 'Ninoy Aquino Day (Holiday)', 'REGULAR', 1, 'Regular holiday'),
('2024-08-21', 'Ninoy Aquino Day', 'REGULAR', 1, 'Regular holiday'),
('2024-09-17', 'International Day of Peace', 'SPECIAL', 0, 'Global observance'),
('2024-09-22', 'September Equinox', 'SPECIAL', 0, 'Astronomical event'),
('2024-10-31', 'National Holiday for November', 'REGULAR', 1, 'Regular holiday'),
('2024-11-01', 'All Saints'' Day', 'REGULAR', 0, 'Observance - not excluded'),
('2024-11-02', 'All Souls'' Day', 'SPECIAL', 0, 'Observance'),
('2024-11-30', 'Bonifacio Day', 'REGULAR', 1, 'Regular holiday'),
('2024-12-08', 'Feast of the Immaculate Conception', 'SPECIAL', 0, 'Religious'),
('2024-12-21', 'December Solstice', 'SPECIAL', 0, 'Astronomical event'),
('2024-12-23', 'Christmas Eve', 'SPECIAL', 0, 'Christmas celebration'),
('2024-12-24', 'Christmas Eve', 'SPECIAL', 0, 'Christmas celebration'),
('2024-12-25', 'Christmas Day', 'REGULAR', 1, 'Regular holiday'),
('2024-12-26', '2nd Day of Christmas', 'SPECIAL', 0, 'Christmas'),
('2024-12-30', 'Rizal Day', 'REGULAR', 1, 'Regular holiday'),
('2024-12-31', 'New Year''s Eve', 'SPECIAL', 0, 'New Year celebration');

-- ============================================================================
-- PUBLIC HOLIDAYS SEED DATA - 2025
-- ============================================================================
INSERT IGNORE INTO public_holiday (holiday_date, holiday_name, holiday_type, exclude_from_leave, remarks) VALUES
('2025-01-01', 'New Year''s Day', 'REGULAR', 1, 'Regular holiday'),
('2025-01-02', 'Special Non-Working Day', 'SPECIAL', 0, 'Declared holiday'),
('2025-01-29', 'Chinese New Year', 'SPECIAL', 0, 'Chinese New Year'),
('2025-02-14', 'Valentine''s Day', 'SPECIAL', 0, 'Simple celebration'),
('2025-03-01', 'Women''s Month', 'SPECIAL', 0, 'Women''s Month celebration'),
('2025-03-20', 'March Equinox', 'SPECIAL', 0, 'Astronomical event'),
('2025-04-09', 'Araw ng Kagitingan (Bataan Day)', 'REGULAR', 1, 'Regular holiday - Summer camp 1942'),
('2025-04-13', 'Palm Sunday', 'SPECIAL', 0, 'Holy Week'),
('2025-04-14', 'Holy Monday', 'SPECIAL', 0, 'Holy Week'),
('2025-04-15', 'Holy Tuesday', 'SPECIAL', 0, 'Holy Week'),
('2025-04-16', 'Maundy Thursday', 'REGULAR', 1, 'Holy Week'),
('2025-04-17', 'Good Friday', 'REGULAR', 1, 'Holy Week'),
('2025-04-18', 'Black Saturday', 'SPECIAL', 0, 'Holy Week'),
('2025-04-19', 'Easter Sunday', 'SPECIAL', 0, 'Easter'),
('2025-04-20', ' Eid al-Fitr', 'REGULAR', 1, 'End of Ramadan (estimated)'),
('2025-05-01', 'Labor Day', 'REGULAR', 1, 'Regular holiday'),
('2025-05-11', 'Mother''s Day', 'SPECIAL', 0, 'Mother''s Day celebration'),
('2025-05-30', 'Eid al-Adha', 'REGULAR', 1, 'Feast of Sacrifice'),
('2025-06-12', 'Independence Day', 'REGULAR', 1, 'Regular holiday'),
('2025-06-15', 'Father''s Day', 'SPECIAL', 0, 'Father''s Day celebration'),
('2025-06-21', 'June Solstice', 'SPECIAL', 0, 'Astronomical event'),
('2025-07-21', 'Ninoy Aquino Day', 'SPECIAL', 0, 'Special non-working'),
('2025-07-22', 'Ninoy Aquino Day (Holiday)', 'REGULAR', 1, 'Regular holiday'),
('2025-08-21', 'Ninoy Aquino Day', 'REGULAR', 1, 'Regular holiday'),
('2025-09-17', 'International Day of Peace', 'SPECIAL', 0, 'Global observance'),
('2025-09-22', 'September Equinox', 'SPECIAL', 0, 'Astronomical event'),
('2025-10-31', 'National Holiday for November', 'REGULAR', 1, 'Regular holiday'),
('2025-11-01', 'All Saints'' Day', 'REGULAR', 0, 'Observance - not excluded'),
('2025-11-02', 'All Souls'' Day', 'SPECIAL', 0, 'Observance'),
('2025-11-30', 'Bonifacio Day', 'REGULAR', 1, 'Regular holiday'),
('2025-12-08', 'Feast of the Immaculate Conception', 'SPECIAL', 0, 'Religious'),
('2025-12-21', 'December Solstice', 'SPECIAL', 0, 'Astronomical event'),
('2025-12-23', 'Christmas Eve', 'SPECIAL', 0, 'Christmas celebration'),
('2025-12-24', 'Christmas Eve', 'SPECIAL', 0, 'Christmas celebration'),
('2025-12-25', 'Christmas Day', 'REGULAR', 1, 'Regular holiday'),
('2025-12-26', '2nd Day of Christmas', 'SPECIAL', 0, 'Christmas'),
('2025-12-30', 'Rizal Day', 'REGULAR', 1, 'Regular holiday'),
('2025-12-31', 'New Year''s Eve', 'SPECIAL', 0, 'New Year celebration');

-- ============================================================================
-- PUBLIC HOLIDAYS SEED DATA - 2026
-- ============================================================================
INSERT IGNORE INTO public_holiday (holiday_date, holiday_name, holiday_type, exclude_from_leave, remarks) VALUES
('2026-01-01', 'New Year''s Day', 'REGULAR', 1, 'Regular holiday'),
('2026-01-02', 'Special Non-Working Day', 'SPECIAL', 0, 'Declared holiday'),
('2026-02-17', 'Chinese New Year', 'SPECIAL', 0, 'Chinese New Year'),
('2026-02-14', 'Valentine''s Day', 'SPECIAL', 0, 'Simple celebration'),
('2026-03-01', 'Women''s Month', 'SPECIAL', 0, 'Women''s Month celebration'),
('2026-03-20', 'March Equinox', 'SPECIAL', 0, 'Astronomical event'),
('2026-04-09', 'Araw ng Kagitingan (Bataan Day)', 'REGULAR', 1, 'Regular holiday'),
('2026-04-02', 'Maundy Thursday', 'REGULAR', 1, 'Holy Week'),
('2026-04-03', 'Good Friday', 'REGULAR', 1, 'Holy Week'),
('2026-04-04', 'Black Saturday', 'SPECIAL', 0, 'Holy Week'),
('2026-04-05', 'Easter Sunday', 'SPECIAL', 0, 'Easter'),
('2026-04-06', 'Eid al-Fitr', 'REGULAR', 1, 'End of Ramadan'),
('2026-05-01', 'Labor Day', 'REGULAR', 1, 'Regular holiday'),
('2026-05-10', 'Mother''s Day', 'SPECIAL', 0, 'Mother''s Day celebration'),
('2026-06-12', 'Independence Day', 'REGULAR', 1, 'Regular holiday'),
('2026-06-14', 'Father''s Day', 'SPECIAL', 0, 'Father''s Day celebration'),
('2026-06-21', 'June Solstice', 'SPECIAL', 0, 'Astronomical event'),
('2026-07-21', 'Ninoy Aquino Day', 'SPECIAL', 0, 'Special non-working'),
('2026-07-22', 'Ninoy Aquino Day (Holiday)', 'REGULAR', 1, 'Regular holiday'),
('2026-08-21', 'Ninoy Aquino Day', 'REGULAR', 1, 'Regular holiday'),
('2026-09-17', 'International Day of Peace', 'SPECIAL', 0, 'Global observance'),
('2026-09-22', 'September Equinox', 'SPECIAL', 0, 'Astronomical event'),
('2026-10-31', 'National Holiday for November', 'REGULAR', 1, 'Regular holiday'),
('2026-11-01', 'All Saints'' Day', 'REGULAR', 0, 'Observance'),
('2026-11-02', 'All Souls'' Day', 'SPECIAL', 0, 'Observance'),
('2026-11-30', 'Bonifacio Day', 'REGULAR', 1, 'Regular holiday'),
('2026-12-08', 'Feast of the Immaculate Conception', 'SPECIAL', 0, 'Religious'),
('2026-12-21', 'December Solstice', 'SPECIAL', 0, 'Astronomical event'),
('2026-12-24', 'Christmas Eve', 'SPECIAL', 0, 'Christmas celebration'),
('2026-12-25', 'Christmas Day', 'REGULAR', 1, 'Regular holiday'),
('2026-12-26', '2nd Day of Christmas', 'SPECIAL', 0, 'Christmas'),
('2026-12-30', 'Rizal Day', 'REGULAR', 1, 'Regular holiday'),
('2026-12-31', 'New Year''s Eve', 'SPECIAL', 0, 'New Year celebration');

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================
SELECT 'Leave Types' AS Table_Name, COUNT(*) AS Record_Count FROM leave_type
UNION ALL
SELECT 'Public Holidays', COUNT(*) FROM public_holiday;

-- Show leave types
SELECT id, leave_code, leave_name, accrual_per_month, carry_over_max, active 
FROM leave_type ORDER BY sort_order;

-- Show sample public holidays (next 60 days)
SELECT holiday_date, holiday_name, holiday_type, exclude_from_leave
FROM public_holiday 
WHERE holiday_date >= CURDATE() 
ORDER BY holiday_date 
LIMIT 20;

SET SQL_SAFE_UPDATES = 1;

SELECT 'Seed data loaded successfully!' AS Result;
