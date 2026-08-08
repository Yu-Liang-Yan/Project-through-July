package com.guardianbaby.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final WebSocketSessionManager sessionManager;
    private final ObjectMapper objectMapper;

    public void push(Long userId, String type, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", type, "payload", payload));
            sessionManager.sendToUser(userId, json);
        } catch (Exception e) {
            log.warn("Failed to serialize WS message for userId={}", userId, e);
        }
    }

    public void pushToMany(Iterable<Long> userIds, String type, Object payload) {
        for (Long userId : userIds) {
            push(userId, type, payload);
        }
    }
}
