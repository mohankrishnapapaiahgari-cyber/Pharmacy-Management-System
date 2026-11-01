package com.example.demo.service;

import com.example.demo.dto.PrescriptionRequestDTO;
import com.example.demo.dto.PrescriptionRequestDTO.PrescriptionItemRequestDTO;
import com.example.demo.dto.PrescriptionResponseDTO;
import com.example.demo.dto.PrescriptionResponseDTO.PrescriptionItemDTO;
import com.example.demo.dto.PrescriptionValidationDTO;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository prescriptionItemRepository;
    private final DoctorRepository doctorRepository;
    private final CustomerRepository customerRepository;
    private final MedicineRepository medicineRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               PrescriptionItemRepository prescriptionItemRepository,
                               DoctorRepository doctorRepository,
                               CustomerRepository customerRepository,
                               MedicineRepository medicineRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionItemRepository = prescriptionItemRepository;
        this.doctorRepository = doctorRepository;
        this.customerRepository = customerRepository;
        this.medicineRepository = medicineRepository;
    }

    // =========================================================
    // CREATE PRESCRIPTION (atomic)
    // =========================================================

    @Transactional
    public PrescriptionResponseDTO createPrescription(PrescriptionRequestDTO request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + request.getCustomerId()));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + request.getDoctorId()));

        // Create prescription header
        Prescription prescription = new Prescription();
        prescription.setCustomer(customer);
        prescription.setDoctor(doctor);
        prescription.setPrescriptionDate(
                request.getPrescriptionDate() != null ? request.getPrescriptionDate() : LocalDate.now());
        prescription.setValidityPeriodDays(
                request.getValidityPeriodDays() != null ? request.getValidityPeriodDays() : 30);
        prescription.setDiagnosisNotes(request.getDiagnosisNotes());
        prescription = prescriptionRepository.save(prescription);

        // Create line items
        List<PrescriptionItemDTO> itemDTOs = new ArrayList<>();
        for (PrescriptionItemRequestDTO itemReq : request.getItems()) {
            Medicine medicine = medicineRepository.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> new RuntimeException("Medicine not found with ID: " + itemReq.getMedicineId()));

            PrescriptionItem item = new PrescriptionItem();
            item.setPrescription(prescription);
            item.setMedicine(medicine);
            item.setQuantityPrescribed(itemReq.getQuantityPrescribed());
            item.setDoseAmount(itemReq.getDoseAmount());
            item.setFrequency(itemReq.getFrequency());
            item.setTiming(itemReq.getTiming());
            item.setDuration(itemReq.getDuration());
            prescriptionItemRepository.save(item);

            PrescriptionItemDTO dto = new PrescriptionItemDTO();
            dto.setMedicineId(medicine.getMedicineId());
            dto.setMedicineName(medicine.getMedicineName());
            dto.setQuantityPrescribed(itemReq.getQuantityPrescribed());
            dto.setDoseAmount(itemReq.getDoseAmount());
            dto.setFrequency(itemReq.getFrequency());
            dto.setTiming(itemReq.getTiming());
            dto.setDuration(itemReq.getDuration());
            itemDTOs.add(dto);
        }

        return buildResponseDTO(prescription, customer, doctor, itemDTOs);
    }

    // =========================================================
    // GET PRESCRIPTIONS BY CUSTOMER
    // =========================================================

    @Transactional(readOnly = true)
    public List<PrescriptionResponseDTO> getPrescriptionsByCustomer(Long customerId) {
        List<Prescription> prescriptions = prescriptionRepository.findByCustomerIdWithDetails(customerId);

        return prescriptions.stream().map(p -> {
            List<PrescriptionItem> items = prescriptionItemRepository
                    .findByPrescriptionIdWithMedicine(p.getPrescriptionId());

            List<PrescriptionItemDTO> itemDTOs = items.stream().map(item -> {
                PrescriptionItemDTO dto = new PrescriptionItemDTO();
                dto.setMedicineId(item.getMedicine().getMedicineId());
                dto.setMedicineName(item.getMedicine().getMedicineName());
                dto.setQuantityPrescribed(item.getQuantityPrescribed());
                dto.setDoseAmount(item.getDoseAmount());
                dto.setFrequency(item.getFrequency());
                dto.setTiming(item.getTiming());
                dto.setDuration(item.getDuration());
                return dto;
            }).collect(Collectors.toList());

            return buildResponseDTO(p, p.getCustomer(), p.getDoctor(), itemDTOs);
        }).collect(Collectors.toList());
    }

    // =========================================================
    // VALIDATE PRESCRIPTION FOR CHECKOUT
    // =========================================================

    /**
     * Validates whether a prescription covers a specific medicine.
     * Returns valid=true ONLY if:
     *   1. The prescription exists
     *   2. It has not expired (prescription_date + validity_period_days >= today)
     *   3. It contains the specified medicine
     */
    @Transactional(readOnly = true)
    public PrescriptionValidationDTO validatePrescriptionForMedicine(
            Long prescriptionId, Long medicineId, int quantity) {

        // 1. Check prescription exists
        Prescription prescription = prescriptionRepository.findById(prescriptionId).orElse(null);
        if (prescription == null) {
            return new PrescriptionValidationDTO(false,
                    "Prescription #" + prescriptionId + " not found.", prescriptionId);
        }

        // 2. Check expiry
        LocalDate expiryDate = prescription.getPrescriptionDate()
                .plusDays(prescription.getValidityPeriodDays());
        if (LocalDate.now().isAfter(expiryDate)) {
            return new PrescriptionValidationDTO(false,
                    "Prescription #" + prescriptionId + " has expired on " + expiryDate + ".",
                    prescriptionId);
        }

        // 3. Check medicine is prescribed
        List<PrescriptionItem> matchingItems = prescriptionItemRepository
                .findByPrescriptionAndMedicine(prescriptionId, medicineId);
        if (matchingItems.isEmpty()) {
            return new PrescriptionValidationDTO(false,
                    "The prescribed medicine is not listed in prescription #" + prescriptionId + ".",
                    prescriptionId);
        }

        return new PrescriptionValidationDTO(true,
                "Prescription #" + prescriptionId + " is valid. Expires: " + expiryDate,
                prescriptionId);
    }

    /**
     * Validates a prescription covers ALL specified Schedule H medicine IDs.
     * Used by InvoiceService during checkout.
     */
    @Transactional(readOnly = true)
    public void validatePrescriptionForScheduleHMedicines(
            Long prescriptionId, List<Long> scheduleHMedicineIds) {

        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException(
                        "Prescription #" + prescriptionId + " not found. "
                        + "A valid prescription is required for Schedule H drugs."));

        LocalDate expiryDate = prescription.getPrescriptionDate()
                .plusDays(prescription.getValidityPeriodDays());
        if (LocalDate.now().isAfter(expiryDate)) {
            throw new RuntimeException(
                    "Prescription #" + prescriptionId + " expired on " + expiryDate
                    + ". Cannot checkout Schedule H drugs with an expired prescription.");
        }

        for (Long medId : scheduleHMedicineIds) {
            List<PrescriptionItem> matches = prescriptionItemRepository
                    .findByPrescriptionAndMedicine(prescriptionId, medId);
            if (matches.isEmpty()) {
                Medicine med = medicineRepository.findById(medId).orElse(null);
                String medName = med != null ? med.getMedicineName() : "ID:" + medId;
                throw new RuntimeException(
                        "Schedule H drug \"" + medName
                        + "\" is not listed in prescription #" + prescriptionId + ".");
            }
        }
    }

    // =========================================================
    // HELPER
    // =========================================================

    private PrescriptionResponseDTO buildResponseDTO(Prescription p, Customer c, Doctor d,
                                                      List<PrescriptionItemDTO> items) {
        PrescriptionResponseDTO dto = new PrescriptionResponseDTO();
        dto.setPrescriptionId(p.getPrescriptionId());
        dto.setCustomerName(c.getFullName());
        dto.setCustomerId(c.getCustomerId());
        dto.setDoctorName(d.getName());
        dto.setDoctorLicense(d.getMedicalLicenseNo());
        dto.setPrescriptionDate(p.getPrescriptionDate());
        dto.setValidityPeriodDays(p.getValidityPeriodDays());

        LocalDate expiryDate = p.getPrescriptionDate().plusDays(p.getValidityPeriodDays());
        dto.setExpiryDate(expiryDate);
        dto.setStatus(LocalDate.now().isAfter(expiryDate) ? "Expired" : "Active");
        dto.setDiagnosisNotes(p.getDiagnosisNotes());
        dto.setItems(items);
        return dto;
    }
}
