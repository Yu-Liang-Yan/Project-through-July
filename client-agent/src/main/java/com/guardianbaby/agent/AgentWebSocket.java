package com.guardianbaby.agent;

import javax.swing.SwingUtilities;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class AgentWebSocket {
    private WebSocket ws;
    private boolean connected = false;
    private Consumer<String> onMessage;

    public void connect(String serverUrl, String token, Consumer<String> messageHandler) {
        this.onMessage = messageHandler;
        String wsUrl = serverUrl.replace("http://", "ws://").replace("https://", "wss://") + "/ws?token=" + token;

        try {
            HttpClient client = HttpClient.newHttpClient();
            client.newWebSocketBuilder()
                .buildAsync(URI.create(wsUrl), new java.net.http.WebSocket.Listener() {
                    @Override
                    public void onOpen(WebSocket webSocket) {
                        ws = webSocket;
                        connected = true;
                        SwingUtilities.invokeLater(() -> onMessage.accept("__CONNECTED__"));
                        System.out.println("[WS] Connected to " + wsUrl);
                        webSocket.request(1);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        SwingUtilities.invokeLater(() -> onMessage.accept(data.toString()));
                        webSocket.request(1);
                        return null;
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        connected = false;
                        SwingUtilities.invokeLater(() -> onMessage.accept("__DISCONNECTED__"));
                        System.out.println("[WS] Disconnected: " + reason);
                        return null;
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        connected = false;
                        System.err.println("[WS] Error: " + error.getMessage());
                    }
                });
        } catch (Exception e) {
            System.err.println("[WS] Connection failed: " + e.getMessage());
        }
    }

    public void close() {
        if (ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "Agent shutting down");
    }

    public boolean isConnected() {
        return connected;
    }
}
