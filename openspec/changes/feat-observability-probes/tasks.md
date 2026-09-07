# Tasks: 可观测性探针 + 存量修复

- [x] pom.xml 引入 spring-boot-starter-actuator
- [x] application.yml 增加 management 暴露 health/info + probes.enabled + show-details=when_authorized + info.app 段
- [x] JwtFilter 白名单增加 /actuator（探针免 JWT）
- [x] HmacFilter.shouldNotFilter 放行 /actuator（生产 signature 开启时探针免签名）
- [x] 新增 AppInfoContributor，/actuator/info 暴露应用标识
- [x] FacWorkstation 补 @TableId(type=IdType.INPUT) 消除启动警告
- [x] mvn test 0 failure（71 case）
- [x] 真实启动冒烟：/actuator/health|readiness|liveness|info 均 200，FacWorkstation 警告归零
