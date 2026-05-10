package com.davidgerasimov.studentapp;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.davidgerasimov.studentapp.nfc.HceService;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView txtWelcome, txtNfcStatus, txtStudentId,
            txtLastAttendance, txtNfcEnabled;
    private NfcAdapter nfcAdapter;
    private String studentId;
    private String userName;

    // Secret key for token generation - same must be used in Teacher App
    private static final String SECRET_KEY = "UGD_SRS_2026_SECRET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        userName = getIntent().getStringExtra("userName");
        studentId = getIntent().getStringExtra("studentId");

        txtWelcome = findViewById(R.id.txtWelcome);
        txtNfcStatus = findViewById(R.id.txtNfcStatus);
        txtStudentId = findViewById(R.id.txtStudentId);
        txtLastAttendance = findViewById(R.id.txtLastAttendance);
        txtNfcEnabled = findViewById(R.id.txtNfcEnabled);

        txtWelcome.setText("Welcome, " + userName + "!");
        txtStudentId.setText("Student ID: " + studentId);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        checkNfcStatus();

        // Generate secure token
        String secureToken = generateSecureToken(studentId);
        HceService.setStudentPayload(secureToken);

        txtNfcStatus.setText("📱 Hold phone near teacher's device to mark attendance");
        txtLastAttendance.setText("Token refreshes every hour for security");

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            HceService.setStudentPayload("");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private String generateSecureToken(String studentId) {
        try {
            // Token = studentId|hourlyTimestamp|hash
            String hourlyTimestamp = new SimpleDateFormat("yyyyMMddHH",
                    Locale.getDefault()).format(new Date());
            String rawToken = studentId + "|" + hourlyTimestamp;
            String hash = sha256(rawToken + SECRET_KEY);
            // Only send first 8 chars of hash to keep payload small
            return studentId + "|" + hourlyTimestamp + "|" + hash.substring(0, 8);
        } catch (Exception e) {
            // Fallback to basic token
            return studentId + "|" + System.currentTimeMillis();
        }
    }

    public static String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private void checkNfcStatus() {
        if (nfcAdapter == null) {
            txtNfcEnabled.setText("❌ NFC not available on this device");
        } else if (!nfcAdapter.isEnabled()) {
            txtNfcEnabled.setText("⚠️ NFC is disabled — tap here to enable");
            txtNfcEnabled.setOnClickListener(v ->
                    startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)));
        } else {
            txtNfcEnabled.setText("✅ NFC is enabled and ready");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNfcStatus();
    }
}