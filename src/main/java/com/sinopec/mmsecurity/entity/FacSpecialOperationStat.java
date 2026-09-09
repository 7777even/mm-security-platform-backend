package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 八大特殊作业在建数量统计（真实数据源，替代大屏硬编码 specialOperations）。 */
@Data
@TableName("fac_special_operation_stat")
public class FacSpecialOperationStat {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String label;
    private Integer statCount;
    private Integer sortNo;
}
