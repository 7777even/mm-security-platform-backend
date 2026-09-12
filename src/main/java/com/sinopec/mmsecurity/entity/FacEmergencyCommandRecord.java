package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 应急指令下发与状态推进记录（A2 业务写侧，V47）。
 *
 * <p><b>语义边界（D4 拍板）</b>：本表只记录<b>系统内部</b>的指令下发与状态推进，
 * 是业务留痕，<b>不得</b>据此触发任何物理设备。物理下行（消防泵、广播强切、
 * 门禁断电、疏散、喷淋等）仍由 {@link com.sinopec.mmsecurity.security.HardControlPaths}
 * 红线在前后端双重拦截。</p>
 *
 * <p>状态推进以 {@code prevStatus} → {@code currStatus} 成对留痕，
 * 完整变更审计另由 {@code SystemAuditHelper} 落 fac_audit_log。</p>
 */
@Data
@TableName("fac_emergency_command_record")
public class FacEmergencyCommandRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 指令编码（对应指挥指令/行动 id） */
    private String commandCode;
    private String commandName;
    /** 指令类别（固定指令 / 临时指令） */
    private String commandKind;
    /** 推进前状态（首次下发为空） */
    private String prevStatus;
    /** 推进后状态 */
    private String currStatus;
    private String dispatchMode;
    /** 下发对象（岗位 / 人员 / 组织） */
    private String target;
    private String remark;
    /** 操作人 */
    private String operator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
