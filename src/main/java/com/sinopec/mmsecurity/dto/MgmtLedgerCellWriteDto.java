package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 管理台账单元格写请求。colIndex 对应列标题顺序（从 0 开始）；
 * type 为 ok/warn/bad/null，前端据此着色，缺省为 null（普通文本）。
 */
@Data
public class MgmtLedgerCellWriteDto {
    private Integer colIndex;
    private String text;
    private String type;
}
