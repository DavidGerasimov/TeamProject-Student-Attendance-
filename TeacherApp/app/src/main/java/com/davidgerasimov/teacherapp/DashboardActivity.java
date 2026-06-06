package com.davidgerasimov.teacherapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.davidgerasimov.teacherapp.api.ApiRepository;
import com.davidgerasimov.teacherapp.api.SupabaseClient;
import com.davidgerasimov.teacherapp.model.User;
import com.davidgerasimov.teacherapp.nfc.NfcHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtWelcome, txtNfcStatus, txtLastScanned;
    private Spinner spinnerSubject;
    private ListView listAttendance;
    private NfcHelper nfcHelper;
    private ApiRepository repository = new ApiRepository();
    private List<String[]> subjects = new ArrayList<>();
    private List<String> attendanceItems = new ArrayList<>();
    private ArrayAdapter<String> attendanceAdapter;
    private int teacherId;
    private String teacherName;

    private static final String SECRET_KEY = "UGD_SRS_2026_SECRET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        teacherId = getIntent().getIntExtra("userId", 0);
        teacherName = getIntent().getStringExtra("userName");

        txtWelcome = findViewById(R.id.txtWelcome);
        txtNfcStatus = findViewById(R.id.txtNfcStatus);
        txtLastScanned = findViewById(R.id.txtLastScanned);
        spinnerSubject = findViewById(R.id.spinnerSubject);
        listAttendance = findViewById(R.id.listAttendance);

        txtWelcome.setText("Welcome, " + teacherName + "!");

        attendanceAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, attendanceItems);
        listAttendance.setAdapter(attendanceAdapter);

        nfcHelper = new NfcHelper(this);

        if (!nfcHelper.isNfcAvailable()) {
            txtNfcStatus.setText("❌ NFC not available on this device");
        } else if (!nfcHelper.isNfcEnabled()) {
            txtNfcStatus.setText("⚠️ Please enable NFC in settings");
        } else {
            txtNfcStatus.setText("📱 Ready to scan NFC");
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        findViewById(R.id.btnAddSubject).setOnClickListener(v ->
                showAddSubjectDialog());

        loadSubjects();
    }

    private void showAddSubjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Subject");

        final EditText input = new EditText(this);
        input.setHint("Subject name");
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subjectName = input.getText().toString().trim();
            if (!subjectName.isEmpty()) {
                addSubject(subjectName);
            } else {
                Toast.makeText(this, "Please enter a subject name",
                        Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void addSubject(String subjectName) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", subjectName);
            json.put("teacher_id", teacherId);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.get("application/json; charset=utf-8"));

            Request request = SupabaseClient.getRequestBuilder("subjects")
                    .post(body)
                    .build();

            SupabaseClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    runOnUiThread(() -> Toast.makeText(DashboardActivity.this,
                            "Error adding subject: " + e.getMessage(),
                            Toast.LENGTH_LONG).show());
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws java.io.IOException {
                    runOnUiThread(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(DashboardActivity.this,
                                    "Subject added successfully!",
                                    Toast.LENGTH_SHORT).show();
                            loadSubjects();
                        } else {
                            Toast.makeText(DashboardActivity.this,
                                    "Error adding subject!",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void loadSubjects() {
        repository.getSubjectsByTeacher(teacherId, new ApiRepository.SubjectCallback() {
            @Override
            public void onSuccess(List<String[]> subjectList) {
                runOnUiThread(() -> {
                    subjects.clear();
                    subjects.addAll(subjectList);
                    List<String> subjectNames = new ArrayList<>();
                    for (String[] s : subjectList) {
                        subjectNames.add(s[1]);
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            DashboardActivity.this,
                            android.R.layout.simple_spinner_item,
                            subjectNames);
                    adapter.setDropDownViewResource(
                            android.R.layout.simple_spinner_dropdown_item);
                    spinnerSubject.setAdapter(adapter);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this,
                        "Error loading subjects: " + error,
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleNfcScan(String rawPayload) {
        txtNfcStatus.setText("🔍 Validating token...");

        try {
            String[] parts = rawPayload.split("\\|");
            if (parts.length < 2) {
                txtNfcStatus.setText("❌ Invalid token format!");
                return;
            }

            String studentId = parts[0];

            if (parts.length == 3) {
                String receivedHash = parts[2];
                boolean isValid = false;

                for (int i = 0; i <= 1; i++) {
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.add(java.util.Calendar.HOUR_OF_DAY, -i);
                    String checkTimestamp = new java.text.SimpleDateFormat(
                            "yyyyMMddHH",
                            java.util.Locale.getDefault()).format(cal.getTime());
                    String rawToken = studentId + "|" + checkTimestamp;
                    String expectedHash = sha256(rawToken + SECRET_KEY)
                            .substring(0, 8);
                    if (expectedHash.equals(receivedHash)) {
                        isValid = true;
                        break;
                    }
                }

                if (!isValid) {
                    txtNfcStatus.setText("❌ Invalid or expired token!");
                    Toast.makeText(this, "Security validation failed!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            repository.getStudentByStudentId(studentId,
                    new ApiRepository.UserCallback() {
                        @Override
                        public void onSuccess(User student) {
                            runOnUiThread(() -> {
                                int selectedSubjectIndex =
                                        spinnerSubject.getSelectedItemPosition();
                                if (subjects.isEmpty() || selectedSubjectIndex < 0) {
                                    Toast.makeText(DashboardActivity.this,
                                            "Please select a subject first!",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                int subjectId = Integer.parseInt(
                                        subjects.get(selectedSubjectIndex)[0]);

                                // Check for duplicate before recording
                                repository.checkDuplicateAttendance(student.getId(),
                                        subjectId, isDuplicate -> {
                                            runOnUiThread(() -> {
                                                if (isDuplicate) {
                                                    txtNfcStatus.setText("⚠️ Already scanned today!");
                                                    txtLastScanned.setText(student.getFullName() +
                                                            " already marked present for this subject!");
                                                    Toast.makeText(DashboardActivity.this,
                                                            student.getFullName() +
                                                                    " already marked present!",
                                                            Toast.LENGTH_LONG).show();
                                                    return;
                                                }

                                                txtLastScanned.setText("✅ " +
                                                        student.getFullName() + " scanned!");
                                                txtNfcStatus.setText("📱 Ready to scan NFC");

                                                repository.recordAttendance(student.getId(),
                                                        subjectId, teacherId,
                                                        new ApiRepository.SaveCallback() {
                                                            @Override
                                                            public void onSuccess() {
                                                                runOnUiThread(() -> {
                                                                    String item = student.getFullName()
                                                                            + " - Present ✅";
                                                                    attendanceItems.add(0, item);
                                                                    attendanceAdapter.notifyDataSetChanged();
                                                                    Toast.makeText(DashboardActivity.this,
                                                                            student.getFullName() +
                                                                                    " marked present!",
                                                                            Toast.LENGTH_SHORT).show();
                                                                });
                                                            }

                                                            @Override
                                                            public void onError(String error) {
                                                                runOnUiThread(() ->
                                                                        Toast.makeText(DashboardActivity.this,
                                                                                "Error recording: " + error,
                                                                                Toast.LENGTH_LONG).show());
                                                            }
                                                        });
                                            });
                                        });
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                txtNfcStatus.setText("❌ Student not found!");
                                txtLastScanned.setText("Unknown student: " + studentId);
                            });
                        }
                    });
        } catch (Exception e) {
            txtNfcStatus.setText("❌ Token validation error!");
        }
    }

    private String sha256(String input) {
        try {
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
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcHelper.enableForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcHelper.disableForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String rawPayload = nfcHelper.readStudentIdFromIntent(intent);
        if (rawPayload != null) {
            handleNfcScan(rawPayload);
        }
    }
}