package com.guardianbaby.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DiscoveryResponder {
    private static final int PORT = 9999;
    private static final byte[] MAGIC = "GUARDIAN_DISCOVER".getBytes(StandardCharsets.UTF_8);
    private final ObjectMapper mapper = new ObjectMapper();
    private final String deviceName;
    private final String deviceType;
    private volatile boolean running = true;
    private Thread listenerThread;

    public DiscoveryResponder(String deviceName, String deviceType) {
        this.deviceName = deviceName;
        this.deviceType = deviceType;
    }

    public void start() {
        listenerThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(PORT)) {
                System.out.println("[Discovery] Listening on UDP port " + PORT);
                byte[] buf = new byte[1024];
                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);

                        String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                        if (!"GUARDIAN_DISCOVER".equals(msg)) continue;

                        // Respond with device info
                        String response = mapper.writeValueAsString(Map.of(
                            "name", deviceName,
                            "type", deviceType,
                            "hostname", InetAddress.getLocalHost().getHostName()
                        ));
                        byte[] respBytes = response.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket respPacket = new DatagramPacket(
                            respBytes, respBytes.length,
                            packet.getAddress(), packet.getPort()
                        );
                        socket.send(respPacket);
                        System.out.println("[Discovery] Responded to " + packet.getAddress().getHostAddress());
                    } catch (Exception e) {
                        if (running) System.err.println("[Discovery] Error: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("[Discovery] Failed to bind: " + e.getMessage());
            }
        }, "discovery-responder");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void stop() {
        running = false;
        if (listenerThread != null) listenerThread.interrupt();
    }
}
