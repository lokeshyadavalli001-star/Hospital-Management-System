package com.hms.ui;

import com.hms.exception.HospitalException;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.User;
import com.hms.model.enums.DoctorStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Menu controller for Medical Consultants/Doctors.
 * Focuses on clinical schedules, patient consultations, and notes recording.
 */
public class DoctorMenu extends ConsoleMenu {

    private final String doctorId;

    public DoctorMenu(Scanner scanner, User currentUser, UserService userService,
                      PatientService patientService, DoctorService doctorService,
                      AppointmentService appointmentService, BillingService billingService,
                      ReportService reportService, AuditService auditService) {
        super(scanner, currentUser, userService, patientService, doctorService,
                appointmentService, billingService, reportService, auditService);

        // Resolve Doctor ID (either directly or by username)
        if (currentUser instanceof Doctor) {
            this.doctorId = currentUser.getId();
        } else {
            Optional<Doctor> dOpt = doctorService.getAllDoctors().stream()
                    .filter(d -> d.getUsername().equalsIgnoreCase(currentUser.getUsername()))
                    .findFirst();
            this.doctorId = dOpt.map(Doctor::getId).orElse(currentUser.getId());
        }
    }

    @Override
    public void showMenu() {
        boolean exit = false;
        while (!exit) {
            ConsoleUtil.printHeader("DOCTOR CLINICAL CONSOLE");
            displayUserHeader();
            System.out.println("Doctor ID: " + doctorId);
            System.out.println();
            System.out.println(" 1. View Today's Appointments");
            System.out.println(" 2. View All Assigned Appointments");
            System.out.println(" 3. Complete Consultation & Record Notes");
            System.out.println(" 4. Look Up Patient Medical Demographics");
            System.out.println(" 5. Update My Availability Status");
            System.out.println(" 6. Change Password");
            System.out.println(" 7. Logout");
            System.out.println();

            int choice = ConsoleUtil.promptInt(scanner, "Enter selection", 1, 7);
            switch (choice) {
                case 1:
                    viewTodayAppointments();
                    break;
                case 2:
                    viewAllAppointments();
                    break;
                case 3:
                    completeConsultation();
                    break;
                case 4:
                    lookupPatient();
                    break;
                case 5:
                    updateAvailability();
                    break;
                case 6:
                    changePassword();
                    break;
                case 7:
                    ConsoleUtil.printInfo("Logging out of Doctor console...");
                    exit = true;
                    break;
            }
        }
    }

    private void viewTodayAppointments() {
        ConsoleUtil.printSubHeader("TODAY'S CLINICAL SCHEDULE (" + LocalDate.now() + ")");
        List<Appointment> list = appointmentService.getAppointmentsByDoctor(doctorId).stream()
                .filter(a -> a.getAppointmentDate().equals(LocalDate.now()))
                .collect(Collectors.toList());
        renderAppointments(list);
        ConsoleUtil.pause(scanner);
    }

    private void viewAllAppointments() {
        ConsoleUtil.printSubHeader("ALL ASSIGNED APPOINTMENTS");
        List<Appointment> list = appointmentService.getAppointmentsByDoctor(doctorId);
        renderAppointments(list);
        ConsoleUtil.pause(scanner);
    }

    private void renderAppointments(List<Appointment> list) {
        String[] headers = {"Appt ID", "Patient ID", "Date", "Time", "Status", "Reason", "Notes"};
        List<String[]> rows = new ArrayList<>();
        for (Appointment a : list) {
            rows.add(new String[]{
                    a.getId(), a.getPatientId(),
                    DateTimeUtil.formatDate(a.getAppointmentDate()),
                    DateTimeUtil.formatTime(a.getAppointmentTime()),
                    a.getStatus().name(), a.getReason(),
                    a.getConsultationNotes()
            });
        }
        ConsoleUtil.printTable(headers, rows);
    }

    private void completeConsultation() {
        ConsoleUtil.printSubHeader("RECORD CLINICAL CONSULTATION");
        String apptId = ConsoleUtil.promptString(scanner, "Appointment ID", true);
        try {
            Appointment appt = appointmentService.getAppointmentById(apptId);
            if (!appt.getDoctorId().equalsIgnoreCase(doctorId)) {
                ConsoleUtil.printError("This appointment is assigned to another doctor (" + appt.getDoctorId() + ").");
                ConsoleUtil.pause(scanner);
                return;
            }

            System.out.println("Patient: " + appt.getPatientId() + " | Reason: " + appt.getReason());
            String notes = ConsoleUtil.promptString(scanner, "Consultation / Prescription Notes", true);
            appointmentService.completeAppointment(apptId, notes, currentUser.getUsername());
            ConsoleUtil.printSuccess("Appointment marked as COMPLETED with consultation notes saved.");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void lookupPatient() {
        ConsoleUtil.printSubHeader("PATIENT DEMOGRAPHICS LOOKUP");
        String patId = ConsoleUtil.promptString(scanner, "Enter Patient ID", true);
        try {
            Patient p = patientService.getPatientById(patId);
            System.out.println();
            System.out.println("==================================================");
            System.out.println("Patient ID       : " + p.getId());
            System.out.println("Name             : " + p.getName());
            System.out.println("Age / Gender     : " + p.getAge() + " / " + p.getGender());
            System.out.println("Blood Group      : " + p.getBloodGroup());
            System.out.println("Phone            : " + p.getPhone());
            System.out.println("Emergency Contact: " + p.getEmergencyContact());
            System.out.println("Registration Date: " + DateTimeUtil.formatDate(p.getRegistrationDate()));
            System.out.println("Active Record    : " + (p.isActive() ? "Yes" : "No"));
            System.out.println("==================================================");
        } catch (HospitalException e) {
            ConsoleUtil.printError(e.getMessage());
        }
        ConsoleUtil.pause(scanner);
    }

    private void updateAvailability() {
        ConsoleUtil.printSubHeader("UPDATE CLINICAL AVAILABILITY");
        try {
            Doctor d = doctorService.getDoctorById(doctorId);
            System.out.println("Current Status: " + d.getStatus());
            System.out.println("Select Status: 1. ACTIVE  2. ON_LEAVE  3. INACTIVE");
            int opt = ConsoleUtil.promptInt(scanner, "Option", 1, 3);
            DoctorStatus newStat = (opt == 1) ? DoctorStatus.ACTIVE : (opt == 2 ? DoctorStatus.ON_LEAVE : DoctorStatus.INACTIVE);
            doctorService.setDoctorStatus(doctorId, newStat, currentUser.getUsername());
            ConsoleUtil.printSuccess("Availability updated to: " + newStat);
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
