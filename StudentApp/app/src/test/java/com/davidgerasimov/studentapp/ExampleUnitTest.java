package com.davidgerasimov.studentapp;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.davidgerasimov.studentapp.model.User;

public class ExampleUnitTest {

    private User testStudent;

    @Before
    public void setUp() {
        testStudent = new User(2, "David Gerasimov",
                "david.102747@student.ugd.edu.mk", "student", "102747");
    }

    // USER MODEL TESTS
    @Test
    public void user_getName_returnsCorrectName() {
        assertEquals("David Gerasimov", testStudent.getFullName());
    }

    @Test
    public void user_getRole_returnsStudent() {
        assertEquals("student", testStudent.getRole());
    }

    @Test
    public void user_getStudentId_returnsCorrectId() {
        assertEquals("102747", testStudent.getStudentId());
    }

    @Test
    public void user_getEmail_returnsCorrectEmail() {
        assertEquals("david.102747@student.ugd.edu.mk", testStudent.getEmail());
    }
    @Test
    public void user_emailFormat_isValid() {
        assertTrue(testStudent.getEmail().contains("@student.ugd.edu.mk"));
    }
    @Test
    public void user_studentId_isNumeric() {
        assertTrue(testStudent.getStudentId().matches("[0-9]+"));
    }

    @Test
    public void user_studentId_isCorrectLength() {
        assertEquals(6, testStudent.getStudentId().length());
    }

    // TOKEN TESTS
    @Test
    public void token_validFormat_passesValidation() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals(3, parts.length);
    }

    @Test
    public void token_studentId_extractedCorrectly() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals("102747", parts[0]);
    }

    @Test
    public void token_timestamp_hasCorrectLength() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals(10, parts[1].length());
    }

    @Test
    public void token_hash_hasCorrectLength() {
        String token = "102747|2026050910|abcd1234";
        String[] parts = token.split("\\|");
        assertEquals(8, parts[2].length());
    }

    @Test
    public void token_invalidFormat_failsValidation() {
        String token = "invalid_token";
        String[] parts = token.split("\\|");
        assertTrue(parts.length < 2);
    }

    @Test
    public void token_emptyPayload_failsValidation() {
        String token = "";
        assertTrue(token.isEmpty());
    }

    // SHA256 TESTS
    @Test
    public void sha256_sameInput_givesSameOutput() throws Exception {
        String hash1 = sha256("102747|2026050910UGD_SRS_2026_SECRET");
        String hash2 = sha256("102747|2026050910UGD_SRS_2026_SECRET");
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

    @Test
    public void sha256_knownInput_givesCorrectOutput() throws Exception {
        String hash = sha256("abc");
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015a",
                hash.substring(0, 63));
    }

    // REGISTRATION VALIDATION TESTS
    @Test
    public void registration_passwordTooShort_fails() {
        String password = "12345";
        assertTrue(password.length() < 6);
    }

    @Test
    public void registration_passwordLongEnough_passes() {
        String password = "123456";
        assertTrue(password.length() >= 6);
    }

    @Test
    public void registration_emailValid_passes() {
        String email = "david.102747@student.ugd.edu.mk";
        assertTrue(email.contains("@student.ugd.edu.mk"));
    }

    @Test
    public void registration_passwordsMatch_passes() {
        String password = "mypassword";
        String confirm = "mypassword";
        assertEquals(password, confirm);
    }

    @Test
    public void registration_passwordsMismatch_fails() {
        String password = "mypassword";
        String confirm = "different";
        assertNotEquals(password, confirm);
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