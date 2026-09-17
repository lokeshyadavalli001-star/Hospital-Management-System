package com.hms.ui;

import com.hms.model.User;
import com.hms.service.AppointmentService;
import com.hms.service.AuditService;
import com.hms.service.BillingService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;
import com.hms.service.ReportService;
import com.hms.service.UserService;
import com.hms.util.ConsoleUtil;

import java.util.Scanner;

/**
 * Abstract console menu controller managing shared services and common UI lifecycle methods.
 */
public abstract class ConsoleMenu {

    protected final Scanner scanner;
    protected final User currentUser;
    protected final UserService userService;
    protected final PatientService patientService;
    protected final DoctorService doctorService;
    protected final AppointmentService appointmentService;
    protected final BillingService billingService;
    protected final ReportService reportService;
    protected final AuditService auditService;

    public ConsoleMenu(Scanner scanner,
                       User currentUser,
                       UserService userService,
                       PatientService patientService,
                       DoctorService doctorService,
                       AppointmentService appointmentService,
                       BillingService billingService,
                       ReportService reportService,
                       AuditService auditService) {
        this.scanner = scanner;
        this.currentUser = currentUser;
        this.userService = userService;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
        this.reportService = reportService;
        this.auditService = auditService;
    }

    /**
     * Entry point to launch the role-specific interactive menu loop.
     */
    public abstract void showMenu();

    protected void displayUserHeader() {
        System.out.println();
        System.out.println("Logged in as: " + currentUser.getFullName() + " (" + currentUser.getUsername()
                + ") | Role: " + currentUser.getRole());
        System.out.println(currentUser.getRoleDescription());
        ConsoleUtil.printDivider();
    }
}
