-- V2__seed_data.sql

-- Diagnostic Centres
INSERT INTO diagnostic_centres (id, name, location, created_at, updated_at) VALUES
(1, 'HealthFirst Diagnostics', 'Mumbai, Maharashtra', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'MedLab Plus', 'Delhi, NCR', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'CityHealth Labs', 'Bangalore, Karnataka', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Diagnostic Tests
INSERT INTO diagnostic_tests (id, name, description, created_at) VALUES
(1, 'Complete Blood Count (CBC)', 'Measures different components of blood including RBC, WBC, and platelets', CURRENT_TIMESTAMP),
(2, 'Lipid Profile', 'Measures cholesterol and triglyceride levels', CURRENT_TIMESTAMP),
(3, 'Thyroid Panel (TSH)', 'Measures thyroid-stimulating hormone levels', CURRENT_TIMESTAMP),
(4, 'Blood Glucose Fasting', 'Measures blood sugar levels after an overnight fast', CURRENT_TIMESTAMP),
(5, 'Liver Function Test (LFT)', 'Evaluates liver health through enzyme and protein levels', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Centre-Test Offerings (Overlapping tests with centre-specific pricing)
INSERT INTO centre_tests (id, centre_id, test_id, price, created_at) VALUES
-- HealthFirst Diagnostics (Centre 1)
(1, 1, 1, 500.00, CURRENT_TIMESTAMP),
(2, 1, 2, 800.00, CURRENT_TIMESTAMP),
(3, 1, 3, 600.00, CURRENT_TIMESTAMP),
(4, 1, 4, 200.00, CURRENT_TIMESTAMP),
-- MedLab Plus (Centre 2: CBC is 450, Lipid is 750, Glucose is 180, LFT is 900)
(5, 2, 1, 450.00, CURRENT_TIMESTAMP),
(6, 2, 2, 750.00, CURRENT_TIMESTAMP),
(7, 2, 4, 180.00, CURRENT_TIMESTAMP),
(8, 2, 5, 900.00, CURRENT_TIMESTAMP),
-- CityHealth Labs (Centre 3: CBC is 550, TSH is 650, LFT is 850)
(9, 3, 1, 550.00, CURRENT_TIMESTAMP),
(10, 3, 3, 650.00, CURRENT_TIMESTAMP),
(11, 3, 5, 850.00, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Reset sequence counters if postgres
SELECT setval('diagnostic_centres_id_seq', (SELECT MAX(id) FROM diagnostic_centres));
SELECT setval('diagnostic_tests_id_seq', (SELECT MAX(id) FROM diagnostic_tests));
SELECT setval('centre_tests_id_seq', (SELECT MAX(id) FROM centre_tests));
