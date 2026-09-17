package com.hms.model;

import com.hms.model.enums.PaymentStatus;
import com.hms.repository.Identifiable;

import java.time.LocalDate;

/**
 * Represents an itemized medical invoice and payment ledger.
 * Encapsulates accounting arithmetic and payment reconciliation logic.
 */
public class Bill implements Identifiable, Searchable {
    private final String id;
    private final String appointmentId;
    private final String patientId;
    private double consultationFee;
    private double serviceCharges;
    private double medicineCharges;
    private double roomCharges;
    private double discountAmount;
    private double taxRate; // percentage e.g. 5.0 for 5%
    private double subtotal;
    private double taxAmount;
    private double finalAmount;
    private double paidAmount;
    private double balanceAmount;
    private PaymentStatus paymentStatus;
    private final LocalDate billingDate;
    private LocalDate paidDate;

    public Bill(String id, String appointmentId, String patientId,
                double consultationFee, double serviceCharges, double medicineCharges,
                double roomCharges, double discountAmount, double taxRate,
                double paidAmount, PaymentStatus paymentStatus,
                LocalDate billingDate, LocalDate paidDate) {
        this.id = id;
        this.appointmentId = (appointmentId != null) ? appointmentId : "N/A";
        this.patientId = patientId;
        this.consultationFee = Math.max(0, consultationFee);
        this.serviceCharges = Math.max(0, serviceCharges);
        this.medicineCharges = Math.max(0, medicineCharges);
        this.roomCharges = Math.max(0, roomCharges);
        this.discountAmount = Math.max(0, discountAmount);
        this.taxRate = Math.max(0, taxRate);
        this.paidAmount = Math.max(0, paidAmount);
        this.billingDate = (billingDate != null) ? billingDate : LocalDate.now();
        this.paidDate = paidDate;

        recalculate();

        if (paymentStatus != null) {
            this.paymentStatus = paymentStatus;
        }
    }

    /**
     * Recalculates subtotal, taxes, final amount, and balance.
     */
    public void recalculate() {
        this.subtotal = consultationFee + serviceCharges + medicineCharges + roomCharges;
        double discountedBase = Math.max(0, subtotal - discountAmount);
        this.taxAmount = Math.round((discountedBase * (taxRate / 100.0)) * 100.0) / 100.0;
        this.finalAmount = Math.round((discountedBase + taxAmount) * 100.0) / 100.0;
        this.balanceAmount = Math.round((finalAmount - paidAmount) * 100.0) / 100.0;

        if (balanceAmount <= 0.001) {
            this.paymentStatus = PaymentStatus.PAID;
            this.balanceAmount = 0.0;
            if (this.paidDate == null) {
                this.paidDate = LocalDate.now();
            }
        } else if (paidAmount > 0.001) {
            this.paymentStatus = PaymentStatus.PARTIAL;
        } else {
            this.paymentStatus = PaymentStatus.PENDING;
        }
    }

    public void addPayment(double amount) {
        if (amount <= 0) {
            return;
        }
        this.paidAmount = Math.round((this.paidAmount + amount) * 100.0) / 100.0;
        recalculate();
    }

    @Override
    public String getId() {
        return id;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        recalculate();
    }

    public double getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(double serviceCharges) {
        this.serviceCharges = serviceCharges;
        recalculate();
    }

    public double getMedicineCharges() {
        return medicineCharges;
    }

    public void setMedicineCharges(double medicineCharges) {
        this.medicineCharges = medicineCharges;
        recalculate();
    }

    public double getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(double roomCharges) {
        this.roomCharges = roomCharges;
        recalculate();
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        recalculate();
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
        recalculate();
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public double getBalanceAmount() {
        return balanceAmount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public LocalDate getPaidDate() {
        return paidDate;
    }

    public void setPaidDate(LocalDate paidDate) {
        this.paidDate = paidDate;
    }

    @Override
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String q = query.toLowerCase();
        return id.toLowerCase().contains(q)
                || appointmentId.toLowerCase().contains(q)
                || patientId.toLowerCase().contains(q)
                || paymentStatus.name().toLowerCase().contains(q)
                || billingDate.toString().contains(q);
    }

    @Override
    public String toString() {
        return String.format("[%s] Patient: %s | Appt: %s | Total: INR %.2f | Paid: INR %.2f | Due: INR %.2f | Status: %s",
                id, patientId, appointmentId, finalAmount, paidAmount, balanceAmount, paymentStatus);
    }
}
