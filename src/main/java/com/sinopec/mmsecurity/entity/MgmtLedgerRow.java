package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 管理台账数据行。一个 domain 下有多行，行内单元格存于 mgmt_ledger_cell。
 *
 * 列名 domain_code：`domain` 是达梦 DM8 保留字，按仓库规约改列名 + @TableField 映射，
 * Java/对外 JSON 字段名不变。
 */
@Data
@TableName("mgmt_ledger_row")
public class MgmtLedgerRow {
    private Long id;
    @TableField("domain_code")
    private String domain;
    private Integer rowNo;
    private Integer sortNo;
}
