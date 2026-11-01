-- ============================================================
-- Aruna Pharmacies — Seed Data for Prescription Compliance (Demo 2)
-- Complete & Fixed: Resolves foreign key defaults & Schedule H setup
-- ============================================================
USE PharmacyDB;

-- ------------------------------------------------------------
-- 0. Ensure Parent Master Data Exists (Avoid Foreign Key Errors)
-- ------------------------------------------------------------
INSERT INTO CATEGORY (category_id, category_name) 
VALUES (1, 'General Medicine')
ON DUPLICATE KEY UPDATE category_name = category_name;

INSERT INTO MANUFACTURER (manufacturer_id, manufacturer_name, license_number) 
VALUES (1, 'Apex Pharma', 'LIC-1001')
ON DUPLICATE KEY UPDATE manufacturer_name = manufacturer_name;

-- ------------------------------------------------------------
-- 1. Insert Sample Doctors
-- ------------------------------------------------------------
INSERT INTO DOCTOR (name, medical_license_no, phone, hospital_clinic, specialization)
SELECT 'Dr. Meera Sharma', 'MH-MED-20145', '9876543210', 'Apollo Clinic, Hyderabad', 'General Medicine'
WHERE NOT EXISTS (SELECT 1 FROM DOCTOR WHERE medical_license_no = 'MH-MED-20145');

INSERT INTO DOCTOR (name, medical_license_no, phone, hospital_clinic, specialization)
SELECT 'Dr. Rajesh Verma', 'KA-MED-31892', '9123456780', 'City Hospital, Bangalore', 'Pulmonology'
WHERE NOT EXISTS (SELECT 1 FROM DOCTOR WHERE medical_license_no = 'KA-MED-31892');

-- ------------------------------------------------------------
-- 2. Setup Schedule H Medicine (Amoxicillin)
-- ------------------------------------------------------------
-- Update if already present
UPDATE MEDICINE 
SET schedule_type = 'Schedule H' 
WHERE medicine_name LIKE '%Amoxicillin%';

-- Insert Amoxicillin with category_id and manufacturer_id
INSERT INTO MEDICINE (category_id, manufacturer_id, medicine_name, generic_name, unit_price, schedule_type, reorder_level)
SELECT 1, 1, 'Amoxicillin 250mg', 'Amoxicillin', 45.00, 'Schedule H', 50
WHERE NOT EXISTS (SELECT 1 FROM MEDICINE WHERE medicine_name = 'Amoxicillin 250mg');

-- Insert stock batch for Amoxicillin
INSERT INTO BATCH_INVENTORY (medicine_id, batch_number, expiry_date, quantity_in_stock)
SELECT m.medicine_id, 'AMOX-BATCH-001', DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 100
FROM MEDICINE m
WHERE m.medicine_name = 'Amoxicillin 250mg'
AND NOT EXISTS (
    SELECT 1 FROM BATCH_INVENTORY b WHERE b.medicine_id = m.medicine_id AND b.batch_number = 'AMOX-BATCH-001'
);

-- ------------------------------------------------------------
-- 3. Setup OTC Comparison Medicine (Paracetamol)
-- ------------------------------------------------------------
UPDATE MEDICINE 
SET schedule_type = 'OTC' 
WHERE medicine_name LIKE '%Paracetamol%' AND (schedule_type IS NULL OR schedule_type = '');

INSERT INTO MEDICINE (category_id, manufacturer_id, medicine_name, generic_name, unit_price, schedule_type, reorder_level)
SELECT 1, 1, 'Paracetamol 500mg', 'Acetaminophen', 15.00, 'OTC', 100
WHERE NOT EXISTS (SELECT 1 FROM MEDICINE WHERE medicine_name = 'Paracetamol 500mg');

INSERT INTO BATCH_INVENTORY (medicine_id, batch_number, expiry_date, quantity_in_stock)
SELECT m.medicine_id, 'PARA-BATCH-001', DATE_ADD(CURDATE(), INTERVAL 12 MONTH), 500
FROM MEDICINE m
WHERE m.medicine_name = 'Paracetamol 500mg'
AND NOT EXISTS (
    SELECT 1 FROM BATCH_INVENTORY b WHERE b.medicine_id = m.medicine_id AND b.batch_number = 'PARA-BATCH-001'
);

-- ------------------------------------------------------------
-- 4. Insert Sample Customer
-- ------------------------------------------------------------
INSERT INTO CUSTOMER (full_name, phone)
SELECT 'Suresh Kumar', '9988776655'
WHERE NOT EXISTS (SELECT 1 FROM CUSTOMER WHERE phone = '9988776655');

-- ------------------------------------------------------------
-- 5. Insert Valid Prescription for Customer
-- ------------------------------------------------------------
INSERT INTO PRESCRIPTION (customer_id, doctor_id, prescription_date, validity_period_days, diagnosis_notes)
SELECT c.customer_id, d.doctor_id, CURDATE(), 30, 'Upper respiratory tract infection. Prescribed Amoxicillin course.'
FROM CUSTOMER c, DOCTOR d
WHERE c.phone = '9988776655' AND d.medical_license_no = 'MH-MED-20145'
AND NOT EXISTS (
    SELECT 1 FROM PRESCRIPTION p 
    WHERE p.customer_id = c.customer_id 
      AND p.doctor_id = d.doctor_id
      AND p.prescription_date = CURDATE()
);

-- ------------------------------------------------------------
-- 6. Link Prescription to Amoxicillin
-- ------------------------------------------------------------
INSERT INTO PRESCRIPTION_ITEM (prescription_id, medicine_id, quantity_prescribed, dose_amount, frequency, timing, duration)
SELECT p.prescription_id, m.medicine_id, 21, '250mg', 'Three times a day', 'After meals', '7 days'
FROM PRESCRIPTION p
JOIN CUSTOMER c ON p.customer_id = c.customer_id
JOIN DOCTOR d ON p.doctor_id = d.doctor_id
JOIN MEDICINE m ON m.medicine_name = 'Amoxicillin 250mg'
WHERE c.phone = '9988776655' AND d.medical_license_no = 'MH-MED-20145'
AND NOT EXISTS (
    SELECT 1 FROM PRESCRIPTION_ITEM pi 
    WHERE pi.prescription_id = p.prescription_id AND pi.medicine_id = m.medicine_id
);

-- ------------------------------------------------------------
-- 7. Verification Queries (Confirm Everything is Active)
-- ------------------------------------------------------------
SELECT '--- DOCTORS ---' AS section;
SELECT * FROM DOCTOR;

SELECT '--- SCHEDULE H MEDICINES ---' AS section;
SELECT medicine_id, medicine_name, schedule_type, unit_price FROM MEDICINE WHERE schedule_type = 'Schedule H';

SELECT '--- SAMPLE PRESCRIPTIONS ---' AS section;
SELECT p.prescription_id, c.full_name AS patient_name, d.name AS doctor_name, 
       p.prescription_date, p.validity_period_days, 
       DATE_ADD(p.prescription_date, INTERVAL p.validity_period_days DAY) AS expires_on
FROM PRESCRIPTION p
JOIN CUSTOMER c ON p.customer_id = c.customer_id
JOIN DOCTOR d ON p.doctor_id = d.doctor_id;

SELECT '--- PRESCRIPTION ITEMS ---' AS section;
SELECT pi.prescription_id, m.medicine_name, pi.quantity_prescribed, pi.dose_amount, pi.frequency, pi.timing, pi.duration
FROM PRESCRIPTION_ITEM pi
JOIN MEDICINE m ON pi.medicine_id = m.medicine_id;