package com.davidgerasimov.teacherapp.api;

import com.davidgerasimov.teacherapp.model.Attendance;
import com.davidgerasimov.teacherapp.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiRepository {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface AttendanceCallback {
        void onSuccess(List<Attendance> attendanceList);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface SubjectCallback {
        void onSuccess(List<String[]> subjects);
        void onError(String error);
    }

    public void login(String email, String password, UserCallback callback) {
        String endpoint = "users?email=eq." + email +
                "&password_hash=eq." + password + "&select=*";

        Request request = SupabaseClient.getRequestBuilder(endpoint)
                .get()
                .build();

        SupabaseClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);
                            User user = new User();
                            user.setId(obj.getInt("id"));
                            user.setFullName(obj.getString("full_name"));
                            user.setEmail(obj.getString("email"));
                            user.setRole(obj.getString("role"));
                            user.setStudentId(obj.optString("student_id", ""));
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Invalid email or password");
                        }
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
        });
    }

    public void getStudentByStudentId(String studentId, UserCallback callback) {
        String endpoint = "users?student_id=eq." + studentId + "&select=*";

        Request request = SupabaseClient.getRequestBuilder(endpoint)
                .get()
                .build();

        SupabaseClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONArray array = new JSONArray(body);
                        if (array.length() > 0) {
                            JSONObject obj = array.getJSONObject(0);
                            User user = new User();
                            user.setId(obj.getInt("id"));
                            user.setFullName(obj.getString("full_name"));
                            user.setEmail(obj.getString("email"));
                            user.setRole(obj.getString("role"));
                            user.setStudentId(obj.optString("student_id", ""));
                            callback.onSuccess(user);
                        } else {
                            callback.onError("Student not found");
                        }
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
        });
    }

    public void recordAttendance(int studentId, int subjectId, int teacherId, SaveCallback callback) {
        try {
            JSONObject json = new JSONObject();
            json.put("student_id", studentId);
            json.put("subject_id", subjectId);
            json.put("teacher_id", teacherId);
            json.put("status", "present");

            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = SupabaseClient.getRequestBuilder("attendance")
                    .post(body)
                    .build();

            SupabaseClient.getClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    if (response.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        callback.onError("Error: " + response.code() +
                                " " + response.body().string());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    public void getAttendanceBySubject(int subjectId, AttendanceCallback callback) {
        String endpoint = "attendance?subject_id=eq." + subjectId + "&select=*";

        Request request = SupabaseClient.getRequestBuilder(endpoint)
                .get()
                .build();

        SupabaseClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONArray array = new JSONArray(body);
                        List<Attendance> list = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            Attendance a = new Attendance();
                            a.setId(obj.getInt("id"));
                            a.setStudentId(obj.getInt("student_id"));
                            a.setSubjectId(obj.getInt("subject_id"));
                            a.setTeacherId(obj.getInt("teacher_id"));
                            a.setTimestamp(obj.optString("timestamp", ""));
                            a.setStatus(obj.optString("status", "present"));
                            list.add(a);
                        }
                        callback.onSuccess(list);
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
        });
    }

    public void getSubjectsByTeacher(int teacherId, SubjectCallback callback) {
        String endpoint = "subjects?teacher_id=eq." + teacherId + "&select=*";

        Request request = SupabaseClient.getRequestBuilder(endpoint)
                .get()
                .build();

        SupabaseClient.getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (response.isSuccessful()) {
                    try {
                        String body = response.body().string();
                        JSONArray array = new JSONArray(body);
                        List<String[]> subjects = new ArrayList<>();
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            subjects.add(new String[]{
                                    String.valueOf(obj.getInt("id")),
                                    obj.getString("name")
                            });
                        }
                        callback.onSuccess(subjects);
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                } else {
                    callback.onError("Error: " + response.code());
                }
            }
        });
    }
}