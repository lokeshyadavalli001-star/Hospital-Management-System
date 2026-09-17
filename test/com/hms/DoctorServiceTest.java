package com.hms;

import com.hms.exception.DoctorNotFoundException;
import com.hms.exception.InvalidInputException;
import com.hms.model.Doctor;
import com.hms.model.enums.DoctorStatus;
import com.hms.service.DoctorService;

import java.time.LocalTime;
import java.util.List;

/**
 * Tests for doctor management, department filtering, and availability toggling.
 */
public class DoctorServiceTest {

    public void run(DoctorService doctorService) {
        System.out.println("\n[SUITE] Running Doctor Service Tests...");

        String docId = null;

        // Test 1: Add new doctor
        try {
            Doctor doc = doctorService.addDoctor("dr_ramesh", "Pass@123", "Dr. Ramesh Gupta",
                    "ramesh@test.com", "9811223344", "Cardiology", "Cardiology",
                    750.0, DoctorStatus.ACTIVE, LocalTime.of(9, 0), LocalTime.of(16, 0),
                    "MBBS, MD", "TEST");
            TestRunner.assertTrue(doc != null && doc.getId().startsWith("DOC-"), "Doctor created with DOC- prefix ID");
            TestRunner.assertEquals("Cardiology", doc.getDepartment(), "Department correctly set");
            docId = doc.getId();
        } catch (InvalidInputException e) {
            TestRunner.assertTrue(false, "Doctor creation failed: " + e.getMessage());
        }

        // Test 2: Department filtering
        List<Doctor> cardiologists = doctorService.getDoctorsByDepartment("Cardiology");
        TestRunner.assertTrue(!cardiologists.isEmpty(), "Located doctor via department filter");

        // Test 3: Status change
        try {
            doctorService.setDoctorStatus(docId, DoctorStatus.ON_LEAVE, "TEST");
            Doctor updated = doctorService.getDoctorById(docId);
            TestRunner.assertEquals(DoctorStatus.ON_LEAVE, updated.getStatus(), "Doctor status updated to ON_LEAVE");
            // Set back to ACTIVE for subsequent tests
            doctorService.setDoctorStatus(docId, DoctorStatus.ACTIVE, "TEST");
        } catch (DoctorNotFoundException e) {
            TestRunner.assertTrue(false, "Doctor status update failed: " + e.getMessage());
        }

        // Test 4: Time availability check
        try {
            Doctor doc = doctorService.getDoctorById(docId);
            TestRunner.assertTrue(doc.isAvailableAt(LocalTime.of(10, 30)), "Doctor available within working hours (10:30)");
            TestRunner.assertTrue(!doc.isAvailableAt(LocalTime.of(18, 0)), "Doctor unavailable outside working hours (18:00)");
        } catch (DoctorNotFoundException e) {
            TestRunner.assertTrue(false, "Doctor retrieval failed: " + e.getMessage());
        }
    }
}
