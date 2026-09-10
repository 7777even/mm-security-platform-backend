package com.sinopec.mmsecurity.controller;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.GlobalExceptionHandler;
import com.sinopec.mmsecurity.dto.PermissionCodeItem;
import com.sinopec.mmsecurity.dto.SystemMenuNode;
import com.sinopec.mmsecurity.service.SystemMenuService;
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
 * SystemMenuController（standalone MockMvc）：菜单权限树、权限码字典与删除保护冲突。
 */
class SystemMenuControllerTest {

    private final SystemMenuService service = mock(SystemMenuService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SystemMenuController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    @Test
    void tree_returnsNestedNodes() throws Exception {
        SystemMenuNode button = new SystemMenuNode();
        button.setId(11L);
        button.setName("新增用户");
        button.setMenuType("BUTTON");
        button.setPermCode("system:user:create");

        SystemMenuNode parent = new SystemMenuNode();
        parent.setId(10L);
        parent.setName("用户管理");
        parent.setMenuType("MENU");
        parent.setChildren(List.of(button));
        when(service.tree()).thenReturn(List.of(parent));

        mockMvc.perform(get("/api/v1/system/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data[0].menuType").value("MENU"))
                .andExpect(jsonPath("$.data[0].children[0].permCode").value("system:user:create"));
    }

    @Test
    void permissions_returnsCodeDictionary() throws Exception {
        PermissionCodeItem item = new PermissionCodeItem();
        item.setPermCode("system:user:create");
        item.setName("新增用户");
        item.setMenuId(11L);
        when(service.permissions()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/v1/system/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].permCode").value("system:user:create"));
    }

    @Test
    void create_returnsNode() throws Exception {
        SystemMenuNode node = new SystemMenuNode();
        node.setId(20L);
        node.setCode("system-dict");
        when(service.create(any())).thenReturn(node);

        mockMvc.perform(post("/api/v1/system/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"数据字典\",\"code\":\"system-dict\",\"menuType\":\"MENU\",\"path\":\"/system/dicts\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("system-dict"));
    }

    @Test
    void create_missingCode_returnsParamInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/system/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"缺编码\",\"menuType\":\"MENU\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(100));
    }

    @Test
    void delete_withChildren_returns409() throws Exception {
        when(service.delete(anyLong()))
                .thenThrow(new BusinessException(409, "该节点下仍有 2 个子节点，请先删除子节点"));

        mockMvc.perform(delete("/api/v1/system/menus/10"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }

    @Test
    void delete_grantedNode_returns409() throws Exception {
        when(service.delete(anyLong()))
                .thenThrow(new BusinessException(409, "该节点已被 1 个角色授权，请先解除授权"));

        mockMvc.perform(delete("/api/v1/system/menus/11"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));
    }
}
