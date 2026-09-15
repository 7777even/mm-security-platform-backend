package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理台账列筛选定义。column 对应列标题，options 为可选项（含“全部”首项）。
 */
@Data
public class MgmtLedgerFilterDto {
    /** 列标题 */
    private String column;
    /** 可选项，首项通常为“全部” */
    private List<String> options;
}
