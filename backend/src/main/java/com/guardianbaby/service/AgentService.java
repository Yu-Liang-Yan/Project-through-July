package com.guardianbaby.service;

public interface AgentService {
    void heartbeat(Long userId);
    void reportUsage(Long userId, String deviceName, String appName, int durationSeconds);
}
