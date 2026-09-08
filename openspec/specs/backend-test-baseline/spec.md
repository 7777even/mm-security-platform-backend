# backend-test-baseline Specification

## Purpose

后端零依赖单测基线。由 Change `harden-backend-baseline`（已归档）回填。

## Requirements

### Requirement: 后端单测基线

系统须提供不依赖 Spring 上下文、数据库与外部服务的单元测试，覆盖 Controller 参数透传、Service 委托、JWT 与硬控关键路径。

#### Scenario: 回归保护

- **WHEN** 修改 Controller 分页参数、JWT 签发逻辑或硬控名单
- **THEN** 对应单测失败并阻止合入

#### Scenario: 测试形态

- **WHEN** 运行 `mvn test`
- **THEN** 全部单测以 standalone MockMvc + 纯 Mockito 运行，不启动 Spring 上下文、不连接数据库与外部服务
