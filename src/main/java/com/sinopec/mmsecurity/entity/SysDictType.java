package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典类型（如 alarm_level）。字典项存 sys_dict_item，按 dict_code 关联。
 */
@Data
@TableName("sys_dict_type")
public class SysDictType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 字典标识（唯一） */
    @TableField("dict_code")
    private String dictCode;

    /** 字典名称 */
    @TableField("dict_name")
    private String dictName;

    private String description;

    /** 1 启用 / 0 停用 */
    private Integer status;

    /** 1 内置字典（禁删除） */
    @TableField("built_in")
    private Integer builtIn;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
