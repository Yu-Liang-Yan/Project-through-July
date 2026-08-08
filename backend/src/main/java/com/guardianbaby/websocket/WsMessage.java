package com.guardianbaby.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage {
    private String type;      // APPROVAL | ALERT | DEVICE_STATUS | APPROVAL_COUNT
    private Object payload;   // 具体数据

    public static WsMessage of(String type, Object payload) {
        return new WsMessage(type, payload);
    }

    public String toJson() {
        return "{\"type\":\"" + type + "\",\"payload\":" + payload + "}";
    }
}
