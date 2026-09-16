package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 应急演练（移动端演练信息真实数据源，替代 apps/mobile/data/mock.ts 的 drills 静态数据）。 */
@Data
@TableName("fac_drill")
public class FacDrill {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 演练编号（对外展示，如 YL-008）。 */
    private String drillCode;

    private String name;

    /** 演练类型（实战演练 / 桌面推演）。 */
    private String drillType;

    /** 演练形式。 */
    private String form;

    /** 演练时间（如 2026-08-18 09:30-11:30）。 */
    private String timeRange;

    private String place;

    /** 演练状态（计划中 / 进行中 / 已结束）。 */
    private String status;

    /** 参与部门（顿号分隔）。 */
    private String departments;
}
