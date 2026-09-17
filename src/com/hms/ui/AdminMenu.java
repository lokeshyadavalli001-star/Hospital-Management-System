package com.hms.ui;

import com.hms.exception.HospitalException;
import com.hms.model.Admin;
import com.hms.model.Appointment;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.Gender;
import com.hms.model.enums.UserRole;
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
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Menu controller for Administrators with complete oversight and administrative commands.
 */
public class AdminMenu extends ConsoleMenu {

    public AdminMenu(Scanner scanner, User currentUser, UserService userService,
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
            ConsoleUtil.printHeader("ADMINISTRATOR DASHBOARD");
            displayUserHeader();
            System.out.println(" 1. Patient Management");
            System.out.println(" 2. Doctor Management");
            System.out.println(" 3. Appointment Management");
            System.out.println(" 4. Billing & Financial Ledger");
            System.out.println(" 5. User Account & Access Control");
            System.out.println(" 6. Hospital Reports & Analytics");
            System.out.println(" 7. Security Audit Logs");
            System.out.println(" 8. Change My Password");
            System.out.println(" 9. Logout");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Enter selection", 1, 9);
            switch (choice) {
                case 1:
                    managePatients();
                    break;
                case 2:
                    manageDoctors();
                    break;
                case 3:
                    manageAppointments();
                    break;
                case 4:
                    manageBilling();
                    break;
                case 5:
                    manageUsers();
                    break;
                case 6:
                    showReports();
                    break;
                case 7:
                    viewAuditLogs();
                    break;
                case 8:
                    changePassword();
                    break;
                case 9:
                    ConsoleUtil.printInfo("Logging out of Administrator account...");
                    exit = true;
                    break;
            }
        }
    }

    // -------------------------------------------------------------
    // PATIENT MANAGEMENT
    // -------------------------------------------------------------
    private void managePatients() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printSubHeader("PATIENT MANAGEMENT");
            System.out.println(" 1. Register New Patient");
            System.out.println(" 2. Search Patients (by Name, ID, or Phone)");
            System.out.println(" 3. Update Existing Patient");
            System.out.println(" 4. Deactivate Patient Record");
            System.out.println(" 5. List All Patients");
            System.out.println(" 6. Back to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Select", 1, 6);
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
                    deactivatePatient();
                    break;
                case 5:
                    listAllPatients();
                    break;
                case 6:
                    back = true;
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
            String phone = ConsoleUtil.promptString(scanner, "Phone Number (10-13 digits)", true);
            String email = ConsoleUtil.promptString(scanner, "Email Address (optional)", false);
            String address = ConsoleUtil.promptString(scanner, "Residential Address", false);
            String blood = ConsoleUtil.promptString(scanner, "Blood Group (e.g., A+, O+, B-)", false);
            String emergency = ConsoleUtil.promptString(scanner, "Emergency Contact", false);

            Patient p = patientService.registerPatient(name, age, gender, phone, email, address, blood, emergency, currentUser.getUsername());
            ConsoleUtil.printSuccess("Patient registered successfully! Assigned ID: " + p.getId());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void searchPatients() {
        ConsoleUtil.printSubHeader("SEARCH PATIENTS");
        String q = ConsoleUtil.promptString(scanner, "Enter search keyword (name, phone, blood group, or ID)", true);
        List<Patient> results = patientService.searchPatients(q);
        renderPatientTable(results);
        ConsoleUtil.pause(scanner);
    }

    private void listAllPatients() {
        ConsoleUtil.printSubHeader("ALL PATIENT DIRECTORY");
        List<Patient> list = patientService.getAllPatients();
        renderPatientTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void renderPatientTable(List<Patient> list) {
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
    }

    private void updatePatient() {
        ConsoleUtil.printSubHeader("UPDATE PATIENT");
        String id = ConsoleUtil.promptString(scanner, "Enter Patient ID to update", true);
        try {
            Patient p = patientService.getPatientById(id);
            System.out.println("Editing: " + p.getName() + " (" + p.getId() + ")");
            String name = ConsoleUtil.promptStringWithDefault(scanner, "Full Name", p.getName());
            int age = ConsoleUtil.promptInt(scanner, "Age", 0, 130);
            String phone = ConsoleUtil.promptStringWithDefault(scanner, "Phone", p.getPhone());
            String email = ConsoleUtil.promptStringWithDefault(scanner, "Email", p.getEmail());
            String address = ConsoleUtil.promptStringWithDefault(scanner, "Address", p.getAddress());
            String blood = ConsoleUtil.promptStringWithDefault(scanner, "Blood Group", p.getBloodGroup());
            String emergency = ConsoleUtil.promptStringWithDefault(scanner, "Emergency Contact", p.getEmergencyContact());
            System.out.println("Active Status: 1. Active  2. Inactive");
            int statChoice = ConsoleUtil.promptInt(scanner, "Status", 1, 2);

            patientService.updatePatient(id, name, age, p.getGender(), phone, email, address, blood, emergency,
                    (statChoice == 1), currentUser.getUsername());
            ConsoleUtil.printSuccess("Patient record updated successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void deactivatePatient() {
        ConsoleUtil.printSubHeader("DEACTIVATE PATIENT");
        String id = ConsoleUtil.promptString(scanner, "Enter Patient ID to deactivate", true);
        try {
            patientService.deactivatePatient(id, currentUser.getUsername());
            ConsoleUtil.printSuccess("Patient " + id + " has been marked inactive.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    // -------------------------------------------------------------
    // DOCTOR MANAGEMENT
    // -------------------------------------------------------------
    private void manageDoctors() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printSubHeader("DOCTOR MANAGEMENT");
            System.out.println(" 1. Add New Doctor");
            System.out.println(" 2. Search Doctors");
            System.out.println(" 3. Update Doctor Details");
            System.out.println(" 4. Modify Doctor Availability Status");
            System.out.println(" 5. List All Doctors");
            System.out.println(" 6. Back to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Select", 1, 6);
            switch (choice) {
                case 1:
                    addDoctor();
                    break;
                case 2:
                    searchDoctors();
                    break;
                case 3:
                    updateDoctor();
                    break;
                case 4:
                    changeDoctorStatus();
                    break;
                case 5:
                    listAllDoctors();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private void addDoctor() {
        ConsoleUtil.printSubHeader("ADD NEW DOCTOR");
        try {
            String fullName = ConsoleUtil.promptString(scanner, "Full Name (e.g. Dr. Jane Smith)", true);
            String username = ConsoleUtil.promptString(scanner, "System Username for Doctor", true);
            String password = ConsoleUtil.promptString(scanner, "Initial Password", true);
            String specialization = ConsoleUtil.promptString(scanner, "Specialization (e.g. Cardiology)", true);
            String department = ConsoleUtil.promptString(scanner, "Department (e.g. Cardiology, OPD, Ortho)", true);
            double fee = ConsoleUtil.promptDouble(scanner, "Consultation Fee (INR)", 0.0, 50000.0);
            String phone = ConsoleUtil.promptString(scanner, "Phone Number", false);
            String email = ConsoleUtil.promptString(scanner, "Email Address", false);
            LocalTime from = ConsoleUtil.promptTime(scanner, "Available From Time");
            LocalTime to = ConsoleUtil.promptTime(scanner, "Available To Time");
            String qual = ConsoleUtil.promptString(scanner, "Qualification (e.g. MBBS, MD)", false);

            Doctor d = doctorService.addDoctor(username, password, fullName, email, phone,
                    specialization, department, fee, DoctorStatus.ACTIVE, from, to, qual, currentUser.getUsername());
            ConsoleUtil.printSuccess("Doctor added successfully! ID: " + d.getId() + " | Username: " + d.getUsername());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void searchDoctors() {
        ConsoleUtil.printSubHeader("SEARCH DOCTORS");
        String q = ConsoleUtil.promptString(scanner, "Enter search keyword (name, specialization, department, ID)", true);
        List<Doctor> results = doctorService.searchDoctors(q);
        renderDoctorTable(results);
        ConsoleUtil.pause(scanner);
    }

    private void listAllDoctors() {
        ConsoleUtil.printSubHeader("ALL MEDICAL PRACTITIONERS");
        List<Doctor> list = doctorService.getAllDoctors();
        renderDoctorTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void renderDoctorTable(List<Doctor> list) {
        String[] headers = {"ID", "Name", "Department", "Specialization", "Fee (INR)", "Hours", "Status"};
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
    }

    private void updateDoctor() {
        ConsoleUtil.printSubHeader("UPDATE DOCTOR DETAILS");
        String id = ConsoleUtil.promptString(scanner, "Enter Doctor ID", true);
        try {
            Doctor d = doctorService.getDoctorById(id);
            String fullName = ConsoleUtil.promptStringWithDefault(scanner, "Full Name", d.getFullName());
            String email = ConsoleUtil.promptStringWithDefault(scanner, "Email", d.getEmail());
            String phone = ConsoleUtil.promptStringWithDefault(scanner, "Phone", d.getPhone());
            String spec = ConsoleUtil.promptStringWithDefault(scanner, "Specialization", d.getSpecialization());
            String dept = ConsoleUtil.promptStringWithDefault(scanner, "Department", d.getDepartment());
            double fee = ConsoleUtil.promptDouble(scanner, "Consultation Fee", 0, 50000);
            LocalTime from = ConsoleUtil.promptTime(scanner, "Available From");
            LocalTime to = ConsoleUtil.promptTime(scanner, "Available To");
            String qual = ConsoleUtil.promptStringWithDefault(scanner, "Qualification", d.getQualification());

            doctorService.updateDoctor(id, fullName, email, phone, spec, dept, fee, d.getStatus(), from, to, qual, currentUser.getUsername());
            ConsoleUtil.printSuccess("Doctor record updated successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void changeDoctorStatus() {
        ConsoleUtil.printSubHeader("MODIFY DOCTOR AVAILABILITY STATUS");
        String id = ConsoleUtil.promptString(scanner, "Enter Doctor ID", true);
        try {
            Doctor d = doctorService.getDoctorById(id);
            System.out.println("Doctor: Dr. " + d.getFullName() + " | Current Status: " + d.getStatus());
            System.out.println(" 1. ACTIVE  2. ON_LEAVE  3. INACTIVE");
            int opt = ConsoleUtil.promptInt(scanner, "Select New Status", 1, 3);
            DoctorStatus newStatus = (opt == 1) ? DoctorStatus.ACTIVE : (opt == 2 ? DoctorStatus.ON_LEAVE : DoctorStatus.INACTIVE);
            doctorService.setDoctorStatus(id, newStatus, currentUser.getUsername());
            ConsoleUtil.printSuccess("Status for " + id + " updated to " + newStatus);
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    // -------------------------------------------------------------
    // APPOINTMENT MANAGEMENT
    // -------------------------------------------------------------
    private void manageAppointments() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printSubHeader("APPOINTMENT MANAGEMENT");
            System.out.println(" 1. Book New Appointment");
            System.out.println(" 2. Reschedule Appointment");
            System.out.println(" 3. Cancel Appointment");
            System.out.println(" 4. View Today's Appointments");
            System.out.println(" 5. View All Upcoming Appointments");
            System.out.println(" 6. Search Appointments");
            System.out.println(" 7. Back to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Select", 1, 7);
            switch (choice) {
                case 1:
                    bookAppointment();
                    break;
                case 2:
                    rescheduleAppointment();
                    break;
                case 3:
                    cancelAppointment();
                    break;
                case 4:
                    viewTodayAppointments();
                    break;
                case 5:
                    viewUpcomingAppointments();
                    break;
                case 6:
                    searchAppointments();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private void bookAppointment() {
        ConsoleUtil.printSubHeader("BOOK NEW APPOINTMENT");
        try {
            String patientId = ConsoleUtil.promptString(scanner, "Patient ID (e.g. PAT-1001)", true);
            String doctorId = ConsoleUtil.promptString(scanner, "Doctor ID (e.g. DOC-2001)", true);
            LocalDate date = ConsoleUtil.promptDate(scanner, "Appointment Date", false);
            LocalTime time = ConsoleUtil.promptTime(scanner, "Appointment Time");
            String reason = ConsoleUtil.promptString(scanner, "Consultation Reason / Symptoms", true);

            Appointment appt = appointmentService.bookAppointment(patientId, doctorId, date, time, reason, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment booked successfully! ID: " + appt.getId());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void rescheduleAppointment() {
        ConsoleUtil.printSubHeader("RESCHEDULE APPOINTMENT");
        String id = ConsoleUtil.promptString(scanner, "Enter Appointment ID", true);
        try {
            LocalDate newDate = ConsoleUtil.promptDate(scanner, "New Appointment Date", false);
            LocalTime newTime = ConsoleUtil.promptTime(scanner, "New Appointment Time");

            appointmentService.rescheduleAppointment(id, newDate, newTime, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment " + id + " rescheduled successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void cancelAppointment() {
        ConsoleUtil.printSubHeader("CANCEL APPOINTMENT");
        String id = ConsoleUtil.promptString(scanner, "Enter Appointment ID to cancel", true);
        try {
            String reason = ConsoleUtil.promptString(scanner, "Cancellation Reason", true);
            appointmentService.cancelAppointment(id, reason, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment " + id + " cancelled.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewTodayAppointments() {
        ConsoleUtil.printSubHeader("TODAY'S APPOINTMENTS (" + LocalDate.now() + ")");
        List<Appointment> list = appointmentService.getTodaysAppointments();
        renderAppointmentTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void viewUpcomingAppointments() {
        ConsoleUtil.printSubHeader("UPCOMING APPOINTMENTS");
        List<Appointment> list = appointmentService.getUpcomingAppointments();
        renderAppointmentTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void searchAppointments() {
        ConsoleUtil.printSubHeader("SEARCH APPOINTMENTS");
        String q = ConsoleUtil.promptString(scanner, "Search keyword (Patient ID, Doctor ID, Date, ID)", true);
        List<Appointment> list = appointmentService.searchAppointments(q);
        renderAppointmentTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void renderAppointmentTable(List<Appointment> list) {
        String[] headers = {"ID", "Patient", "Doctor", "Date", "Time", "Status", "Reason"};
        List<String[]> rows = new ArrayList<>();
        for (Appointment a : list) {
            rows.add(new String[]{
                    a.getId(), a.getPatientId(), a.getDoctorId(),
                    DateTimeUtil.formatDate(a.getAppointmentDate()),
                    DateTimeUtil.formatTime(a.getAppointmentTime()),
                    a.getStatus().name(), a.getReason()
            });
        }
        ConsoleUtil.printTable(headers, rows);
    }

    // -------------------------------------------------------------
    // BILLING MANAGEMENT
    // -------------------------------------------------------------
    private void manageBilling() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printSubHeader("BILLING & FINANCIAL LEDGER");
            System.out.println(" 1. Generate New Bill");
            System.out.println(" 2. Record Payment on Bill");
            System.out.println(" 3. View / Print Invoice Receipt");
            System.out.println(" 4. List All Invoices");
            System.out.println(" 5. List Pending / Outstanding Bills");
            System.out.println(" 6. Back to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Select", 1, 6);
            switch (choice) {
                case 1:
                    generateBill();
                    break;
                case 2:
                    recordPayment();
                    break;
                case 3:
                    viewInvoice();
                    break;
                case 4:
                    listAllBills();
                    break;
                case 5:
                    listPendingBills();
                    break;
                case 6:
                    back = true;
                    break;
            }
        }
    }

    private void generateBill() {
        ConsoleUtil.printSubHeader("GENERATE INVOICE");
        try {
            String patientId = ConsoleUtil.promptString(scanner, "Patient ID", true);
            String apptId = ConsoleUtil.promptString(scanner, "Appointment ID (optional, press Enter to skip)", false);
            double consultationFee = ConsoleUtil.promptDouble(scanner, "Consultation Fee (INR)", 0.0, 100000.0);
            double serviceCharges = ConsoleUtil.promptDouble(scanner, "Service / Lab Charges (INR)", 0.0, 200000.0);
            double medicineCharges = ConsoleUtil.promptDouble(scanner, "Medicine Charges (INR)", 0.0, 200000.0);
            double roomCharges = ConsoleUtil.promptDouble(scanner, "Room Charges (INR)", 0.0, 500000.0);
            double discount = ConsoleUtil.promptDouble(scanner, "Concession / Discount (INR)", 0.0, 100000.0);
            double taxRate = ConsoleUtil.promptDouble(scanner, "Tax Rate (%)", 0.0, 30.0);

            Bill bill = billingService.generateBill(apptId.isEmpty() ? "N/A" : apptId, patientId,
                    consultationFee, serviceCharges, medicineCharges, roomCharges, discount, taxRate, currentUser.getUsername());

            ConsoleUtil.printSuccess("Bill generated successfully! Invoice ID: " + bill.getId() + " | Final Amount: INR " + bill.getFinalAmount());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void recordPayment() {
        ConsoleUtil.printSubHeader("RECORD PAYMENT");
        String billId = ConsoleUtil.promptString(scanner, "Enter Bill ID", true);
        try {
            Bill bill = billingService.getBillById(billId);
            System.out.printf("Bill: %s | Total: INR %.2f | Paid: INR %.2f | Outstanding: INR %.2f\n",
                    bill.getId(), bill.getFinalAmount(), bill.getPaidAmount(), bill.getBalanceAmount());
            if (bill.getBalanceAmount() <= 0.001) {
                ConsoleUtil.printInfo("This bill is already fully settled.");
                ConsoleUtil.pause(scanner);
                return;
            }

            double amount = ConsoleUtil.promptDouble(scanner, "Payment Amount Received (INR)", 0.01, bill.getBalanceAmount());
            billingService.recordPayment(billId, amount, currentUser.getUsername());
            ConsoleUtil.printSuccess("Payment recorded successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewInvoice() {
        ConsoleUtil.printSubHeader("VIEW INVOICE RECEIPT");
        String billId = ConsoleUtil.promptString(scanner, "Enter Bill ID", true);
        try {
            Bill bill = billingService.getBillById(billId);
            Patient patient = null;
            try {
                patient = patientService.getPatientById(bill.getPatientId());
            } catch (Exception ignored) {}

            Doctor doctor = null;
            if (!bill.getAppointmentId().equalsIgnoreCase("N/A")) {
                try {
                    Appointment appt = appointmentService.getAppointmentById(bill.getAppointmentId());
                    doctor = doctorService.getDoctorById(appt.getDoctorId());
                } catch (Exception ignored) {}
            }

            String receipt = billingService.renderInvoiceText(bill, patient, doctor);
            System.out.println(receipt);
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void listAllBills() {
        ConsoleUtil.printSubHeader("ALL BILLING INVOICES");
        List<Bill> list = billingService.getAllBills();
        renderBillTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void listPendingBills() {
        ConsoleUtil.printSubHeader("OUTSTANDING / PENDING BILLS");
        List<Bill> list = billingService.getPendingBills();
        renderBillTable(list);
        ConsoleUtil.pause(scanner);
    }

    private void renderBillTable(List<Bill> list) {
        String[] headers = {"Invoice ID", "Patient", "Appt ID", "Total (INR)", "Paid (INR)", "Due (INR)", "Status", "Date"};
        List<String[]> rows = new ArrayList<>();
        for (Bill b : list) {
            rows.add(new String[]{
                    b.getId(), b.getPatientId(), b.getAppointmentId(),
                    String.format("%.2f", b.getFinalAmount()),
                    String.format("%.2f", b.getPaidAmount()),
                    String.format("%.2f", b.getBalanceAmount()),
                    b.getPaymentStatus().name(),
                    DateTimeUtil.formatDate(b.getBillingDate())
            });
        }
        ConsoleUtil.printTable(headers, rows);
    }

    // -------------------------------------------------------------
    // USER ACCOUNT MANAGEMENT
    // -------------------------------------------------------------
    private void manageUsers() {
        boolean back = false;
        while (!back) {
            ConsoleUtil.printSubHeader("USER ACCESS & CREDENTIALS");
            System.out.println(" 1. Create Staff Account (Admin, Receptionist)");
            System.out.println(" 2. List All User Accounts");
            System.out.println(" 3. Toggle Account Status (Activate/Deactivate)");
            System.out.println(" 4. Back to Main Menu");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Select", 1, 4);
            switch (choice) {
                case 1:
                    createStaffUser();
                    break;
                case 2:
                    listAllUsers();
                    break;
                case 3:
                    toggleUserStatus();
                    break;
                case 4:
                    back = true;
                    break;
            }
        }
    }

    private void createStaffUser() {
        ConsoleUtil.printSubHeader("CREATE STAFF ACCOUNT");
        try {
            String username = ConsoleUtil.promptString(scanner, "Username", true);
            String password = ConsoleUtil.promptString(scanner, "Password", true);
            String fullName = ConsoleUtil.promptString(scanner, "Full Name", true);
            String email = ConsoleUtil.promptString(scanner, "Email", false);
            String phone = ConsoleUtil.promptString(scanner, "Phone", false);

            System.out.println("Select Role: 1. ADMIN  2. RECEPTIONIST");
            int rChoice = ConsoleUtil.promptInt(scanner, "Role", 1, 2);
            UserRole role = (rChoice == 1) ? UserRole.ADMIN : UserRole.RECEPTIONIST;

            String extra1 = "";
            String extra2 = "";
            if (role == UserRole.ADMIN) {
                extra1 = ConsoleUtil.promptString(scanner, "Department Access", false);
            } else {
                extra1 = ConsoleUtil.promptString(scanner, "Shift (DAY/NIGHT)", false);
                extra2 = ConsoleUtil.promptString(scanner, "Desk Number", false);
            }

            User user = userService.createUser(username, password, fullName, email, phone, role, extra1, extra2, currentUser.getUsername());
            ConsoleUtil.printSuccess("User created successfully! ID: " + user.getId() + " | Role: " + user.getRole());
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void listAllUsers() {
        ConsoleUtil.printSubHeader("USER ACCOUNT DIRECTORY");
        List<User> list = userService.getAllUsers();
        String[] headers = {"ID", "Username", "Full Name", "Role", "Active", "Created"};
        List<String[]> rows = new ArrayList<>();
        for (User u : list) {
            rows.add(new String[]{
                    u.getId(), u.getUsername(), u.getFullName(), u.getRole().name(),
                    u.isActive() ? "Active" : "Disabled",
                    DateTimeUtil.formatDateTime(u.getCreatedAt())
            });
        }
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.pause(scanner);
    }

    private void toggleUserStatus() {
        ConsoleUtil.printSubHeader("ACTIVATE / DEACTIVATE USER");
        String id = ConsoleUtil.promptString(scanner, "Enter User ID", true);
        if (id.equalsIgnoreCase(currentUser.getId())) {
            ConsoleUtil.printWarning("You cannot deactivate your own logged-in account.");
            ConsoleUtil.pause(scanner);
            return;
        }
        try {
            Optional<User> opt = userService.getUserById(id);
            if (opt.isEmpty()) {
                ConsoleUtil.printError("User not found.");
            } else {
                User u = opt.get();
                boolean target = !u.isActive();
                userService.setUserActiveStatus(id, target, currentUser.getUsername());
                ConsoleUtil.printSuccess("User account " + id + " is now " + (target ? "ACTIVE" : "DEACTIVATED") + ".");
            }
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    // -------------------------------------------------------------
    // REPORTS & ANALYTICS
    // -------------------------------------------------------------
    private void showReports() {
        ConsoleUtil.printHeader("HOSPITAL ANALYTICS & EXECUTIVE DASHBOARD");
        ReportService.DailySummary summary = reportService.generateDailySummary(LocalDate.now());

        System.out.println("Report Generation Date: " + DateTimeUtil.formatDate(summary.date));
        System.out.println();
        System.out.println("  PATIENTS STATS:");
        System.out.println("    - Total Registered : " + summary.totalPatients);
        System.out.println("    - Active Patients   : " + summary.activePatients);
        System.out.println();
        System.out.println("  MEDICAL STAFF STATS:");
        System.out.println("    - Total Doctors     : " + summary.totalDoctors);
        System.out.println("    - Active On-Duty    : " + summary.activeDoctors);
        System.out.println("    - By Department     :");
        for (Map.Entry<String, Long> entry : summary.doctorsByDept.entrySet()) {
            System.out.printf("        * %-20s: %d doctor(s)\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
        System.out.println("  TODAY'S APPOINTMENT WORKFLOW:");
        System.out.println("    - Total for Today   : " + summary.todaysAppointments);
        System.out.println("    - Completed Visits  : " + summary.completedAppointments);
        System.out.println("    - Cancelled Visits  : " + summary.cancelledAppointments);
        System.out.println("    - Scheduled/Pending : " + summary.upcomingAppointments);
        System.out.println();
        System.out.println("  FINANCIAL REVENUE SUMMARY:");
        System.out.printf("    - Total Billed      : INR %,.2f\n", summary.totalRevenueBilled);
        System.out.printf("    - Cash Collected    : INR %,.2f\n", summary.totalRevenueCollected);
        System.out.printf("    - Outstanding Due   : INR %,.2f\n", summary.totalRevenuePending);
        ConsoleUtil.printDivider();

        System.out.println("Would you like to export this report to a CSV file? (1. Yes  2. No)");
        int exp = ConsoleUtil.promptInt(scanner, "Select", 1, 2);
        if (exp == 1) {
            String path = "data/daily_summary_" + DateTimeUtil.formatDate(summary.date) + ".csv";
            boolean ok = reportService.exportDailySummaryCsv(summary, path);
            if (ok) {
                ConsoleUtil.printSuccess("Report exported successfully to: " + path);
            }
        }
        ConsoleUtil.pause(scanner);
    }

    private void viewAuditLogs() {
        ConsoleUtil.printSubHeader("SYSTEM AUDIT TRAIL");
        int count = ConsoleUtil.promptInt(scanner, "Number of recent log events to view", 5, 100);
        List<String> logs = auditService.getRecentLogs(count);
        if (logs.isEmpty()) {
            System.out.println("(No audit logs recorded yet)");
        } else {
            for (String line : logs) {
                System.out.println(line);
            }
        }
        ConsoleUtil.pause(scanner);
    }

    private void changePassword() {
        ConsoleUtil.printSubHeader("CHANGE PASSWORD");
        String oldPass = ConsoleUtil.promptString(scanner, "Current Password", true);
        String newPass = ConsoleUtil.promptString(scanner, "New Password (min 4 characters)", true);
        try {
            userService.changePassword(currentUser.getUsername(), oldPass, newPass);
            ConsoleUtil.printSuccess("Password changed successfully.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }
}
