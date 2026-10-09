package com.sinopec.mmsecurity.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 系统参数配置节点（列表 / 详情响应）。 */
@Data
public class SysConfigNode implements Serializable {
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private String configGroup;
    private String configType;
    private String options;
    private String remark;
    private Integer sortOrder;
    private Integer status;
    private Integer builtIn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
