package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 应急指令下发 / 状态推进写请求（A2 业务写侧）。
 *
 * <p><b>语义边界（D4 拍板）</b>：本请求只登记<b>系统内部</b>的指令下发与状态推进，
 * 服务端绝不据此触发任何物理设备；消防泵、广播强切、门禁断电、疏散喷淋等下行控制
 * 属零下行红线，由 {@link com.sinopec.mmsecurity.security.HardControlPaths} 在前后端双重拦截。</p>
 *
 * <p>契约 {@code emergency.openapi.json#/components/schemas/EmergencyCommandRecordWriteRequest}；
 * 落库表 V47 {@code fac_emergency_command_record}。</p>
 */
@Data
public class EmergencyCommandRecordWriteRequest implements Serializable {

    /** 指令编码（对应指挥指令 / 行动 id），必填 */
    private String commandCode;

    /** 指令名称 */
    private String commandName;

    /** 指令类别（固定指令 / 临时指令） */
    private String commandKind;

    /** 推进后状态（待执行 / 执行中 / 已完成），必填 */
    private String currStatus;

    /** 下发方式（系统下发 / 人工下发） */
    private String dispatchMode;

    /** 下发对象（岗位 / 人员 / 组织） */
    private String target;

    /** 备注说明 */
    private String remark;
}
