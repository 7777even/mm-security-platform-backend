package com.sinopec.mmsecurity.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.service.AlarmAssembler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 告警实时推送（基于 fac_alarm 表的增量轮询）。
 *
 * 每 12 秒检查一次：若存在比已推送告警更新的记录（按 occurred_at desc、id desc 取最新一条），
 * 经 {@link AlarmAssembler} 转换为 {@link AlarmItem} 后广播到所有已连接会话；否则不推送。
 * 彻底去除原 Random 造数逻辑，推送内容均为库中真实告警。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlarmSimulator {

    private final AlarmWebSocketHandler handler;
    private final AlarmMapper alarmMapper;
    private final AlarmAssembler assembler;

    /** 已推送的最新告警物理主键，避免重复广播同一条 */
    private volatile Long lastPushedId;

    @PostConstruct
    public void banner() {
        log.info("告警实时推送已启用：每 12 秒轮询 fac_alarm 增量，推送最新真实告警 → /ws/alarm");
    }

    @Scheduled(fixedDelay = 12000)
    public void push() {
        if (handler.sessionCount() == 0) {
            return;
        }
        FacAlarm latest = alarmMapper.selectOne(new LambdaQueryWrapper<FacAlarm>()
                .eq(FacAlarm::getDeleted, 0)
                .orderByDesc(FacAlarm::getOccurredAt)
                .orderByDesc(FacAlarm::getId)
                .last("LIMIT 1"));
        if (latest == null) {
            return;
        }
        if (latest.getId() != null && latest.getId().equals(lastPushedId)) {
            return;
        }
        lastPushedId = latest.getId();
        AlarmItem item = assembler.toItem(latest);
        handler.broadcastAlarm(item);
        log.debug("推送最新告警：alarmId={}, occurredAt={}", item.getAlarmId(), item.getTs());
    }
}
