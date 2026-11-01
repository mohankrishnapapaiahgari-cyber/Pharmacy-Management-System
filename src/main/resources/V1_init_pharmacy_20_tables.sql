-- ======================================================================
-- Aruna Pharmacies Management System - Definitive 20-Table 3NF Schema
-- File: V1_init_pharmacy_20_tables.sql
-- ======================================================================

CREATE DATABASE IF NOT EXISTS PharmacyDB;
USE PharmacyDB;

-- Drop foreign keys and tables in reverse dependency order if recreating
SET FOREIGN_KEY_CHECKS = 0;

DROP TRIGGER IF EXISTS trg_check_stock_before_sale;
DROP VIEW IF EXISTS vw_medicine_stock_summary;

DROP TABLE IF EXISTS SALES_RETURN_ITEM;
DROP TABLE IF EXISTS SALES_RETURN;
DROP TABLE IF EXISTS PAYMENT;
DROP TABLE IF EXISTS INVOICE_ITEM;
DROP TABLE IF EXISTS INVOICE;
DROP TABLE IF EXISTS PRESCRIPTION_ITEM;
DROP TABLE IF EXISTS PRESCRIPTION;
DROP TABLE IF EXISTS PURCHASE_ORDER_ITEM;
DROP TABLE IF EXISTS PURCHASE_ORDER;
DROP TABLE IF EXISTS MEDICINE_SUPPLIER;
DROP TABLE IF EXISTS SUPPLIER;
DROP TABLE IF EXISTS EMPLOYEE;
DROP TABLE IF EXISTS DOCTOR;
DROP TABLE IF EXISTS CUSTOMER;
DROP TABLE IF EXISTS ROLE;
DROP TABLE IF EXISTS BATCH_INVENTORY;
DROP TABLE IF EXISTS MEDICINE;
DROP TABLE IF EXISTS MANUFACTURER;
DROP TABLE IF EXISTS SHELF_LOCATION;
DROP TABLE IF EXISTS CATEGORY;

SET FOREIGN_KEY_CHECKS = 1;

-- ======================================================================
-- MODULE 1 — Catalog & Inventory (5 tables)
-- ======================================================================

CREATE TABLE CATEGORY (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    requires_temperature_control BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB;

CREATE TABLE SHELF_LOCATION (
    location_id INT AUTO_INCREMENT PRIMARY KEY,
    temperature_zone VARCHAR(50),
    aisle_number INT,
    shelf_number INT,
    rack_code VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE MANUFACTURER (
    manufacturer_id INT AUTO_INCREMENT PRIMARY KEY,
    manufacturer_name VARCHAR(150) NOT NULL,
    license_number VARCHAR(50) UNIQUE,
    contact_no VARCHAR(15),
    email VARCHAR(100),
    street_address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(10)
) ENGINE=InnoDB;

CREATE TABLE MEDICINE (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    category_id INT,
    manufacturer_id INT,
    medicine_name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200),
    brand_name VARCHAR(200),
    dosage_form VARCHAR(50) NOT NULL,
    strength VARCHAR(50),
    schedule_type VARCHAR(20),
    unit_price DECIMAL(10,2) NOT NULL,
    reorder_level INT NOT NULL DEFAULT 10,
    reorder_quantity INT NOT NULL DEFAULT 50,
    is_active BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_medicine_category FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id) ON DELETE RESTRICT,
    CONSTRAINT fk_medicine_manufacturer FOREIGN KEY (manufacturer_id) REFERENCES MANUFACTURER(manufacturer_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE BATCH_INVENTORY (
    batch_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    location_id INT NULL,
    batch_number VARCHAR(50) NOT NULL,
    expiry_date DATE NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    purchase_unit_cost DECIMAL(10,2),
    mfg_date DATE,
    CONSTRAINT uq_medicine_batch UNIQUE (medicine_id, batch_number),
    CONSTRAINT fk_batch_medicine FOREIGN KEY (medicine_id) REFERENCES MEDICINE(medicine_id) ON DELETE RESTRICT,
    CONSTRAINT fk_batch_location FOREIGN KEY (location_id) REFERENCES SHELF_LOCATION(location_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ======================================================================
-- MODULE 2 — Stakeholders & Access Control (5 tables)
-- ======================================================================

CREATE TABLE ROLE (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE CUSTOMER (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(15) UNIQUE,
    email VARCHAR(100),
    gender VARCHAR(10),
    dob DATE,
    street_address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(10)
) ENGINE=InnoDB;

CREATE TABLE DOCTOR (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    medical_license_no VARCHAR(50) UNIQUE,
    phone VARCHAR(15),
    hospital_clinic VARCHAR(200),
    specialization VARCHAR(100)
) ENGINE=InnoDB;

CREATE TABLE EMPLOYEE (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    role_id INT NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    salary DECIMAL(10,2),
    joining_date DATE,
    phone VARCHAR(15),
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    designation VARCHAR(100),
    work_shift VARCHAR(50),
    CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES ROLE(role_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE SUPPLIER (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(150) NOT NULL,
    gst_number VARCHAR(20) UNIQUE,
    phone VARCHAR(15),
    email VARCHAR(100),
    contact_person VARCHAR(150),
    payment_terms VARCHAR(100),
    street_address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(10)
) ENGINE=InnoDB;

-- ======================================================================
-- MODULE 3 — Procurement (3 tables)
-- ======================================================================

CREATE TABLE MEDICINE_SUPPLIER (
    medicine_id INT NOT NULL,
    supplier_id INT NOT NULL,
    supply_price DECIMAL(10,2),
    lead_time_days INT,
    PRIMARY KEY (medicine_id, supplier_id),
    CONSTRAINT fk_medsupp_medicine FOREIGN KEY (medicine_id) REFERENCES MEDICINE(medicine_id) ON DELETE CASCADE,
    CONSTRAINT fk_medsupp_supplier FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(supplier_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE PURCHASE_ORDER (
    po_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    created_by_employee_id INT NOT NULL,
    order_date DATE NOT NULL,
    order_status VARCHAR(30) DEFAULT 'PENDING',
    expected_delivery_date DATE,
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(supplier_id) ON DELETE RESTRICT,
    CONSTRAINT fk_po_employee FOREIGN KEY (created_by_employee_id) REFERENCES EMPLOYEE(employee_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE PURCHASE_ORDER_ITEM (
    po_item_id INT AUTO_INCREMENT PRIMARY KEY,
    po_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_ordered INT NOT NULL,
    quantity_received INT DEFAULT 0,
    purchase_price DECIMAL(10,2),
    CONSTRAINT uq_po_medicine UNIQUE (po_id, medicine_id),
    CONSTRAINT fk_poi_po FOREIGN KEY (po_id) REFERENCES PURCHASE_ORDER(po_id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_medicine FOREIGN KEY (medicine_id) REFERENCES MEDICINE(medicine_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ======================================================================
-- MODULE 4 — Prescriptions & Billing (5 tables)
-- ======================================================================

CREATE TABLE PRESCRIPTION (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    doctor_id INT NOT NULL,
    prescription_date DATE NOT NULL,
    validity_period_days INT DEFAULT 30,
    diagnosis_notes TEXT,
    prescription_image_url VARCHAR(500),
    CONSTRAINT fk_prescription_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id) ON DELETE RESTRICT,
    CONSTRAINT fk_prescription_doctor FOREIGN KEY (doctor_id) REFERENCES DOCTOR(doctor_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE PRESCRIPTION_ITEM (
    prescription_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity_prescribed INT,
    dose_amount VARCHAR(50),
    frequency VARCHAR(50),
    timing VARCHAR(50),
    duration VARCHAR(50),
    PRIMARY KEY (prescription_id, medicine_id),
    CONSTRAINT fk_pi_prescription FOREIGN KEY (prescription_id) REFERENCES PRESCRIPTION(prescription_id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_medicine FOREIGN KEY (medicine_id) REFERENCES MEDICINE(medicine_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE INVOICE (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NULL,
    employee_id INT NOT NULL,
    prescription_id INT NULL,
    invoice_date DATE NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    payment_status VARCHAR(20) DEFAULT 'UNPAID',
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customer_id) REFERENCES CUSTOMER(customer_id) ON DELETE SET NULL,
    CONSTRAINT fk_invoice_employee FOREIGN KEY (employee_id) REFERENCES EMPLOYEE(employee_id) ON DELETE RESTRICT,
    CONSTRAINT fk_invoice_prescription FOREIGN KEY (prescription_id) REFERENCES PRESCRIPTION(prescription_id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE INVOICE_ITEM (
    invoice_item_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    batch_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_selling_price DECIMAL(10,2) NOT NULL,
    tax_rate DECIMAL(5,2) DEFAULT 0.00,
    CONSTRAINT uq_invoice_batch UNIQUE (invoice_id, batch_id),
    CONSTRAINT fk_ii_invoice FOREIGN KEY (invoice_id) REFERENCES INVOICE(invoice_id) ON DELETE CASCADE,
    CONSTRAINT fk_ii_batch FOREIGN KEY (batch_id) REFERENCES BATCH_INVENTORY(batch_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE PAYMENT (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    amount_paid DECIMAL(10,2) NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(30),
    transaction_reference VARCHAR(100),
    CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES INVOICE(invoice_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ======================================================================
-- MODULE 5 — Returns (2 tables)
-- ======================================================================

CREATE TABLE SALES_RETURN (
    return_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    processed_by_employee_id INT NOT NULL,
    return_date DATE NOT NULL,
    reason TEXT,
    CONSTRAINT fk_sr_invoice FOREIGN KEY (invoice_id) REFERENCES INVOICE(invoice_id) ON DELETE RESTRICT,
    CONSTRAINT fk_sr_employee FOREIGN KEY (processed_by_employee_id) REFERENCES EMPLOYEE(employee_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE SALES_RETURN_ITEM (
    return_id INT NOT NULL,
    invoice_item_id INT NOT NULL,
    quantity_returned INT NOT NULL,
    refund_price DECIMAL(10,2),
    disposition VARCHAR(30),
    PRIMARY KEY (return_id, invoice_item_id),
    CONSTRAINT fk_sri_return FOREIGN KEY (return_id) REFERENCES SALES_RETURN(return_id) ON DELETE CASCADE,
    CONSTRAINT fk_sri_invoice_item FOREIGN KEY (invoice_item_id) REFERENCES INVOICE_ITEM(invoice_item_id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ======================================================================
-- INDEXES FOR FREQUENTLY FILTERED COLUMNS
-- ======================================================================
CREATE INDEX idx_medicine_name ON MEDICINE(medicine_name);
CREATE INDEX idx_batch_expiry ON BATCH_INVENTORY(expiry_date);
CREATE INDEX idx_invoice_date ON INVOICE(invoice_date);

-- ======================================================================
-- DATABASE TRIGGER: Stock Check Before Sale
-- ======================================================================
DELIMITER //
CREATE TRIGGER trg_check_stock_before_sale
BEFORE INSERT ON INVOICE_ITEM
FOR EACH ROW
BEGIN
    DECLARE available_qty INT;
    SELECT quantity_in_stock INTO available_qty
    FROM BATCH_INVENTORY WHERE batch_id = NEW.batch_id;
    IF NEW.quantity > available_qty THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Insufficient stock for this batch';
    END IF;
END //
DELIMITER ;

-- ======================================================================
-- SQL VIEW: Stock Summary
-- ======================================================================
CREATE OR REPLACE VIEW vw_medicine_stock_summary AS
SELECT m.medicine_id, m.medicine_name, m.generic_name, c.category_name, m.reorder_level,
       COALESCE(SUM(b.quantity_in_stock), 0) AS total_available_stock,
       CASE 
           WHEN COALESCE(SUM(b.quantity_in_stock), 0) = 0 THEN 'OUT_OF_STOCK'
           WHEN COALESCE(SUM(b.quantity_in_stock), 0) <= m.reorder_level THEN 'LOW_STOCK'
           ELSE 'HEALTHY'
       END AS stock_status
FROM MEDICINE m
JOIN CATEGORY c ON m.category_id = c.category_id
LEFT JOIN BATCH_INVENTORY b ON m.medicine_id = b.medicine_id AND b.expiry_date >= CURDATE()
GROUP BY m.medicine_id, m.medicine_name, m.generic_name, c.category_name, m.reorder_level;
