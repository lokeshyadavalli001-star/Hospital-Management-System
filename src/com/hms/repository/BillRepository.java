package com.hms.repository;

import com.hms.model.Bill;
import com.hms.model.enums.PaymentStatus;
import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repository managing medical invoices, receipts, and outstanding account balances.
 */
public class BillRepository extends AbstractCsvRepository<Bill> {

    public BillRepository(String filePath) {
        super(filePath);
    }

    @Override
    protected String getCsvHeader() {
        return "id,appointmentId,patientId,consultationFee,serviceCharges,medicineCharges,roomCharges,discountAmount,taxRate,paidAmount,paymentStatus,billingDate,paidDate";
    }

    @Override
    protected String serialize(Bill b) {
        return FileUtil.toCsvLine(Arrays.asList(
                b.getId(),
                b.getAppointmentId(),
                b.getPatientId(),
                String.valueOf(b.getConsultationFee()),
                String.valueOf(b.getServiceCharges()),
                String.valueOf(b.getMedicineCharges()),
                String.valueOf(b.getRoomCharges()),
                String.valueOf(b.getDiscountAmount()),
                String.valueOf(b.getTaxRate()),
                String.valueOf(b.getPaidAmount()),
                b.getPaymentStatus().name(),
                DateTimeUtil.formatDate(b.getBillingDate()),
                DateTimeUtil.formatDate(b.getPaidDate())
        ));
    }

    @Override
    protected Bill deserialize(List<String> tokens) {
        if (tokens.size() < 13) {
            return null;
        }
        String id = tokens.get(0);
        String appointmentId = tokens.get(1);
        String patientId = tokens.get(2);
        double consultationFee = Double.parseDouble(tokens.get(3));
        double serviceCharges = Double.parseDouble(tokens.get(4));
        double medicineCharges = Double.parseDouble(tokens.get(5));
        double roomCharges = Double.parseDouble(tokens.get(6));
        double discountAmount = Double.parseDouble(tokens.get(7));
        double taxRate = Double.parseDouble(tokens.get(8));
        double paidAmount = Double.parseDouble(tokens.get(9));
        PaymentStatus status = PaymentStatus.fromString(tokens.get(10));
        LocalDate billingDate = DateTimeUtil.parseDate(tokens.get(11));
        LocalDate paidDate = DateTimeUtil.parseDate(tokens.get(12));

        return new Bill(id, appointmentId, patientId, consultationFee, serviceCharges,
                medicineCharges, roomCharges, discountAmount, taxRate, paidAmount, status, billingDate, paidDate);
    }

    public List<Bill> findByPatientId(String patientId) {
        if (patientId == null) return List.of();
        return findAll().stream()
                .filter(b -> b.getPatientId().equalsIgnoreCase(patientId.trim()))
                .collect(Collectors.toList());
    }

    public Optional<Bill> findByAppointmentId(String appointmentId) {
        if (appointmentId == null) return Optional.empty();
        return findAll().stream()
                .filter(b -> b.getAppointmentId().equalsIgnoreCase(appointmentId.trim()))
                .findFirst();
    }

    public List<Bill> findByPaymentStatus(PaymentStatus status) {
        if (status == null) return findAll();
        return findAll().stream()
                .filter(b -> b.getPaymentStatus() == status)
                .collect(Collectors.toList());
    }
}
