package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/** 工业电视 - 运行统计实体（对应 H2 表 fac_tv_operation_stat，单行）。 */
@Data
@TableName(value = "fac_tv_operation_stat")
public class FacTvOperationStat implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer totalCount;

    private Integer offlineCount;

    private Integer faultCount;

    private Integer integrityRate;

    private Integer onlineRate;

    private Integer eventTotal;
}
