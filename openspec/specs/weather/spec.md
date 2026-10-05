# weather Specification

## Purpose

天气观测与预报聚合能力：一次返回实时观测、逐时预报与逐日预报，取代前端硬编码的 `weatherMock`
业务数据，供大屏天气监测面板展示。

> **来源说明（重要）**：本 capability **没有对应的已归档 spec-delta**（历史上该端点随契约回填直接落地）。
> 本文件由契约真源 `frontend-scaffold/docs/api/weather.openapi.json` 与 `WeatherController` 实现反推，
> 属 2026-10-06「spec-delta 回填」治理批次的补建项；若后续发现原始决策记录，应以原始记录为准并回链。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/weather/overview` | 登录即可 | 天气观测与预报聚合（实时 / 逐时 / 逐日） |

## Requirements

### Requirement: 天气观测与预报聚合

系统 SHALL 提供 `GET /api/v1/weather/overview`，一次返回三部分数据，供天气监测面板展示：

- `current`：实时观测——temperature / condition / airQuality / airQualityLevel / windDirection /
  windSpeed / windLevel / humidity / pressure / visibility / rainfall / updatedAt；
- `hourly[]`：逐时预报——time / rain / wind / temperature / pressure / humidity；
- `daily[]`：逐日预报——day / date / condition / icon / high / low / wind / humidity / rain。

响应统一 B3 包络（HTTP 200 + `code=0`）；未鉴权返回 401。

#### Scenario: 面板一次拉取三块数据
- **WHEN** 大屏天气监测面板请求 `GET /api/v1/weather/overview`
- **THEN** 返回 `current` / `hourly` / `daily` 三段，`code=0`，前端不再回退本地 mock 业务数据

#### Scenario: 未鉴权
- **WHEN** 未携带有效令牌请求该端点
- **THEN** 返回 401（`JwtFilter` 强制令牌，本端点不在免鉴权白名单）

### Requirement: 只读且不产生广播

本域为纯只读能力，SHALL NOT 提供任何写端点，亦不产生实时广播事件
（天气数据由采集侧更新，前端按面板刷新节奏拉取）。
