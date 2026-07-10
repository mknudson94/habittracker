-- Test data to recreate the "Habits" screenshot dated Jun 30 (Tuesday)
-- Week shown: Wed 6/24, Thu 6/25, Fri 6/26, Sat 6/27, Sun 6/28, Mon 6/29, Tue 6/30
-- Timestamps (created_at) are epoch milliseconds, assuming UTC.

-- ============================================================
-- USER
-- ============================================================
INSERT INTO "user" (id, email, created_at, user_timezone) VALUES
('1', 'test.user@example.com', 1767225600000, 'America/New_York'); -- created 2026-01-01

-- ============================================================
-- HABITS
-- ============================================================
INSERT INTO habit (id, user_id, name, created_at) VALUES
('1', '1', 'Drink 8 Glasses of Water', 1780272000000), -- created 2026-06-01
('2', '1', 'Read 30 Minutes',          1780272000000), -- created 2026-06-01
('3', '1', 'Meditate',                 1781481600000); -- created 2026-06-15

-- ============================================================
-- CHECK-INS
-- ============================================================

-- Habit 1: Drink 8 Glasses of Water
INSERT INTO check_in (id, habit_id, completed_date) VALUES
('1', '1', '2026-07-06'),
('2', '1', '2026-07-07'),
('3', '1', '2026-07-09'),
('4', '1', '2026-07-10');

-- Habit 2: Read 30 Minutes
INSERT INTO check_in (id, habit_id, completed_date) VALUES
('5', '2', '2026-07-04'),
('6', '2', '2026-07-06'),
('7', '2', '2026-07-08'),
('8', '2', '2026-07-09');

-- Habit 3: Meditate
INSERT INTO check_in (id, habit_id, completed_date) VALUES
('9', '3', '2026-07-07'),
('10', '3', '2026-07-08'),
('11', '3', '2026-07-10');