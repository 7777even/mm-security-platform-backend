package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统参数配置（运行期可配项，如标题、阈值开关、ABAC 防区规则等）。
 *
 * <p>config_key 唯一；config_type 控制前端渲染（STRING/NUMBER/BOOLEAN/JSON/SELECT）；
 * built_in=1 的内置配置不可删除（防误删系统级开关）。</p>
 */
@Data
@TableName("sys_config")
public class SysConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("config_key")
    private String configKey;

    @TableField("config_value")
    private String configValue;

    @TableField("config_name")
    private String configName;

    @TableField("config_group")
    private String configGroup;

    @TableField("config_type")
    private String configType;

    @TableField("options")
    private String options;

    @TableField("remark")
    private String remark;

    @TableField("sort_order")
    private Integer sortOrder;

    private Integer status;

    @TableField("built_in")
    private Integer builtIn;

    private Integer deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
