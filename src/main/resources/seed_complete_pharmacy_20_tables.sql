-- ======================================================================
-- Aruna Pharmacies Management System - Definitive Master Seed Data
-- File: seed_complete_pharmacy_20_tables.sql
-- ======================================================================

USE PharmacyDB;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE SALES_RETURN_ITEM;
TRUNCATE TABLE SALES_RETURN;
TRUNCATE TABLE PAYMENT;
TRUNCATE TABLE INVOICE_ITEM;
TRUNCATE TABLE INVOICE;
TRUNCATE TABLE PRESCRIPTION_ITEM;
TRUNCATE TABLE PRESCRIPTION;
TRUNCATE TABLE PURCHASE_ORDER_ITEM;
TRUNCATE TABLE PURCHASE_ORDER;
TRUNCATE TABLE MEDICINE_SUPPLIER;
TRUNCATE TABLE SUPPLIER;
TRUNCATE TABLE EMPLOYEE;
TRUNCATE TABLE DOCTOR;
TRUNCATE TABLE CUSTOMER;
TRUNCATE TABLE ROLE;
TRUNCATE TABLE BATCH_INVENTORY;
TRUNCATE TABLE MEDICINE;
TRUNCATE TABLE MANUFACTURER;
TRUNCATE TABLE SHELF_LOCATION;
TRUNCATE TABLE CATEGORY;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------------------------------------------------
-- 1. ROLE (3 Core Roles)
-- ----------------------------------------------------------------------
INSERT INTO ROLE (role_id, role_name) VALUES
(1, 'Owner'),
(2, 'Pharmacist'),
(3, 'Cashier');

-- ----------------------------------------------------------------------
-- 2. EMPLOYEE (3 Staff Members with verified credentials)
--    owner@arunapharmacy.com / owner123
--    pharmacist@arunapharmacy.com / pharma123
--    cashier@arunapharmacy.com / cash123
-- ----------------------------------------------------------------------
INSERT INTO EMPLOYEE (employee_id, role_id, first_name, last_name, salary, joining_date, phone, email, password_hash, is_active, designation, work_shift) VALUES
(1, 1, 'Aruna', 'Devi', 95000.00, '2023-01-10', '9876500001', 'owner@arunapharmacy.com', 'owner123', TRUE, 'Chief Pharmacy Officer / Owner', 'Morning'),
(2, 2, 'Ramesh', 'Varma', 60000.00, '2023-06-15', '9876500002', 'pharmacist@arunapharmacy.com', 'pharma123', TRUE, 'Registered Lead Pharmacist', 'Day'),
(3, 3, 'Suresh', 'Reddy', 35000.00, '2024-01-05', '9876500003', 'cashier@arunapharmacy.com', 'cash123', TRUE, 'Front-Desk Billing Specialist', 'Evening');

-- ----------------------------------------------------------------------
-- 3. CATEGORY (4 Categories)
-- ----------------------------------------------------------------------
INSERT INTO CATEGORY (category_id, category_name, description, requires_temperature_control) VALUES
(1, 'Antibiotics & Anti-Infectives', 'Prescription antibiotics, antibacterials, and antimicrobials', FALSE),
(2, 'Analgesics & Antipyretics', 'Pain relievers, anti-inflammatory drugs, and fever reducers', FALSE),
(3, 'Cardiovascular & Cold Chain', 'Cardiovascular medications, insulins, and temperature-sensitive biologics', TRUE),
(4, 'General Medicine & OTC', 'Over-the-counter and general healthcare essentials', FALSE);

-- ----------------------------------------------------------------------
-- 4. SHELF_LOCATION (4 Verified Physical Locations)
-- ----------------------------------------------------------------------
INSERT INTO SHELF_LOCATION (location_id, temperature_zone, aisle_number, shelf_number, rack_code) VALUES
(1, 'Room Temperature (15-25 C)', 1, 2, 'A1-S2'),
(2, 'Room Temperature (15-25 C)', 1, 4, 'A1-S4'),
(3, 'Cold Chain Refrigerator (2-8 C)', 2, 1, 'FRIDGE-01'),
(4, 'Controlled Narcotics Locker', 3, 1, 'LOCKER-N1');

-- ----------------------------------------------------------------------
-- 5. MANUFACTURER (4 Manufacturers)
-- ----------------------------------------------------------------------
INSERT INTO MANUFACTURER (manufacturer_id, manufacturer_name, license_number, contact_no, email, street_address, city, state, postal_code) VALUES
(1, 'Cipla Therapeutics Ltd', 'MFG-CIP-2021-098', '022-24828000', 'contact@cipla.com', 'Cipla House, Peninsula Business Park, Lower Parel', 'Mumbai', 'Maharashtra', '400013'),
(2, 'Sun Pharma Industries', 'MFG-SUN-2019-441', '022-43244324', 'info@sunpharma.com', 'Sun House, Plot No. 201 B/1, Western Express Highway', 'Mumbai', 'Maharashtra', '400063'),
(3, 'Dr. Reddys Laboratories', 'MFG-DRR-2020-782', '040-49002900', 'support@drreddys.com', '8-2-337, Road No. 3, Banjara Hills', 'Hyderabad', 'Telangana', '500034'),
(4, 'Apex Pharma', 'LIC-1001', '020-27128000', 'info@apexpharma.in', 'Plot 10, MIDC Bhosari', 'Pune', 'Maharashtra', '411026');

-- ----------------------------------------------------------------------
-- 6. MEDICINE (7 Medicines across Schedule H and OTC)
-- ----------------------------------------------------------------------
INSERT INTO MEDICINE (medicine_id, category_id, manufacturer_id, medicine_name, generic_name, brand_name, dosage_form, strength, schedule_type, unit_price, reorder_level, reorder_quantity, is_active) VALUES
(1, 1, 1, 'Amoxicillin & Clavulanate 625mg', 'Amoxicillin + Potassium Clavulanate', 'Augmentin 625 Duo', 'Tablet', '625mg', 'Schedule H', 22.50, 20, 100, TRUE),
(2, 2, 2, 'Paracetamol 650mg Fast-Action', 'Paracetamol / Acetaminophen', 'Dolo 650', 'Tablet', '650mg', 'OTC', 3.50, 50, 200, TRUE),
(3, 1, 3, 'Azithromycin 500mg Once-Daily', 'Azithromycin Dihydrate', 'Azithral 500', 'Tablet', '500mg', 'Schedule H', 28.00, 15, 60, TRUE),
(4, 2, 1, 'Ibuprofen & Paracetamol Suspension', 'Ibuprofen + Paracetamol', 'Combiflam Plus', 'Syrup', '100ml', 'OTC', 45.00, 10, 40, TRUE),
(5, 3, 3, 'Atorvastatin Calcium 20mg', 'Atorvastatin', 'Atorva 20', 'Tablet', '20mg', 'Schedule H', 18.00, 25, 80, TRUE),
(6, 1, 4, 'Amoxicillin 250mg', 'Amoxicillin', 'Amoxil 250', 'Capsule', '250mg', 'Schedule H', 45.00, 50, 100, TRUE),
(7, 2, 4, 'Paracetamol 500mg', 'Acetaminophen', 'Calpol 500', 'Tablet', '500mg', 'OTC', 15.00, 100, 200, TRUE);

-- ----------------------------------------------------------------------
-- 7. BATCH_INVENTORY (FEFO Batches: Expiring, Low-Stock, and Healthy)
-- ----------------------------------------------------------------------
INSERT INTO BATCH_INVENTORY (batch_id, medicine_id, location_id, batch_number, expiry_date, quantity_in_stock, purchase_unit_cost, mfg_date) VALUES
(1, 1, 1, 'AMX-2024-001', DATE_ADD(CURDATE(), INTERVAL 25 DAY),  15, 14.00, DATE_SUB(CURDATE(), INTERVAL 300 DAY)),
(2, 1, 1, 'AMX-2024-002', DATE_ADD(CURDATE(), INTERVAL 180 DAY), 80, 14.50, DATE_SUB(CURDATE(), INTERVAL 120 DAY)),
(3, 2, 2, 'DOL-2024-A1',  DATE_ADD(CURDATE(), INTERVAL 45 DAY),  120,  2.10, DATE_SUB(CURDATE(), INTERVAL 200 DAY)),
(4, 2, 2, 'DOL-2024-B2',  DATE_ADD(CURDATE(), INTERVAL 365 DAY), 300,  2.20, DATE_SUB(CURDATE(), INTERVAL 60 DAY)),
(5, 3, 1, 'AZI-2024-99',  DATE_ADD(CURDATE(), INTERVAL 50 DAY),   12, 18.00, DATE_SUB(CURDATE(), INTERVAL 250 DAY)),
(6, 4, 2, 'CMB-2024-S1',  DATE_ADD(CURDATE(), INTERVAL 400 DAY),  35, 28.00, DATE_SUB(CURDATE(), INTERVAL 90 DAY)),
(7, 5, 3, 'ATV-2024-C1',  DATE_ADD(CURDATE(), INTERVAL 75 DAY),   18, 11.50, DATE_SUB(CURDATE(), INTERVAL 180 DAY)),
(8, 6, 1, 'AMOX-BATCH-001', DATE_ADD(CURDATE(), INTERVAL 180 DAY), 100, 30.00, DATE_SUB(CURDATE(), INTERVAL 60 DAY)),
(9, 7, 2, 'PARA-BATCH-001', DATE_ADD(CURDATE(), INTERVAL 365 DAY), 500, 10.00, DATE_SUB(CURDATE(), INTERVAL 30 DAY));

-- ----------------------------------------------------------------------
-- 8. CUSTOMER (3 Registered Customers)
-- ----------------------------------------------------------------------
INSERT INTO CUSTOMER (customer_id, full_name, phone, email, gender, dob, street_address, city, state, postal_code) VALUES
(1, 'Venkatesh Rao', '9848012345', 'venkatesh.rao@gmail.com', 'Male', '1982-04-12', 'Flat 402, Sai Balaji Residency, Madhapur', 'Hyderabad', 'Telangana', '500081'),
(2, 'Ananya Sharma', '9848098765', 'ananya.sharma@yahoo.com', 'Female', '1991-09-28', 'House 12-4/A, Jubilee Enclave, Kondapur', 'Hyderabad', 'Telangana', '500084'),
(3, 'Suresh Kumar', '9988776655', 'suresh.kumar@gmail.com', 'Male', '1985-07-15', '14-2-108, Jubilee Hills', 'Hyderabad', 'Telangana', '500033');

-- ----------------------------------------------------------------------
-- 9. DOCTOR (4 Registered Doctors)
-- ----------------------------------------------------------------------
INSERT INTO DOCTOR (doctor_id, name, medical_license_no, phone, hospital_clinic, specialization) VALUES
(1, 'Dr. K. S. Murthy, MD', 'TSMC-MED-2012-8841', '9849011223', 'Apollo Hospitals, Jubilee Hills', 'Internal Medicine & Critical Care'),
(2, 'Dr. Radhika Sen, MS', 'TSMC-MED-2016-5519', '9849033445', 'Care Outpatient Clinic, Hitec City', 'Pulmonology & General Physician'),
(3, 'Dr. Meera Sharma', 'MH-MED-20145', '9876543210', 'Apollo Clinic, Hyderabad', 'General Medicine'),
(4, 'Dr. Rajesh Verma', 'KA-MED-31892', '9123456780', 'City Hospital, Bangalore', 'Pulmonology');

-- ----------------------------------------------------------------------
-- 10. SUPPLIER (4 Approved Suppliers)
-- ----------------------------------------------------------------------
INSERT INTO SUPPLIER (supplier_id, supplier_name, gst_number, phone, email, contact_person, payment_terms, street_address, city, state, postal_code) VALUES
(1, 'Sri Balaji Pharma Distributors', '36AABCS1429B1Z8', '040-27618999', 'orders@balajipharma.in', 'Naveen Kumar', 'Net 30 Days', 'Shop 14-16, Wholesale Medicine Complex, Koti', 'Hyderabad', 'Telangana', '500095'),
(2, 'MedSource India Logistics', '36AAECM8921K1ZW', '040-66778899', 'dispatch@medsource.co.in', 'Pooja Hegde', 'Net 15 Days', 'Plot 88, IDA Mallapur, Nacharam Industrial Area', 'Hyderabad', 'Telangana', '500076'),
(3, 'MedLine Pharma Distributors', 'GST27ABCDE1234F1Z5', '9876543210', 'orders@medline.com', 'Ravi Kumar', 'Net 30 Days', '45, Industrial Area, Hyderabad', 'Hyderabad', 'Telangana', '500051'),
(4, 'SunPharma Wholesale', 'GST27XYZAB5678G2Z9', '9988776655', 'supply@sunpharma-ws.com', 'Priya Reddy', 'Net 15 Days', '12, Pharma Hub, Bengaluru', 'Bengaluru', 'Karnataka', '560001');

-- ----------------------------------------------------------------------
-- 11. MEDICINE_SUPPLIER (Supplier Pricing & Lead Times)
-- ----------------------------------------------------------------------
INSERT INTO MEDICINE_SUPPLIER (medicine_id, supplier_id, supply_price, lead_time_days) VALUES
(1, 1, 14.50, 2),
(2, 1,  2.15, 1),
(3, 1, 18.00, 3),
(4, 2, 28.00, 2),
(5, 2, 11.50, 4),
(1, 3, 45.00, 3),
(2, 3, 120.00, 5),
(1, 4, 42.50, 2),
(6, 3, 30.00, 3),
(7, 4, 10.00, 2);

-- ----------------------------------------------------------------------
-- 12. PURCHASE_ORDER (2 Pending Orders across Employees)
-- ----------------------------------------------------------------------
INSERT INTO PURCHASE_ORDER (po_id, supplier_id, created_by_employee_id, order_date, order_status, expected_delivery_date) VALUES
(1, 1, 1, CURDATE(), 'PENDING', DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(2, 3, 2, CURDATE(), 'PENDING', DATE_ADD(CURDATE(), INTERVAL 5 DAY));

-- ----------------------------------------------------------------------
-- 13. PURCHASE_ORDER_ITEM (Items for PO 1 and PO 2)
-- ----------------------------------------------------------------------
INSERT INTO PURCHASE_ORDER_ITEM (po_item_id, po_id, medicine_id, quantity_ordered, quantity_received, purchase_price) VALUES
(1, 1, 1, 100, 0, 14.50),
(2, 1, 2, 200, 0,  2.15),
(3, 2, 1, 200, 0, 45.00),
(4, 2, 2, 100, 0, 120.00);

-- ----------------------------------------------------------------------
-- 14. PRESCRIPTION (2 Active Prescriptions)
-- ----------------------------------------------------------------------
INSERT INTO PRESCRIPTION (prescription_id, customer_id, doctor_id, prescription_date, validity_period_days, diagnosis_notes, prescription_image_url) VALUES
(1, 1, 1, CURDATE(), 30, 'Acute bacterial sinusitis with mild fever. Prescribed 5-day course.', 'https://assets.arunapharmacy.com/rx/rx_demo_001.png'),
(2, 3, 3, CURDATE(), 30, 'Upper respiratory tract infection. Prescribed Amoxicillin course.', 'https://assets.arunapharmacy.com/rx/rx_demo_002.png');

-- ----------------------------------------------------------------------
-- 15. PRESCRIPTION_ITEM (Prescribed Medicines for Rx 1 and Rx 2)
-- ----------------------------------------------------------------------
INSERT INTO PRESCRIPTION_ITEM (prescription_id, medicine_id, quantity_prescribed, dose_amount, frequency, timing, duration) VALUES
(1, 1, 10, '1 Tablet', 'Twice daily (BD)', 'After Food', '5 Days'),
(1, 2, 10, '1 Tablet', 'SOS when fever > 100 F', 'After Food', '3 Days'),
(2, 6, 21, '250mg', 'Three times a day', 'After meals', '7 days');

-- ----------------------------------------------------------------------
-- 16. INVOICE (1 Completed Sample Invoice)
-- ----------------------------------------------------------------------
INSERT INTO INVOICE (invoice_id, customer_id, employee_id, prescription_id, invoice_date, discount_amount, payment_status) VALUES
(1, 1, 3, 1, CURDATE(), 0.00, 'PAID');

-- ----------------------------------------------------------------------
-- 17. INVOICE_ITEM (Sold Items on Invoice 1)
-- ----------------------------------------------------------------------
INSERT INTO INVOICE_ITEM (invoice_item_id, invoice_id, batch_id, quantity, unit_selling_price, tax_rate) VALUES
(1, 1, 1, 4, 22.50, 12.00),
(2, 1, 3, 6,  3.50, 12.00);

-- ----------------------------------------------------------------------
-- 18. PAYMENT (Settlement for Invoice 1)
-- ----------------------------------------------------------------------
-- Line 1: 4 * 22.50 = 90.00 + 12% tax (10.80) = 100.80
-- Line 2: 6 * 3.50  = 21.00 + 12% tax (2.52)  = 23.52
-- Total = 124.32
INSERT INTO PAYMENT (payment_id, invoice_id, amount_paid, payment_date, payment_method, transaction_reference) VALUES
(1, 1, 124.32, CURDATE(), 'UPI', 'UPI/20241001/99812489');

-- ----------------------------------------------------------------------
-- 19. SALES_RETURN (Sample Completed Return)
-- ----------------------------------------------------------------------
INSERT INTO SALES_RETURN (return_id, invoice_id, processed_by_employee_id, return_date, reason) VALUES
(1, 1, 2, CURDATE(), 'Patient recovered early; returning unused unopened tablets');

-- ----------------------------------------------------------------------
-- 20. SALES_RETURN_ITEM (Return line linking to invoice_item_id)
-- ----------------------------------------------------------------------
-- 2 units of Paracetamol returned at refund_price 3.92 (3.50 + 12% GST)
INSERT INTO SALES_RETURN_ITEM (return_id, invoice_item_id, quantity_returned, refund_price, disposition) VALUES
(1, 2, 2, 3.92, 'RETURNED_TO_STOCK');

COMMIT;
