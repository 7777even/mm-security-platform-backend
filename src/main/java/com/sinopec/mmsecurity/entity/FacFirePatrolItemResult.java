package com.sinopec.mmsecurity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 防火巡查检查项非「正常」结果（异常 / 不适用）；缺省项由服务端按标准表补齐为「正常」。 */
@Data
@TableName("fac_fire_patrol_item_result")
public class FacFirePatrolItemResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long patrolId;
    private String itemCode;
    /** 取值：异常 / 不适用 */
    private String checkResult;
    private String abnormalDesc;
    private String photoFile;
}
