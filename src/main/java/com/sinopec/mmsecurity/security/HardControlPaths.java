package com.sinopec.mmsecurity.security;

import java.util.List;

/**
 * 零下行控制红线：硬控路径<b>单一真源</b>（D5）。
 *
 * <p>背景：此前后端 {@code HardControlInterceptor} 与前端
 * {@code hardControlGuard.ts} 各维护一份清单，且命名不一致
 * （后端 {@code fire/release}、前端 {@code fire-pump} 指向同一类设备），
 * 新增下行端点时易只在一端登记，留下未兜底路径（风险 R2）。</p>
 *
 * <p>收敛方式：本类以「相对 {@code /api/v1/} 的路径后缀」为唯一真源，
 * 同时保留两套历史命名（不丢失任何一端既有覆盖）：</p>
 * <ul>
 *   <li>后端：{@link #matches(String)} 用 {@code /api/v1/ + 后缀} 前缀匹配请求 URI；</li>
 *   <li>前端：经 {@code GET /api/v1/system/hard-control-paths} 取同一份后缀生成正则。</li>
 * </ul>
 *
 * <p>新增任何下行控制能力，必须在此显式登记并评审；未登记即视为不存在，
 * 两端均不会拦截。</p>
 */
public final class HardControlPaths {

    /** API 统一前缀。 */
    public static final String API_PREFIX = "/api/v1/";

    /**
     * 硬控路径后缀（相对 {@code /api/v1/}），单一真源，顺序无关。
     *
     * <p>覆盖四类物理下行：设备指令 / 消防灭控 / 门禁 / 广播 / 疏散喷淋，
     * 含后端历史命名与前端历史命名两套写法。</p>
     */
    public static final List<String> SUFFIXES = List.of(
            // 设备下行指令
            "devices/cmd",
            "devices/control",
            // 消防灭控（后端 fire/* 与前端 fire-pump 两种历史命名）
            "fire/release",
            "fire/suppress",
            "fire-pump",
            // 门禁（后端 doors/* 与前端 door-lock/* 两种历史命名）
            "doors/lock",
            "doors/unlock",
            "door-lock/power",
            "door-lock/lock",
            "door-lock/unlock",
            // 广播强切
            "broadcast/issue",
            "broadcast/force",
            "broadcast/cut",
            "broadcast/power",
            // 疏散 / 喷淋触发
            "emergency/trigger",
            "evacuation/trigger",
            "sprinkler/control"
    );

    private HardControlPaths() {
    }

    /** 全量完整路径（含 {@code /api/v1/} 前缀），供对外暴露与文档使用。 */
    public static List<String> fullPaths() {
        return SUFFIXES.stream().map(s -> API_PREFIX + s).toList();
    }

    /**
     * 判断请求 URI 是否命中硬控红线。
     *
     * @param uri 请求 URI（含上下文与 {@code /api/v1/} 前缀）
     * @return true 表示命中，调用方应拒绝该写请求
     */
    public static boolean matches(String uri) {
        if (uri == null || uri.isEmpty()) {
            return false;
        }
        for (String suffix : SUFFIXES) {
            if (uri.startsWith(API_PREFIX + suffix)) {
                return true;
            }
        }
        return false;
    }
}
