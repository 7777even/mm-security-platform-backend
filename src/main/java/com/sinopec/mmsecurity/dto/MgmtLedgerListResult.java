package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理台账列表结果：列标题 + 筛选定义 + 二维单元格（外层为行，内层为列）。
 */
@Data
public class MgmtLedgerListResult {
    private List<String> columns;
    private List<MgmtLedgerFilterDto> filters;
    /** 行集合，每行是等长于 columns 的单元格数组 */
    private List<List<MgmtLedgerCellDto>> rows;
    private long total;
    private int page;
    private int size;
}
