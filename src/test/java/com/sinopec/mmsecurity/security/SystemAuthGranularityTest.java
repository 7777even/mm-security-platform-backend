package com.sinopec.mmsecurity.security;

import com.sinopec.mmsecurity.common.BusinessException;
import com.sinopec.mmsecurity.common.ResultCode;
import com.sinopec.mmsecurity.controller.SystemDictController;
import com.sinopec.mmsecurity.controller.SystemMenuController;
import com.sinopec.mmsecurity.controller.SystemRoleController;
import com.sinopec.mmsecurity.controller.SystemUserController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 系统管理域鉴权粒度细化回归：真实 4 个控制器的方法级 {@code @RequireAuth(perm=...)}
 * 经 RequireAuthInterceptor 判定。验证：
 * <ul>
 *   <li>ADMIN（持全部 system:* perm）访问全部系统管理端点放行；</li>
 *   <li>无 system:* 授权的角色访问全部系统管理端点 → 403；</li>
 *   <li>{@code GET /system/dicts/{dictCode}}（options）仅登录即可，无 perm 也放行。</li>
 * </ul>
 *
 * <p>拦截器语义为「方法级注解整体覆盖类级」，故方法级 perm 生效、类级 role="ADMIN"
 * 仅作为未标 perm 方法的兜底。本测试覆盖真实注解，防止 perm 码或方法签名漂移。</p>
 */
class SystemAuthGranularityTest {

    private final RoleAuthorityService roleAuthority = mock(RoleAuthorityService.class);
    private final RequireAuthInterceptor interceptor = new RequireAuthInterceptor(roleAuthority);

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    private static final SystemUserController USER_CTRL = new SystemUserController(null);
    private static final SystemRoleController ROLE_CTRL = new SystemRoleController(null);
    private static final SystemMenuController MENU_CTRL = new SystemMenuController(null);
    private static final SystemDictController DICT_CTRL = new SystemDictController(null);

    /** 系统管理受 perm 保护的端点（class → method 名列表）。 */
    private static final Map<String, List<String>> PROTECTED = Map.of(
            "SystemUserController", List.of("page", "get", "create", "update", "delete", "updateStatus", "assignRole", "resetPassword"),
            "SystemRoleController", List.of("list", "get", "create", "update", "delete", "updateStatus", "menus", "assignMenus"),
            "SystemMenuController", List.of("tree", "create", "update", "delete", "permissions"),
            "SystemDictController", List.of("typePage", "createType", "updateType", "deleteType", "itemPage", "createItem", "updateItem", "deleteItem")
    );

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void adminHoldingAllPerms_passesAllSystemEndpoints() {
        UserContext.set(new LoginUser(1L, "admin", "ADMIN"));
        when(roleAuthority.hasPerm(anyString(), anyString())).thenReturn(true);

        for (var entry : PROTECTED.entrySet()) {
            Object bean = beanOf(entry.getKey());
            for (String m : entry.getValue()) {
                assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler(bean, m)),
                        () -> "ADMIN 应放行 " + entry.getKey() + "#" + m);
            }
        }
        // options 仅登录即可
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler(DICT_CTRL, "options")),
                "options() 应仅登录即可访问");
    }

    @Test
    void roleWithoutSystemPerms_rejected403OnAllSystemEndpoints() {
        UserContext.set(new LoginUser(9L, "viewer", "VIEWER"));
        when(roleAuthority.hasPerm(anyString(), anyString())).thenReturn(false);

        for (var entry : PROTECTED.entrySet()) {
            Object bean = beanOf(entry.getKey());
            for (String m : entry.getValue()) {
                BusinessException ex = assertThrows(BusinessException.class,
                        () -> interceptor.preHandle(request, response, handler(bean, m)),
                        () -> "无 system:* 授权的角色应被拦截于 " + entry.getKey() + "#" + m);
                assertEquals(ResultCode.FORBIDDEN, ex.getCode());
            }
        }
        // options 仅登录即可，无 perm 也应放行
        assertDoesNotThrow(() -> interceptor.preHandle(request, response, handler(DICT_CTRL, "options")),
                "options() 应仅登录即可访问");
    }

    private static Object beanOf(String className) {
        return switch (className) {
            case "SystemUserController" -> USER_CTRL;
            case "SystemRoleController" -> ROLE_CTRL;
            case "SystemMenuController" -> MENU_CTRL;
            default -> DICT_CTRL;
        };
    }

    private HandlerMethod handler(Object bean, String methodName) {
        Method target = null;
        for (Method m : bean.getClass().getDeclaredMethods()) {
            if (m.getName().equals(methodName)) {
                target = m;
                break;
            }
        }
        if (target == null) {
            throw new IllegalStateException("未找到方法：" + methodName);
        }
        return new HandlerMethod(bean, target);
    }
}
