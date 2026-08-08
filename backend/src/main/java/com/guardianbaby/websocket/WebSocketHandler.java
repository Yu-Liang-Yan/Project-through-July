package com.guardianbaby.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {

    private final WebSocketSessionManager sessionManager;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.register(userId, session);
            log.info("WebSocket connected: userId={}", userId);
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        // 客户端暂不发送消息，仅接收推送
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable ex) {
        log.error("WebSocket error: sessionId={}", session.getId(), ex);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.remove(userId, session);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
