package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 化学品安全技术说明书 MSDS（移动端化学品知识真实数据源）。 */
@Data
@TableName("fac_msds")
public class FacMsds {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** CAS 号（对外主键口径，如 74-85-1）。 */
    private String cas;

    /** 危险性分类。 */
    private String classification;

    /** 物理状态。 */
    private String state;

    /** 沸点。 */
    private String boilingPoint;

    /** 闪点。 */
    private String flashPoint;

    /** 爆炸极限。 */
    private String explosionLimit;

    /** 储存要求。 */
    private String storage;

    /** 安全措施。 */
    private String safety;

    /** 应急处置。 */
    private String emergency;
}
