package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 工位/工作站主数据。物理主键 = workstation_id。
 * 态势页工位卡片与在线工位数（onlineWorkstation）的真实数据源。
 */
@Data
@TableName("fac_workstation")
public class FacWorkstation {

    /** 工位业务 ID（如 WS-01），物理主键 */
    private String workstationId;

    private String name;
    private String zone;
    private Boolean online;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer deleted;
}
