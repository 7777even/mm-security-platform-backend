package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.DeleteResult;
import com.sinopec.mmsecurity.dto.SystemRoleItem;
import com.sinopec.mmsecurity.service.SystemRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SystemRoleController（standalone MockMvc）：列表、增改删、授权读写与冲突映射。
 */
class SystemRoleControllerTest {

    private final SystemRoleService service = mock(SystemRoleService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SystemRoleController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void list_returnsArray() throws Exception {
        SystemRoleItem item = new SystemRoleItem();
        item.setId(1L);
        item.setRoleCode("ADMIN");
        item.setRoleName("系统管理员");
        item.setBuiltIn(true);
        item.setUserCount(1L);
        when(service.list(any())).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/system/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].roleCode").value("ADMIN"))
                .andExpect(jsonPath("$.data[0].builtIn").value(true))
                .andExpect(jsonPath("$.data[0].userCount").value(1));
    }

    @Test
    void create_returnsItem() throws Exception {
        SystemRoleItem item = new SystemRoleItem();
        item.setId(7L);
        item.setRoleCode("SCHEDULER");
        when(service.create(any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/system/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"scheduler\",\"roleName\":\"值班调度\",\"dataScope\":\"DEPT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("SCHEDULER"));
    }

    @Test
    void deleteBuiltInRole_returns403() throws Exception {
        when(service.delete(anyLong()))
                .thenThrow(new BusinessException(403, "内置角色不可删除"));

        mockMvc.perform(delete("/api/v1/system/roles/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void deleteRoleWithUsers_returns409() throws Exception {
        when(service.delete(anyLong()))
                .thenThrow(new BusinessException(409, "该角色下仍有 2 个用户，不可删除"));

        mockMvc.perform(delete("/api/v1/system/roles/7"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void updateStatus_passesQueryParam() throws Exception {
        SystemRoleItem item = new SystemRoleItem();
        item.setId(7L);
        item.setStatus(0);
        when(service.updateStatus(7L, 0)).thenReturn(item);

        mockMvc.perform(put("/api/v1/system/roles/7/status").param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void menus_returnsGrantedIds() throws Exception {
        when(service.menus(7L)).thenReturn(List.of(1L, 2L, 3L));

        mockMvc.perform(get("/api/v1/system/roles/7/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    void assignMenus_returnsSavedIds() throws Exception {
        when(service.assignMenus(eq(7L), any())).thenReturn(List.of(10L, 11L));

        mockMvc.perform(put("/api/v1/system/roles/7/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[10,11]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0]").value(10));
    }

    @Test
    void assignMenus_clearAdmin_returns409() throws Exception {
        when(service.assignMenus(eq(1L), any()))
                .thenThrow(new BusinessException(409, "ADMIN 角色不可清空授权（会立即造成管理员无权限）"));

        mockMvc.perform(put("/api/v1/system/roles/1/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"menuIds\":[]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void delete_returnsDeleteResult() throws Exception {
        DeleteResult r = new DeleteResult();
        r.setOk(true);
        when(service.delete(7L)).thenReturn(r);

        mockMvc.perform(delete("/api/v1/system/roles/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ok").value(true));
    }
}
