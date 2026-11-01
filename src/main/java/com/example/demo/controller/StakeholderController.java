package com.example.demo.controller;

import com.example.demo.exception.PharmacyBusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stakeholders")
public class StakeholderController {

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final DoctorRepository doctorRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineSupplierRepository medicineSupplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public StakeholderController(EmployeeRepository employeeRepository,
                                 RoleRepository roleRepository,
                                 SupplierRepository supplierRepository,
                                 CustomerRepository customerRepository,
                                 DoctorRepository doctorRepository,
                                 MedicineRepository medicineRepository,
                                 MedicineSupplierRepository medicineSupplierRepository,
                                 PurchaseOrderRepository purchaseOrderRepository) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.supplierRepository = supplierRepository;
        this.customerRepository = customerRepository;
        this.doctorRepository = doctorRepository;
        this.medicineRepository = medicineRepository;
        this.medicineSupplierRepository = medicineSupplierRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    // =========================================================
    // 1. EMPLOYEES
    // =========================================================
    @GetMapping("/employees")
    public ResponseEntity<List<Employee>> listEmployees() {
        return ResponseEntity.ok(employeeRepository.findAll());
    }

    @PostMapping("/employees")
    public ResponseEntity<?> createEmployee(@RequestBody Map<String, Object> payload) {
        String firstName = (String) payload.get("firstName");
        String lastName = (String) payload.get("lastName");
        String email = (String) payload.get("email");
        String phone = (String) payload.get("phone");
        String password = (String) payload.get("password");
        Object roleIdObj = payload.get("roleId");

        // E22: Missing required fields
        if (firstName == null || firstName.isBlank() || email == null || email.isBlank() || roleIdObj == null) {
            throw new PharmacyBusinessException("E22", "First name, email, and role are required fields.");
        }

        // E20: Invalid email format
        if (!email.contains("@") || !email.contains(".")) {
            throw new PharmacyBusinessException("E20", "Invalid email format: " + email);
        }

        // E14: Duplicate email
        if (employeeRepository.findByEmail(email.trim()).isPresent()) {
            throw new PharmacyBusinessException("E14", "An employee with email " + email + " already exists.");
        }

        Long roleId = Long.parseLong(roleIdObj.toString());
        // E18: Invalid role
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new PharmacyBusinessException("E18", "Role ID " + roleId + " does not exist."));

        Employee emp = new Employee();
        emp.setFirstName(firstName.trim());
        emp.setLastName(lastName != null ? lastName.trim() : "");
        emp.setEmail(email.trim());
        emp.setPhone(phone != null ? phone.trim() : null);
        emp.setPasswordHash(password != null && !password.isBlank() ? password : "password123");
        emp.setRole(role);
        emp.setIsActive(true);
        if (payload.get("salary") != null) {
            emp.setSalary(new BigDecimal(payload.get("salary").toString()));
        }
        if (payload.get("designation") != null) {
            emp.setDesignation((String) payload.get("designation"));
        }
        if (payload.get("workShift") != null) {
            emp.setWorkShift((String) payload.get("workShift"));
        }

        Employee saved = employeeRepository.save(emp);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/employees/{id}/toggle-active")
    public ResponseEntity<?> toggleEmployeeActive(@PathVariable("id") Long id) {
        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new PharmacyBusinessException("E40", "Employee not found with ID: " + id));

        // E19: Deactivating employee with open POs
        if (emp.getIsActive() != null && emp.getIsActive()) {
            List<PurchaseOrder> openPOs = purchaseOrderRepository.findByOrderStatusWithDetails("PENDING");
            boolean hasOpenPo = openPOs.stream().anyMatch(po -> po.getCreatedByEmployee().getEmployeeId().equals(id));
            if (hasOpenPo) {
                throw new PharmacyBusinessException("E19", "Cannot deactivate employee with open, pending purchase orders.");
            }
            emp.setIsActive(false);
        } else {
            emp.setIsActive(true);
        }

        return ResponseEntity.ok(employeeRepository.save(emp));
    }

    // =========================================================
    // 2. SUPPLIERS
    // =========================================================
    @GetMapping("/suppliers")
    public ResponseEntity<List<Supplier>> listSuppliers() {
        return ResponseEntity.ok(supplierRepository.findAll());
    }

    @PostMapping("/suppliers")
    public ResponseEntity<?> createSupplier(@RequestBody Supplier supplier) {
        if (supplier.getSupplierName() == null || supplier.getSupplierName().isBlank()) {
            throw new PharmacyBusinessException("E22", "Supplier name is required.");
        }

        // E16: Duplicate GST check
        if (supplier.getGstNumber() != null && !supplier.getGstNumber().isBlank()) {
            boolean exists = supplierRepository.findAll().stream()
                    .anyMatch(s -> supplier.getGstNumber().equalsIgnoreCase(s.getGstNumber()));
            if (exists) {
                throw new PharmacyBusinessException("E16", "Supplier with GST Number " + supplier.getGstNumber() + " already exists.");
            }
        }

        Supplier saved = supplierRepository.save(supplier);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // =========================================================
    // 3. DOCTORS
    // =========================================================
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> listDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }

    @PostMapping("/doctors")
    public ResponseEntity<?> createDoctor(@RequestBody Doctor doctor) {
        if (doctor.getName() == null || doctor.getName().isBlank() ||
            doctor.getMedicalLicenseNo() == null || doctor.getMedicalLicenseNo().isBlank()) {
            throw new PharmacyBusinessException("E22", "Doctor name and medical license number are required.");
        }

        // E15: Duplicate medical license
        boolean exists = doctorRepository.findAll().stream()
                .anyMatch(d -> doctor.getMedicalLicenseNo().equalsIgnoreCase(d.getMedicalLicenseNo()));
        if (exists) {
            throw new PharmacyBusinessException("E15", "A doctor with medical license " + doctor.getMedicalLicenseNo() + " already exists.");
        }

        Doctor saved = doctorRepository.save(doctor);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // =========================================================
    // 4. MEDICINE-SUPPLIER PRICING MAPPINGS
    // =========================================================
    @GetMapping("/medicine-suppliers")
    public ResponseEntity<List<MedicineSupplier>> listMedicineSuppliers() {
        return ResponseEntity.ok(medicineSupplierRepository.findAll());
    }

    @PostMapping("/medicine-suppliers")
    public ResponseEntity<?> saveMedicineSupplier(@RequestBody Map<String, Object> payload) {
        Long medId = Long.parseLong(payload.get("medicineId").toString());
        Long suppId = Long.parseLong(payload.get("supplierId").toString());
        BigDecimal price = new BigDecimal(payload.get("supplyPrice").toString());
        Integer leadTime = payload.get("leadTimeDays") != null ? Integer.parseInt(payload.get("leadTimeDays").toString()) : 3;

        Medicine med = medicineRepository.findById(medId)
                .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found: " + medId));
        Supplier supp = supplierRepository.findById(suppId)
                .orElseThrow(() -> new PharmacyBusinessException("E23", "Supplier not found: " + suppId));

        MedicineSupplier ms = new MedicineSupplier();
        ms.setMedicine(med);
        ms.setSupplier(supp);
        ms.setSupplyPrice(price);
        ms.setLeadTimeDays(leadTime);

        MedicineSupplier saved = medicineSupplierRepository.save(ms);
        return ResponseEntity.ok(saved);
    }
}
