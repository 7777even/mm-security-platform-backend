package com.sinopec.mmsecurity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 * 自定义 Info 贡献器：向 /actuator/info 注入应用标识（名称/描述/版本）。
 *
 * Spring Boot 默认不会暴露 application.yml 中随意写的 info.* 键，需通过 InfoContributor 显式贡献。
 * 这样运维/探针拉取 /actuator/info 能拿到可辨识的服务元数据，而非空 {}。
 */
@Component
public class AppInfoContributor implements InfoContributor {

    private final String name;
    private final String description;
    private final String version;

    public AppInfoContributor(
            @Value("${info.app.name:mm-security-backend}") String name,
            @Value("${info.app.description:}") String description,
            @Value("${info.app.version:dev}") String version) {
        this.name = name;
        this.description = description;
        this.version = version;
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("name", name);
        builder.withDetail("description", description);
        builder.withDetail("version", version);
    }
}
