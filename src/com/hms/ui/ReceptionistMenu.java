package com.hms.ui;

import com.hms.exception.HospitalException;
import com.hms.model.Appointment;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.model.enums.Gender;
import com.hms.service.AppointmentService;
import com.hms.service.AuditService;
import com.hms.service.BillingService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;
import com.hms.service.ReportService;
import com.hms.service.UserService;
import com.hms.util.ConsoleUtil;
import com.hms.util.DateTimeUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Menu controller for Front-Desk Receptionists handling patient registration, scheduling, and billing intake.
 */
public class ReceptionistMenu extends ConsoleMenu {

    public ReceptionistMenu(Scanner scanner, User currentUser, UserService userService,
                            PatientService patientService, DoctorService doctorService,
                            AppointmentService appointmentService, BillingService billingService,
                            ReportService reportService, AuditService auditService) {
        super(scanner, currentUser, userService, patientService, doctorService,
                appointmentService, billingService, reportService, auditService);
    }

    @Override
    public void showMenu() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtil.printHeader("RECEPTIONIST FRONT-DESK PORTAL");
            displayUserHeader();
            System.out.println(" 1. Register New Patient");
            System.out.println(" 2. Search Patients");
            System.out.println(" 3. Update Patient Details");
            System.out.println(" 4. View Doctor Directory & Availability");
            System.out.println(" 5. Book Appointment");
            System.out.println(" 6. Reschedule Appointment");
            System.out.println(" 7. Cancel Appointment");
            System.out.println(" 8. View Today's Appointments");
            System.out.println(" 9. Generate Invoice / Bill");
            System.out.println(" 10. Record Bill Payment");
            System.out.println(" 11. View / Print Invoice Receipt");
            System.out.println(" 12. Change Password");
            System.out.println(" 13. Logout");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Enter selection", 1, 13);
            switch (choice) {
                case 1:
                    registerPatient();
                    break;
                case 2:
                    searchPatients();
                    break;
                case 3:
                    updatePatient();
                    break;
                case 4:
                    viewDoctors();
                    break;
                case 5:
                    bookAppointment();
                    break;
                case 6:
                    rescheduleAppointment();
                    break;
                case 7:
                    cancelAppointment();
                    break;
                case 8:
                    viewTodayAppointments();
                    break;
                case 9:
                    generateBill();
                    break;
                case 10:
                    recordPayment();
                    break;
                case 11:
                    viewInvoice();
                    break;
                case 12:
                    changePassword();
                    break;
                case 13:
                    ConsoleUtil.printInfo("Logging out of Receptionist portal...");
                    exit = true;
                    break;
            }
        }
    }

    private void registerPatient() {
        ConsoleUtil.printSubHeader("REGISTER NEW PATIENT");
        try {
            String name = ConsoleUtil.promptString(scanner, "Full Name", true);
            int age = ConsoleUtil.promptInt(scanner, "Age", 0, 130);
            System.out.println("Gender: 1. MALE  2. FEMALE  3. OTHER");
            int gChoice = ConsoleUtil.promptInt(scanner, "Select Gender", 1, 3);
            Gender gender = (gChoice == 1) ? Gender.MALE : (gChoice == 2 ? Gender.FEMALE : Gender.OTHER);
            String phone = ConsoleUtil.promptString(scanner, "Phone Number", true);
            String email = ConsoleUtil.promptString(scanner, "Email Address", false);
            String address = ConsoleUtil.promptString(scanner, "Residential Address", false);
            String blood = ConsoleUtil.promptString(scanner, "Blood Group (e.g. O+, A-)", false);
            String emergency = ConsoleUtil.promptString(scanner, "Emergency Contact", false);

            Patient p = patientService.registerPatient(name, age, gender, phone, email, address, blood, emergency, currentUser.getUsername());
            ConsoleUtil.printSuccess("Patient registered successfully! Patient ID: " + p.getId());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void searchPatients() {
        ConsoleUtil.printSubHeader("SEARCH PATIENTS");
        String q = ConsoleUtil.promptString(scanner, "Search by Name, Phone, or ID", true);
        List<Patient> list = patientService.searchPatients(q);
        String[] headers = {"ID", "Name", "Age", "Gender", "Phone", "Blood", "Active"};
        List<String[]> rows = new ArrayList<>();
        for (Patient p : list) {
            rows.add(new String[]{
                    p.getId(), p.getName(), String.valueOf(p.getAge()),
                    p.getGender().name(), p.getPhone(), p.getBloodGroup(),
                    p.isActive() ? "Active" : "Inactive"
            });
        }
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.pause(scanner);
    }

    private void updatePatient() {
        ConsoleUtil.printSubHeader("UPDATE PATIENT DEMOGRAPHICS");
        String id = ConsoleUtil.promptString(scanner, "Enter Patient ID", true);
        try {
            Patient p = patientService.getPatientById(id);
            String name = ConsoleUtil.promptStringWithDefault(scanner, "Full Name", p.getName());
            int age = ConsoleUtil.promptInt(scanner, "Age", 0, 130);
            String phone = ConsoleUtil.promptStringWithDefault(scanner, "Phone", p.getPhone());
            String email = ConsoleUtil.promptStringWithDefault(scanner, "Email", p.getEmail());
            String address = ConsoleUtil.promptStringWithDefault(scanner, "Address", p.getAddress());
            String blood = ConsoleUtil.promptStringWithDefault(scanner, "Blood Group", p.getBloodGroup());
            String emergency = ConsoleUtil.promptStringWithDefault(scanner, "Emergency Contact", p.getEmergencyContact());

            patientService.updatePatient(id, name, age, p.getGender(), phone, email, address, blood, emergency, p.isActive(), currentUser.getUsername());
            ConsoleUtil.printSuccess("Patient record updated successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewDoctors() {
        ConsoleUtil.printSubHeader("DOCTOR DIRECTORY & CLINIC HOURS");
        List<Doctor> list = doctorService.getAllDoctors();
        String[] headers = {"ID", "Name", "Department", "Specialization", "Fee (INR)", "Clinic Hours", "Status"};
        List<String[]> rows = new ArrayList<>();
        for (Doctor d : list) {
            rows.add(new String[]{
                    d.getId(), d.getFullName(), d.getDepartment(), d.getSpecialization(),
                    String.format("%.2f", d.getConsultationFee()),
                    DateTimeUtil.formatTime(d.getAvailableFrom()) + "-" + DateTimeUtil.formatTime(d.getAvailableTo()),
                    d.getStatus().name()
            });
        }
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.pause(scanner);
    }

    private void bookAppointment() {
        ConsoleUtil.printSubHeader("BOOK OUTPATIENT APPOINTMENT");
        try {
            String patientId = ConsoleUtil.promptString(scanner, "Patient ID", true);
            String doctorId = ConsoleUtil.promptString(scanner, "Doctor ID", true);
            LocalDate date = ConsoleUtil.promptDate(scanner, "Date", false);
            LocalTime time = ConsoleUtil.promptTime(scanner, "Time");
            String reason = ConsoleUtil.promptString(scanner, "Reason for Visit", true);

            Appointment appt = appointmentService.bookAppointment(patientId, doctorId, date, time, reason, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment booked successfully! Appointment ID: " + appt.getId());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void rescheduleAppointment() {
        ConsoleUtil.printSubHeader("RESCHEDULE APPOINTMENT");
        String id = ConsoleUtil.promptString(scanner, "Appointment ID", true);
        try {
            LocalDate date = ConsoleUtil.promptDate(scanner, "New Date", false);
            LocalTime time = ConsoleUtil.promptTime(scanner, "New Time");
            appointmentService.rescheduleAppointment(id, date, time, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment successfully rescheduled.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void cancelAppointment() {
        ConsoleUtil.printSubHeader("CANCEL APPOINTMENT");
        String id = ConsoleUtil.promptString(scanner, "Appointment ID", true);
        try {
            String reason = ConsoleUtil.promptString(scanner, "Reason for Cancellation", true);
            appointmentService.cancelAppointment(id, reason, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment cancelled.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewTodayAppointments() {
        ConsoleUtil.printSubHeader("TODAY'S APPOINTMENTS (" + LocalDate.now() + ")");
        List<Appointment> list = appointmentService.getTodaysAppointments();
        String[] headers = {"ID", "Patient", "Doctor", "Time", "Status", "Reason"};
        List<String[]> rows = new ArrayList<>();
        for (Appointment a : list) {
            rows.add(new String[]{
                    a.getId(), a.getPatientId(), a.getDoctorId(),
                    DateTimeUtil.formatTime(a.getAppointmentTime()),
                    a.getStatus().name(), a.getReason()
            });
        }
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.pause(scanner);
    }

    private void generateBill() {
        ConsoleUtil.printSubHeader("GENERATE PATIENT BILL");
        try {
            String patientId = ConsoleUtil.promptString(scanner, "Patient ID", true);
            String apptId = ConsoleUtil.promptString(scanner, "Appointment ID (optional)", false);
            double consultation = ConsoleUtil.promptDouble(scanner, "Consultation Fee (INR)", 0, 100000);
            double services = ConsoleUtil.promptDouble(scanner, "Diagnostic / Service Fee (INR)", 0, 200000);
            double medicines = ConsoleUtil.promptDouble(scanner, "Pharmacy / Medicine Fee (INR)", 0, 200000);
            double room = ConsoleUtil.promptDouble(scanner, "Room / Bed Charges (INR)", 0, 500000);
            double discount = ConsoleUtil.promptDouble(scanner, "Discount (INR)", 0, 100000);
            double taxRate = ConsoleUtil.promptDouble(scanner, "Tax Rate (%)", 0, 30);

            Bill bill = billingService.generateBill(apptId.isEmpty() ? "N/A" : apptId, patientId,
                    consultation, services, medicines, room, discount, taxRate, currentUser.getUsername());

            ConsoleUtil.printSuccess("Invoice created successfully! ID: " + bill.getId() + " | Final: INR " + bill.getFinalAmount());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void recordPayment() {
        ConsoleUtil.printSubHeader("RECORD PAYMENT");
        String id = ConsoleUtil.promptString(scanner, "Bill / Invoice ID", true);
        try {
            Bill b = billingService.getBillById(id);
            System.out.printf("Invoice: %s | Total: INR %.2f | Due Balance: INR %.2f\n",
                    b.getId(), b.getFinalAmount(), b.getBalanceAmount());
            if (b.getBalanceAmount() <= 0.001) {
                ConsoleUtil.printInfo("Bill is already fully paid.");
                ConsoleUtil.pause(scanner);
                return;
            }

            double amount = ConsoleUtil.promptDouble(scanner, "Amount Collected (INR)", 0.01, b.getBalanceAmount());
            billingService.recordPayment(id, amount, currentUser.getUsername());
            ConsoleUtil.printSuccess("Payment recorded.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewInvoice() {
        ConsoleUtil.printSubHeader("VIEW INVOICE RECEIPT");
        String billId = ConsoleUtil.promptString(scanner, "Invoice ID", true);
        try {
            Bill bill = billingService.getBillById(billId);
            Patient patient = null;
            try {
                patient = patientService.getPatientById(bill.getPatientId());
            } catch (Exception ignored) {}

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
