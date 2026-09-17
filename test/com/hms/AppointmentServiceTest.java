package com.hms;

import com.hms.exception.AppointmentConflictException;
import com.hms.exception.HospitalException;
import com.hms.model.Appointment;
import com.hms.model.Doctor;
import com.hms.model.Patient;
import com.hms.model.enums.AppointmentStatus;
import com.hms.model.enums.DoctorStatus;
import com.hms.model.enums.Gender;
import com.hms.service.AppointmentService;
import com.hms.service.DoctorService;
import com.hms.service.PatientService;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Tests for appointment scheduling, conflict detection, rescheduling, and status transitions.
 */
public class AppointmentServiceTest {

    public void run(AppointmentService apptService, PatientService patientService, DoctorService doctorService) {
        System.out.println("\n[SUITE] Running Appointment Service Tests...");

        Patient p1 = null;
        Patient p2 = null;
        Doctor d1 = null;

        try {
            p1 = patientService.registerPatient("Test Patient One", 32, Gender.MALE, "9876500001",
                    "p1@test.com", "City", "A+", "", "SETUP");
            p2 = patientService.registerPatient("Test Patient Two", 29, Gender.FEMALE, "9876500002",
                    "p2@test.com", "City", "B+", "", "SETUP");
            d1 = doctorService.addDoctor("dr_appt_test", "Pass@123", "Dr. Appointment Specialist",
                    "doc@test.com", "9876500003", "General", "OPD",
                    500.0, DoctorStatus.ACTIVE, LocalTime.of(9, 0), LocalTime.of(17, 0), "MBBS", "SETUP");
        } catch (Exception e) {
            TestRunner.assertTrue(false, "Setup failed for appointment tests: " + e.getMessage());
            return;
        }

        LocalDate bookingDate = LocalDate.now().plusDays(2);
        LocalTime bookingTime = LocalTime.of(11, 0);
        String apptId = null;

        // Test 1: Successful booking
        try {
            Appointment appt = apptService.bookAppointment(p1.getId(), d1.getId(),
                    bookingDate, bookingTime, "Fever & Cold", "TEST");
            TestRunner.assertTrue(appt != null && appt.getId().startsWith("APT-"), "Appointment booked successfully");
            TestRunner.assertEquals(AppointmentStatus.SCHEDULED, appt.getStatus(), "Initial status is SCHEDULED");
            apptId = appt.getId();
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Booking failed unexpectedly: " + e.getMessage());
        }

        // Test 2: Conflict Detection - Double booking same doctor at same slot
        try {
            apptService.bookAppointment(p2.getId(), d1.getId(), bookingDate, bookingTime, "Headache", "TEST");
            TestRunner.assertTrue(false, "Should have thrown AppointmentConflictException for doctor slot collision");
        } catch (AppointmentConflictException e) {
            TestRunner.assertTrue(true, "Doctor slot conflict correctly detected and prevented");
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Unexpected exception type for conflict: " + e);
        }

        // Test 3: Reschedule Appointment
        try {
            LocalTime newTime = LocalTime.of(14, 0);
            Appointment rescheduled = apptService.rescheduleAppointment(apptId, bookingDate, newTime, "TEST");
            TestRunner.assertEquals(AppointmentStatus.RESCHEDULED, rescheduled.getStatus(), "Status updated to RESCHEDULED");
            TestRunner.assertEquals(newTime, rescheduled.getAppointmentTime(), "Appointment time updated");
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Rescheduling failed: " + e.getMessage());
        }

        // Test 4: Complete Appointment
        try {
            apptService.completeAppointment(apptId, "Prescribed Paracetamol 500mg, rest for 2 days", "TEST");
            Appointment completed = apptService.getAppointmentById(apptId);
            TestRunner.assertEquals(AppointmentStatus.COMPLETED, completed.getStatus(), "Status updated to COMPLETED");
            TestRunner.assertTrue(completed.getConsultationNotes().contains("Paracetamol"), "Consultation notes preserved");
        } catch (HospitalException e) {
            TestRunner.assertTrue(false, "Completing appointment failed: " + e.getMessage());
        }

        // Test 5: Rejection of cancellation after completion
        try {
            apptService.cancelAppointment(apptId, "Patient cancelled", "TEST");
            TestRunner.assertTrue(false, "Should reject cancellation of completed appointment");
        } catch (HospitalException e) {
            TestRunner.assertTrue(true, "Correctly prevented cancellation of already completed appointment");
        }
    }
}
