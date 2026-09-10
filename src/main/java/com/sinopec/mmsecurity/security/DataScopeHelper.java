package com.sinopec.mmsecurity.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.util.Set;

/**
 * data_scope 行级 ABAC 注入工具：将解析出的防区集合应用到 MyBatis-Plus 查询条件。
 *
 * <p>各业务实体防区列名不一致（area / zone / areaName …），通用 MyBatis 数据权限拦截器不可行，
 * 故由各域 Service 在列表查询构建 {@link LambdaQueryWrapper} 之后、select 之前<b>显式</b>调用。</p>
 *
 * <p>三态语义（design.md §2）：</p>
 * <ul>
 *   <li>{@code zones == null}  → 不加任何条件（data_scope=ALL，看全部）；</li>
 *   <li>{@code zones} 为空集合 → {@code 1=0}（无可见防区，最小权限，避免返回任何行）；</li>
 *   <li>{@code zones} 非空     → {@code IN (zones)}。</li>
 * </ul>
 */
public final class DataScopeHelper {

    private DataScopeHelper() {
    }

    public static <T> void apply(LambdaQueryWrapper<T> qw, SFunction<T, ?> zoneColumn, Set<String> zones) {
        if (zones == null) {
            return;
        }
        if (zones.isEmpty()) {
            // 无任何可见防区：最小权限兜底，返回空集（避免 IN () 非法 SQL）
            qw.apply("1=0");
            return;
        }
        qw.in(zoneColumn, zones);
    }
}
