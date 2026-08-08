package com.guardianbaby.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AgentHttpClient {
    private final OkHttpClient http;
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();
    private String token;
    private Long userId;
    private Long deviceId;

    public AgentHttpClient(String serverUrl) {
        this.baseUrl = serverUrl.replaceAll("/$", "");
        this.http = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
    }

    @SuppressWarnings("unchecked")
    public boolean login(String username, String password) {
        try {
            String body = mapper.writeValueAsString(Map.of("username", username, "password", password));
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/auth/login")
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .build();
            try (Response res = http.newCall(req).execute()) {
                var json = mapper.readTree(res.body().string());
                if (!json.path("success").asBoolean()) return false;
                this.token = json.path("data").path("token").asText();
                this.userId = json.path("data").path("id").asLong();
                return true;
            }
        } catch (IOException e) {
            System.err.println("[HTTP] Login failed: " + e.getMessage());
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean registerDevice(String name, String type) {
        try {
            String body = mapper.writeValueAsString(Map.of("name", name, "type", type));
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/devices?userId=" + userId)
                    .addHeader("Authorization", "Bearer " + token)
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .build();
            try (Response res = http.newCall(req).execute()) {
                var json = mapper.readTree(res.body().string());
                if (!json.path("success").asBoolean()) return false;
                this.deviceId = json.path("data").path("id").asLong();
                return true;
            }
        } catch (IOException e) {
            System.err.println("[HTTP] Register failed: " + e.getMessage());
            return false;
        }
    }

    public boolean sendHeartbeat() {
        try {
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/agent/heartbeat?userId=" + userId)
                    .addHeader("Authorization", "Bearer " + token)
                    .post(RequestBody.create("{}", MediaType.parse("application/json")))
                    .build();
            try (Response res = http.newCall(req).execute()) {
                return res.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean reportUsage(String appName, int durationSeconds) {
        try {
            String body = mapper.writeValueAsString(Map.of(
                "userId", userId,
                "deviceName", deviceId != null ? deviceId.toString() : "agent",
                "appName", appName,
                "durationSeconds", durationSeconds
            ));
            Request req = new Request.Builder()
                    .url(baseUrl + "/api/agent/usage")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(RequestBody.create(body, MediaType.parse("application/json")))
                    .build();
            try (Response res = http.newCall(req).execute()) {
                return res.isSuccessful();
            }
        } catch (IOException e) {
            return false;
        }
    }

    public String token() { return token; }
    public Long userId() { return userId; }
    public Long deviceId() { return deviceId; }
    public String baseUrl() { return baseUrl; }
}
