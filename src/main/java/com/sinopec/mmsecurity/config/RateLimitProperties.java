package com.sinopec.mmsecurity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口限流配置（外部化，支持环境变量覆盖，如 {@code RATELIMIT_ENABLED=false}）。
 *
 * 绑定前缀 {@code ratelimit}，示例：
 * <pre>
 * ratelimit:
 *   enabled: true
 *   global-qps: 200
 *   global-burst: 400
 *   trust-forwarded: true
 *   ip-whitelist:
 *     - 127.0.0.1
 *     - ::1
 *   rules:
 *     - path: /api/v1/auth/login
 *       qps: 5
 *       burst: 10
 *     - path: /api/v1/auth/refresh
 *       qps: 10
 *       burst: 20
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "ratelimit")
public class RateLimitProperties {

    /** 总开关，默认开启。Dev 可通过环境变量 RATELIMIT_ENABLED=false 关闭便于联调 */
    private boolean enabled = true;
    /** 全局默认每秒令牌补充速率 */
    private int globalQps = 200;
    /** 全局默认桶容量（允许的最大并发突发） */
    private int globalBurst = 400;
    /** 是否信任 X-Forwarded-For 取真实客户端 IP（前置有可信 LB 时开启） */
    private boolean trustForwarded = true;
    /** 白名单 IP，命中直接放行不限流（本机探针、内网运维台等） */
    private List<String> ipWhitelist = new ArrayList<>(List.of("127.0.0.1", "::1"));
    /** 特定路径的限流覆盖规则，按顺序匹配第一个命中项 */
    private List<PathRule> rules = new ArrayList<>();

    public static class PathRule {
        /** Ant 风格路径，如 /api/v1/auth/login、/api/v1/alarm/** */
        private String path;
        private int qps = 50;
        private int burst = 100;

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public int getQps() { return qps; }
        public void setQps(int qps) { this.qps = qps; }
        public int getBurst() { return burst; }
        public void setBurst(int burst) { this.burst = burst; }
    }

    // getters / setters
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getGlobalQps() { return globalQps; }
    public void setGlobalQps(int globalQps) { this.globalQps = globalQps; }
    public int getGlobalBurst() { return globalBurst; }
    public void setGlobalBurst(int globalBurst) { this.globalBurst = globalBurst; }
    public boolean isTrustForwarded() { return trustForwarded; }
    public void setTrustForwarded(boolean trustForwarded) { this.trustForwarded = trustForwarded; }
    public List<String> getIpWhitelist() { return ipWhitelist; }
    public void setIpWhitelist(List<String> ipWhitelist) { this.ipWhitelist = ipWhitelist; }
    public List<PathRule> getRules() { return rules; }
    public void setRules(List<PathRule> rules) { this.rules = rules; }
}
