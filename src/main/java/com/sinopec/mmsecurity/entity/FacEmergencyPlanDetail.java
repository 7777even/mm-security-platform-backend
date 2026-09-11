package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 应急预案详情字段实体（对应 H2 表 fac_emergency_plan_detail，V39）。
 *
 * <p>取代前端 EmergencyPlanPanel 预案详情弹窗硬编码的 5 段字段（基础/评审/备案/公布/评估信息）。</p>
 */
@Data
@TableName(value = "fac_emergency_plan_detail")
public class FacEmergencyPlanDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 段标题：基础信息 / 评审信息 / 备案信息 / 公布信息 / 评估信息 */
    private String sectionTitle;

    /** 字段标签：所属组织 / 预案编号 / ... */
    private String fieldLabel;

    /** 字段值 */
    private String fieldValue;

    /** 段排序 */
    private Integer sectionSort;

    /** 段内字段排序 */
    private Integer fieldSort;
}
