package com.sinopec.mmsecurity.service;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.dto.FormRecordCreateRequest;
import com.sinopec.mmsecurity.dto.FormRecordItem;
import com.sinopec.mmsecurity.entity.FacFormRecord;
import com.sinopec.mmsecurity.mapper.FacFormRecordMapper;
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

/** 流程填报记录服务校验（纯 Mockito，不起 Spring 上下文、不连 DB）。 */
@ExtendWith(MockitoExtension.class)
class FormRecordServiceTest {

    @Mock
    private FacFormRecordMapper formRecordMapper;

    @InjectMocks
    private FormRecordService service;

    private static FormRecordCreateRequest createReq() {
        FormRecordCreateRequest req = new FormRecordCreateRequest();
        req.setFormType("隐患排查");
        req.setTitle("储罐区防静电接地巡检填报");
        req.setReporter("张伟");
        req.setDepartment("储运部");
        req.setDetailJson("{\"location\":\"储罐区T-301\",\"level\":\"一般\"}");
        req.setStatus("DRAFT");
        return req;
    }

    private static FacFormRecord row(long id) {
        FacFormRecord e = new FacFormRecord();
        e.setId(id);
        e.setFormNo("FR-20260901-0001");
        e.setFormType("隐患排查");
        e.setTitle("储罐区防静电接地巡检填报");
        e.setReporter("张伟");
        e.setDepartment("储运部");
        e.setStatus("REVIEWED");
        e.setVersion(0L);
        return e;
    }

    /**
     * V66 种子以显式 id（1、2）插入，自增序列不会随之推进 —— 若交给数据库分配主键，
     * 新增行会撞 id=1 导致 create 恒定 409。这里固定 nextId = max(id)+1。
     */
    @Test
    void create_assignsExplicitNextIdFromMaxExistingId() {
        when(formRecordMapper.selectOne(any())).thenReturn(row(2L));

        FormRecordItem item = service.create(createReq());

        ArgumentCaptor<FacFormRecord> cap = ArgumentCaptor.forClass(FacFormRecord.class);
        verify(formRecordMapper).insert(cap.capture());
        assertEquals(3L, cap.getValue().getId());
        assertEquals(3L, item.getId());
    }

    @Test
    void create_startsFromOneWhenTableEmpty() {
        when(formRecordMapper.selectOne(any())).thenReturn(null);

        service.create(createReq());

        ArgumentCaptor<FacFormRecord> cap = ArgumentCaptor.forClass(FacFormRecord.class);
        verify(formRecordMapper).insert(cap.capture());
        assertEquals(1L, cap.getValue().getId());
    }

    @Test
    void create_rejectsIllegalFormType() {
        FormRecordCreateRequest req = createReq();
        req.setFormType("非法类型");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.create(req));

        assertEquals(ResultCode.PARAM_INVALID, ex.getCode());
        verify(formRecordMapper, never()).insert(any());
    }

    @Test
    void delete_existingRowRemovesIt() {
        when(formRecordMapper.selectById(3L)).thenReturn(row(3L));

        service.delete(3L);

        verify(formRecordMapper).deleteById(3L);
    }

    @Test
    void delete_missingRowThrowsNotFound() {
        when(formRecordMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(999L));

        assertEquals(ResultCode.NOT_FOUND, ex.getCode());
        verify(formRecordMapper, never()).deleteById(any());
    }
}
