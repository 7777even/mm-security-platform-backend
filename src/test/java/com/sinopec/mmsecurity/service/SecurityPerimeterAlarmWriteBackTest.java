package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.PerimeterAlarmDetail;
import com.sinopec.mmsecurity.dto.PerimeterAlarmUpdateRequest;
import com.sinopec.mmsecurity.entity.FacPerimeterAlarm;
import com.sinopec.mmsecurity.mapper.FacPerimeterAlarmMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SecurityService#updatePerimeterAlarm（纯 Mockito，不启动 Spring 上下文）：
 * read-modify-write 局部更新 + 中文状态枚举校验 + B3 异常分支。
 * 与 FireAlarmServiceTest 同源范式；SecurityService 其余 Mapper 由 Mockito 注入为 null，仅 perimeterAlarmMapper 参与本测试。
 */
@ExtendWith(MockitoExtension.class)
class SecurityPerimeterAlarmWriteBackTest {

    @Mock
    private FacPerimeterAlarmMapper perimeterAlarmMapper;

    @InjectMocks
    private SecurityService securityService;

    private static FacPerimeterAlarm existing(Long id) {
        FacPerimeterAlarm e = new FacPerimeterAlarm();
        e.setId(id);
        e.setStatus("未确认");
        e.setFalseAlarm("未核实");
        e.setTitle("周界入侵告警");
        e.setObjectName("南门西侧周界");
        e.setVersion(0L);
        return e;
    }

    @Test
    void update_statusOnly_persistsAndReturnsDetail() {
        when(perimeterAlarmMapper.selectById(1L)).thenReturn(existing(1L));
        PerimeterAlarmUpdateRequest req = new PerimeterAlarmUpdateRequest();
        req.setStatus("已确认");

        PerimeterAlarmDetail detail = securityService.updatePerimeterAlarm(1L, req);

        ArgumentCaptor<FacPerimeterAlarm> captor = ArgumentCaptor.forClass(FacPerimeterAlarm.class);
        verify(perimeterAlarmMapper).updateById(captor.capture());
        assertEquals("已确认", captor.getValue().getStatus());
        assertEquals("未核实", captor.getValue().getFalseAlarm());
        assertEquals("已确认", detail.getStatus());
    }

    @Test
    void update_falseAlarmOnly_keepsStatus() {
        when(perimeterAlarmMapper.selectById(2L)).thenReturn(existing(2L));
        PerimeterAlarmUpdateRequest req = new PerimeterAlarmUpdateRequest();
        req.setFalseAlarm("否");

        PerimeterAlarmDetail detail = securityService.updatePerimeterAlarm(2L, req);

        ArgumentCaptor<FacPerimeterAlarm> captor = ArgumentCaptor.forClass(FacPerimeterAlarm.class);
        verify(perimeterAlarmMapper).updateById(captor.capture());
        assertEquals("未确认", captor.getValue().getStatus());
        assertEquals("否", captor.getValue().getFalseAlarm());
        assertEquals("否", detail.getFalseAlarm());
    }

    @Test
    void update_invalidStatus_throwsParamInvalid() {
        when(perimeterAlarmMapper.selectById(3L)).thenReturn(existing(3L));
        PerimeterAlarmUpdateRequest req = new PerimeterAlarmUpdateRequest();
        req.setStatus("挂起");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> securityService.updatePerimeterAlarm(3L, req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(perimeterAlarmMapper, never()).updateById(any());
    }

    @Test
    void update_invalidFalseAlarm_throwsParamInvalid() {
        when(perimeterAlarmMapper.selectById(4L)).thenReturn(existing(4L));
        PerimeterAlarmUpdateRequest req = new PerimeterAlarmUpdateRequest();
        req.setFalseAlarm("也许");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> securityService.updatePerimeterAlarm(4L, req));
        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(perimeterAlarmMapper, never()).updateById(any());
    }

    @Test
    void update_notFound_throwsNotFound() {
        when(perimeterAlarmMapper.selectById(999L)).thenReturn(null);
        PerimeterAlarmUpdateRequest req = new PerimeterAlarmUpdateRequest();
        req.setStatus("已确认");

        BusinessException ex =
                assertThrows(BusinessException.class, () -> securityService.updatePerimeterAlarm(999L, req));
        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(perimeterAlarmMapper, never()).updateById(any());
    }

    @Test
    void update_disposalFields_persistsAll() {
        when(perimeterAlarmMapper.selectById(5L)).thenReturn(existing(5L));
        PerimeterAlarmUpdateRequest req = new PerimeterAlarmUpdateRequest();
        req.setHandleResult("经核实为检修人员临时跨越，已现场纠正");
        req.setHandleTime("2026-09-23 10:30:00");
        req.setDispatchPersonnel("王成,赵五");
        req.setNotifyApp(true);
        req.setNotifySms(false);

        PerimeterAlarmDetail detail = securityService.updatePerimeterAlarm(5L, req);

        ArgumentCaptor<FacPerimeterAlarm> captor = ArgumentCaptor.forClass(FacPerimeterAlarm.class);
        verify(perimeterAlarmMapper).updateById(captor.capture());
        FacPerimeterAlarm saved = captor.getValue();
        assertEquals("经核实为检修人员临时跨越，已现场纠正", saved.getHandleResult());
        assertEquals("2026-09-23 10:30:00", saved.getHandleTime());
        assertEquals("王成,赵五", saved.getDispatchPersonnel());
        assertEquals(Boolean.TRUE, saved.getNotifyApp());
        assertEquals(Boolean.FALSE, saved.getNotifySms());
        // 状态未传，保持原值
        assertEquals("未确认", saved.getStatus());
        assertEquals("经核实为检修人员临时跨越，已现场纠正", detail.getHandleResult());
    }
}
