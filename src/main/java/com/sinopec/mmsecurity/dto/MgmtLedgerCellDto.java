package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 管理台账单元格。type 为 ok / warn / bad / null，前端据此着色。
 */
@Data
public class MgmtLedgerCellDto {
    /** 单元格文本 */
    private String text;
    /** 状态类型：ok=正常(green) / warn=警告(orange) / bad=异常(red) / 空=普通 */
    private String type;
}
