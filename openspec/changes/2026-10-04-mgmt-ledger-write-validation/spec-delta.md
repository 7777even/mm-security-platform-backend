# Spec Delta: 写端点单元格结构性校验

## 变更
- `MgmtLedgerService` 写方法在落库前对单元格做结构性校验，非法返回 `PARAM_INVALID(100)`。
- 接口路径/参数/响应结构不变。

## 待办
- 逐列必填/类型语义校验需扩展 `MgmtLedgerMeta`（向后兼容解析 columnsJson 为带 required/type 的对象），本批未做。
