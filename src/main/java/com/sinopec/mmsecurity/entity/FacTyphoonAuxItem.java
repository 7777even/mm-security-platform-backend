package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 台风辅助知识库条目。 */
@Data
@TableName("fac_typhoon_aux_item")
public class FacTyphoonAuxItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属事件 id */
    private Long incidentId;
    /** 第一行标题 */
    private String line1;
    /** 第二行标题 */
    private String line2;
    /** 条目数量 */
    private Integer itemCount;
    /** 计数色调 */
    private String countTone;
    /** 图标序号 */
    private Integer iconIndex;
    /** 排序号 */
    private Integer sortNo;}
