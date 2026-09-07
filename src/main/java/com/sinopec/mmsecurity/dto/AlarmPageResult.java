package com.sinopec.mmsecurity.dto;

import com.sinopec.mmsecurity.entity.FacAlarm;
import lombok.Data;

import java.util.List;

/**
 * 报警分页结果。
 *
 * 字段与前端脚手架 _shared.json 的 {@code PageResult} 完全同构（list/total/page/size），
 * 作为跨库契约对齐的单一形态，避免 Controller 直接返回 Map 导致前端类型丢失。
 *
 * 注意：{@code list} 元素为后端实体 {@link FacAlarm}，其字段命名（id/status(int)/occurredAt/content）
 * 与前端契约 {@code AlarmItem}（alarmId/status(string)/ts/description）存在差异，属契约对齐债，
 * 待独立 openspec Change 通过 VO/转换层收敛，不在本次 mock 清理范围。
 */
@Data
public class AlarmPageResult {

    /** 当前页数据 */
    private List<FacAlarm> list;

    /** 总记录数 */
    private long total;

    /** 当前页码（1-based） */
    private long page;

    /** 每页大小 */
    private long size;
}
