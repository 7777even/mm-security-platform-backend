package com.sinopec.mmsecurity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 菜单项（GET /auth/menus）。
 *
 * <p>契约对齐前端 {@code src/router/menu.ts#MENU_ROUTE_SPECS}：{@code id} 必须是该映射的
 * 字符串 key（如 {@code fm-fire}），前端据此装配对应子应用路由；{@code path} 为前端路由路径。
 * 此前的实现返回数字 id(1..5) + 后端资源路径（/dashboard、/facility…），与前端 key 完全对不上，
 * 导致前端动态路由全部被跳过、只能降级用内置 DEFAULT_MENUS。这里改为以 fm-* 子应用目录名为 id。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MenuVO(
        /** 菜单 id，与前端路由 name / 权限码对齐（如 fm-fire） */
        String id,
        /** 展示名 */
        String name,
        /** 前端路由路径（如 /fire） */
        String path) {
}
