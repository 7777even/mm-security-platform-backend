# emergency-reference Specification

## Purpose

应急指挥参考数据端点（应急力量 / 已结案件 / 值班 / 通讯录 / 知识库）。由 Change `implement-remaining-contracts`（已归档）回填。

## Requirements

### Requirement: 应急参考数据只读端点

系统须提供以下只读端点，全部以 B3 包络返回：

| 端点 | 响应 |
| --- | --- |
| `GET /api/v1/emergency/strength` | EmergencyStrength（应急力量） |
| `GET /api/v1/emergency/closed-cases` | ClosedCaseList（`fac_alarm` CLOSED 聚合） |
| `GET /api/v1/emergency/duty` | DutyRoster（值班表） |
| `GET /api/v1/emergency/phones` | EmergencyPhoneBook（应急通讯录） |
| `GET /api/v1/emergency/knowledge` | KnowledgeList（知识库） |

#### Scenario: 端点鉴权

- **WHEN** 未携带有效令牌访问上述任一端点
- **THEN** 返回 401（B3 包络），这些端点不在免鉴权白名单内

#### Scenario: 已结案件来源

- **WHEN** `GET /api/v1/emergency/closed-cases`
- **THEN** 数据来源于 `fac_alarm` 中 `status=CLOSED` 的聚合，不读取虚构事实
