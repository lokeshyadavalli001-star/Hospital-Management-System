package com.hms;

import com.hms.exception.HospitalException;
import com.hms.exception.InsufficientPaymentException;
import com.hms.model.Bill;
import com.hms.model.Patient;
import com.hms.model.enums.Gender;
import com.hms.model.enums.PaymentStatus;
import com.hms.service.BillingService;
import com.hms.service.PatientService;

/**
 * Tests for billing itemization, discount/tax calculations, and payment status transitions.
 */
public class BillingServiceTest {

    public void run(BillingService billingService, PatientService patientService) {
        System.out.println("\n[SUITE] Running Billing & Accounting Tests...");

        Patient patient = null;
        try {
            patient = patientService.registerPatient("Billing Test Subject", 40, Gender.MALE,
                    "9876599999", "bill@test.com", "City", "B+", "", "SETUP");
        } catch (Exception e) {
            TestRunner.assertTrue(false, "Failed to create patient for billing tests: " + e.getMessage());
            return;
        }

        String billId = null;

        // Test 1: Invoicing arithmetic
        try {
            // Consultation 500, Services 300, Medicines 200, Room 0 = 1000
            // Discount: 100 -> 900
            // Tax: 5% of 900 = 45
            // Final: 945.00
            Bill bill = billingService.generateBill("APT-TEST", patient.getId(),
                    500.0, 300.0, 200.0, 0.0, 100.0, 5.0, "TEST");

            TestRunner.assertTrue(bill != null && bill.getId().startsWith("BIL-"), "Bill generated with BIL- prefix ID");
            TestRunner.assertEquals(1000.0, bill.getSubtotal(), 0.01, "Gross subtotal computed accurately");
            TestRunner.assertEquals(45.0, bill.getTaxAmount(), 0.01, "Tax calculated at 5% on discounted amount");
            TestRunner.assertEquals(945.0, bill.getFinalAmount(), 0.01, "Net final amount accurately calculated");
            TestRunner.assertEquals(945.0, bill.getBalanceAmount(), 0.01, "Initial balance equals net payable");
            TestRunner.assertEquals(PaymentStatus.PENDING, bill.getPaymentStatus(), "Initial status is PENDING");
            billId = bill.getId();
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Bill generation failed: " + e.getMessage());
        }

        // Test 2: Partial Payment
        try {
            Bill updated = billingService.recordPayment(billId, 500.0, "TEST");
            TestRunner.assertEquals(500.0, updated.getPaidAmount(), 0.01, "Paid amount recorded as 500.00");
            TestRunner.assertEquals(445.0, updated.getBalanceAmount(), 0.01, "Remaining balance is 445.00");
            TestRunner.assertEquals(PaymentStatus.PARTIAL, updated.getPaymentStatus(), "Status updated to PARTIAL");
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Partial payment failed: " + e.getMessage());
        }

        // Test 3: Overpayment Rejection
        try {
            billingService.recordPayment(billId, 600.0, "TEST"); // 600 > 445
            TestRunner.assertTrue(false, "Should have rejected overpayment exceeding balance");
        } catch (InsufficientPaymentException e) {
            TestRunner.assertTrue(true, "Overpayment correctly blocked with InsufficientPaymentException");
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Unexpected exception on overpayment: " + e);
        }

        // Test 4: Full Settlement
        try {
            Bill settled = billingService.recordPayment(billId, 445.0, "TEST");
            TestRunner.assertEquals(0.0, settled.getBalanceAmount(), 0.01, "Remaining balance is 0.00");
            TestRunner.assertEquals(PaymentStatus.PAID, settled.getPaymentStatus(), "Status updated to PAID");
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Full settlement failed: " + e.getMessage());
        }

        // Test 5: Rejection of payment on already paid bill
        try {
            billingService.recordPayment(billId, 10.0, "TEST");
            TestRunner.assertTrue(false, "Should reject payment on already settled invoice");
        } catch (HospitalException e) {
            TestRunner.assertTrue(true, "Correctly rejected subsequent payment on settled invoice");
        }
    }
}
