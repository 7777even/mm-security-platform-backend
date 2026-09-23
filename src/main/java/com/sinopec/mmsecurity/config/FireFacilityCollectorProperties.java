package com.sinopec.mmsecurity.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 消防监测采集器配置（外部化，支持环境变量覆盖，如 {@code FIRE_FACILITY_COLLECTOR_ENABLED=true}）。
 *
 * <p>绑定前缀 {@code fire-facility.collector}，示例：
 * <pre>
 * fire-facility:
 *   collector:
 *     enabled: false
 *     mode: upstream
 *     cron: "0 0/1 * * * ?"
 *     upstream:
 *       url: https://fire-host.example/monitor/api
 *       token: ${FIRE_COLLECTOR_TOKEN}
 *       timeout-seconds: 10
 *       insecure-tls: false
 * </pre>
 *
 * <p>默认关闭：监测表保持 Flyway 种子 / 上次真实上报值，不造假数据。接入真实采集源时
 * {@code enabled=true} 并实现 {@code FireFacilitySourceAdapter} 对接上游（消防主机 / 物联网平台 /
 * SCADA），采集器按 {@code cron} 周期拉取 → 复用上报写链路落库 + 触发大屏实时刷新。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "fire-facility.collector")
public class FireFacilityCollectorProperties {

    /** 总开关，默认关闭（无真实采集源时不跑）。 */
    private boolean enabled = false;

    /** 采集模式：upstream=主动拉取 / push=由外部源推送（此时采集器仅作兜底，可关闭）。 */
    private String mode = "upstream";

    /** 拉取周期（Spring cron，默认每分钟整分触发）。 */
    private String cron = "0 0/1 * * * ?";

    /** 上游数据源配置。 */
    private Upstream upstream = new Upstream();

    @Data
    public static class Upstream {
        /** 上游地址（HTTP 拉取时必填）。为空表示未配置真实采集源。 */
        private String url;
        /** 鉴权令牌（如有）。 */
        private String token;
        /** 连接/读取超时（秒）。 */
        private int timeoutSeconds = 10;
        /** 是否跳过 TLS 校验（仅内网自签证书测试用）。 */
        private boolean insecureTls = false;
    }
}
