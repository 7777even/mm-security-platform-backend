package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.util.List;

/**
 * 报警分页结果。
 *
 * 字段与前端脚手架 _shared.json 的 {@code PageResult} 完全同构（list/total/page/size），
 * {@code list} 元素为 {@link AlarmItem}（与前端 AlarmItem 契约字节级对齐）。
 */
@Data
public class AlarmPageResult {

    /** 当前页数据 */
    private List<AlarmItem> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
