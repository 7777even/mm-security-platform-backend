package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理台账单元格。cell_type 取值 ok / warn / bad / null（null 表示普通文本），
 * 前端据此着色（对应 mgmtMenus 的 MgmtCell.type）。
 */
@Data
@TableName("mgmt_ledger_cell")
public class MgmtLedgerCell {
    private Long id;
    private Long rowId;
    private Integer colIndex;
    private String colKey;
    private String cellText;
    private String cellType;
}
