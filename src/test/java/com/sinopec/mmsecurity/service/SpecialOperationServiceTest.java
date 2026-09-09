package com.sinopec.mmsecurity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sinopec.mmsecurity.dto.SpecialOperationDetail;
import com.sinopec.mmsecurity.dto.SpecialOperationPage;
import com.sinopec.mmsecurity.entity.FacSpecialOperationGas;
import com.sinopec.mmsecurity.entity.FacSpecialOperationPerson;
import com.sinopec.mmsecurity.entity.FacSpecialOperationTicket;
import com.sinopec.mmsecurity.entity.FacSpecialOperationVideo;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationGasMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationPersonMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationTicketMapper;
import com.sinopec.mmsecurity.mapper.FacSpecialOperationVideoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** 特殊作业服务逻辑校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class SpecialOperationServiceTest {

    @Mock
    private FacSpecialOperationTicketMapper ticketMapper;
    @Mock
    private FacSpecialOperationVideoMapper videoMapper;
    @Mock
    private FacSpecialOperationGasMapper gasMapper;
    @Mock
    private FacSpecialOperationPersonMapper personMapper;

    @InjectMocks
    private SpecialOperationService service;

    private static FacSpecialOperationTicket ticket(long id) {
        FacSpecialOperationTicket t = new FacSpecialOperationTicket();
        t.setId(id);
        t.setTicketArea("重油加氢装置");
        t.setOpType("动火作业");
        t.setOpLevel("二级");
        t.setTicketStatus("已签发");
        t.setWorkLocation("重油加氢装置");
        return t;
    }

    @Test
    void list_mapsPagedTickets() {
        Page<FacSpecialOperationTicket> page = new Page<>(1, 10);
        page.setTotal(12);
        page.setRecords(List.of(ticket(1L)));
        when(ticketMapper.selectPage(any(), any())).thenReturn(page);

        SpecialOperationPage result = service.list(1, 10, null, null, null, null);

        assertEquals(12, result.getTotal());
        assertEquals(2, result.getPages());
        assertEquals(1, result.getList().size());
        assertEquals("动火作业", result.getList().get(0).getType());
        assertEquals("二级", result.getList().get(0).getLevel());
    }

    @Test
    void detail_mapsChildren() {
        when(ticketMapper.selectById(1L)).thenReturn(ticket(1L));
        FacSpecialOperationVideo video = new FacSpecialOperationVideo();
        video.setId(1L);
        video.setTicketId(1L);
        video.setName("现场视频-1");
        video.setLocation("重油加氢装置监控点1");
        when(videoMapper.selectList(any())).thenReturn(List.of(video));
        FacSpecialOperationGas gas = new FacSpecialOperationGas();
        gas.setId(1L);
        gas.setTicketId(1L);
        gas.setName("可燃气体");
        gas.setValueText("0.2%LEL");
        gas.setStatusName("正常");
        when(gasMapper.selectList(any())).thenReturn(List.of(gas));
        FacSpecialOperationPerson person = new FacSpecialOperationPerson();
        person.setId(1L);
        person.setTicketId(1L);
        person.setName("阮国述");
        person.setRoleName("施工人员");
        person.setPhone("13800001111");
        when(personMapper.selectList(any())).thenReturn(List.of(person));

        SpecialOperationDetail detail = service.detail(1L);

        assertEquals("重油加氢装置", detail.getArea());
        assertEquals(1, detail.getVideos().size());
        assertEquals("0.2%LEL", detail.getGasPoints().get(0).getValue());
        assertEquals("正常", detail.getGasPoints().get(0).getStatus());
        assertEquals("施工人员", detail.getPersonnel().get(0).getRole());
    }

    @Test
    void detail_returnsNullWhenMissing() {
        when(ticketMapper.selectById(999L)).thenReturn(null);

        assertNull(service.detail(999L));
    }
}
