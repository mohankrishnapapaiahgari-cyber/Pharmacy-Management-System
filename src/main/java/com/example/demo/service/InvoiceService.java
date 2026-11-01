package com.example.demo.service;

import com.example.demo.dto.CheckoutItemDTO;
import com.example.demo.dto.CheckoutRequestDTO;
import com.example.demo.dto.InvoiceResponseDTO;
import com.example.demo.dto.InvoiceResponseDTO.InvoiceItemDetail;
import com.example.demo.exception.PharmacyBusinessException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    // Default GST tax rate for medicines (12%)
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("12.00");

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;
    private final MedicineRepository medicineRepository;
    private final BatchInventoryRepository batchInventoryRepository;
    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionService prescriptionService;
    private final PaymentRepository paymentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          InvoiceItemRepository invoiceItemRepository,
                          MedicineRepository medicineRepository,
                          BatchInventoryRepository batchInventoryRepository,
                          CustomerRepository customerRepository,
                          EmployeeRepository employeeRepository,
                          PrescriptionRepository prescriptionRepository,
                          PrescriptionService prescriptionService,
                          PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.medicineRepository = medicineRepository;
        this.batchInventoryRepository = batchInventoryRepository;
        this.customerRepository = customerRepository;
        this.employeeRepository = employeeRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.prescriptionService = prescriptionService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Executes atomic POS checkout workflow with comprehensive validations (E33 - E46).
     * If ANY step fails, rolls back completely.
     */
    @Transactional(rollbackFor = Exception.class)
    public InvoiceResponseDTO processSale(CheckoutRequestDTO request) {
        // E38: Empty cart validation
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new PharmacyBusinessException("E38", "Cart is empty. Add items before checkout.");
        }

        // E43: Negative discount validation
        BigDecimal discount = request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO;
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new PharmacyBusinessException("E43", "Discount amount cannot be negative.");
        }

        // E40: Resolve Employee
        Long empId = request.getEmployeeId() != null ? request.getEmployeeId() : 1L;
        Employee employee = employeeRepository.findById(empId)
                .orElseThrow(() -> new PharmacyBusinessException("E40", "Employee not found with ID: " + empId));

        // E39: Resolve Customer (nullable for walk-ins)
        Customer customer = null;
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new PharmacyBusinessException("E39", "Customer not found with ID: " + request.getCustomerId()));
        }

        // Resolve Prescription if provided
        Prescription prescription = null;
        if (request.getPrescriptionId() != null) {
            prescription = prescriptionRepository.findById(request.getPrescriptionId())
                    .orElseThrow(() -> new PharmacyBusinessException("E41", "Prescription not found with ID: " + request.getPrescriptionId()));
        }

        // Validate each item in the cart
        List<Long> scheduleHMedicineIds = new ArrayList<>();
        for (CheckoutItemDTO cartItem : request.getItems()) {
            // E37: Item quantity <= 0
            if (cartItem.getQuantity() <= 0) {
                throw new PharmacyBusinessException("E37", "Item quantity must be greater than zero.");
            }

            Medicine med = medicineRepository.findById(cartItem.getMedicineId())
                    .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found with ID: " + cartItem.getMedicineId()));

            // E35: Inactive medicine check
            if (med.getIsActive() != null && !med.getIsActive()) {
                throw new PharmacyBusinessException("E35", "Medicine " + med.getMedicineName() + " is currently inactive.");
            }

            if ("Schedule H".equalsIgnoreCase(med.getScheduleType()) || "Schedule H1".equalsIgnoreCase(med.getScheduleType())) {
                scheduleHMedicineIds.add(med.getMedicineId());
            }
        }

        // E36: Schedule H prescription validation
        if (!scheduleHMedicineIds.isEmpty()) {
            if (request.getPrescriptionId() == null) {
                List<String> names = scheduleHMedicineIds.stream()
                        .map(id -> medicineRepository.findById(id).map(Medicine::getMedicineName).orElse("ID:" + id))
                        .collect(Collectors.toList());
                throw new PharmacyBusinessException("E36",
                        "Prescription required! The following Schedule H drugs need a valid prescription: "
                        + String.join(", ", names) + ".");
            }
            prescriptionService.validatePrescriptionForScheduleHMedicines(
                    request.getPrescriptionId(), scheduleHMedicineIds);
        }

        // Create Invoice header
        Invoice invoice = new Invoice();
        invoice.setCustomer(customer);
        invoice.setEmployee(employee);
        invoice.setPrescription(prescription);
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setDiscountAmount(discount);
        invoice.setPaymentStatus("PAID");
        invoice = invoiceRepository.save(invoice);

        // FEFO batch deduction
        List<InvoiceItemDetail> itemDetails = new ArrayList<>();
        BigDecimal runningSubtotal = BigDecimal.ZERO;
        BigDecimal runningTax = BigDecimal.ZERO;

        for (CheckoutItemDTO cartItem : request.getItems()) {
            Medicine medicine = medicineRepository.findById(cartItem.getMedicineId())
                    .orElseThrow(() -> new PharmacyBusinessException("E01", "Medicine not found with ID: " + cartItem.getMedicineId()));

            if (cartItem.getBatchId() != null) {
                // Specific physical batch selected by cashier/pharmacist
                BatchInventory specificBatch = batchInventoryRepository.findById(cartItem.getBatchId())
                        .orElseThrow(() -> new PharmacyBusinessException("E32", "Batch not found with ID: " + cartItem.getBatchId()));

                if (!specificBatch.getMedicine().getMedicineId().equals(medicine.getMedicineId())) {
                    throw new PharmacyBusinessException("E32", "Batch " + specificBatch.getBatchNumber()
                            + " does not belong to medicine: " + medicine.getMedicineName());
                }

                if (specificBatch.getExpiryDate() != null && specificBatch.getExpiryDate().isBefore(LocalDate.now())) {
                    throw new PharmacyBusinessException("E34", "Selected batch " + specificBatch.getBatchNumber()
                            + " has expired on " + specificBatch.getExpiryDate() + " and cannot be dispensed.");
                }

                if (specificBatch.getQuantityInStock() < cartItem.getQuantity()) {
                    throw new PharmacyBusinessException("E33", "Insufficient stock in batch " + specificBatch.getBatchNumber()
                            + ". Requested: " + cartItem.getQuantity() + ", Available: " + specificBatch.getQuantityInStock());
                }

                // Deduct stock in DB
                specificBatch.setQuantityInStock(specificBatch.getQuantityInStock() - cartItem.getQuantity());
                batchInventoryRepository.save(specificBatch);

                // Create InvoiceItem
                InvoiceItem invoiceItem = new InvoiceItem();
                invoiceItem.setInvoice(invoice);
                invoiceItem.setBatch(specificBatch);
                invoiceItem.setQuantity(cartItem.getQuantity());
                invoiceItem.setUnitSellingPrice(medicine.getUnitPrice());
                invoiceItem.setTaxRate(DEFAULT_TAX_RATE);
                invoiceItem = invoiceItemRepository.save(invoiceItem);

                // Compute financial lines
                BigDecimal lineSubtotal = medicine.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                BigDecimal lineTax = lineSubtotal.multiply(DEFAULT_TAX_RATE)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal lineTotal = lineSubtotal.add(lineTax);

                runningSubtotal = runningSubtotal.add(lineSubtotal);
                runningTax = runningTax.add(lineTax);

                InvoiceItemDetail detail = new InvoiceItemDetail();
                detail.setInvoiceItemId(invoiceItem.getInvoiceItemId());
                detail.setBatchId(specificBatch.getBatchId());
                detail.setMedicineName(medicine.getMedicineName());
                detail.setBatchNumber(specificBatch.getBatchNumber());
                detail.setQuantity(cartItem.getQuantity());
                detail.setUnitSellingPrice(medicine.getUnitPrice());
                detail.setTaxRate(DEFAULT_TAX_RATE);
                detail.setLineTotal(lineTotal);
                itemDetails.add(detail);
            } else {
                // Automatic FEFO batch deduction
                List<BatchInventory> batches = batchInventoryRepository.findBatchesByMedicineFEFO(medicine.getMedicineId());

                int totalAvailable = batches.stream().mapToInt(BatchInventory::getQuantityInStock).sum();
                // E33: Insufficient stock
                if (totalAvailable < cartItem.getQuantity()) {
                    throw new PharmacyBusinessException("E33",
                            "Insufficient stock for " + medicine.getMedicineName()
                            + ". Requested: " + cartItem.getQuantity() + ", Available: " + totalAvailable);
                }

                int remainingQty = cartItem.getQuantity();
                for (BatchInventory batch : batches) {
                    if (remainingQty <= 0) break;

                    // E34: Batch expired check
                    if (batch.getExpiryDate() != null && batch.getExpiryDate().isBefore(LocalDate.now())) {
                        continue; // Skip expired batches (should be filtered by query anyway)
                    }

                    int deduct = Math.min(remainingQty, batch.getQuantityInStock());
                    if (deduct <= 0) continue;

                    // Deduct stock in DB
                    batch.setQuantityInStock(batch.getQuantityInStock() - deduct);
                    batchInventoryRepository.save(batch);

                    // Create InvoiceItem
                    InvoiceItem invoiceItem = new InvoiceItem();
                    invoiceItem.setInvoice(invoice);
                    invoiceItem.setBatch(batch);
                    invoiceItem.setQuantity(deduct);
                    invoiceItem.setUnitSellingPrice(medicine.getUnitPrice());
                    invoiceItem.setTaxRate(DEFAULT_TAX_RATE);
                    invoiceItem = invoiceItemRepository.save(invoiceItem);

                    // Compute financial lines
                    BigDecimal lineSubtotal = medicine.getUnitPrice().multiply(BigDecimal.valueOf(deduct));
                    BigDecimal lineTax = lineSubtotal.multiply(DEFAULT_TAX_RATE)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal lineTotal = lineSubtotal.add(lineTax);

                    runningSubtotal = runningSubtotal.add(lineSubtotal);
                    runningTax = runningTax.add(lineTax);

                    InvoiceItemDetail detail = new InvoiceItemDetail();
                    detail.setInvoiceItemId(invoiceItem.getInvoiceItemId());
                    detail.setBatchId(batch.getBatchId());
                    detail.setMedicineName(medicine.getMedicineName());
                    detail.setBatchNumber(batch.getBatchNumber());
                    detail.setQuantity(deduct);
                    detail.setUnitSellingPrice(medicine.getUnitPrice());
                    detail.setTaxRate(DEFAULT_TAX_RATE);
                    detail.setLineTotal(lineTotal);
                    itemDetails.add(detail);

                    remainingQty -= deduct;
                }

                if (remainingQty > 0) {
                    throw new PharmacyBusinessException("E33", "Failed to fulfill requested quantity for " + medicine.getMedicineName());
                }
            }
        }

        // E44: Check discount doesn't exceed subtotal
        if (discount.compareTo(runningSubtotal) > 0) {
            throw new PharmacyBusinessException("E44", "Discount of ₹" + discount + " exceeds subtotal of ₹" + runningSubtotal);
        }

        BigDecimal grandTotal = runningSubtotal.subtract(discount).add(runningTax);
        if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
            grandTotal = BigDecimal.ZERO;
        }

        // Atomic Payment creation
        String paymentMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod().toUpperCase()
                : "CASH";

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmountPaid(grandTotal);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(paymentMethod);
        payment.setTransactionReference(request.getTransactionReference());
        paymentRepository.save(payment);

        // Build Response
        InvoiceResponseDTO response = new InvoiceResponseDTO();
        response.setInvoiceId(invoice.getInvoiceId());
        response.setInvoiceDate(invoice.getInvoiceDate());
        response.setPaymentStatus(invoice.getPaymentStatus());
        response.setPaymentMethod(paymentMethod);
        response.setTransactionReference(request.getTransactionReference());
        response.setItems(itemDetails);
        response.setSubtotal(runningSubtotal);
        response.setDiscountAmount(discount);
        response.setTotalTax(runningTax);
        response.setGrandTotal(grandTotal);

        return response;
    }

    // Alias for backwards compatibility
    @Transactional(rollbackFor = Exception.class)
    public InvoiceResponseDTO checkout(CheckoutRequestDTO request) {
        return processSale(request);
    }
}
