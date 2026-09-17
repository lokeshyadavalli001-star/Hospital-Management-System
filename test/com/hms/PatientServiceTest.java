package com.hms;

import com.hms.exception.InvalidInputException;
import com.hms.exception.PatientNotFoundException;
import com.hms.model.Patient;
import com.hms.model.enums.Gender;
import com.hms.service.PatientService;

import java.util.List;

/**
 * Tests for patient registration, validation rules, and demographic searches.
 */
public class PatientServiceTest {

    public void run(PatientService patientService) {
        System.out.println("\n[SUITE] Running Patient Service Tests...");

        String registeredId = null;

        // Test 1: Valid patient registration
        try {
            Patient p = patientService.registerPatient("Amit Kumar", 45, Gender.MALE, "9876543210",
                    "amit@example.com", "Sector 14, Noida", "O+", "9876543211", "TEST");
            TestRunner.assertTrue(p != null && p.getId().startsWith("PAT-"), "Patient registered with PAT- prefix ID");
            TestRunner.assertEquals("Amit Kumar", p.getName(), "Patient name preserved");
            TestRunner.assertEquals(45, p.getAge(), "Patient age matches");
            registeredId = p.getId();
        } catch (InvalidInputException e) {
            TestRunner.assertTrue(false, "Patient registration failed: " + e.getMessage());
        }

        // Test 2: Validation rejection on invalid phone
        try {
            patientService.registerPatient("Bad Phone User", 30, Gender.FEMALE, "12345",
                    "test@example.com", "Delhi", "A+", "", "TEST");
            TestRunner.assertTrue(false, "Should reject invalid short phone number");
        } catch (InvalidInputException e) {
            TestRunner.assertTrue(true, "Validation correctly rejected invalid phone number");
        }

        // Test 3: Validation rejection on invalid age
        try {
            patientService.registerPatient("Bad Age User", -5, Gender.MALE, "9876543212",
                    "test@example.com", "Delhi", "B+", "", "TEST");
            TestRunner.assertTrue(false, "Should reject negative age");
        } catch (InvalidInputException e) {
            TestRunner.assertTrue(true, "Validation correctly rejected negative age");
        }

        // Test 4: Search by phone
        try {
            Patient found = patientService.findByPhone("9876543210");
            TestRunner.assertTrue(found != null, "Patient located by phone number");
            TestRunner.assertEquals(registeredId, found.getId(), "Correct patient ID matched by phone");
        } catch (PatientNotFoundException e) {
            TestRunner.assertTrue(false, "Failed to find patient by phone: " + e.getMessage());
        }

        // Test 5: Search keyword
        List<Patient> matches = patientService.searchPatients("Amit");
        TestRunner.assertTrue(!matches.isEmpty(), "Search query matched registered patient name");
    }
}
