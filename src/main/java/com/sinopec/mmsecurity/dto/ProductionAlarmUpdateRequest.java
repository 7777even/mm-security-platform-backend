package com.sinopec.mmsecurity.dto;

import lombok.Data;

/**
 * 生产报警写回请求：确认/处理中/已处置状态流转，误报标记，以及处置情况文本/时间/派单人员/通知方式。
 * 字段均为可选——前端按实际发生的状态机跃迁局部更新，未涉及的字段不传（read-modify-write）。
 * <p>枚举约束（与前端生产报警详情处置状态机、fac_production_alarm 现状对齐）：
 * <ul>
 *   <li>status：未处置 / 已确认 / 处理中 / 已处置</li>
 *   <li>falseAlarm：是 / 否 / 未核实</li>
 *   <li>handleResult / handleTime / dispatchPersonnel：处置情况文本 / 处置时间 / 派单人员(逗号分隔)</li>
 *   <li>notifyMethod：通知方式（APP/SMS，多个以英文逗号分隔，如 APP,SMS）</li>
 * </ul>
 */
@Data
public class ProductionAlarmUpdateRequest {
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
