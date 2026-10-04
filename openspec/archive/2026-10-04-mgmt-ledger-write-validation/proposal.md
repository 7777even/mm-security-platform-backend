# Proposal: 通用台账写端点单元格结构性校验

## 背景
`MgmtLedgerService.createRow/updateRow`→`insertCells` 对单元格内容**零校验**：colIndex 越界、
重复、空数组均可直接落库，产生错位/脏数据。前端 `MgmtRecordEditDialog` 的 UI 校验挡不住直接 API 调用。

## 目标
在落库前加后端结构性校验（最后一道防线）：
- 单元格数组非空；
- 每个单元格 `colIndex` 非空且不越界（0 ≤ colIndex < 列数）；
- 同一请求内 `colIndex` 不重复。
非法一律抛 `BusinessException(PARAM_INVALID=100)`。

## 非目标（本批不做）
逐列「必填 / 类型（数字/日期）」语义校验：当前 `MgmtLedgerMeta.columnsJson` 仅存列标题，无必填/类型定义，
需先扩 meta schema（向后兼容解析）才能做。留作后续（与 fire-facility-patrol 大屏联动同为显式 deferred 项）。

## 兼容性
纯后端行为增强，接口签名/响应不变，前端契约不受影响。
