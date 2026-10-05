# video Specification

## Purpose

视频监控能力：相机台账、视频导航（视频墙 / 控制页两级导航）、重点视频分组、抓拍快照、
联动配置与联动选项。承接 mgmt 管理端「视频监控」模块、大屏视频墙 / 视频控制页与移动端
`videos` / `video-player` 页面。

> 录像截图的采集入库与确认能力见 `tv` capability（`/api/v1/tv/snapshots`）；本 capability 覆盖
> `/api/v1/video/*`，抓拍读取 `/api/v1/video/cameras/{id}/snapshot` 归本域。

本 capability 的 Requirement 来自**已归档 Change 的 spec-delta 回填**（2026-10-06 治理批次），
来源：`openspec/archive/2026-10-03-mgmt-monitor-video-comm-crud-realtime/`（video.camera 部分）、
`openspec/archive/2026-09-11-video-wall-navigation/`（导航端点）。

## Endpoints

| Method | Path | 权限 | 说明 |
| ------ | ---- | ---- | ---- |
| GET | `/api/v1/video/cameras` | `video:camera-write` | 相机列表 |
| POST | `/api/v1/video/cameras` | `video:camera-write` | 相机新增 |
| PUT | `/api/v1/video/cameras/{id}` | `video:camera-write` | 相机编辑 |
| DELETE | `/api/v1/video/cameras/{id}` | `video:camera-write` | 相机删除 |
| GET | `/api/v1/video/cameras/{id}/snapshot` | `video:camera-write` | 相机抓拍 JPEG 字节 |
| GET | `/api/v1/video/important-groups` | `video:camera-write` | 重点视频分组 |
| GET | `/api/v1/video/linkages` | `video:camera-write` | 联动配置列表 |
| POST | `/api/v1/video/linkages` | `role:ADMIN` | 联动配置新增 |
| PUT | `/api/v1/video/linkages/{configCode}` | `role:ADMIN` | 联动配置更新 |
| DELETE | `/api/v1/video/linkages/{configCode}` | `role:ADMIN` | 联动配置删除 |
| GET | `/api/v1/video/linkages/{configCode}/rules` | `video:camera-write` | 联动规则明细 |
| GET | `/api/v1/video/linkage-options` | `video:camera-write` | 联动下拉选项（四组，相机名/类型由 `fac_video_camera` 派生） |
| GET | `/api/v1/video/navigation` | `video:camera-write` | 视频控制页导航树 |
| GET | `/api/v1/video/wall-navigation` | `video:camera-write` | 视频墙导航树 |

## Requirements

### Requirement: 相机台账 CRUD 与广播

系统 SHALL 提供 `POST /api/v1/video/cameras`、`PUT /api/v1/video/cameras/{id}`、
`DELETE /api/v1/video/cameras/{id}`（权限码 `video:camera-write`）；
`VideoService` 的 createCamera / updateCamera / deleteCamera 在事务提交成功后
SHALL 广播 `video.camera.changed`，使 mgmt 视频列表与联动选项自动重拉。

- 写请求体 `VideoCameraWriteRequest` 含 6 字段：name / cameraType / location / statusName / hd / thumbIndex；
- 读端点 `/video/cameras` GET 响应 schema 不变（写端点不变更既有读契约）。

### Requirement: 联动配置 CRUD

系统 SHALL 提供 `POST / PUT / DELETE /api/v1/video/linkages[/ {configCode}]`（`role:ADMIN`），
以及规则明细只读 `GET /api/v1/video/linkages/{configCode}/rules`、下拉选项
`GET /api/v1/video/linkage-options`。

- 写请求体 `VideoLinkageSaveRequest`（含 `VideoLinkageRuleInput` 规则数组）；
- 本域契约各自本地定义 `DeleteResult`（**不可跨域共享引用**）；
- 联动选项四组下拉的相机名 / 类型由 `fac_video_camera` 派生，非前端硬编码。

### Requirement: 视频导航与重点分组只读端点

系统 SHALL 提供 `GET /api/v1/video/navigation`（视频控制页导航树）、
`GET /api/v1/video/wall-navigation`（视频墙导航树）、
`GET /api/v1/video/important-groups`（重点视频分组）三个只读端点，
取代前端硬编码导航数据；响应统一 B3 包络，只读端点不产生广播事件。

### Requirement: 相机抓拍

系统 SHALL 提供 `GET /api/v1/video/cameras/{id}/snapshot` 返回相机抓拍的 `image/jpeg` 字节流；
该相机无抓拍时返回 404（属设计，非空态兜底）。

### Requirement: 失败路径 B3 包络

所有失败路径走 B3 包络（HTTP 200 + `code != 0`）：参数非法 100、不存在 404、冲突 409；
未鉴权 401、无权限 403。
