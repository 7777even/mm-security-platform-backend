package com.sinopec.mmsecurity.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RateLimiter 令牌桶算法单元测试（纯逻辑，无 Spring 上下文）。
 */
class RateLimiterTest {

    @Test
    void allowsUpToBurstThenRejects() {
        RateLimiter limiter = new RateLimiter(1.0, 2.0); // 1 qps, capacity 2
        assertTrue(limiter.tryAcquire("ip"));
        assertTrue(limiter.tryAcquire("ip"));
        assertFalse(limiter.tryAcquire("ip")); // 桶空 -> 拒绝
    }

    @Test
    void refillsOverTime() throws InterruptedException {
        RateLimiter limiter = new RateLimiter(10.0, 1.0); // 10 qps, capacity 1
        assertTrue(limiter.tryAcquire("ip"));
        assertFalse(limiter.tryAcquire("ip")); // 立即再取 -> 空
        Thread.sleep(200); // 0.2s 补充约 2 个令牌，cap 1 -> 重新满
        assertTrue(limiter.tryAcquire("ip"));
    }

    @Test
    void independentPerKey() {
        RateLimiter limiter = new RateLimiter(1.0, 1.0);
        assertTrue(limiter.tryAcquire("a"));
        assertTrue(limiter.tryAcquire("b")); // 不同 key 独立计数
        assertFalse(limiter.tryAcquire("a"));
    }

    @Test
    void rejectsInvalidConfig() {
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new RateLimiter(1, 0));
    }
}
