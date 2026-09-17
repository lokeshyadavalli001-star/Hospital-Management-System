package com.hms;

import com.hms.exception.AuthenticationException;
import com.hms.exception.InvalidInputException;
import com.hms.model.User;
import com.hms.model.enums.UserRole;
import com.hms.service.UserService;

/**
 * Tests for authentication, password hashing, and role checks.
 */
public class AuthenticationTest {

    public void run(UserService userService) {
        System.out.println("\n[SUITE] Running Authentication & Security Tests...");

        // Test 1: User creation
        try {
            User u = userService.createUser("testadmin", "Secret@123", "Test Admin",
                    "admin@test.com", "9998887770", UserRole.ADMIN, "ALL", "", "SETUP");
            TestRunner.assertTrue(u != null && u.getId().startsWith("USR-"), "User created with unique USR ID");
            TestRunner.assertEquals("testadmin", u.getUsername(), "Username matches");
            TestRunner.assertTrue(!u.getPasswordHash().equals("Secret@123"), "Password stored as hash, not plain text");
        } catch (InvalidInputException e) {
            TestRunner.assertTrue(false, "User creation failed: " + e.getMessage());
        }

        // Test 2: Successful authentication
        try {
            User authenticated = userService.authenticate("testadmin", "Secret@123");
            TestRunner.assertTrue(authenticated != null, "Successful login with correct credentials");
            TestRunner.assertEquals(UserRole.ADMIN, authenticated.getRole(), "User role correctly identified as ADMIN");
        } catch (AuthenticationException e) {
            TestRunner.assertTrue(false, "Authentication unexpectedly failed: " + e.getMessage());
        }

        // Test 3: Failed authentication on wrong password
        try {
            userService.authenticate("testadmin", "WrongPassword!");
            TestRunner.assertTrue(false, "Login should have failed for wrong password");
        } catch (AuthenticationException e) {
            TestRunner.assertTrue(true, "Authentication correctly rejected invalid password");
        }

        // Test 4: Rejection of nonexistent user
        try {
            userService.authenticate("nonexistent_user", "AnyPassword");
            TestRunner.assertTrue(false, "Login should have failed for nonexistent user");
        } catch (AuthenticationException e) {
            TestRunner.assertTrue(true, "Authentication correctly rejected nonexistent user");
        }
    }
}
