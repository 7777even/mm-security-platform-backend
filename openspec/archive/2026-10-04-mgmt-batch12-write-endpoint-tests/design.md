# Design: 四域写端点补测与自然键语义修复

## 决策

### 1. 主键策略三态，断言不可套用
| 域 | 实体主键 | 分配方式 | 缺测时的风险 |
| --- | --- | --- | --- |
| video.camera | Long AUTO | `LedgerIdSupport.nextId` + `nextSortNo` | 序列滞后撞主键 409 |
| communication.device | Long AUTO + **业务自然键 deviceCode** | 同上；按 `deviceCode` 定位行 | 同上 |
| device | String `IdType.INPUT`（20 位 MDM 编码） | 客户端给定，写方法内判重 → 409 | 误用 `LedgerIdSupport`（签名要 `Function<T,Long>`） |
| communication.record | Long AUTO + **业务自然键 recordNo** | `LedgerIdSupport.nextId`；按 `recordNo` 定位行 | 序列滞后撞主键 409 |

### 2. 删除口径必须与读端点对齐
`FacDevice` 的读端点 `page()` 恒定带 `deleted = 0` 过滤 → `deleteDevice` 必须置 `deleted = 1` 软删。
物理删除会让「写口径」与「读口径」错位（删了但列表还在，或反之）。已有 `deleteDevice_marksDeletedInsteadOfPhysicalDelete`
同时断言了 `deleted == 1` 与「未调用 `deleteById`」，防止将来被悄悄改成物理删。

### 3. 定位键不得被请求体覆盖（本次修复两处）
PUT 端点以 `/{自然键}` 或 `/{主键}` 定位资源时，路径变量才是权威。`CommDeviceService.applyDeviceFields`
与 `CommRecordService.applyRecordFields` 原先会覆写定位键，导致 body 与 path 不一致时：
资源被改到另一个键值下，随即从自己原来的 URL 中消失，同名 URL 也定位不到原记录。

修法与第 3 批 `HazardService`、以及本就正确的 `DeviceService.applyDeviceFields` 保持一致：
填充方法只写业务列，**创建分支单独设置定位键**。两处均已加 `…DoesNotOverridePathKey` 回归用例。

### 4. 判重语义
`DeviceService.createDevice` 命中同编码时抛 `BusinessException(ResultCode.CONFLICT)`（code 409），
上层 `GlobalExceptionHandler` 转 B3 包络；单测需同时保证该情形下不会误落一次 `insert`。

## 验证口径
每例都必须能「若实现改回错误写法就失败」——例如判重用例用 `never()).insert(…)` 断言，
缺失路径用 `never()).deleteById(…)` 断言，而不是只看返回值。
