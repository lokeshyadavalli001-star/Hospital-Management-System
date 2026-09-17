package com.hms.service;

import com.hms.exception.HospitalException;
import com.hms.exception.InsufficientPaymentException;
import com.hms.exception.InvalidInputException;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.enums.PaymentStatus;
import com.hms.repository.BillRepository;
import com.hms.util.DateTimeUtil;
import com.hms.util.IdGenerator;
import com.hms.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing invoicing calculations, payment reconciliations, and receipt generation.
 */
public class BillingService {

    private final BillRepository billRepository;
    private final PatientService patientService;
    private final AuditService auditService;

    public BillingService(BillRepository billRepository, PatientService patientService, AuditService auditService) {
        this.billRepository = billRepository;
        this.patientService = patientService;
        this.auditService = auditService;
    }

    public Bill generateBill(String appointmentId, String patientId,
                             double consultationFee, double serviceCharges,
                             double medicineCharges, double roomCharges,
                             double discountAmount, double taxRate,
                             String actor) throws HospitalException {
        // Validate patient
        patientService.getPatientById(patientId);

        if (!InputValidator.isNonNegative(consultationFee)
                || !InputValidator.isNonNegative(serviceCharges)
                || !InputValidator.isNonNegative(medicineCharges)
                || !InputValidator.isNonNegative(roomCharges)
                || !InputValidator.isNonNegative(discountAmount)
                || !InputValidator.isNonNegative(taxRate)) {
            throw new InvalidInputException("Monetary figures and tax rates cannot be negative.");
        }

        String billId = IdGenerator.nextBillId();
        Bill bill = new Bill(billId, appointmentId, patientId, consultationFee,
                serviceCharges, medicineCharges, roomCharges,
                discountAmount, taxRate, 0.0, PaymentStatus.PENDING,
                LocalDate.now(), null);

        billRepository.save(bill);
        auditService.log("BILLING", "GENERATE", actor,
                String.format("Generated invoice %s for Patient %s (Total: INR %.2f)",
                        billId, patientId, bill.getFinalAmount()));
        return bill;
    }

    public Bill recordPayment(String billId, double amount, String actor) throws HospitalException {
        Bill bill = getBillById(billId);

        if (bill.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidInputException("Bill " + billId + " is already fully settled.");
        }

        if (amount <= 0.0) {
            throw new InvalidInputException("Payment amount must be greater than zero.");
        }

        if (amount > bill.getBalanceAmount() + 0.001) {
            throw new InsufficientPaymentException(String.format(
                    "Payment amount (INR %.2f) exceeds outstanding balance (INR %.2f).",
                    amount, bill.getBalanceAmount()));
        }

        bill.addPayment(amount);
        billRepository.save(bill);

        auditService.log("BILLING", "PAYMENT", actor,
                String.format("Recorded payment of INR %.2f on Bill %s. Remaining: INR %.2f (Status: %s)",
                        amount, billId, bill.getBalanceAmount(), bill.getPaymentStatus()));
        return bill;
    }

    public Bill getBillById(String id) throws InvalidInputException {
        if (id == null) {
            throw new InvalidInputException("Bill ID cannot be null.");
        }
        return billRepository.findById(id)
                .orElseThrow(() -> new InvalidInputException("No bill found with ID: " + id));
    }

    public Optional<Bill> getBillByAppointmentId(String appointmentId) {
        return billRepository.findByAppointmentId(appointmentId);
    }

    public List<Bill> getBillsByPatient(String patientId) {
        return billRepository.findByPatientId(patientId);
    }

    public List<Bill> getAllBills() {
        return billRepository.findAll();
    }

    public List<Bill> getPendingBills() {
        return billRepository.findAll().stream()
                .filter(b -> b.getPaymentStatus() != PaymentStatus.PAID)
                .collect(Collectors.toList());
    }

    public List<Bill> searchBills(String query) {
        return billRepository.search(query);
    }

    /**
     * Produces a formatted printable ASCII invoice receipt.
     */
    public String renderInvoiceText(Bill bill, Patient patient, Doctor doctor) {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================================================\n");
        sb.append("                          CITY GENERAL HOSPITAL INVOICE                        \n");
        sb.append("                     Health, Compassion & Quality Care                         \n");
        sb.append("================================================================================\n");
        sb.append(String.format("Invoice ID    : %-25s Billing Date: %s\n", bill.getId(), DateTimeUtil.formatDate(bill.getBillingDate())));
        sb.append(String.format("Appointment ID: %-25s Payment Status: %s\n", bill.getAppointmentId(), bill.getPaymentStatus()));
        sb.append("--------------------------------------------------------------------------------\n");
        if (patient != null) {
            sb.append(String.format("Patient Name  : %-25s Patient ID  : %s\n", patient.getName(), patient.getId()));
            sb.append(String.format("Phone / Blood : %-25s Age / Gender: %d / %s\n",
                    patient.getPhone() + " (" + patient.getBloodGroup() + ")", patient.getAge(), patient.getGender()));
        }
        if (doctor != null) {
            sb.append(String.format("Consultant    : Dr. %-21s Dept / Spec : %s (%s)\n",
                    doctor.getFullName(), doctor.getDepartment(), doctor.getSpecialization()));
        }
        sb.append("================================================================================\n");
        sb.append(String.format(" %-40s | %15s |\n", "ITEM / SERVICE DESCRIPTION", "AMOUNT (INR)"));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-40s | %15.2f |\n", "Doctor Consultation Fee", bill.getConsultationFee()));
        sb.append(String.format(" %-40s | %15.2f |\n", "Diagnostic / Service Charges", bill.getServiceCharges()));
        sb.append(String.format(" %-40s | %15.2f |\n", "Pharmacy / Medicine Charges", bill.getMedicineCharges()));
        sb.append(String.format(" %-40s | %15.2f |\n", "Room / Facility Charges", bill.getRoomCharges()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format(" %-40s | %15.2f |\n", "Gross Subtotal", bill.getSubtotal()));
        sb.append(String.format(" %-40s | %15.2f |\n", "Discount / Concession (-)", bill.getDiscountAmount()));
        sb.append(String.format(" %-40s | %15.2f |\n", String.format("Applicable Tax (%.1f%%)", bill.getTaxRate()), bill.getTaxAmount()));
        sb.append("================================================================================\n");
        sb.append(String.format(" %-40s | %15.2f |\n", "NET AMOUNT PAYABLE", bill.getFinalAmount()));
        sb.append(String.format(" %-40s | %15.2f |\n", "Amount Received to Date", bill.getPaidAmount()));
        sb.append(String.format(" %-40s | %15.2f |\n", "CURRENT OUTSTANDING BALANCE", bill.getBalanceAmount()));
        sb.append("================================================================================\n");
        if (bill.getPaidDate() != null) {
            sb.append(String.format("Paid Date: %s\n", DateTimeUtil.formatDate(bill.getPaidDate())));
        }
        sb.append("Thank you for choosing City General Hospital. Wishing you speedy recovery!\n");
        sb.append("================================================================================\n");
        return sb.toString();
    }
}
