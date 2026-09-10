package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.DictItemItem;
import com.sinopec.mmsecurity.dto.DictItemPageResult;
import com.sinopec.mmsecurity.dto.DictTypeItem;
import com.sinopec.mmsecurity.dto.DictTypePageResult;
import com.sinopec.mmsecurity.service.SystemDictService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SystemDictController（standalone MockMvc）：字典类型/项两级维护 + 业务只读 options。
 */
class SystemDictControllerTest {

    private final SystemDictService service = mock(SystemDictService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SystemDictController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void typePage_returnsEnvelope() throws Exception {
        DictTypePageResult result = new DictTypePageResult();
        DictTypeItem item = new DictTypeItem();
        item.setId(1L);
        item.setDictCode("alarm_level");
        item.setBuiltIn(false);
        result.setList(List.of(item));
        result.setTotal(1);
        when(service.typePage(1L, 10L, null)).thenReturn(result);

        mockMvc.perform(get("/api/v1/system/dict-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].dictCode").value("alarm_level"));
    }

    @Test
    void createType_returnsItem() throws Exception {
        DictTypeItem item = new DictTypeItem();
        item.setId(2L);
        item.setDictCode("alarm_level");
        when(service.createType(any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/system/dict-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dictCode\":\"alarm_level\",\"dictName\":\"报警等级\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dictCode").value("alarm_level"));
    }

    @Test
    void deleteBuiltInType_returns403() throws Exception {
        when(service.deleteType(anyLong())).thenThrow(new BusinessException(403, "内置字典不可删除"));

        mockMvc.perform(delete("/api/v1/system/dict-types/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void itemPage_requiresDictCode() throws Exception {
        mockMvc.perform(get("/api/v1/system/dict-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void itemPage_returnsEnvelope() throws Exception {
        DictItemPageResult result = new DictItemPageResult();
        DictItemItem item = new DictItemItem();
        item.setId(1L);
        item.setDictCode("alarm_level");
        item.setItemValue("1");
        item.setItemLabel("一级");
        result.setList(List.of(item));
        result.setTotal(1);
        when(service.itemPage(1L, 20L, "alarm_level")).thenReturn(result);

        mockMvc.perform(get("/api/v1/system/dict-items").param("dictCode", "alarm_level"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.list[0].itemLabel").value("一级"));
    }

    @Test
    void options_returnsEnabledItems() throws Exception {
        DictItemItem item = new DictItemItem();
        item.setDictCode("alarm_level");
        item.setItemValue("1");
        item.setItemLabel("一级");
        when(service.options("alarm_level")).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/system/dicts/alarm_level"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].itemValue").value("1"));
    }

    @Test
    void deleteItem_returnsDeleteResult() throws Exception {
        DeleteResult r = new DeleteResult();
        r.setOk(true);
        when(service.deleteItem(3L)).thenReturn(r);

        mockMvc.perform(delete("/api/v1/system/dict-items/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
