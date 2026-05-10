package com.davidgerasimov.studentapp.api;

import com.davidgerasimov.studentapp.model.User;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRepository {

    public interface UserCallback {
        void onSuccess(User user);
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
}