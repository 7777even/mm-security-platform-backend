package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 应急生产安全知识（真实数据源，替代原 EmergencyService 硬编码常量）。 */
@Data
@TableName("sys_knowledge_item")
public class SysKnowledgeItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private Integer count;
    private String icon;
}
