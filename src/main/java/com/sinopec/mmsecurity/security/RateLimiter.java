package com.sinopec.mmsecurity.security;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 进程内令牌桶限流器（无外部依赖，单机维度）。
 *
 * 设计取舍：
 * - 不引入 Redis/网关，单机维度即可挡掉绝大多数刷接口与重放类流量；
 *   多实例横向扩展时各实例独立计数，需在前置 LB/CDN 层叠加全局限流（文档已说明）。
 * - 每把 key（如「IP:规则路径」）持有一个 Bucket；Bucket 内部 synchronized 保证线程安全。
 * - key 数量为 IP 量级（万级以内），不显式清理也能长期稳定运行；
 *   容量保护：超过 {@code MAX_BUCKETS} 时清理最久未访问的桶，避免内存增长失控。
 */
public class RateLimiter {

    /** 桶数量硬上限，超出触发 LRU 清理 */
    private static final int MAX_BUCKETS = 20_000;

    private final double refillTokensPerSecond;
    private final double capacity;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(double refillTokensPerSecond, double capacity) {
        if (refillTokensPerSecond <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("refillTokensPerSecond 与 capacity 必须为正");
        }
        this.refillTokensPerSecond = refillTokensPerSecond;
        this.capacity = capacity;
    }

    /**
     * 尝试消费一个令牌。成功返回 true，桶空返回 false（被限流）。
     */
    public boolean tryAcquire(String key) {
        long now = System.nanoTime();
        Bucket bucket = buckets.get(key);
        if (bucket == null) {
            // computeIfAbsent 保证每 key 单例；放宽松路径减少锁竞争
            bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, now));
        }
        evictIfNeeded();
        return bucket.tryAcquire(now, refillTokensPerSecond, capacity);
    }

    private void evictIfNeeded() {
        if (buckets.size() <= MAX_BUCKETS) {
            return;
        }
        // 简单保护：超出上限时清掉 10% 最久未访问的桶
        buckets.entrySet().stream()
                .sorted((a, b) -> Long.compare(a.getValue().lastAccessNanos, b.getValue().lastAccessNanos))
                .limit(buckets.size() / 10)
                .map(java.util.Map.Entry::getKey)
                .toList()
                .forEach(buckets::remove);
    }

    /** 令牌桶：持有当前令牌数与上次补充时间戳 */
    static final class Bucket {
        private double tokens;
        private long lastRefillNanos;
        private long lastAccessNanos;

        Bucket(double tokens, long now) {
            this.tokens = tokens;
            this.lastRefillNanos = now;
            this.lastAccessNanos = now;
        }

        synchronized boolean tryAcquire(long now, double refillPerSec, double capacity) {
            long elapsedNanos = now - lastRefillNanos;
            if (elapsedNanos > 0) {
                double added = (elapsedNanos / 1_000_000_000.0) * refillPerSec;
                tokens = Math.min(capacity, tokens + added);
                lastRefillNanos = now;
            }
            lastAccessNanos = now;
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
