package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 管理台账行写请求：一组按列顺序排列的单元格。
 * 用于新增/更新某 domain 下的台账行（行与单元格在同一事务内维护）。
 */
@Data
public class MgmtLedgerRowWriteRequest {
    private List<MgmtLedgerCellWriteDto> cells;
}
