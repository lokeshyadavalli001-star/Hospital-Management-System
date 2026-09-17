package com.hms.ui;

import com.hms.exception.HospitalException;
import com.hms.model.Appointment;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.service.AppointmentService;
import com.hms.service.AuditService;
import com.hms.service.BillingService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;
import com.hms.service.ReportService;
import com.hms.service.UserService;
import com.hms.util.ConsoleUtil;
import com.hms.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Self-service portal for Patients to inspect their own medical records, appointments, and billing statements.
 */
public class PatientMenu extends ConsoleMenu {

    private final String patientId;

    public PatientMenu(Scanner scanner, User currentUser, UserService userService,
                       PatientService patientService, DoctorService doctorService,
                       AppointmentService appointmentService, BillingService billingService,
                       ReportService reportService, AuditService auditService) {
        super(scanner, currentUser, userService, patientService, doctorService,
                appointmentService, billingService, reportService, auditService);

        // Resolve patient ID matching username or phone
        Optional<Patient> pOpt = patientService.getAllPatients().stream()
                .filter(p -> p.getId().equalsIgnoreCase(currentUser.getUsername())
                        || (p.getPhone() != null && p.getPhone().equals(currentUser.getPhone())))
                .findFirst();
        this.patientId = pOpt.map(Patient::getId).orElse(currentUser.getId());
    }

    @Override
    public void showMenu() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtil.printHeader("PATIENT SELF-SERVICE PORTAL");
            displayUserHeader();
            System.out.println("Patient ID: " + patientId);
            System.out.println();
            System.out.println(" 1. View My Demographic Profile");
            System.out.println(" 2. View My Appointments History");
            System.out.println(" 3. View My Invoices & Balances");
            System.out.println(" 4. Print / View Invoice Receipt");
            System.out.println(" 5. Change Password");
            System.out.println(" 6. Logout");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Enter selection", 1, 6);
            switch (choice) {
                case 1:
                    viewProfile();
                    break;
                case 2:
                    viewAppointments();
                    break;
                case 3:
                    viewBills();
                    break;
                case 4:
                    viewInvoice();
                    break;
                case 5:
                    changePassword();
                    break;
                case 6:
                    ConsoleUtil.printInfo("Logging out of Patient portal...");
                    exit = true;
                    break;
            }
        }
    }

    private void viewProfile() {
        ConsoleUtil.printSubHeader("MY DEMOGRAPHIC PROFILE");
        try {
            Patient p = patientService.getPatientById(patientId);
            System.out.println();
            System.out.println("==================================================");
            System.out.println("Patient ID       : " + p.getId());
            System.out.println("Full Name        : " + p.getName());
            System.out.println("Age              : " + p.getAge());
            System.out.println("Gender           : " + p.getGender());
            System.out.println("Phone            : " + p.getPhone());
            System.out.println("Email            : " + p.getEmail());
            System.out.println("Address          : " + p.getAddress());
            System.out.println("Blood Group      : " + p.getBloodGroup());
            System.out.println("Emergency Contact: " + p.getEmergencyContact());
            System.out.println("Registered Since : " + DateTimeUtil.formatDate(p.getRegistrationDate()));
            System.out.println("==================================================");
        } catch (HospitalException e) {
            ConsoleUtil.printError("Could not retrieve patient profile: " + e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewAppointments() {
        ConsoleUtil.printSubHeader("MY APPOINTMENT HISTORY");
        List<Appointment> list = appointmentService.getAppointmentsByPatient(patientId);
        String[] headers = {"Appt ID", "Doctor ID", "Date", "Time", "Status", "Reason", "Consultation Notes"};
        List<String[]> rows = new ArrayList<>();
        for (Appointment a : list) {
            rows.add(new String[]{
                    a.getId(), a.getDoctorId(),
                    DateTimeUtil.formatDate(a.getAppointmentDate()),
                    DateTimeUtil.formatTime(a.getAppointmentTime()),
                    a.getStatus().name(), a.getReason(),
                    a.getConsultationNotes()
            });
        }
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.pause(scanner);
    }

    private void viewBills() {
        ConsoleUtil.printSubHeader("MY BILLS & OUTSTANDING BALANCES");
        List<Bill> list = billingService.getBillsByPatient(patientId);
        String[] headers = {"Invoice ID", "Appt ID", "Total (INR)", "Paid (INR)", "Due Balance (INR)", "Status", "Date"};
        List<String[]> rows = new ArrayList<>();
        for (Bill b : list) {
            rows.add(new String[]{
                    b.getId(), b.getAppointmentId(),
                    String.format("%.2f", b.getFinalAmount()),
                    String.format("%.2f", b.getPaidAmount()),
                    String.format("%.2f", b.getBalanceAmount()),
                    b.getPaymentStatus().name(),
                    DateTimeUtil.formatDate(b.getBillingDate())
            });
        }
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.pause(scanner);
    }

    private void viewInvoice() {
        ConsoleUtil.printSubHeader("VIEW INVOICE RECEIPT");
        String billId = ConsoleUtil.promptString(scanner, "Enter Invoice ID", true);
        try {
            Bill bill = billingService.getBillById(billId);
            if (!bill.getPatientId().equalsIgnoreCase(patientId)) {
                ConsoleUtil.printError("Access denied: You can only view your own invoices.");
                ConsoleUtil.pause(scanner);
                return;
            }

            Patient patient = patientService.getPatientById(patientId);
            Doctor doctor = null;
            if (!bill.getAppointmentId().equalsIgnoreCase("N/A")) {
                try {
                    Appointment a = appointmentService.getAppointmentById(bill.getAppointmentId());
                    doctor = doctorService.getDoctorById(a.getDoctorId());
                } catch (Exception ignored) {}
            }

            System.out.println(billingService.renderInvoiceText(bill, patient, doctor));
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void changePassword() {
        ConsoleUtil.printSubHeader("CHANGE PASSWORD");
        String oldPass = ConsoleUtil.promptString(scanner, "Current Password", true);
        String newPass = ConsoleUtil.promptString(scanner, "New Password", true);
        try {
            userService.changePassword(currentUser.getUsername(), oldPass, newPass);
            ConsoleUtil.printSuccess("Password updated successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }
}
