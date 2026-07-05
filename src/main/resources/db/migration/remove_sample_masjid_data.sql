-- Remove sample/dummy data from masjid_transactions table
DELETE FROM masjid_transactions WHERE description LIKE '%demonstration%' OR description LIKE '%sample%';

-- Remove specific dummy records that were added during migration
DELETE FROM masjid_transactions WHERE 
    (type = 'Income' AND date = '2025-01-01' AND amount = 15000.00 AND source = 'Congregation') OR
    (type = 'Income' AND date = '2025-01-05' AND amount = 50000.00 AND source = 'Anonymous Donor') OR
    (type = 'Income' AND date = '2025-01-10' AND amount = 5000.00 AND source = 'Community Member') OR
    (type = 'Income' AND date = '2025-01-15' AND amount = 25000.00 AND source = 'Local Family') OR
    (type = 'Expense' AND date = '2025-01-02' AND amount = 3500.00 AND vendor = 'WAPDA') OR
    (type = 'Expense' AND date = '2025-01-08' AND amount = 30000.00 AND vendor = 'Imam Muhammad Ali') OR
    (type = 'Expense' AND date = '2025-01-12' AND amount = 2500.00 AND vendor = 'Local Store') OR
    (type = 'Expense' AND date = '2025-01-20' AND amount = 1500.00 AND vendor = 'WASA');
