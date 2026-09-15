package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理台账数据行。一个 domain 下有多行，行内单元格存于 mgmt_ledger_cell。
 */
@Data
@TableName("mgmt_ledger_row")
public class MgmtLedgerRow {
    private Long id;
    private String domain;
    private Integer rowNo;
    private Integer sortNo;
}
