package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 消防报警写回请求：确认/派单/闭环状态流转，以及误报标记。
 * 字段均为可选——前端按实际发生的状态机跃迁局部更新，未涉及的字段不传。
 * <p>枚举约束（与 sys_dict_item 字典 {@code fire_alarm_status} / {@code fire_alarm_false} 对齐）：
 * <ul>
 *   <li>status：ACTIVE 待处理 / ACKED 已确认 / DISPATCHED 已派单 / CLOSED 已闭环</li>
 *   <li>falseAlarm：是 / 否 / 未核实</li>
 *   <li>handleResult / handleTime / dispatchPersonnel / notifyMethod：处置情况文本 / 处置时间 / 派单人员 / 通知方式</li>
 * </ul>
 */
@Data
public class FireAlarmUpdateRequest {
    /** 处置状态。不传则不更新状态。 */
    private String status;
    /** 是否误报。不传则不更新。 */
    private String falseAlarm;
    /** 处置情况文本。不传则不更新。 */
    private String handleResult;
    /** 处置时间（格式 yyyy-MM-dd HH:mm:ss）。不传则不更新。 */
    private String handleTime;
    /** 派单人员（多个以英文逗号分隔）。不传则不更新。 */
    private String dispatchPersonnel;
    /** 通知方式（APP/SMS，多个以英文逗号分隔，如 APP,SMS）。不传则不更新。 */
    private String notifyMethod;
}
