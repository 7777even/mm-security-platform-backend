package com.sinopec.mmsecurity.websocket;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 告警推送模拟器（开发联调用）。
 * 每 12 秒广播一条模拟告警。生产环境切换为基于 fac_alarm 表的增量事件驱动推送。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmSimulator {

    private final AlarmWebSocketHandler handler;
    private final Random rnd = new Random();

    private static final List<String> LEVELS = List.of("1", "2", "3", "4");
    private static final List<String> TYPES = List.of("FIRE", "GAS", "FLOOD", "INTRUSION");
    private static final List<String> ZONES = List.of("罐区A", "装置B", "装卸区", "危化仓库");

    @PostConstruct
    public void banner() {
        log.info("告警推送模拟器已启动，每 12 秒广播一条模拟告警 → /ws/alarm");
    }

    @Scheduled(fixedDelay = 12000)
    public void push() {
        if (handler.sessionCount() == 0) {
            return;
        }
        String rndCode = randomDeviceCode();
        Map<String, Object> alarm = Map.of(
                "id", UUID.randomUUID().toString(),
                "deviceCode", rndCode,
                "level", LEVELS.get(rnd.nextInt(LEVELS.size())),
                "type", TYPES.get(rnd.nextInt(TYPES.size())),
                "title", ZONES.get(rnd.nextInt(ZONES.size())) + " 感应异常",
                "occurredAt", java.time.Instant.now().toString()
        );
        handler.broadcastAlarm(alarm);
    }

    private String randomDeviceCode() {
        char[] cs = new char[20];
        for (int i = 0; i < 20; i++) {
            if (rnd.nextBoolean()) cs[i] = (char) ('0' + rnd.nextInt(10));
            else cs[i] = (char) ('A' + rnd.nextInt(26));
        }
        return new String(cs);
    }
}
