-- ============================================================
-- Seed: Procurement & Stock Intake (Phase 3)
-- Run AFTER seed_auth_data.sql and seed_prescription_data.sql
-- ============================================================

-- SHELF LOCATIONS
INSERT IGNORE INTO SHELF_LOCATION (location_id, temperature_zone, aisle_number, shelf_number, rack_code) VALUES
(1, 'Ambient',      'A', '1', 'A1-01'),
(2, 'Ambient',      'A', '2', 'A2-01'),
(3, 'Refrigerated', 'B', '1', 'B1-01');

-- SUPPLIERS
INSERT IGNORE INTO SUPPLIER (supplier_id, supplier_name, gst_number, phone, email, address, contact_person, payment_terms) VALUES
(1, 'MedLine Pharma Distributors', 'GST27ABCDE1234F1Z5', '9876543210', 'orders@medline.com',
   '45, Industrial Area, Hyderabad', 'Ravi Kumar', 'Net 30'),
(2, 'SunPharma Wholesale', 'GST27XYZAB5678G2Z9', '9988776655', 'supply@sunpharma-ws.com',
   '12, Pharma Hub, Bengaluru', 'Priya Reddy', 'Net 15');

-- MEDICINE–SUPPLIER LINKS (medicine_id 1 and 2 assumed from existing seed)
INSERT IGNORE INTO MEDICINE_SUPPLIER (medicine_id, supplier_id, supply_price, lead_time_days) VALUES
(1, 1, 45.00, 3),
(2, 1, 120.00, 5),
(1, 2, 42.50, 2);

-- PURCHASE ORDER (PENDING) — created by employee_id 2 (Pharmacist from auth seed)
INSERT IGNORE INTO PURCHASE_ORDER (po_id, supplier_id, created_by_employee_id, order_date, order_status, expected_delivery_date) VALUES
(1, 1, 2, CURDATE(), 'PENDING', DATE_ADD(CURDATE(), INTERVAL 5 DAY));

-- PO ITEMS
INSERT IGNORE INTO PURCHASE_ORDER_ITEM (po_id, medicine_id, quantity_ordered, quantity_received, purchase_price) VALUES
(1, 1, 200, 0, 45.00),
(1, 2, 100, 0, 120.00);
