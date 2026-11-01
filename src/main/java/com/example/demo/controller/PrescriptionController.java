package com.example.demo.controller;

import com.example.demo.dto.PrescriptionRequestDTO;
import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.dto.PrescriptionValidationDTO;
import com.example.demo.model.Doctor;
import com.example.demo.repository.DoctorRepository;
import com.example.demo.service.PrescriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for prescription management.
 *
 * Endpoints:
 *   POST /api/prescriptions                         → create a new prescription
 *   GET  /api/prescriptions/customer/{customerId}    → fetch prescriptions by customer
 *   GET  /api/prescriptions/validate                 → validate prescription for checkout
 *   GET  /api/doctors                                → list all registered doctors
 */
@RestController
@RequestMapping("/api")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final DoctorRepository doctorRepository;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  DoctorRepository doctorRepository) {
        this.prescriptionService = prescriptionService;
        this.doctorRepository = doctorRepository;
    }

    // =========================================================
    // 1. Create Prescription
    // =========================================================
    @PostMapping("/prescriptions")
    public ResponseEntity<?> createPrescription(@RequestBody PrescriptionRequestDTO request) {
        if (request.getCustomerId() == null || request.getDoctorId() == null
                || request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Customer, Doctor, and at least one medicine are required."));
        }
        try {
            PrescriptionResponseDTO response = prescriptionService.createPrescription(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // =========================================================
    // 2. Get Prescriptions by Customer
    // =========================================================
    @GetMapping("/prescriptions/customer/{customerId}")
    public ResponseEntity<List<PrescriptionResponseDTO>> getByCustomer(
            @PathVariable Long customerId) {
        return ResponseEntity.ok(prescriptionService.getPrescriptionsByCustomer(customerId));
    }

    // =========================================================
    // 3. Validate Prescription for Checkout
    // =========================================================
    @GetMapping("/prescriptions/validate")
    public ResponseEntity<PrescriptionValidationDTO> validate(
            @RequestParam("prescriptionId") Long prescriptionId,
            @RequestParam("medicineId") Long medicineId,
            @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        PrescriptionValidationDTO result = prescriptionService
                .validatePrescriptionForMedicine(prescriptionId, medicineId, quantity);
        return ResponseEntity.ok(result);
    }

    // =========================================================
    // 4. List All Doctors (for dropdowns)
    // =========================================================
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> listDoctors() {
        return ResponseEntity.ok(doctorRepository.findAll());
    }
}
