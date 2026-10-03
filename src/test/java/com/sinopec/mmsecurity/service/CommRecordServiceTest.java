package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.dto.CommRecordWriteRequest;
import com.sinopec.mmsecurity.dto.CommunicationRecord;
import com.sinopec.mmsecurity.dto.CommunicationRecordList;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.entity.FacCommRecord;
import com.sinopec.mmsecurity.mapper.FacCommRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通讯通知记录服务（纯 Mockito，不起 Spring 上下文、不连 DB）。
 *
 * <p>写端点部分校验 id 走 {@link LedgerIdSupport} 的 max+1 —— 规避 H2 / PG / DM
 * 在 seeder 里显式插 id 后自增序列不推进、新增撞主键 409「数据冲突」的老问题。
 */
@ExtendWith(MockitoExtension.class)
class CommRecordServiceTest {

    @Mock
    private FacCommRecordMapper commRecordMapper;

    @InjectMocks
    private CommRecordService service;

    private static FacCommRecord record(Long id, String recordNo) {
        FacCommRecord r = new FacCommRecord();
        r.setId(id);
        r.setRecordNo(recordNo);
        r.setRecordType("sms");
        r.setOccurredAt("2026-09-20 08:30");
        r.setCategory("应急通知");
        r.setSender("指挥中心");
        r.setReceiver("全体值班人员");
        r.setSummary("应急演练通知");
        return r;
    }

    private static CommRecordWriteRequest request() {
        CommRecordWriteRequest in = new CommRecordWriteRequest();
        in.setRecordNo("SMS-2026-010");
        in.setRecordType("sms");
        in.setOccurredAt("2026-09-20 08:30");
        in.setCategory("应急通知");
        in.setSender("指挥中心");
        in.setReceiver("全体值班人员");
        in.setSummary("应急演练通知");
        return in;
    }

    @Test
    void list_mapsItemsAndCountsTotal() {
        when(commRecordMapper.selectList(any())).thenReturn(List.of(record(1L, "SMS-001")));

        CommunicationRecordList result = service.list("sms");

        assertEquals(1, result.getTotal());
        CommunicationRecord item = result.getItems().get(0);
        assertEquals("SMS-001", item.getRecordNo());
        assertEquals("应急通知", item.getCategory());
    }

    @Test
    void createRecord_emptyTable_assignsIdOneAndVersionZero() {
        when(commRecordMapper.selectOne(any())).thenReturn(null);

        CommunicationRecord created = service.createRecord(request());

        assertEquals("SMS-2026-010", created.getRecordNo());
        ArgumentCaptor<FacCommRecord> captor = ArgumentCaptor.forClass(FacCommRecord.class);
        verify(commRecordMapper).insert(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals(0L, captor.getValue().getVersion());
    }

    @Test
    void createRecord_existingRows_assignsMaxIdPlusOne() {
        when(commRecordMapper.selectOne(any())).thenReturn(record(41L, "SMS-041"));

        service.createRecord(request());

        ArgumentCaptor<FacCommRecord> captor = ArgumentCaptor.forClass(FacCommRecord.class);
        verify(commRecordMapper).insert(captor.capture());
        assertEquals(42L, captor.getValue().getId());
    }

    @Test
    void updateRecord_appliesFieldsByRecordNo() {
        FacCommRecord existing = record(1L, "SMS-2026-010");
        when(commRecordMapper.selectList(any())).thenReturn(List.of(existing));
        CommRecordWriteRequest in = request();
        in.setSummary("改后的摘要");
        in.setSender("安全环保部");

        CommunicationRecord updated = service.updateRecord("SMS-2026-010", in);

        assertEquals("改后的摘要", updated.getSummary());
        assertEquals("安全环保部", updated.getSender());
        verify(commRecordMapper).updateById(existing);
    }

    @Test
    void updateRecord_bodyRecordNoDoesNotOverridePathKey() {
        // PUT /communication/records/{recordNo}：定位的业务自然键必须由路径决定。
        // 若被请求体覆盖，body 与 path 不一致时会把记录改到另一个编号下，资源从原 URL 消失。
        FacCommRecord existing = record(1L, "SMS-2026-010");
        when(commRecordMapper.selectList(any())).thenReturn(List.of(existing));
        CommRecordWriteRequest in = request();
        in.setRecordNo("SMS-OTHER");

        service.updateRecord("SMS-2026-010", in);

        assertEquals("SMS-2026-010", existing.getRecordNo());
    }

    @Test
    void updateRecord_notFound_returnsNull() {
        when(commRecordMapper.selectList(any())).thenReturn(List.of());

        assertNull(service.updateRecord("NOPE", request()));
    }

    @Test
    void deleteRecord_present_returnsOkTrue() {
        when(commRecordMapper.selectList(any())).thenReturn(List.of(record(1L, "SMS-2026-010")));
        when(commRecordMapper.deleteById(1L)).thenReturn(1);

        DeleteResult result = service.deleteRecord("SMS-2026-010");

        assertTrue(result.getOk());
    }

    @Test
    void deleteRecord_absent_returnsOkFalseAndSkipsDelete() {
        when(commRecordMapper.selectList(any())).thenReturn(List.of());

        assertFalse(service.deleteRecord("NOPE").getOk());
        verify(commRecordMapper, never()).deleteById(anyLong());
    }
}
