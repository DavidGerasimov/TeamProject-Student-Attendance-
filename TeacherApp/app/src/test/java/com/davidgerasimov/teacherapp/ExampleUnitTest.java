package com.davidgerasimov.teacherapp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.davidgerasimov.teacherapp.model.Attendance;
import com.davidgerasimov.teacherapp.model.User;

public class ExampleUnitTest {

    private User testTeacher;
    private User testStudent;
    private Attendance testAttendance;

    @Before
    public void setUp() {
        testTeacher = new User(1, "Professor Sasko",
                "sasko@ugd.edu.mk", "teacher", null);
        testStudent = new User(2, "David Gerasimov",
                "102747@student.ugd.edu.mk", "student", "102747");
        testAttendance = new Attendance(2, 1, 1, "present");
    }

    // USER MODEL TESTS
    @Test
    public void user_getName_returnsCorrectName() {
        assertEquals("Professor Sasko", testTeacher.getFullName());
    }

    @Test
    public void user_getRole_returnsCorrectRole() {
        assertEquals("teacher", testTeacher.getRole());
    }

    @Test
    public void user_getStudentId_returnsCorrectId() {
        assertEquals("102747", testStudent.getStudentId());
    }

    @Test
    public void user_isTeacher_returnsTrue() {
        assertTrue(testTeacher.getRole().equals("teacher"));
    }

    @Test
    public void user_isStudent_returnsTrue() {
        assertTrue(testStudent.getRole().equals("student"));
    }

    @Test
    public void user_emailFormat_isValid() {
        assertTrue(testStudent.getEmail().contains("@student.ugd.edu.mk"));
    }

    @Test
    public void user_teacherEmail_isValid() {
        assertTrue(testTeacher.getEmail().contains("@ugd.edu.mk"));
    }

    // ATTENDANCE MODEL TESTS
    @Test
    public void attendance_getStudentId_returnsCorrectId() {
        assertEquals(2, testAttendance.getStudentId());
    }

    @Test
    public void attendance_getSubjectId_returnsCorrectId() {
        assertEquals(1, testAttendance.getSubjectId());
    }

    @Test
    public void attendance_getStatus_returnsPresent() {
        assertEquals("present", testAttendance.getStatus());
    }

    @Test
    public void attendance_getTeacherId_returnsCorrectId() {
        assertEquals(1, testAttendance.getTeacherId());
    }

    // TOKEN VALIDATION TESTS
    @Test
    public void token_validFormat_passesValidation() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals(3, parts.length);
    }

    @Test
    public void token_invalidFormat_failsValidation() {
        String token = "invalid_token";
        String[] parts = token.split("\\|");
        assertTrue(parts.length < 2);
    }

    @Test
    public void token_studentId_extractedCorrectly() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals("102747", parts[0]);
    }

    @Test
    public void token_timestamp_extractedCorrectly() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals("2026050910", parts[1]);
    }

    @Test
    public void token_hash_extractedCorrectly() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals("abcd1234", parts[2]);
    }

    // SHA256 TESTS
    @Test
    public void sha256_sameInput_givesSameOutput() throws Exception {
        String input = "102747|2026050910UGD_SRS_2026_SECRET";
        String hash1 = sha256(input);
        String hash2 = sha256(input);
        assertEquals(hash1, hash2);
    }

    @Test
    public void sha256_differentInput_givesDifferentOutput() throws Exception {
        String hash1 = sha256("input1");
        String hash2 = sha256("input2");
        assertNotEquals(hash1, hash2);
    }

    @Test
    public void sha256_output_is64CharsLong() throws Exception {
        String hash = sha256("test");
        assertEquals(64, hash.length());
    }

    private String sha256(String input) throws Exception {
        java.security.MessageDigest digest =
                java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}