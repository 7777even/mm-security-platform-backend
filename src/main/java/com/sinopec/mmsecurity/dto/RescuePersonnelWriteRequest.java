package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 救援人员（应急专家）新增 / 编辑入参，字段名对齐 {@link RescuePersonnelItem}，
 * 便于前端把列表行直接灌进表单、保存时原样回传（无需在视图层做字段映射）。
 *
 * <p><b>新增语义</b>：{@code name} / {@code squadron} / {@code role} 必填（service 校验，
 * 缺失抛 B3 PARAM_INVALID）；{@code sortNo} 由 service 取当前最大值 +1，保证列表顺序稳定。
 *
 * <p><b>编辑语义</b>：局部更新——字段为 {@code null} 表示不修改，service 承担 read-modify-write。
 * 与既有四域（应急指令 / 值班签到 / 台风调度 / 巡更执行）写端点同口径。</p>
 */
@Data
public class RescuePersonnelWriteRequest {

    /** 姓名 */
    private String name;

    /** 所属中队 */
    private String squadron;

    /** 岗位（如 指挥员 / 战斗员 / 驾驶员） */
    private String role;

    /** 所属分组（可选，用于专家分类） */
    private String personGroup;

    /** 联系电话 */
    private String phone;

    /** 值班状态（如 在岗 / 备勤 / 休整） */
    private String dutyStatus;
}
