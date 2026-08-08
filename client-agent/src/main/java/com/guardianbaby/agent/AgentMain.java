package com.guardianbaby.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AgentMain {
    private final AgentConfig config;
    private final AgentHttpClient http;
    private final AgentWebSocket wsClient;
    private final DataCollector collector;
    private final DiscoveryResponder discoveryResponder;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // UI
    private JFrame frame;
    private JLabel statusDot;
    private JLabel statusText;
    private JLabel deviceLabel;
    private JTextArea eventLog;
    private JButton connectBtn;
    private JButton disconnectBtn;
    private JButton submitRequestBtn;
    private JComboBox<String> requestTypeCombo;

    private volatile boolean running = false;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    public AgentMain(AgentConfig config) {
        this.config = config;
        this.http = new AgentHttpClient(config.serverUrl());
        this.wsClient = new AgentWebSocket();
        this.collector = new DataCollector();
        this.discoveryResponder = new DiscoveryResponder(config.deviceName(), config.deviceType());
    }

    public void start() {
        SwingUtilities.invokeLater(this::buildUI);
    }

    private void buildUI() {
        frame = new JFrame("守护宝贝 Agent - " + config.deviceName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(520, 480);
        frame.setMinimumSize(new Dimension(400, 350));
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Top: Status bar
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        statusPanel.setBackground(SystemColor.control);
        statusDot = new JLabel("●");
        statusDot.setFont(new Font("Dialog", Font.BOLD, 20));
        statusDot.setForeground(Color.RED);
        statusText = new JLabel("未连接");
        statusText.setFont(new Font("Dialog", Font.BOLD, 14));
        deviceLabel = new JLabel("设备: " + config.deviceName() + " | " + config.serverUrl());
        deviceLabel.setFont(new Font("Dialog", Font.PLAIN, 12));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.add(statusPanel, BorderLayout.NORTH);
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        infoBar.add(deviceLabel);
        topBar.add(infoBar, BorderLayout.CENTER);
        mainPanel.add(topBar, BorderLayout.NORTH);

        // Center: Event log
        eventLog = new JTextArea();
        eventLog.setEditable(false);
        eventLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(eventLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("事件日志"));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom: Controls
        JPanel ctrlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));

        connectBtn = new JButton("连接服务器");
        connectBtn.addActionListener(e -> doConnect());
        ctrlPanel.add(connectBtn);

        disconnectBtn = new JButton("断开");
        disconnectBtn.setEnabled(false);
        disconnectBtn.addActionListener(e -> doDisconnect());
        ctrlPanel.add(disconnectBtn);

        ctrlPanel.add(new JLabel("  |  "));
        ctrlPanel.add(new JLabel("发起请求:"));
        requestTypeCombo = new JComboBox<>(new String[]{"TIME_EXTENSION", "UNBLOCK"});
        ctrlPanel.add(requestTypeCombo);

        submitRequestBtn = new JButton("提交");
        submitRequestBtn.setEnabled(false);
        submitRequestBtn.addActionListener(e -> doSubmitRequest());
        ctrlPanel.add(submitRequestBtn);

        mainPanel.add(ctrlPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });

        frame.setVisible(true);
        log("Agent 启动完成。点击「连接服务器」开始监控。");
    }

    private void doConnect() {
        connectBtn.setEnabled(false);
        log("正在连接 " + config.serverUrl() + " ...");

        new Thread(() -> {
            // 1. Login
            if (!http.login(config.username(), config.password())) {
                SwingUtilities.invokeLater(() -> {
                    log("[错误] 登录失败，请检查用户名和密码");
                    connectBtn.setEnabled(true);
                    updateStatus(false);
                });
                return;
            }
            log("登录成功 (userId=" + http.userId() + ")");

            // 2. Register device
            if (!http.registerDevice(config.deviceName(), config.deviceType())) {
                SwingUtilities.invokeLater(() -> {
                    log("[错误] 设备注册失败");
                    connectBtn.setEnabled(true);
                    updateStatus(false);
                });
                return;
            }
            log("设备已注册 (deviceId=" + http.deviceId() + ")");

            // 3. Connect WebSocket
            wsClient.connect(config.serverUrl(), http.token(), msg -> {
                if ("__CONNECTED__".equals(msg)) {
                    running = true;
                    updateStatus(true);
                    log("WebSocket 已连接");
                    discoveryResponder.start();
                    startPeriodicTasks();
                    SwingUtilities.invokeLater(() -> {
                        disconnectBtn.setEnabled(true);
                        submitRequestBtn.setEnabled(true);
                    });
                } else if ("__DISCONNECTED__".equals(msg)) {
                    running = false;
                    discoveryResponder.stop();
                    updateStatus(false);
                    log("WebSocket 已断开");
                    SwingUtilities.invokeLater(() -> {
                        connectBtn.setEnabled(true);
                        disconnectBtn.setEnabled(false);
                        submitRequestBtn.setEnabled(false);
                    });
                } else {
                    handleMessage(msg);
                }
            });
        }).start();
    }

    private void doDisconnect() {
        discoveryResponder.stop();
        shutdown();
        SwingUtilities.invokeLater(() -> {
            connectBtn.setEnabled(true);
            disconnectBtn.setEnabled(false);
            submitRequestBtn.setEnabled(false);
            updateStatus(false);
        });
    }

    private void doSubmitRequest() {
        String type = (String) requestTypeCombo.getSelectedItem();
        String target = type.equals("UNBLOCK") ? "抖音" : null;
        new Thread(() -> {
            try {
                String body = mapper.writeValueAsString(new java.util.HashMap<>() {{
                    put("requesterId", http.userId());
                    put("type", type);
                    put("description", type.equals("TIME_EXTENSION") ? "请求延长使用时间" : "请求解除封锁: " + target);
                    put("extraMinutes", type.equals("TIME_EXTENSION") ? 30 : null);
                    put("targetName", target);
                }});
                var req = new okhttp3.Request.Builder()
                        .url(http.baseUrl() + "/api/approvals")
                        .addHeader("Authorization", "Bearer " + http.token())
                        .post(okhttp3.RequestBody.create(body, okhttp3.MediaType.parse("application/json")))
                        .build();
                try (var res = new okhttp3.OkHttpClient().newCall(req).execute()) {
                    var json = mapper.readTree(res.body().string());
                    String msg = json.path("success").asBoolean() ? "请求已提交" : "提交失败: " + json.path("message").asText();
                    SwingUtilities.invokeLater(() -> log(msg));
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> log("提交请求失败: " + e.getMessage()));
            }
        }).start();
    }

    private void startPeriodicTasks() {
        // Heartbeat every 30s
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) return;
            boolean ok = http.sendHeartbeat();
            if (ok) SwingUtilities.invokeLater(() -> log("[心跳] ✓"));
        }, 30, 30, TimeUnit.SECONDS);

        // Usage data collection every 60s
        scheduler.scheduleAtFixedRate(() -> {
            if (!running) return;
            var events = collector.collect();
            for (var event : events) {
                http.reportUsage(event.app(), event.durationSeconds());
            }
            SwingUtilities.invokeLater(() -> log("[上报] " + events.size() + " 条使用记录"));
        }, 10, 60, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private void handleMessage(String msg) {
        try {
            var json = mapper.readTree(msg);
            String type = json.path("type").asText();
            var payload = json.path("payload");

            switch (type) {
                case "DEVICE_STATUS" -> log("[命令] 设备 " + payload.path("name").asText()
                        + " 状态变更: " + payload.path("status").asText());
                case "APPROVAL_RESULT" -> log("[审批] 请求状态: " + payload.path("status").asText()
                        + " (" + payload.path("responseMessage").asText("") + ")");
                case "APPROVAL_NEW" -> log("[通知] 新审批请求: " + payload.path("requesterName").asText());
                default -> log("[消息] " + type + ": " + payload.toPrettyString());
            }
        } catch (Exception e) {
            log("[消息] " + msg);
        }
    }

    private void updateStatus(boolean connected) {
        if (connected) {
            statusDot.setForeground(new Color(0, 180, 0));
            statusText.setText("已连接");
        } else {
            statusDot.setForeground(Color.RED);
            statusText.setText("未连接");
        }
    }

    private void log(String text) {
        String time = sdf.format(new Date());
        eventLog.append("[" + time + "] " + text + "\n");
    }

    private void shutdown() {
        running = false;
        discoveryResponder.stop();
        scheduler.shutdown();
        wsClient.close();
    }

    public static void main(String[] args) {
        AgentConfig config = AgentConfig.fromArgs(args);
        AgentMain agent = new AgentMain(config);
        agent.start();
    }
}
