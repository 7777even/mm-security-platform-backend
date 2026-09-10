package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典项。dict_code 关联 {@link SysDictType}（按标识而非外键，便于跨方言与导入导出）。
 */
@Data
@TableName("sys_dict_item")
public class SysDictItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属字典标识 */
    @TableField("dict_code")
    private String dictCode;

    /** 字典值 */
    @TableField("item_value")
    private String itemValue;

    /** 字典显示名 */
    @TableField("item_label")
    private String itemLabel;

    @TableField("sort_order")
    private Integer sortOrder;

    /** 1 启用 / 0 停用 */
    private Integer status;

    private String description;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
