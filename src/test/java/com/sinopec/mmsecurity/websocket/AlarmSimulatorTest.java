package com.sinopec.mmsecurity.websocket;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sinopec.mmsecurity.dto.AlarmItem;
import com.sinopec.mmsecurity.entity.FacAlarm;
import com.sinopec.mmsecurity.mapper.AlarmMapper;
import com.sinopec.mmsecurity.service.AlarmAssembler;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AlarmSimulator（纯 Mockito，不启动 Spring 上下文）：
 * - 无 WS 会话时不推送
 * - 库中最新的真实告警经 AlarmAssembler 转换后推送
 * - 与已推送告警相同（id 不变）时不重复推送
 * - 库中无告警时不推送
 * 彻底去除 Random 造数逻辑。
 */
class AlarmSimulatorTest {

    private final AlarmWebSocketHandler handler = mock(AlarmWebSocketHandler.class);
    private final AlarmMapper alarmMapper = mock(AlarmMapper.class);
    private final AlarmAssembler assembler = mock(AlarmAssembler.class);
    private final AlarmSimulator simulator = new AlarmSimulator(handler, alarmMapper, assembler);

    @Test
    void push_noSession_doesNotBroadcast() {
        when(handler.sessionCount()).thenReturn(0);
        simulator.push();
        verify(handler, never()).broadcastAlarm(any(AlarmItem.class));
    }

    @Test
    void push_newAlarm_broadcastsAndTracksId() {
        when(handler.sessionCount()).thenReturn(1);
        FacAlarm latest = alarm(10L);
        when(alarmMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);
        AlarmItem item = new AlarmItem();
        item.setAlarmId("AE-2026-001");
        when(assembler.toItem(latest)).thenReturn(item);

        simulator.push();
        verify(handler, times(1)).broadcastAlarm(item);
    }

    @Test
    void push_sameAlarm_doesNotRebroadcast() {
        when(handler.sessionCount()).thenReturn(1);
        FacAlarm latest = alarm(10L);
        when(alarmMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(latest);
        AlarmItem item = new AlarmItem();
        when(assembler.toItem(latest)).thenReturn(item);

        simulator.push(); // 首次推送
        simulator.push(); // 最新告警 id 未变，应跳过
        verify(handler, times(1)).broadcastAlarm(item);
    }

    @Test
    void push_noAlarm_doesNotBroadcast() {
        when(handler.sessionCount()).thenReturn(1);
        when(alarmMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        simulator.push();
        verify(handler, never()).broadcastAlarm(any(AlarmItem.class));
    }

    private FacAlarm alarm(Long id) {
        FacAlarm a = new FacAlarm();
        a.setId(id);
        a.setAlarmId("AE-2026-00" + id);
        return a;
    }
}
