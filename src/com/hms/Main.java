package com.hms;

import com.hms.exception.AuthenticationException;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.Gender;
import com.hms.model.enums.PaymentStatus;
import com.hms.model.enums.UserRole;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorRepository;
import com.hms.repository.PatientRepository;
import com.hms.repository.UserRepository;
import com.hms.service.AppointmentService;
import com.hms.service.AuditService;
import com.hms.service.BillingService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;
import com.hms.service.ReportService;
import com.hms.service.UserService;
import com.hms.ui.AdminMenu;
import com.hms.ui.DoctorMenu;
import com.hms.ui.LocalHostServer;
import com.hms.ui.PatientMenu;
import com.hms.ui.ReceptionistMenu;
import com.hms.util.ConsoleUtil;
import com.hms.util.PasswordUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Main application entry point for the Hospital Management System (HMS).
 * Bootstraps layered dependencies, seeds initial records if absent, and dispatches authenticated sessions.
 */
public class Main {

    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.csv";
    private static final String PATIENTS_FILE = DATA_DIR + File.separator + "patients.csv";
    private static final String DOCTORS_FILE = DATA_DIR + File.separator + "doctors.csv";
    private static final String APPOINTMENTS_FILE = DATA_DIR + File.separator + "appointments.csv";
    private static final String BILLS_FILE = DATA_DIR + File.separator + "bills.csv";
    private static final String AUDIT_FILE = DATA_DIR + File.separator + "audit.log";

    public static void main(String[] args) {
        // Initialize layers
        AuditService auditService = new AuditService(AUDIT_FILE);
        UserRepository userRepository = new UserRepository(USERS_FILE);
        PatientRepository patientRepository = new PatientRepository(PATIENTS_FILE);
        DoctorRepository doctorRepository = new DoctorRepository(DOCTORS_FILE);
        AppointmentRepository appointmentRepository = new AppointmentRepository(APPOINTMENTS_FILE);
        BillRepository billRepository = new BillRepository(BILLS_FILE);

        UserService userService = new UserService(userRepository, auditService);
        PatientService patientService = new PatientService(patientRepository, auditService);
        DoctorService doctorService = new DoctorService(doctorRepository, userRepository, auditService);
        AppointmentService appointmentService = new AppointmentService(appointmentRepository, patientService, doctorService, auditService);
        BillingService billingService = new BillingService(billRepository, patientService, auditService);
        ReportService reportService = new ReportService(patientService, doctorService, appointmentService, billingService);

        // Seed initial demo data if clean installation
        seedInitialDataIfEmpty(userRepository, patientRepository, doctorRepository, appointmentRepository, billRepository);

        auditService.log("SYSTEM", "STARTUP", "SYSTEM", "Hospital Management System initialized successfully.");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printWelcomeBanner();
            System.out.println(" 1. Sign In / Authenticate");
            System.out.println(" 2. View Demo Login Credentials");
            System.out.println(" 3. Hospital Information & Contacts");
            System.out.println(" 4. Launch Localhost Web Dashboard (http://localhost:8080)");
            System.out.println(" 5. Exit Application");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Enter selection", 1, 5);
            switch (choice) {
                case 1:
                    login(scanner, userService, patientService, doctorService,
                            appointmentService, billingService, reportService, auditService);
                    break;
                case 2:
                    showDemoCredentials(scanner);
                    break;
                case 3:
                    showHospitalInfo(scanner);
                    break;
                case 4:
                    launchWebDashboard(patientService, doctorService, appointmentService, billingService, reportService, auditService, scanner);
                    break;
                case 5:
                    ConsoleUtil.printInfo("Terminating session. Thank you for using City General HMS.");
                    auditService.log("SYSTEM", "SHUTDOWN", "SYSTEM", "Application shut down by user.");
                    running = false;
                    break;
            }
        }
        scanner.close();
    }

    private static void launchWebDashboard(PatientService patientService, DoctorService doctorService,
                                           AppointmentService appointmentService, BillingService billingService,
                                           ReportService reportService, AuditService auditService, Scanner scanner) {
        try {
            LocalHostServer webServer = new LocalHostServer(patientService, doctorService, appointmentService, billingService, reportService, auditService);
            webServer.start();
            System.out.println("[INFO] Server is running in background on http://localhost:8080");
            System.out.println("[INFO] Open your web browser and navigate to http://localhost:8080");
            ConsoleUtil.pause(scanner);
        } catch (Exception e) {
            ConsoleUtil.printError("Could not start localhost server: " + e.getMessage());
            ConsoleUtil.pause(scanner);
        }
    }

    private static void login(Scanner scanner, UserService userService, PatientService patientService,
                              DoctorService doctorService, AppointmentService appointmentService,
                              BillingService billingService, ReportService reportService, AuditService auditService) {
        ConsoleUtil.printHeader("USER AUTHENTICATION");
        String username = ConsoleUtil.promptString(scanner, "Username", true);
        String password = ConsoleUtil.promptString(scanner, "Password", true);

        try {
            User user = userService.authenticate(username, password);
            ConsoleUtil.printSuccess("Authentication successful! Welcome, " + user.getFullName() + ".");

            // Polymorphic menu routing based on UserRole
            switch (user.getRole()) {
                case ADMIN:
                    new AdminMenu(scanner, user, userService, patientService, doctorService,
                            appointmentService, billingService, reportService, auditService).showMenu();
                    break;
                case RECEPTIONIST:
                    new ReceptionistMenu(scanner, user, userService, patientService, doctorService,
                            appointmentService, billingService, reportService, auditService).showMenu();
                    break;
                case DOCTOR:
                    new DoctorMenu(scanner, user, userService, patientService, doctorService,
                            appointmentService, billingService, reportService, auditService).showMenu();
                    break;
                case PATIENT:
                    new PatientMenu(scanner, user, userService, patientService, doctorService,
                            appointmentService, billingService, reportService, auditService).showMenu();
                    break;
                default:
                    ConsoleUtil.printError("Unknown role assigned to user: " + user.getRole());
                    break;
            }
        } catch (AuthenticationException e) {
            ConsoleUtil.printError("Login failed: " + e.getMessage());
            ConsoleUtil.pause(scanner);
        }
    }

    private static void showDemoCredentials(Scanner scanner) {
        ConsoleUtil.printHeader("DEMO CREDENTIALS (FOR EVALUATION)");
        System.out.println("Use the following pre-configured credentials to evaluate the system roles:");
        System.out.println();
        String[] headers = {"Role", "Username", "Password", "Simulated Actor Name"};
        java.util.List<String[]> rows = java.util.List.of(
                new String[]{"ADMIN", "admin", "Admin@123", "Chief Administrator (Dr. Rajesh Mehta)"},
                new String[]{"RECEPTIONIST", "recep", "Recep@123", "Front Desk Desk-01 (Ananya Sen)"},
                new String[]{"DOCTOR", "dr_sharma", "Doctor@123", "Consultant Cardiologist (Dr. Priya Sharma)"},
                new String[]{"DOCTOR", "dr_verma", "Doctor@123", "Senior Neurologist (Dr. Arun Verma)"},
                new String[]{"PATIENT", "pat_rahul", "Patient@123", "Outpatient (Rahul Verma, PAT-1001)"}
        );
        ConsoleUtil.printTable(headers, rows);
        ConsoleUtil.printInfo("All passwords are stored with salted SHA-256 hashes in data/users.csv.");
        ConsoleUtil.pause(scanner);
    }

    private static void showHospitalInfo(Scanner scanner) {
        ConsoleUtil.printHeader("CITY GENERAL HOSPITAL & RESEARCH CENTER");
        System.out.println("Address: 42 Healthcare Boulevard, Tech City, Sector 5");
        System.out.println("Emergency Hotline: +91-98765-43210 (24x7 Ambulance & Trauma Care)");
        System.out.println("OPD Enquiries    : 080-2345-6789 | enquiry@cityhospital.example.com");
        System.out.println("Academic Context : VITyarthi Core Java Capstone Project");
        ConsoleUtil.pause(scanner);
    }

    private static void printWelcomeBanner() {
        System.out.println();
        System.out.println("================================================================================");
        System.out.println("                 HOSPITAL MANAGEMENT SYSTEM (HMS) - CORE JAVA                   ");
        System.out.println("                     City General Hospital & Health Network                     ");
        System.out.println("================================================================================");
    }

    private static void seedInitialDataIfEmpty(UserRepository userRepo, PatientRepository patientRepo,
                                               DoctorRepository docRepo, AppointmentRepository apptRepo,
                                               BillRepository billRepo) {
        if (userRepo.count() == 0) {
            String salt = PasswordUtil.generateSalt();

            // Admin account
            userRepo.save(new com.hms.model.Admin("USR-5001", "admin",
                    PasswordUtil.hashPassword("Admin@123", salt), salt,
                    "Dr. Rajesh Mehta", "admin@cityhospital.example.com", "9876543210",
                    true, LocalDateTime.now().minusDays(60), "ALL_DEPARTMENTS"));

            // Receptionist account
            userRepo.save(new com.hms.model.Receptionist("USR-5002", "recep",
                    PasswordUtil.hashPassword("Recep@123", salt), salt,
                    "Ananya Sen", "ananya@cityhospital.example.com", "9876543211",
                    true, LocalDateTime.now().minusDays(45), "MORNING", "DESK-101"));

            // Patient account
            userRepo.save(new com.hms.model.Admin("USR-5003", "pat_rahul",
                    PasswordUtil.hashPassword("Patient@123", salt), salt,
                    "Rahul Verma", "rahul.verma@example.com", "9876543220",
                    true, LocalDateTime.now().minusDays(30), "PATIENT_VIEW") {
                @Override
                public UserRole getRole() {
                    return UserRole.PATIENT;
                }
            });
        }

        if (docRepo.count() == 0) {
            String salt = PasswordUtil.generateSalt();
            Doctor doc1 = new Doctor("DOC-2001", "dr_sharma",
                    PasswordUtil.hashPassword("Doctor@123", salt), salt,
                    "Priya Sharma", "dr.sharma@cityhospital.example.com", "9876543212",
                    true, LocalDateTime.now().minusDays(90),
                    "Cardiology", "Cardiology", 800.0, DoctorStatus.ACTIVE,
                    LocalTime.of(9, 0), LocalTime.of(15, 0), "MD, DM (Cardiology)");
            docRepo.save(doc1);
            userRepo.save(doc1);

            Doctor doc2 = new Doctor("DOC-2002", "dr_verma",
                    PasswordUtil.hashPassword("Doctor@123", salt), salt,
                    "Arun Verma", "dr.verma@cityhospital.example.com", "9876543213",
                    true, LocalDateTime.now().minusDays(80),
                    "Neurology", "Neurology", 1000.0, DoctorStatus.ACTIVE,
                    LocalTime.of(10, 0), LocalTime.of(16, 0), "MS, MCh (Neurosurgery)");
            docRepo.save(doc2);
            userRepo.save(doc2);

            Doctor doc3 = new Doctor("DOC-2003", "dr_kapoor",
                    PasswordUtil.hashPassword("Doctor@123", salt), salt,
                    "Neha Kapoor", "dr.kapoor@cityhospital.example.com", "9876543214",
                    true, LocalDateTime.now().minusDays(60),
                    "Orthopedics", "Orthopedics", 600.0, DoctorStatus.ACTIVE,
                    LocalTime.of(11, 0), LocalTime.of(18, 0), "MS (Ortho), Fellowship Joint Replacement");
            docRepo.save(doc3);
            userRepo.save(doc3);
        }

        if (patientRepo.count() == 0) {
            patientRepo.save(new Patient("PAT-1001", "Rahul Verma", 34, Gender.MALE,
                    "9876543220", "rahul.verma@example.com", "Flat 4B, Lotus Enclave, Sector 21",
                    "B+", "9876543221", LocalDate.now().minusDays(15), true));

            patientRepo.save(new Patient("PAT-1002", "Sunita Nair", 52, Gender.FEMALE,
                    "9876543222", "sunita.nair@example.com", "Villa 12, Palm Meadows",
                    "O+", "9876543223", LocalDate.now().minusDays(10), true));

            patientRepo.save(new Patient("PAT-1003", "Karan Johar", 28, Gender.MALE,
                    "9876543224", "karan.j@example.com", "7th Cross, Koramangala",
                    "AB+", "9876543225", LocalDate.now().minusDays(5), true));
        }

        if (apptRepo.count() == 0) {
            apptRepo.save(new com.hms.model.Appointment("APT-3001", "PAT-1001", "DOC-2001",
                    LocalDate.now(), LocalTime.of(10, 0),
                    "Chest discomfort & routine ECG check", com.hms.model.enums.AppointmentStatus.SCHEDULED,
                    "", LocalDateTime.now().minusDays(1)));

            apptRepo.save(new com.hms.model.Appointment("APT-3002", "PAT-1002", "DOC-2002",
                    LocalDate.now(), LocalTime.of(11, 0),
                    "Chronic migraine consultation", com.hms.model.enums.AppointmentStatus.SCHEDULED,
                    "", LocalDateTime.now().minusDays(2)));
        }

        if (billRepo.count() == 0) {
            com.hms.model.Bill b1 = new com.hms.model.Bill("BIL-4001", "APT-3001", "PAT-1001",
                    800.0, 450.0, 250.0, 0.0, 100.0, 5.0, 500.0, PaymentStatus.PARTIAL,
                    LocalDate.now().minusDays(1), null);
            billRepo.save(b1);

            com.hms.model.Bill b2 = new com.hms.model.Bill("BIL-4002", "APT-3002", "PAT-1002",
                    1000.0, 300.0, 150.0, 0.0, 0.0, 5.0, 1522.5, PaymentStatus.PAID,
                    LocalDate.now().minusDays(2), LocalDate.now().minusDays(2));
            billRepo.save(b2);
        }
    }
}
