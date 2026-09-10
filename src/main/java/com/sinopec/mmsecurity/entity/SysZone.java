package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 防区主数据（data_scope 行级 ABAC 的维度真源）。
 *
 * <p>zone_name 严格对齐业务表 area 取值（如 fac_brigade_team.area、BRIGADE_AREA 字典），
 * 使 {@code WHERE area IN (当前用户可访问防区)} 可直接命中。其它 area 词表不互通的业务域
 * 接入前须先将其 area 归一到本表。</p>
 */
@Data
@TableName("sys_zone")
public class SysZone implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String zoneCode;

    private String zoneName;

    private Integer sortOrder;

    private Integer status;

    private Integer deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
