package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.PasswordResetResult;
import com.sinopec.mmsecurity.dto.SystemUserItem;
import com.sinopec.mmsecurity.dto.SystemUserPageResult;
import com.sinopec.mmsecurity.service.SystemUserService;
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
 * SystemUserController（standalone MockMvc，不启动 Spring 上下文）：
 * 包络结构、分页透传、防护错误的 HTTP 状态映射。
 * 鉴权由 RequireAuthInterceptor 在 WebMvcConfig 注册，standalone 不挂载，故此处不校验登录态。
 */
class SystemUserControllerTest {

    private final SystemUserService service = mock(SystemUserService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SystemUserController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void page_returnsEnvelope() throws Exception {
        SystemUserPageResult result = new SystemUserPageResult();
        SystemUserItem item = new SystemUserItem();
        item.setId(1L);
        item.setUsername("admin");
        item.setRoleCode("ADMIN");
        result.setList(List.of(item));
        result.setTotal(1);
        result.setPage(1);
        result.setSize(10);
        when(service.page(eq(1L), eq(10L), any(), any(), any())).thenReturn(result);

        mockMvc.perform(get("/api/v1/system/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].username").value("admin"));
    }

    @Test
    void create_returnsItem() throws Exception {
        SystemUserItem item = new SystemUserItem();
        item.setId(2L);
        item.setUsername("zhang.san");
        item.setRoleName("外操");
        when(service.create(any())).thenReturn(item);

        mockMvc.perform(post("/api/v1/system/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"zhang.san\",\"password\":\"Init@12345\",\"realName\":\"张三\",\"roleCode\":\"OUTER_OPER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.roleName").value("外操"));
    }

    @Test
    void create_missingUsername_returnsParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/system/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Init@12345\",\"roleCode\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void deleteSelf_returns403() throws Exception {
        when(service.delete(anyLong()))
                .thenThrow(new BusinessException(403, "不可删除自己的账号"));

        mockMvc.perform(delete("/api/v1/system/users/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void deleteLastAdmin_returns409() throws Exception {
        when(service.delete(anyLong()))
                .thenThrow(new BusinessException(409, "必须保留至少一个启用状态的管理员"));

        mockMvc.perform(delete("/api/v1/system/users/7"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void updateStatus_passesQueryParam() throws Exception {
        SystemUserItem item = new SystemUserItem();
        item.setId(5L);
        item.setStatus(0);
        when(service.updateStatus(5L, 0)).thenReturn(item);

        mockMvc.perform(put("/api/v1/system/users/5/status").param("status", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value(0));
    }

    @Test
    void assignRole_usesBodyRoleCode() throws Exception {
        SystemUserItem item = new SystemUserItem();
        item.setId(5L);
        item.setRoleCode("SCHEDULER");
        when(service.assignRole(5L, "SCHEDULER")).thenReturn(item);

        mockMvc.perform(put("/api/v1/system/users/5/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roleCode\":\"SCHEDULER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("SCHEDULER"));
    }

    @Test
    void resetPassword_returnsTemporaryPassword() throws Exception {
        PasswordResetResult r = new PasswordResetResult();
        r.setTemporaryPassword("Ab3!xyZ9Qw2");
        r.setMustChangePwd(true);
        when(service.resetPassword(5L)).thenReturn(r);

        mockMvc.perform(post("/api/v1/system/users/5/password/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.temporaryPassword").value("Ab3!xyZ9Qw2"))
                .andExpect(jsonPath("$.data.mustChangePwd").value(true));
    }
}
