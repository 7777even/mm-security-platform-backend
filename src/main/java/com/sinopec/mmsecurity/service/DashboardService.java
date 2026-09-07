package com.sinopec.mmsecurity.service;

import org.springframework.stereotype.Service;
import com.sinopec.mmsecurity.websocket.AlarmWebSocketHandler;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AlarmWebSocketHandler wsHandler;
    private final Random rnd = new Random();

    public Map<String, Object> overview() {
        return Map.of(
                "deviceTotal", 128,
                "deviceOnline", 121,
                "deviceFault", 3,
                "deviceOffline", 4,
                "alarmToday", 7,
                "alarmUnhandled", 2,
                "wsConnections", wsHandler.sessionCount()
        );
    }

    public List<Map<String, Object>> workstations() {
        return List.of(
                Map.of("id", "WS001", "name", "中控室A", "status", "ONLINE"),
                Map.of("id", "WS002", "name", "罐区值班室", "status", "ONLINE"),
                Map.of("id", "WS003", "name", "应急指挥中心", "status", "OFFLINE")
        );
    }
}
