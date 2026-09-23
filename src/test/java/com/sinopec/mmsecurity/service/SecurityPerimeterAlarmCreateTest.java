package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.dto.PerimeterAlarmCreateRequest;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * SecurityService#createPerimeterAlarm（纯 Mockito，不启动 Spring 上下文）：
 * 手工录入落库 + 默认值填充（alarmCode/status/falseAlarm/source/version）+ 标题非空校验。
 * 与 SecurityPerimeterAlarmWriteBackTest 同源范式；仅 perimeterAlarmMapper 参与本测试。
 */
@ExtendWith(MockitoExtension.class)
class SecurityPerimeterAlarmCreateTest {

    @Mock
    private FacPerimeterAlarmMapper perimeterAlarmMapper;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void create_persistsWithDefaultsAndReturnsDetail() {
        PerimeterAlarmCreateRequest req = new PerimeterAlarmCreateRequest();
        req.setTitle("南门未经授权翻越");
        req.setLocation("厂区南门西侧 200 米");

        PerimeterAlarmDetail detail = securityService.createPerimeterAlarm(req);

        ArgumentCaptor<FacPerimeterAlarm> captor = ArgumentCaptor.forClass(FacPerimeterAlarm.class);
        verify(perimeterAlarmMapper).insert(captor.capture());
        FacPerimeterAlarm saved = captor.getValue();
        assertEquals("南门未经授权翻越", saved.getTitle());
        assertEquals("周界入侵告警", saved.getAlarmType());
        assertEquals("未确认", saved.getStatus());
        assertEquals("未核实", saved.getFalseAlarm());
        assertEquals("人工录入", saved.getSource());
        assertEquals(0L, saved.getVersion());
        assertNotNull(saved.getAlarmCode());
        assertTrue(saved.getAlarmCode().startsWith("PA-"), "alarmCode 应以 PA- 前缀");
        // 返回详情与落库一致
        assertEquals("未确认", detail.getStatus());
        assertEquals("周界入侵告警", detail.getAlarmType());
    }

    @Test
    void create_blankTitle_throwsParamInvalid() {
        PerimeterAlarmCreateRequest req = new PerimeterAlarmCreateRequest();
        req.setTitle("   ");

        assertThrows(BusinessException.class, () -> securityService.createPerimeterAlarm(req));
        verify(perimeterAlarmMapper, never()).insert(any());
    }
}
