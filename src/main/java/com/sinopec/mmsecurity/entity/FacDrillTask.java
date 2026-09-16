package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 演练任务子项（隶属 fac_drill）。 */
@Data
@TableName("fac_drill_task")
public class FacDrillTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属演练 id。 */
    private Long drillId;

    /** 任务名称。 */
    private String name;

    /** 任务状态（待确认 / 已提交 / 未开始 等）。 */
    private String status;
}
