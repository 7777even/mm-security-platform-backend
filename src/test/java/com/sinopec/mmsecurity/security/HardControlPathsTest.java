package com.sinopec.mmsecurity.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 硬控路径单一真源（D5）：清单完整性防漂移。
 *
 * <p>两侧历史命名都必须仍在册，否则等于把已覆盖的红线悄悄放开：
 * 后端原 8 条（{@code devices/cmd} 等）与前端原 5 个概念
 * （{@code fire-pump}、{@code broadcast/force}、{@code door-lock/power}、
 * {@code evacuation/trigger}、{@code sprinkler/control}）。</p>
 */
class HardControlPathsTest {

    /** 后端既有清单（迁移前 HardControlInterceptor 内常量）。 */
    private static final List<String> LEGACY_BACKEND = List.of(
            "/api/v1/devices/cmd",
            "/api/v1/devices/control",
            "/api/v1/fire/release",
            "/api/v1/fire/suppress",
            "/api/v1/doors/lock",
            "/api/v1/doors/unlock",
            "/api/v1/broadcast/issue",
            "/api/v1/emergency/trigger"
    );

    /** 前端既有概念（迁移前 hardControlGuard.ts 正则），按后端路径形态表达。 */
    private static final List<String> LEGACY_FRONTEND = List.of(
            "/api/v1/fire-pump",
            "/api/v1/broadcast/force",
            "/api/v1/broadcast/cut",
            "/api/v1/broadcast/power",
            "/api/v1/door-lock/power",
            "/api/v1/door-lock/lock",
            "/api/v1/door-lock/unlock",
            "/api/v1/evacuation/trigger",
            "/api/v1/sprinkler/control"
    );

    @Test
    void legacyBackendPaths_allStillCovered() {
        for (String p : LEGACY_BACKEND) {
            assertTrue(HardControlPaths.matches(p), "后端历史硬控路径被移除：" + p);
        }
    }

    @Test
    void legacyFrontendConcepts_allStillCovered() {
        for (String p : LEGACY_FRONTEND) {
            assertTrue(HardControlPaths.matches(p), "前端历史硬控概念被移除：" + p);
        }
    }

    @Test
    void suffixes_allMatchWithPrefixAndSubPath() {
        for (String suffix : HardControlPaths.SUFFIXES) {
            assertTrue(HardControlPaths.matches(HardControlPaths.API_PREFIX + suffix),
                    "后缀自身未命中：" + suffix);
            assertTrue(HardControlPaths.matches(HardControlPaths.API_PREFIX + suffix + "/start"),
                    "后缀子路径未命中：" + suffix);
        }
    }

    @Test
    void fullPaths_sizeAndPrefixAligned() {
        List<String> full = HardControlPaths.fullPaths();
        org.junit.jupiter.api.Assertions.assertEquals(HardControlPaths.SUFFIXES.size(), full.size());
        full.forEach(p -> assertTrue(p.startsWith(HardControlPaths.API_PREFIX)));
    }

    @Test
    void normalPaths_notBlocked() {
        assertFalse(HardControlPaths.matches("/api/v1/devices"));
        assertFalse(HardControlPaths.matches("/api/v1/dashboard"));
        assertFalse(HardControlPaths.matches("/api/v1/fire-alarm/ack"));
        assertFalse(HardControlPaths.matches("/api/v1/emergency/commands"));
    }

    /**
     * A2 业务写侧 4 域端点<b>不得</b>被硬控红线误伤（它们是业务留痕，不是物理下行）。
     *
     * <p>反向锁定：若后续有人在 {@link HardControlPaths#SUFFIXES} 加了 {@code fire/*} 之类
     * 过宽的后缀，这批断言会立刻红——避免把已上线的业务写端点整片打死。</p>
     */
    @Test
    void businessWritePaths_notBlockedByRedline() {
        for (String p : List.of(
                "/api/v1/emergency/command-records",
                "/api/v1/emergency/duty-sign-ins",
                "/api/v1/typhoon/dispatch-orders",
                "/api/v1/fire/patrol-executions")) {
            assertFalse(HardControlPaths.matches(p), "业务写侧端点被硬控红线误伤：" + p);
        }
    }

    @Test
    void nullOrEmpty_notBlocked() {
        assertFalse(HardControlPaths.matches(null));
        assertFalse(HardControlPaths.matches(""));
    }
}
