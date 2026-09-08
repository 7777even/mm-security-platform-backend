package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 消防报警分页结果。字段与前端 _shared.json 的 {@code PageResult} 完全同构（list/total/page/size），
 * 与 DevicePageResult 形态一致，作为跨库契约对齐的单一形态。
 */
@Data
public class FireAlarmPageResult {

    /** 当前页数据 */
    private List<FireAlarmItem> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
