
USE PharmacyDB;
-- ============================================================
-- Aruna Pharmacies — Seed Data for Authentication & RBAC
-- Run this AFTER Spring Boot creates the tables (ddl-auto=update)
-- ============================================================

-- 1. Insert Roles
INSERT INTO ROLE (role_name) VALUES ('Owner')       ON DUPLICATE KEY UPDATE role_name = role_name;
INSERT INTO ROLE (role_name) VALUES ('Pharmacist')  ON DUPLICATE KEY UPDATE role_name = role_name;
INSERT INTO ROLE (role_name) VALUES ('Cashier')     ON DUPLICATE KEY UPDATE role_name = role_name;

-- 2. Insert Demo Employees
--    Passwords are plain text for demo. In production, use BCrypt hashes.

INSERT INTO EMPLOYEE (role_id, first_name, last_name, email, password_hash, is_active)
SELECT r.role_id, 'Aruna', 'Kumari', 'owner@arunapharma.com', 'owner123', TRUE
FROM ROLE r WHERE r.role_name = 'Owner'
AND NOT EXISTS (SELECT 1 FROM EMPLOYEE WHERE email = 'owner@arunapharma.com');

INSERT INTO EMPLOYEE (role_id, first_name, last_name, email, password_hash, is_active)
SELECT r.role_id, 'Ravi', 'Shankar', 'pharmacist@arunapharma.com', 'pharma123', TRUE
FROM ROLE r WHERE r.role_name = 'Pharmacist'
AND NOT EXISTS (SELECT 1 FROM EMPLOYEE WHERE email = 'pharmacist@arunapharma.com');

INSERT INTO EMPLOYEE (role_id, first_name, last_name, email, password_hash, is_active)
SELECT r.role_id, 'Priya', 'Nair', 'cashier@arunapharma.com', 'cash123', TRUE
FROM ROLE r WHERE r.role_name = 'Cashier'
AND NOT EXISTS (SELECT 1 FROM EMPLOYEE WHERE email = 'cashier@arunapharma.com');

-- Verify
SELECT e.employee_id, e.first_name, e.last_name, e.email, r.role_name, e.is_active
FROM EMPLOYEE e JOIN ROLE r ON e.role_id = r.role_id;
