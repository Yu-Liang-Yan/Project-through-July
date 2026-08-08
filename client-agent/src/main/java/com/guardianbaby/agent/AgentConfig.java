package com.guardianbaby.agent;

public class AgentConfig {
    private String serverUrl;
    private String username;
    private String password;
    private String deviceName;
    private String deviceType;

    public AgentConfig() {
        this.serverUrl = "http://localhost:8080";
        this.username = "child";
        this.password = "123456";
        this.deviceName = System.getProperty("user.name") + "-设备";
        this.deviceType = "phone";
    }

    public static AgentConfig fromArgs(String[] args) {
        AgentConfig cfg = new AgentConfig();
        for (String arg : args) {
            if (arg.startsWith("--server=")) cfg.serverUrl = arg.substring(9);
            else if (arg.startsWith("--username=")) cfg.username = arg.substring(11);
            else if (arg.startsWith("--password=")) cfg.password = arg.substring(11);
            else if (arg.startsWith("--device-name=")) cfg.deviceName = arg.substring(14);
            else if (arg.startsWith("--device-type=")) cfg.deviceType = arg.substring(14);
        }
        return cfg;
    }

    public String serverUrl() { return serverUrl; }
    public String username() { return username; }
    public String password() { return password; }
    public String deviceName() { return deviceName; }
    public String deviceType() { return deviceType; }

    public void serverUrl(String v) { serverUrl = v; }
    public void username(String v) { username = v; }
    public void password(String v) { password = v; }
    public void deviceName(String v) { deviceName = v; }
    public void deviceType(String v) { deviceType = v; }
}
