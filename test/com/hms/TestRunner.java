package com.hms;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Zero-dependency automated test runner and assertion framework for HMS.
 * Executes comprehensive unit and integration tests from the terminal.
 */
public class TestRunner {

    private static int totalPassed = 0;
    private static int totalFailed = 0;
    private static final List<String> failureMessages = new ArrayList<>();

    public static void assertTrue(boolean condition, String message) {
        if (condition) {
            totalPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            totalFailed++;
            String failure = "  [FAIL] " + message;
            System.err.println(failure);
            failureMessages.add(message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (match) {
            totalPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            totalFailed++;
            String failure = String.format("  [FAIL] %s - Expected: <%s>, but was: <%s>", message, expected, actual);
            System.err.println(failure);
            failureMessages.add(failure);
        }
    }

    public static void assertEquals(double expected, double actual, double delta, String message) {
        if (Math.abs(expected - actual) <= delta) {
            totalPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            totalFailed++;
            String failure = String.format("  [FAIL] %s - Expected: <%.2f>, but was: <%.2f>", message, expected, actual);
            System.err.println(failure);
            failureMessages.add(failure);
        }
    }

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                  HMS AUTOMATED CLI TEST SUITE EXECUTION                        ");
        System.out.println("================================================================================");

        // Prepare isolated test sandbox directory
        String testDir = "data/test_sandbox";
        new File(testDir).mkdirs();

        String testUsers = testDir + "/users.csv";
        String testPatients = testDir + "/patients.csv";
        String testDoctors = testDir + "/doctors.csv";
        String testAppts = testDir + "/appointments.csv";
        String testBills = testDir + "/bills.csv";
        String testAudit = testDir + "/audit.log";

        // Clean any old sandbox files
        for (String path : new String[]{testUsers, testPatients, testDoctors, testAppts, testBills, testAudit}) {
            new File(path).delete();
        }

        AuditService auditService = new AuditService(testAudit);
        UserRepository userRepo = new UserRepository(testUsers);
        PatientRepository patientRepo = new PatientRepository(testPatients);
        DoctorRepository docRepo = new DoctorRepository(testDoctors);
        AppointmentRepository apptRepo = new AppointmentRepository(testAppts);
        BillRepository billRepo = new BillRepository(testBills);

        UserService userService = new UserService(userRepo, auditService);
        PatientService patientService = new PatientService(patientRepo, auditService);
        DoctorService doctorService = new DoctorService(docRepo, userRepo, auditService);
        AppointmentService apptService = new AppointmentService(apptRepo, patientService, doctorService, auditService);
        BillingService billingService = new BillingService(billRepo, patientService, auditService);
        ReportService reportService = new ReportService(patientService, doctorService, apptService, billingService);

        // Run test suites
        new AuthenticationTest().run(userService);
        new PatientServiceTest().run(patientService);
        new DoctorServiceTest().run(doctorService);
        new AppointmentServiceTest().run(apptService, patientService, doctorService);
        new BillingServiceTest().run(billingService, patientService);

        // Print final verdict
        int totalTests = totalPassed + totalFailed;
        double passPct = (totalTests > 0) ? ((double) totalPassed / totalTests) * 100.0 : 0.0;

        System.out.println();
        System.out.println("================================================================================");
        System.out.printf("  TEST EXECUTION SUMMARY: Total: %d | Passed: %d | Failed: %d (%.1f%% Success)\n",
                totalTests, totalPassed, totalFailed, passPct);
        System.out.println("================================================================================");

        if (totalFailed > 0) {
            System.err.println("\nFailures detected:");
            for (String f : failureMessages) {
                System.err.println(" - " + f);
            }
            System.exit(1);
        } else {
            System.out.println("\nALL TEST CASES PASSED SUCCESSFULLY!");
        }
    }
}
