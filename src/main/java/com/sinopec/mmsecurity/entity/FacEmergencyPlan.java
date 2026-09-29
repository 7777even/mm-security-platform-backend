package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应急预案 - 预案切换目录实体（对应 H2 表 fac_emergency_plan）。
 * tab_key：disposal=应急处置方案 / fire=消防救援预案 / company=公司级应急预案 / superior=上级单位应急预案。
 */
@Data
@TableName(value = "fac_emergency_plan")
public class FacEmergencyPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tabKey;

    private String planName;

    private String accidentType;

    private String facility;

    private Integer sortNo;

    /**
     * 业务域：production=生产域 / fire=消防域 / perimeter=周界域 / superior=上级单位域。
     * 用于生产应急二级功能按域筛选「域内核预案」。
     */
    private String domain;

    /** 核预案标记（0/1）：是否属核生化/重点核管控类预案。 */
    private Boolean nuclear;

    /** 当前是否激活（同域内仅一个激活预案）。 */
    private Boolean isActive;

    /** 累计一键调用次数。 */
    private Integer invokeCount;

    /** 最近一次调用时间。 */
    private LocalDateTime lastInvokedAt;
}
