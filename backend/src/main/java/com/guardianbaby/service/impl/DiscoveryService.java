package com.guardianbaby.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class DiscoveryService {

    private static final int DISCOVERY_PORT = 9999;
    private static final byte[] MAGIC_BYTES = "GUARDIAN_DISCOVER".getBytes(StandardCharsets.UTF_8);
    private static final int TIMEOUT_SECONDS = 3;

    private final ObjectMapper mapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> discover() {
        List<Map<String, Object>> devices = Collections.synchronizedList(new ArrayList<>());

        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(TIMEOUT_SECONDS * 1000);

            // Send broadcast
            DatagramPacket broadcastPacket = new DatagramPacket(
                MAGIC_BYTES, MAGIC_BYTES.length,
                InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT
            );
            socket.send(broadcastPacket);
            log.info("Discovery broadcast sent to port {}", DISCOVERY_PORT);

            // Collect responses
            byte[] buf = new byte[1024];
            long deadline = System.currentTimeMillis() + TIMEOUT_SECONDS * 1000;

            while (System.currentTimeMillis() < deadline) {
                try {
                    DatagramPacket response = new DatagramPacket(buf, buf.length);
                    socket.receive(response);
                    String json = new String(response.getData(), 0, response.getLength(), StandardCharsets.UTF_8);
                    Map<String, Object> info = mapper.readValue(json, Map.class);
                    info.put("ip", response.getAddress().getHostAddress());
                    devices.add(info);
                } catch (SocketTimeoutException e) {
                    break; // timeout, no more responses
                }
            }
        } catch (Exception e) {
            log.warn("Discovery failed: {}", e.getMessage());
        }

        return devices;
    }
}
