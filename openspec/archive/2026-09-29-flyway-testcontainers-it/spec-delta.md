# Spec Delta: 三方言 Flyway 真机迁移验证

> 测试基建 Change，不新增/修改对外 capability Requirement。

## ADDED Evidence
### Scenario: 三方言迁移真机可验证
- **WHEN** 环境具备 Docker daemon（PG）或 `DAMENG_JDBC_URL`（DM8）
- **THEN** Testcontainers/ env IT 对空库应用各自方言全部 V 文件，断言 `info().current()` 非空且 `applied>=80`，CI 自动真跑
- **AND** 环境缺位时 `assumeTrue`/env 门控使 IT 跳过，不拖红日常 `mvn test`
