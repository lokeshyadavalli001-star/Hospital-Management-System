package com.hms.service;

import com.hms.model.Appointment;
import com.hms.model.Bill;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.enums.AppointmentStatus;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.PaymentStatus;
import com.hms.util.DateTimeUtil;
import com.hms.util.FileUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Analytical service delivering real-time aggregates, financial summaries, and CSV exports using Java Streams.
 */
public class ReportService {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final BillingService billingService;

    public ReportService(PatientService patientService,
                         DoctorService doctorService,
                         AppointmentService appointmentService,
                         BillingService billingService) {
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.billingService = billingService;
    }

    public static class DailySummary {
        public LocalDate date;
        public int totalPatients;
        public int activePatients;
        public int totalDoctors;
        public int activeDoctors;
        public Map<String, Long> doctorsByDept;
        public int todaysAppointments;
        public int completedAppointments;
        public int cancelledAppointments;
        public int upcomingAppointments;
        public double totalRevenueBilled;
        public double totalRevenueCollected;
        public double totalRevenuePending;
    }

    public DailySummary generateDailySummary(LocalDate date) {
        DailySummary summary = new DailySummary();
        summary.date = (date != null) ? date : LocalDate.now();

        List<Patient> patients = patientService.getAllPatients();
        summary.totalPatients = patients.size();
        summary.activePatients = (int) patients.stream().filter(Patient::isActive).count();

        List<Doctor> doctors = doctorService.getAllDoctors();
        summary.totalDoctors = doctors.size();
        summary.activeDoctors = (int) doctors.stream().filter(d -> d.getStatus() == DoctorStatus.ACTIVE).count();
        summary.doctorsByDept = doctors.stream()
                .collect(Collectors.groupingBy(Doctor::getDepartment, Collectors.counting()));

        List<Appointment> allAppointments = appointmentService.getAllAppointments();
        List<Appointment> forDay = allAppointments.stream()
                .filter(a -> a.getAppointmentDate().equals(summary.date))
                .collect(Collectors.toList());

        summary.todaysAppointments = forDay.size();
        summary.completedAppointments = (int) forDay.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        summary.cancelledAppointments = (int) forDay.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        summary.upcomingAppointments = (int) forDay.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.RESCHEDULED)
                .count();

        List<Bill> bills = billingService.getAllBills();
        summary.totalRevenueBilled = bills.stream()
                .mapToDouble(Bill::getFinalAmount)
                .sum();
        summary.totalRevenueCollected = bills.stream()
                .mapToDouble(Bill::getPaidAmount)
                .sum();
        summary.totalRevenuePending = bills.stream()
                .mapToDouble(Bill::getBalanceAmount)
                .sum();

        return summary;
    }

    public Map<String, Long> getDoctorWorkloadSummary() {
        return appointmentService.getAllAppointments().stream()
                .collect(Collectors.groupingBy(Appointment::getDoctorId, Collectors.counting()));
    }

    public boolean exportDailySummaryCsv(DailySummary summary, String targetPath) {
        File file = new File(targetPath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            writer.write("Metric,Value");
            writer.newLine();
            writer.write("Report Date," + DateTimeUtil.formatDate(summary.date));
            writer.newLine();
            writer.write("Total Registered Patients," + summary.totalPatients);
            writer.newLine();
            writer.write("Active Patients," + summary.activePatients);
            writer.newLine();
            writer.write("Total Doctors," + summary.totalDoctors);
            writer.newLine();
            writer.write("Active Doctors," + summary.activeDoctors);
            writer.newLine();
            writer.write("Day Appointments Total," + summary.todaysAppointments);
            writer.newLine();
            writer.write("Day Appointments Completed," + summary.completedAppointments);
            writer.newLine();
            writer.write("Day Appointments Cancelled," + summary.cancelledAppointments);
            writer.newLine();
            writer.write("Day Appointments Upcoming," + summary.upcomingAppointments);
            writer.newLine();
            writer.write("Total Billed (INR)," + String.format("%.2f", summary.totalRevenueBilled));
            writer.newLine();
            writer.write("Total Collected (INR)," + String.format("%.2f", summary.totalRevenueCollected));
            writer.newLine();
            writer.write("Total Outstanding (INR)," + String.format("%.2f", summary.totalRevenuePending));
            writer.newLine();

            // Department breakdown
            writer.write("--- Department Breakdown ---,");
            writer.newLine();
            for (Map.Entry<String, Long> entry : summary.doctorsByDept.entrySet()) {
                writer.write(FileUtil.escapeCsvField(entry.getKey()) + "," + entry.getValue());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to export CSV report: " + e.getMessage());
            return false;
        }
    }
}
