package com.davidgerasimov.studentapp.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class SupabaseClient {

    private static final String BASE_URL = "https://dcmdwnmegcgvktcctnnm.supabase.co/rest/v1/";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRjbWR3bm1lZ2Nndmt0Y2N0bm5tIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzgzMjg1ODAsImV4cCI6MjA5MzkwNDU4MH0.pUfRKI3HFAGhDMkQL5W1XA1QsAR4lPa89ldnDKMrpsg";

    private static OkHttpClient httpClient;

    public static OkHttpClient getClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }
        return httpClient;
    }

    public static Request.Builder getRequestBuilder(String endpoint) {
        return new Request.Builder()
                .url(BASE_URL + endpoint)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation");
    }

    public static String getApiKey() {
        return API_KEY;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}